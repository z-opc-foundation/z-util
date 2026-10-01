package testclass;

/**
 * 控制流夹具：包含 try/catch 模式以触发 exception_table 反编译。
 */
public class ControlFlowFixture {

    /**
     * 单 try/catch：捕获 RuntimeException。
     * 生成 exception_table 一条 [0..N, handler_pc, ClassInfo(RuntimeException)]
     */
    public int safeParse(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /**
     * 多 catch：IOException + 兜底 RuntimeException。
     */
    public String safeLength(Object obj) {
        try {
            return String.valueOf(obj);
        } catch (NullPointerException e) {
            return "null";
        } catch (RuntimeException e) {
            return "err";
        }
    }

    /**
     * finally 兜底（catch_type=0）。
     */
    private static int finallyCount;

    public int withFinally(int x) {
        try {
            return x + 1;
        } finally {
            finallyCount++;
        }
    }
}