//package com.zifang.util.workflow.engine.spark.services;
//
//import com.zifang.util.core.util.GsonUtil;
//import com.zifang.util.workflow.annoation.EngineService;
//import com.zifang.util.workflow.config.ExecutableWorkflowNode;
//import com.zifang.util.workflow.engine.spark.AbstractSparkEngineService;
//
//import java.util.HashMap;
//import java.util.Map;
//
/// **
// * 数据集JOIN处理器（占位实现）。
// * <p>
// * 该处理器是占位实现，具体逻辑待扩展。
// * 用于执行多个数据集的JOIN操作。
// *
// * @see AbstractSparkEngineService
// * @see GsonUtil
// */
//@EngineService(name = "engine.service.joinHandler")
//public class JoinHandler extends AbstractSparkEngineService {
//
//    @Override
//    public void defaultHandler() {
//        Map<String, String> properties = GsonUtil.changeToSubClass(invokeParameter, HashMap.class);
//
//        try {
//            dataset = sparkContextInstance.getSqlContext().sql(properties.get("sql"));
//            dataset.show();
//            for (ExecutableWorkflowNode executableWorkflowNodePost : executableWorkflowNode.getPost()) {
//                executableWorkflowNodePost.setDatasetPre(dataset);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//}
