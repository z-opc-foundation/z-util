import {defineConfig} from 'vite'
import react from '@vitejs/plugin-react'
import {resolve} from 'node:path'
import {fileURLToPath} from 'node:url'
import {safeEmptyOutDir} from '../../build-tools/safe-vite-plugin.mjs'

const localSiblings = process.env.LOCAL_SIBLINGS === '1'
const frontendRoot = fileURLToPath(new URL('../', import.meta.url))
const workspaceRoot = fileURLToPath(new URL('../../../', import.meta.url))

export default defineConfig({
  plugins: [safeEmptyOutDir(), react()],
  resolve: {
    dedupe: ['react', 'react-dom', 'react-router-dom', 'antd', '@ant-design/icons', 'axios'],
    alias: localSiblings ? {
      '@yuku123/z-frontend-common/ui': resolve(workspaceRoot, 'z-frontend-common/src/components/ui/index.js'),
      '@yuku123/z-frontend-common': resolve(workspaceRoot, 'z-frontend-common/src/index.js'),
    } : {},
  },
  server: {port: 3034, fs: {allow: [localSiblings ? workspaceRoot : frontendRoot]}, proxy: {'/api': {target: 'http://localhost:8888', changeOrigin: true}}},
})
