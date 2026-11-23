import {useEffect, useRef, useState} from 'react'
import {Alert, Button, Card, Col, Descriptions, Row, Skeleton, Space, Statistic, Tag, Typography} from 'antd'
import {ApiOutlined, CheckCircleOutlined, ClockCircleOutlined, DashboardOutlined, LinkOutlined} from '@ant-design/icons'

const {Title, Paragraph, Text} = Typography
const wrapValue = {overflowWrap: 'anywhere', whiteSpace: 'normal', minWidth: 0}

/** Status 页：独立读取两个 actuator 端点；请求失败不代表服务 DOWN。 */
export default function Status() {
    const [health, setHealth] = useState(null)
    const [info, setInfo] = useState(null)
    const [err, setErr] = useState(null)
    const [loading, setLoading] = useState(true)
    const [hasLoaded, setHasLoaded] = useState(false)
    const [retry, setRetry] = useState(0)
    const latestRequest = useRef(0)

    useEffect(() => {
        let mounted = true
        let controller
        const load = async () => {
            const request = ++latestRequest.current
            controller?.abort()
            controller = new AbortController()
            const {signal} = controller
            setLoading(true)
            const read = async (url) => {
                const response = await fetch(url, {signal})
                if (!response.ok) throw new Error(`HTTP ${response.status}`)
                const data = await response.json()
                if (!data || typeof data !== 'object' || Array.isArray(data)) {
                    throw new Error('响应不是有效对象')
                }
                if (url === '/actuator/health' && (typeof data.status !== 'string' || !data.status.trim())) {
                    throw new Error('响应缺少健康状态')
                }
                return data
            }
            const [hRes, iRes] = await Promise.allSettled([
                read('/actuator/health'),
                read('/actuator/info'),
            ])
            if (!mounted || request !== latestRequest.current) return
            // 保留另一端点及上次成功的数据；警告明确说明这些数据可能已过期。
            if (hRes.status === 'fulfilled') setHealth(hRes.value)
            if (iRes.status === 'fulfilled') setInfo(iRes.value)
            const errors = [hRes, iRes].flatMap((result, index) => result.status === 'rejected'
                ? [`/actuator/${index === 0 ? 'health' : 'info'}: ${result.reason?.message || String(result.reason)}`]
                : [])
            setErr(errors.length ? errors.join('；') : null)
            setHasLoaded(true)
            setLoading(false)
        }
        load()
        const t = setInterval(load, 10000)
        return () => {
            mounted = false
            controller?.abort()
            clearInterval(t)
        }
    }, [retry])

    const status = health?.status?.trim() || 'UNKNOWN'
    const color = status === 'UP' ? 'green' : status === 'DOWN' ? 'red' : 'gold'
    const buildInfo = info?.build || info?.git || {}
    const commitId = buildInfo?.commit?.id

    return (
        <Space direction="vertical" size="large" style={{width: '100%', minWidth: 0, ...wrapValue}}>
            <Card>
                <Space align="start" size="large" wrap style={{maxWidth: '100%'}} styles={{item: {maxWidth: '100%', minWidth: 0}}}>
                    <DashboardOutlined style={{fontSize: 36, color: '#1677ff'}}/>
                    <div>
                        <Title level={3} style={{margin: 0}}>服务状态</Title>
                        <Paragraph type="secondary" style={{marginBottom: 0}}>
                            后端 actuator 探针 · 10 秒自动刷新
                        </Paragraph>
                    </div>
                    <Tag color={color} style={{fontSize: 16, padding: '4px 12px', maxWidth: '100%', ...wrapValue}}>
                        {status === 'UP' ? <CheckCircleOutlined/> : <ClockCircleOutlined/>} {status}
                    </Tag>
                </Space>
            </Card>

            {err && <Alert type="warning" showIcon message="状态获取失败"
                           description={<>{err}{(health || info) && <div>已保留成功获取的数据，可能已过期。</div>}</>}
                           action={<Button size="small" loading={loading} onClick={() => setRetry((value) => value + 1)}>重试</Button>}/>}

            <Row gutter={[16, 16]}>
                <Col xs={24} md={8} style={{minWidth: 0}}>
                    <Card>
                        <Statistic title="健康状态" value={status}
                                    valueStyle={{...wrapValue, color: status === 'UP' ? '#3f8600' : status === 'DOWN' ? '#cf1322' : '#ad6800'}}/>
                    </Card>
                </Col>
                <Col xs={24} md={8} style={{minWidth: 0}}>
                    <Card>
                        <Statistic title="构建版本"
                                    value={buildInfo?.version || (typeof commitId === 'string' ? commitId.slice(0, 7) : '') || 'N/A'}
                                    valueStyle={wrapValue} prefix={<ApiOutlined/>}/>
                    </Card>
                </Col>
                <Col xs={24} md={8} style={{minWidth: 0}}>
                    <Card>
                        <Statistic title="构建时间" valueStyle={wrapValue}
                                    value={buildInfo?.time ? new Date(buildInfo.time).toLocaleString() : 'N/A'}/>
                    </Card>
                </Col>
            </Row>

            <Card title={<><LinkOutlined/> Actuator 端点</>}>
                <Descriptions column={1} bordered size="small" styles={{label: wrapValue, content: wrapValue}}>
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

            {loading && !hasLoaded && <Skeleton active/>}
        </Space>
    )
}
