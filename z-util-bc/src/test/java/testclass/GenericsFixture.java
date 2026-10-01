package testclass;

import java.util.Map;

/**
 * 泛型与异常反编译夹具：覆盖 Signature + Exceptions 属性 round-trip。
 */
public class GenericsFixture {

    public Map<String, Integer> counters;

    public int safeDiv(int a, int b) throws ArithmeticException {
        return a / b;
    }

    public java.util.List<String> names;
}