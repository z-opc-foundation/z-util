package com.zifang.util.db.plugin;

/**
 * 插件链接口
 * <p>
 * 提供插件链式调用能力，用于在方法执行前后插入插件逻辑。
 * 该接口是框架扩展点的重要组成部分，允许在目标方法执行前后
 * 动态添加横切关注点，如日志记录、性能监控、事务管理等。
 *
 * <p>插件链采用责任链模式实现，每个插件可以：
 * <ul>
 *   <li>在目标方法执行前执行预处理逻辑</li>
 *   <li>决定是否继续执行链中的下一个插件或目标方法</li>
 *   <li>在目标方法执行后执行后处理逻辑</li>
 *   <li>修改目标方法的参数或返回值</li>
 * </ul>
 *
 * <p>实现该接口的类通常需要配合&#64;Intercepts注解使用，
 * 以声明需要拦截的方法。
 *
 * @see org.apache.ibatis.plugin.Interceptor
 */
public interface IChain {
}
