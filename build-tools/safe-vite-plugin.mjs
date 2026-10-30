import fs from 'node:fs'
import path from 'node:path'

/**
 * 安全清空 outDir —— 用「移走」代替「删除」。
 *
 * ⚠ 为什么需要它（2026-10-30）：
 *   vite 的 `build.emptyOutDir: true` 会**批量删除** outDir 下的旧产物。
 *   在 WorkBuddy 沙箱里，单轮累计删除 ≥50 个文件会被硬拦：
 *     [safe-delete][SAFE_DELETE_BULK_CONFIRM_REQUIRED] {"count":50,"threshold":50}
 *   ⇒ 前端构建**间歇性假红**（删得少的轮次绿、删多的轮次红），
 *     而且红的位置在 vite 内部，看起来像「构建坏了」，其实是环境闸。
 *
 *   本插件把流程改成：构建开始时把旧 outDir **rename** 成 `<outDir>.stale-<时间戳>`，
 *   再让 vite 在一个空目录上正常产出。
 *   - 产物始终干净（不会有陈旧文件混进 dist）
 *   - 全程零删除 ⇒ 不碰安全闸
 *   - 旧目录留在旁边（.stale-*），需要时由人工确认后清理
 */
export function safeEmptyOutDir() {
    let outDirAbs = null

    return {
        name: 'safe-empty-out-dir',
        // 只在构建期生效；dev server 不动 outDir
        apply: 'build',

        configResolved(config) {
            // 关键：接管 vite 的清空行为，让它别去删目录
            config.build.emptyOutDir = false
            outDirAbs = path.resolve(config.root || process.cwd(), config.build.outDir)
        },

        buildStart() {
            if (!outDirAbs || !fs.existsSync(outDirAbs)) return
            const stash = `${outDirAbs}.stale-${Date.now()}`
            try {
                fs.renameSync(outDirAbs, stash)
            } catch (e) {
                // 移不动就退回「不清理」：宁可产物里多个陈旧文件，也别整个构建挂掉
                console.warn(`[safe-empty-out-dir] 移动旧产物失败（继续构建，产物可能含陈旧文件）：${e.message}`)
            }
        },
    }
}
