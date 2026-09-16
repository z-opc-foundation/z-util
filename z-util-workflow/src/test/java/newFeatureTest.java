import com.zifang.util.workflow.conponents.WorkFlowApplicationContext;

/**
 * newFeatureTest类。
 */
public class newFeatureTest {

    //@Test

    /**
     * testAll方法。
     */
    public void testAll() {

        String filePath = "feature/workflow_all.json";

        WorkFlowApplicationContext workFlowApplicationContext = new WorkFlowApplicationContext();
        workFlowApplicationContext.initialByLocalFilePath(filePath);
        workFlowApplicationContext.executeTask();
    }
}
