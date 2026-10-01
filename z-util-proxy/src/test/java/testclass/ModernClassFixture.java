package testclass;

/**
 * 现代类属性夹具：包含注解、内部类、lambda 等会生成 InnerClasses /
 * RuntimeVisibleAnnotations / BootstrapMethods 等现代属性。
 */
@Deprecated
public class ModernClassFixture {

    @Deprecated
    public int value;

    public static class Inner {
        public int x;
    }

    public Runnable lambda = () -> {
        int local = 1;
        local++;
    };

    @Deprecated
    public int tagged() {
        return 0;
    }

    public int branched(int x) {
        if (x > 0) {
            return x;
        } else {
            return -x;
        }
    }

    /**
     * 持有局部类以触发 EnclosingMethod 属性。
     */
    public Object enclosing() {
        class Obj {
            int v;
        }
        return new Obj();
    }

    /**
     * 泛型 List 参数触发 LocalVariableTypeTable 属性（参数签名要保留 List<Integer>）。
     */
    public String withGenerics(java.util.List<Integer> ints) {
        return ints.isEmpty() ? "" : String.valueOf(ints.get(0));
    }
}