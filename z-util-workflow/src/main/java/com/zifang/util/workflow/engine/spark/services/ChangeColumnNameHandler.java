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
// * 列名修改处理器（占位实现）。
// * <p>
// * 该处理器是占位实现，具体逻辑待扩展。
// * 用于修改DataFrame的列名，实现列的重命名操作。
// *
// * @see AbstractSparkEngineService
// * @see CacheEngineService
// */
//@EngineService(name = "engine.service.changeColumn")
//public class ChangeColumnNameHandler extends AbstractSparkEngineService {
//
//    @Override
//    public void defaultHandler() {
//        //得到缓存控制服务器
//        CacheEngineService cacheEngineService = getWorkFlowApplicationContext().getCacheEngineService();
//        //当前内的节点的缓存参数
//        Map<String, String> cacheParameter = executableWorkflowNode.getCache();
//
//        //获得当前节点的前置节点的数据
//        dataset = executableWorkflowNode.getDatasetPre();
//        //得到数据体
//        Map<String, String> properties = GsonUtil.changeToSubClass(invokeParameter, HashMap.class);
//        //传入的是当前的节点
//        for (Map.Entry<String, String> entry : properties.entrySet()) {
//            dataset = dataset.withColumnRenamed(entry.getKey(), entry.getValue());
//        }
//        dataset.show();
//
//        cacheEngineService.doCache(dataset, cacheParameter.get("cacheTempNameAlias"));
//
//    }
//
//}
