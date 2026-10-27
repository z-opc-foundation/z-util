import React from 'react'
import ReactDOM from 'react-dom/client'
import {ConfigProvider} from 'antd'
import zhCN from 'antd/locale/zh_CN'
import App from './App'
import 'antd/dist/reset.css'

// Router 只在 App.jsx 一处：main 再包 BrowserRouter 会触发 "Router inside another Router" 整页崩溃
ReactDOM.createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <ConfigProvider locale={zhCN}>
      <App/>
    </ConfigProvider>
  </React.StrictMode>,
)
