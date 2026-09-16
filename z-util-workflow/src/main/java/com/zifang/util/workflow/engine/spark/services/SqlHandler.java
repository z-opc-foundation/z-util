//package com.zifang.util.workflow.engine.spark.services;
//
//import com.zifang.util.core.util.GsonUtil;
//import com.zifang.util.workflow.annoation.EngineService;
//import com.zifang.util.workflow.engine.spark.AbstractSparkEngineService;
//import com.zifang.util.workflow.engine.spark.CacheEngineService;
//
//import java.util.HashMap;
//import java.util.Map;
//
/// **
// * SQL处理器（占位实现）。
// * <p>
// * 该处理器是占位实现，具体逻辑待扩展。
// * 支持书写任意SQL语句，在Spark SQL上下文中执行查询和转换操作。
// *
// * @see AbstractSparkEngineService
// * @see CacheEngineService
// */
//@EngineService(name = "engine.service.sql")
//public class SqlHandler extends AbstractSparkEngineService {
//
//    @Override
//    public void defaultHandler() {
//
//    }
//
//    public void sql() {
//        //得到缓存控制服务器
//        CacheEngineService cacheEngineService = getWorkFlowApplicationContext().getCacheEngineService();
//        //当前内的节点的缓存参数
//        Map<String, String> cacheParameter = executableWorkflowNode.getCache();
//        //当前的invoke的所需要的参数
//        HashMap transformedParameter = GsonUtil.changeToSubClass(invokeParameter, HashMap.class);
//
//        String sql = String.valueOf(transformedParameter.get("sql"));
//
//        //当前的dataset
//        dataset = sparkContextInstance.getSqlContext().sql(sql);
//
//        dataset.show();
//    }
//}
