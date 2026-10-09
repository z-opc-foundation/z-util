import {useEffect, useState} from 'react'
import {Alert, Card, Col, Descriptions, Row, Skeleton, Space, Statistic, Tag, Typography} from 'antd'
import {ApiOutlined, CheckCircleOutlined, ClockCircleOutlined, DashboardOutlined, LinkOutlined} from '@ant-design/icons'

const {Title, Paragraph, Text} = Typography

/**
 * Status 页：读 Spring Boot Actuator 的 /actuator/health + /actuator/info，
 * 显本仓健康状态、构建信息、常用 actuator 端点跳转。
 * 后端未起时优雅降级（loading skeleton + warn，不抛红）。
 */
export default function Status() {
    const [health, setHealth] = useState(null)
    const [info, setInfo] = useState(null)
    const [err, setErr] = useState(null)

    useEffect(() => {
        let abort = false
        const load = async () => {
            try {
                const [hRes, iRes] = await Promise.allSettled([
                    fetch('/actuator/health').then(r => r.ok ? r.json() : null),
                    fetch('/actuator/info').then(r => r.ok ? r.json() : null),
                ])
                if (abort) return
                setHealth(hRes.status === 'fulfilled' ? hRes.value : null)
                setInfo(iRes.status === 'fulfilled' ? iRes.value : null)
            } catch (e) {
                if (!abort) setErr(e.message || String(e))
            }
        }
        load()
        const t = setInterval(load, 10000)
        return () => { abort = true; clearInterval(t) }
    }, [])

    const status = health?.status || (err ? 'DOWN' : 'UNKNOWN')
    const color = status === 'UP' ? 'green' : status === 'DOWN' ? 'red' : 'gold'
    const buildInfo = info?.build || info?.git || {}

    return (
        <Space direction="vertical" size="large" style={{width: '100%'}}>
            <Card>
                <Space align="start" size="large">
                    <DashboardOutlined style={{fontSize: 36, color: '#1677ff'}}/>
                    <div>
                        <Title level={3} style={{margin: 0}}>服务状态</Title>
                        <Paragraph type="secondary" style={{marginBottom: 0}}>
                            后端 actuator 探针 · 10 秒自动刷新
                        </Paragraph>
                    </div>
                    <Tag color={color} style={{fontSize: 16, padding: '4px 12px'}}>
                        {status === 'UP' ? <CheckCircleOutlined/> : <ClockCircleOutlined/>} {status}
                    </Tag>
                </Space>
            </Card>

            {err && <Alert type="warning" showIcon message="后端未连接" description={err}/>}

            <Row gutter={16}>
                <Col span={8}>
                    <Card>
                        <Statistic title="健康状态" value={status}
                                    valueStyle={{color: status === 'UP' ? '#3f8600' : '#cf1322'}}/>
                    </Card>
                </Col>
                <Col span={8}>
                    <Card>
                        <Statistic title="构建版本"
                                    value={buildInfo?.version || buildInfo?.commit?.id?.slice(0, 7) || 'N/A'}
                                    prefix={<ApiOutlined/>}/>
                    </Card>
                </Col>
                <Col span={8}>
                    <Card>
                        <Statistic title="构建时间"
                                    value={buildInfo?.time ? new Date(buildInfo.time).toLocaleString() : 'N/A'}/>
                    </Card>
                </Col>
            </Row>

            <Card title={<><LinkOutlined/> Actuator 端点</>}>
                <Descriptions column={1} bordered size="small">
                    <Descriptions.Item label={<a href="/actuator/health" target="_blank" rel="noreferrer">/actuator/health</a>}>
                        <Text code>Liveness / Readiness 综合检查</Text>
                    </Descriptions.Item>
                    <Descriptions.Item label={<a href="/actuator/info" target="_blank" rel="noreferrer">/actuator/info</a>}>
                        <Text code>构建元信息（git / version / time）</Text>
                    </Descriptions.Item>
                    <Descriptions.Item label={<a href="/actuator/mappings" target="_blank" rel="noreferrer">/actuator/mappings</a>}>
                        <Text code>HTTP 路由清单（开发调试用）</Text>
                    </Descriptions.Item>
                    <Descriptions.Item label={<a href="/actuator/env" target="_blank" rel="noreferrer">/actuator/env</a>}>
                        <Text code>环境变量与配置属性</Text>
                    </Descriptions.Item>
                </Descriptions>
            </Card>

            {!health && !err && <Skeleton active/>}
        </Space>
    )
}
