import { Component, useEffect, useState } from 'react'
import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom'
import { AppLayout } from '../../../../_shared/z-frontend-common-local/dist/z-frontend-common.es.js'
import { menuItems, routeTable, HomePage, LoginPage } from './pages-manifest'
import { Result, Button } from 'antd'

class ErrorBoundary extends Component {
    constructor(props) { super(props); this.state = { err: null } }
    static getDerivedStateFromError(err) { return { err } }
    componentDidCatch(err, info) { console.error('[App ErrorBoundary]', err, info) }
    render() {
        if (this.state.err) return <pre style={{ padding: 24, color: 'red', whiteSpace: 'pre-wrap' }}>{String(this.state.err?.stack || this.state.err)}</pre>
        return this.props.children
    }
}

function NotFound() {
    return (
        <Result
            status="404"
            title="页面不存在"
            subTitle="路由表里没有这条路径。"
            extra={<Button type="primary" onClick={() => { window.location.href = '/z-util/home' }}>去首页</Button>}
        />
    )
}

// lead 008 §16：短期 hard-coded 登录（admin / 123456），CTC/SSO 接入后换共享 LoginPage loginApi
function LoginRoute() {
    return <LoginPage />
}

function ProtectedShell() {
    const [user, setUser] = useState(null)
    const [ready, setReady] = useState(false)

    useEffect(() => {
        if (!localStorage.getItem('token')) {
            window.location.replace('/z-util/login')
            return
        }
        const raw = localStorage.getItem('userInfo')
        if (raw) { try { setUser(JSON.parse(raw)) } catch { setUser({ name: raw }) } }
        setReady(true)
    }, [])

    if (!ready) return null

    return (
        <AppLayout
            menuItems={menuItems}
            appTitle="util 服务台"
            appVersion="1.0.0"
            appUser={user}
            appIcon={{ icon: <img src="/icon.png" alt="util 服务台" style={{ width: '100%', height: '100%', objectFit: 'cover', borderRadius: 8 }}/>, color: '#64748b', label: 'util 服务台' }}
        />
    )
}

/** lead 008 §16 suit 一次整合：登录路由 + 鉴权壳 + URL 即状态（§11/§14）。 */
export default function App() {
    return (
        <ErrorBoundary>
            <BrowserRouter>
                <Routes>
                    <Route path="/z-util/login" element={<LoginRoute />} />
                    <Route element={<ProtectedShell />}>
                        <Route path="/" element={<Navigate to={menuItems[0].key} replace />} />
                        {routeTable.map((r) => (
                            <Route key={r.path} path={r.path} element={<r.Component />} />
                        ))}
                        <Route path="*" element={<NotFound />} />
                    </Route>
                </Routes>
            </BrowserRouter>
        </ErrorBoundary>
    )
}
