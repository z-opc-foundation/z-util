import com.zifang.util.workflow.conponents.WorkFlowApplicationContext;

/**
 * ExzampleTest类。
 */
public class ExzampleTest {

    //@Test

    /**
     * workflow_read_write_local方法。
     */
    public void workflow_read_write_local() {
        String filePath = "ex/workflow_read_write_local.json";
        WorkFlowApplicationContext workFlowApplicationContext = new WorkFlowApplicationContext();
        workFlowApplicationContext.initialByLocalFilePath(filePath);
        workFlowApplicationContext.executeTask();
    }

    //@Test

    /**
     * workflow_read_write_mysql方法。
     */
    public void workflow_read_write_mysql() {
        String filePath = "ex/workflow_read_write_mysql.json";
        WorkFlowApplicationContext workFlowApplicationContext = new WorkFlowApplicationContext(filePath);
        workFlowApplicationContext.executeTask();
    }

    //@Test

    /**
     * workflow_read_write_hive方法。
     */
    public void workflow_read_write_hive() {
        String filePath = "ex/workflow_read_write_hive.json";
        WorkFlowApplicationContext workFlowApplicationContext = new WorkFlowApplicationContext(filePath);
        workFlowApplicationContext.executeTask();
    }

    //@Test

    /**
     * workflow_pivot方法。
     */
    public void workflow_pivot() {
        String filePath = "ex/workflow_pivot.json";
        WorkFlowApplicationContext workFlowApplicationContext = new WorkFlowApplicationContext(filePath);
        workFlowApplicationContext.executeTask();
    }

    //真实的干活的

    //@Test

    /**
     * aa方法。
     */
    public void aa() {
        String filePath = "ex/workflow_readlocal_writehive.json";
        WorkFlowApplicationContext workFlowApplicationContext = new WorkFlowApplicationContext();
        workFlowApplicationContext.initialByLocalFilePath(filePath);
        workFlowApplicationContext.executeTask();
    }
}
