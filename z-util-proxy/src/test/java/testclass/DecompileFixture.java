package testclass;

import java.util.List;

/**
 * 反编译集成测试夹具：覆盖多种字段类型 / 数组 / 静态块 / 构造方法。
 * 字段与方法保持简单，确保字节码指令落在 SrcCreator 的可识别模式内。
 */
public class DecompileFixture {

    public static int counter;

    public String name;
    public String[] names;
    public int[] nums;
    public long big;
    public double dbl;
    public boolean flag;
    public List<String> list;

    static {
        counter = 7;
    }

    public DecompileFixture() {
        this.name = "abc";
    }

    public int add(int a, int b) {
        return a + b;
    }
}
