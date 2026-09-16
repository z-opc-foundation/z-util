//package com.zifang.util.workflow.engine.spark.services;
//
//import com.zifang.util.core.util.GsonUtil;
//import com.zifang.util.workflow.annoation.EngineService;
//import com.zifang.util.workflow.engine.spark.AbstractSparkEngineService;
//import com.zifang.util.workflow.engine.spark.CacheEngineService;
//import org.apache.spark.sql.SaveMode;
//
//import java.util.HashMap;
//import java.util.Map;
//import java.util.Properties;
//
/// **
// * 资源处理器（占位实现）。
// * <p>
// * 该处理器是占位实现，具体逻辑待扩展。
// * 用于处理各种数据资源的输入输出，包括：
// * <ul>
// *   <li>本地文件读写</li>
// *   <li>MySQL数据库读写</li>
// *   <li>Hive表读写</li>
// * </ul>
// *
// * @see AbstractSparkEngineService
// * @see CacheEngineService
// */
//@EngineService(name = "engine.service.resourceHandler")
//public class ResourceHandler extends AbstractSparkEngineService {
//
//    private static String tempName = "tempName";
//
//    private static String localFile = "localFile";
//
//    @Override
//    public void defaultHandler() {
//
//    }
//
//    public void handleLocalInput() {
//
//        //得到缓存控制服务器
//        CacheEngineService cacheEngineService = getWorkFlowApplicationContext().getCacheEngineService();
//
//        HashMap<String, String> transformedParameter = GsonUtil.changeToSubClass(invokeParameter, HashMap.class);
//        try {
//            dataset = sparkUtil.createDataSet(transformedParameter.get(localFile));
//            cacheEngineService.doCache(dataset, executableWorkflowNode.getCache().get("cacheTempNameAlias"));
//            dataset.show();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    public void handleLocalOutput() {
//        Map<String, String> transformedParameter = GsonUtil.changeToSubClass(invokeParameter, HashMap.class);
//
//        try {
//            sparkContextInstance.getSparkContext()
//                    .hadoopConfiguration()
//                    .set("mapreduce.fileoutputcommitter.marksuccessfuljobs", "false");
//            executableWorkflowNode.getPre().get(0).getDataset()
//                    .repartition(1)
//                    .write()
//                    .mode(SaveMode.Overwrite)
//                    .format("csv")
//                    .option("header", "true")
//                    .save(transformedParameter.get("outputDir"));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    public void handleMysqlInput() {
//
//        Map<String, String> properties = GsonUtil.changeToSubClass(invokeParameter, HashMap.class);
//
//        Properties props = new Properties();
//        props.put("user", properties.get("user"));
//        props.put("password", properties.get("password"));
//
//        //从mysql读数据
//        dataset = sparkContextInstance.getSqlContext()
//                .read()
//                .jdbc(properties.get("dbUrl"), properties.get("table"), props);
//        dataset.show();
//    }
//
//    public void handleMysqlOutput() {
//        Map<String, String> properties = GsonUtil.changeToSubClass(invokeParameter, HashMap.class);
//
//        Properties props = new Properties();
//        props.put("user", properties.get("user"));
//        props.put("password", properties.get("password"));
//
//        executableWorkflowNode.getPre().get(0).getDataset()
//                .write()
//                .mode(SaveMode.Overwrite)
//                .jdbc(properties.get("dbUrl"), properties.get("table"), props);
//    }
//
//
//    public void handleHiveInput() {
//        Map<String, String> properties = GsonUtil.changeToSubClass(invokeParameter, HashMap.class);
//
//        dataset = sparkContextInstance.getSqlContext().sql("select * from " + properties.get("tableName"));
//        dataset.show();
//    }
//
//
//    public void handleHiveOutput() {
//        Map<String, String> properties = GsonUtil.changeToSubClass(invokeParameter, HashMap.class);
//
//        String view = "t" + "_" + System.currentTimeMillis();
//        executableWorkflowNode.getPre().get(0).getDataset().createOrReplaceTempView(view);
//        String tableName = properties.get("tableName");
//        sparkContextInstance.getSqlContext().sql("drop table if exists " + tableName);
//        dataset = sparkContextInstance.getSqlContext().sql("create table " + tableName + " as select * from " + view);
//    }
//
//}
