//package com.zifang.util.workflow.engine.spark;
//
//import com.zifang.util.bigdata.spark.context.SparkContextInstance;
//import com.zifang.util.bigdata.spark.util.SparkUtil;
//import com.zifang.util.workflow.config.ExecutableWorkflowNode;
//import com.zifang.util.workflow.engine.interfaces.AbstractEngineService;
//
//import java.lang.reflect.InvocationTargetException;
//import java.lang.reflect.Method;
//
/// **
// * Spark引擎服务抽象基类（占位实现）。
// * <p>
// * 该类是占位实现，具体逻辑待扩展。
// * 所有Spark处理器（如UdfHandler、FilterHander等）都继承自此类。
// * <p>
// * 提供了：
// * <ul>
// *   <li>SparkContextInstance注入</li>
// *   <li>SparkUtil工具类实例化</li>
// *   <li>基于反射的方法调用机制</li>
// * </ul>
// *
// * @see AbstractEngineService
// * @see SparkContextInstance
// * @see SparkUtil
// */
//public abstract class AbstractSparkEngineService extends AbstractEngineService {
//
//    protected SparkContextInstance sparkContextInstance;
//    protected SparkUtil sparkUtil;
//    protected ExecutableWorkflowNode executableWorkflowNode;
//
//    private static String defaultInvokeDynamicMethod = "defaultHandler";
//
//    public void setSparkContextInstance(SparkContextInstance sparkContextInstance) {
//        this.sparkContextInstance = sparkContextInstance;
//        sparkUtil = new SparkUtil(this.sparkContextInstance);
//    }
//
//    @Override
//    public void exec(ExecutableWorkflowNode executableWorkflowNode) {
//        this.executableWorkflowNode = executableWorkflowNode;
//        String invokeDynamic = executableWorkflowNode.getInvokeDynamic();
//        if (invokeDynamic == null) {
//            invokeDynamic = defaultInvokeDynamicMethod;
//        }
//        try {
//            System.out.println("want to call:" + invokeDynamic);
//            Method method = this.getClass().getMethod(invokeDynamic);
//            method.invoke(this);
//        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public abstract void defaultHandler();
//}
