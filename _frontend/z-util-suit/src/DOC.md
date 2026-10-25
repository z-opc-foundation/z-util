# z-util-suit

z-util（组织底层 Java 工具库）的**独立运行壳**（lead 005 §9.1 suit）：目前是一个「服务台」形态的控制台，
提供登录、首页快捷入口与服务状态（actuator 探针）三块能力；**业务域页面尚未在本壳落地**。

- **npm 包名**：`@yuku123/z-util-suit`（`private: true`，**不发布 npm**）
- **宿主接入**：无（独立壳）。仓内对外出口是 `src/index.js`（页面清单 + 文档页）
- **后端**：⚠ **本仓后端不是 z-util** —— z-util 是**纯 Java 工具库**（`z-util-core` / `z-util-http` / `z-util-jdbc` … 等
  一组 jar 模块，无 web 层、无 Controller、无监听端口）。本壳 dev proxy 的 `/api` 指向
  `http://localhost:8888`（平台侧通用地址），当前页面**没有调用任何业务 API**
- **技术栈**：React 19 + antd 6 + react-router-dom 7 + vite（**应用层**，基座内联进 bundle）

> ⚠ 本仓**没有** `z-util-component` 包：页面代码住在本壳 `src/` 下，
> 因此本文档描述的是**本壳自身的能力**。

---

## 一、能力概览

路由前缀 `/z-util`，菜单与路由都在 `src/pages-manifest.jsx` 里声明，`src/App.jsx` 读数组自己组装。

| 页面 | 路由 | 源文件 | 能力 |
|------|------|--------|------|
| 首页 | `/z-util/home` | `src/HomePage.jsx` | 欢迎语 + 按 `menuItems` 渲染的快捷入口卡片 |
| 服务状态 | `/z-util/status` | `src/StatusPage.jsx` + `src/Status.jsx` | 读 actuator 探针显示健康状态 / 构建信息，10 秒轮询 |
| 登录 | `/z-util/login` | `src/LoginPage.jsx` | mock 登录（**直接在 `App.jsx` 里注册路由，不在路由表里**） |
| 组件文档 | `/z-util/docs` | `src/pages/ComponentDoc.jsx` | 本文件渲染页（左侧 240px TOC + 右侧正文） |

> ⚠ 本壳的路由清单导出的名字是 **`routeTable`**（不是 `routes`）——`src/App.jsx` 按这个名字消费，
> 改名字会直接让壳起不来。新增路由时沿用 `routeTable`。

## 二、导出面

```js
// 仓内出口（src/index.js）
import {menuItems, routeTable, appMeta, ComponentDocPage} from './src/index.js'

// 单个页面（直接引源文件）
import HomePage from './src/HomePage.jsx'
import StatusPage from './src/StatusPage.jsx'
```

`src/index.js` 是本壳**唯一的 barrel**：把 `src/pages-manifest.jsx` 的清单整份转发，再具名再导出 `ComponentDocPage`
（与其它仓「文档页统一叫 `ComponentDocPage`」的口径一致，宿主/主壳可以按同一个名字取）。

## 三、API 端点

下表路径是本壳页面**实际调用**的路径。⚠ **本壳当前没有任何业务 API 调用** —— 只有 actuator 探针。

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/actuator/health` | 健康探针（`Status` 内直接 `fetch`） |
| GET | `/actuator/info` | 构建元信息（`Status` 内直接 `fetch`） |
| GET | `/actuator/mappings` | HTTP 路由清单（`Status` 页仅提供跳转链接） |
| GET | `/actuator/env` | 环境变量与配置属性（`Status` 页仅提供跳转链接） |

## 四、页面说明

### 4.1 首页

登录后着陆页。顶部欢迎语读 `localStorage.userInfo`，下方按 `menuItems`（剔除自身）渲染快捷入口卡片，点击即 `navigate(m.key)`。

### 4.2 服务状态

读 `/actuator/health` + `/actuator/info`，展示健康状态、构建版本、构建时间，并列出 actuator 常用端点跳转。
**后端未起时优雅降级**（loading skeleton + warn，不抛红）；每 10 秒轮询一次。

### 4.3 登录

**mock 登录**：账号由构建期注入 `VITE_MOCK_USER / VITE_MOCK_PASSWORD`（未注入时登录页提示未配置），成功后写 `localStorage.token`（值形如 `mock-admin`）与
`localStorage.userInfo`，再整页跳 `/z-util/home`。 CTC/SSO 接入后应换共享 `LoginPage` 的 `loginApi`。

### 4.4 组件文档

本文件。内容以 `?raw` 在编译期内联，零额外请求；左侧 240px TOC 随滚动联动。

## 五、配置

| 配置项 | 默认值 | 说明 |
|---|---|---|
| dev proxy `/api` | `http://localhost:8888` | 平台侧通用地址；**本壳当前页面尚未使用** |
| dev server port | `3034` | `vite.config.js` 的 `server.port` |
| `LOCAL_SIBLINGS=1` | 关 | 开启后走 `alias: {}`（本仓目前无兄弟包别名） |
| token 存储键 | `token` | `localStorage` |
| userInfo 存储键 | `userInfo` | `localStorage`，JSON 串；解析失败降级为 `{name: raw}` |
| `appTitle` | `util 服务台` | `App.jsx` 里 `AppLayout` 的标题 |

## 六、已知限制

- **本壳没有业务能力**：z-util 的能力在 jar 侧（分页 / JWT / 分布式 ID / 表达式 DSL / JDBC 等一堆模块），
  **没有任何 HTTP 端点**，本壳也就没有任何工具在线试用 / 配置之类的页面。菜单里目前只有首页与服务状态。
- **登录是 mock（账号由环境变量注入）**，没有真实鉴权；`App.jsx` 的 `ProtectedShell` 只判断 `localStorage.token` 是否存在。
- `Status` 页用相对路径 `fetch('/actuator/health')`，**要求前后端同域托管**；分域部署会 404（此时降级为 UNKNOWN）。
- `Status` 页 10 秒轮询一次 actuator，页面常开时会持续打后端。
- dev proxy 的 `/api` 目标 `:8888` 是平台侧通用地址，**与 z-util 仓本身无关**（本仓不起服务）。

## 七、变更记录

| 版本 | 变更 |
|---|---|
| 0.1.0（未发布） | 新增组件文档页 `ComponentDocPage`（lead 008 §7）：`src/DOC.md` + 基座 `ComponentDoc` 薄包装 + 菜单末项 |
| 0.1.0 | 首版：首页 / 服务状态 / mock 登录 + manifest |
