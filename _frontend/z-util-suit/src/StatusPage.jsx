import { Card, Space, Typography } from 'antd'
import Status from './Status'

const { Title, Paragraph } = Typography

/** 服务状态页（原 App 内联内容迁出，lead 008 §13/§14）。 */
export default function StatusPage() {
    return (
        <Space direction="vertical" size="large" style={{ width: '100%' }}>
            <Card>
                <Title level={3} style={{ margin: 0 }}>util 服务台</Title>
                <Paragraph type="secondary" style={{ marginBottom: 0 }}>
                    独立运行壳（lead 005 §9.1 suit）· 后端 actuator 探针见下方
                </Paragraph>
            </Card>
            <Status/>
        </Space>
    )
}
