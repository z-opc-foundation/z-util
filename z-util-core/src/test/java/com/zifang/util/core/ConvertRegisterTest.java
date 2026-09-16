package com.zifang.util.core;

import com.zifang.util.core.lang.converter.Converters;
import com.zifang.util.core.lang.converter.IConverter;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * ConvertRegisterTest类。
 */
public class ConvertRegisterTest {

    @Test
    /**
     * test001方法。
     */
    public void test001() {
        assert Converters.findConverter(Byte.class, Byte.class).to((byte) 1) == ((byte) 1);
        assert Converters.findConverter(Byte.class, Short.class).to((byte) 1) == ((short) 1);
        assert Converters.findConverter(Byte.class, Integer.class).to((byte) 1) == 1;
        assert Converters.findConverter(Byte.class, Long.class).to((byte) 1) == 1L;
        assert Converters.findConverter(Byte.class, Float.class).to((byte) 1) == 1F;
        assert Converters.findConverter(Byte.class, Double.class).to((byte) 1) == 1D;
        assert Converters.findConverter(Byte.class, Character.class).to((byte) 1) == (char) 1;
        assert Converters.findConverter(Byte.class, String.class).to((byte) 1).equals("1");

        assert Converters.findConverter(Short.class, Byte.class).to((short) 1) == ((byte) 1);
        assert Converters.findConverter(Short.class, Short.class).to((short) 1) == ((short) 1);
        assert Converters.findConverter(Short.class, Integer.class).to((short) 1) == 1;
        assert Converters.findConverter(Short.class, Long.class).to((short) 1) == 1L;
        assert Converters.findConverter(Short.class, Float.class).to((short) 1) == 1F;
        assert Converters.findConverter(Short.class, Double.class).to((short) 1) == 1D;
        assert Converters.findConverter(Short.class, Character.class).to((short) 1) == (char) 1;
        assert Converters.findConverter(Short.class, String.class).to((short) 1).equals("1");

        assert Converters.findConverter(Integer.class, Byte.class).to(1) == ((byte) 1);
        assert Converters.findConverter(Integer.class, Short.class).to(1) == ((short) 1);
        assert Converters.findConverter(Integer.class, Integer.class).to(1) == 1;
        assert Converters.findConverter(Integer.class, Long.class).to(1) == 1L;
        assert Converters.findConverter(Integer.class, Float.class).to(1) == 1F;
        assert Converters.findConverter(Integer.class, Double.class).to(1) == 1D;
        assert Converters.findConverter(Integer.class, Character.class).to(1) == (char) 1;
        assert Converters.findConverter(Integer.class, String.class).to(1).equals("1");

        assert Converters.findConverter(Float.class, Byte.class).to(1.0f) == ((byte) 1);
        assert Converters.findConverter(Float.class, Short.class).to(1.0f) == ((short) 1);
        assert Converters.findConverter(Float.class, Integer.class).to(1.0f) == 1;
        assert Converters.findConverter(Float.class, Long.class).to(1.0f) == 1L;
        assert Converters.findConverter(Float.class, Float.class).to(1.0f) == 1F;
        assert Converters.findConverter(Float.class, Double.class).to(1.0f) == 1D;
        assert Converters.findConverter(Float.class, Character.class).to(1.0f) == (char) 1;
        assert Converters.findConverter(Float.class, String.class).to(1.0f).equals("1.0");

        assert Converters.findConverter(Double.class, Byte.class).to(1.0d) == ((byte) 1);
        assert Converters.findConverter(Double.class, Short.class).to(1.0d) == ((short) 1);
        assert Converters.findConverter(Double.class, Integer.class).to(1.0d) == 1;
        assert Converters.findConverter(Double.class, Long.class).to(1.0d) == 1L;
        assert Converters.findConverter(Double.class, Float.class).to(1.0d) == 1F;
        assert Converters.findConverter(Double.class, Double.class).to(1.0d) == 1D;
        assert Converters.findConverter(Double.class, Character.class).to(1.0d) == (char) 1;
        assert Converters.findConverter(Double.class, String.class).to(1.0d).equals("1.0");

        assert Converters.findConverter(String.class, Byte.class).to("1") == ((byte) 1);
        assert Converters.findConverter(String.class, Short.class).to("1") == ((short) 1);
        assert Converters.findConverter(String.class, Integer.class).to("1") == 1;
        assert Converters.findConverter(String.class, Long.class).to("1") == 1L;
        assert Converters.findConverter(String.class, Float.class).to("1") == 1F;
        assert Converters.findConverter(String.class, Double.class).to("1") == 1D;
        assert Converters.findConverter(String.class, Character.class).to("1") == '1';
        assert Converters.findConverter(String.class, String.class).to("1").equals("1");

        assert Converters.findConverter(Character.class, Byte.class).to('1') == ((byte) '1');
        assert Converters.findConverter(Character.class, Short.class).to('1') == ((short) '1');
        assert Converters.findConverter(Character.class, Integer.class).to('1') == (int) '1';
        assert Converters.findConverter(Character.class, Long.class).to('1') == (long) '1';
        assert Converters.findConverter(Character.class, Float.class).to('1') == (float) '1';
        assert Converters.findConverter(Character.class, Double.class).to('1') == (double) '1';
        assert Converters.findConverter(Character.class, Character.class).to('1') == '1';
        assert Converters.findConverter(Character.class, String.class).to('1').equals("1");
    }

    @Test
    /**
     * test002方法。
     */
    public void test002() {
        assert Converters.findConverter(Byte.class, Byte.class).to(null) == ((byte) 0);
        assert Converters.findConverter(Byte.class, Short.class).to(null) == ((short) 0);
        assert Converters.findConverter(Byte.class, Integer.class).to(null) == 0;
        assert Converters.findConverter(Byte.class, Long.class).to(null) == 0L;
        assert Converters.findConverter(Byte.class, Float.class).to(null) == 0F;
        assert Converters.findConverter(Byte.class, Double.class).to(null) == 0D;
        assert Converters.findConverter(Byte.class, Character.class).to(null) == '0';
        assert Converters.findConverter(Byte.class, String.class).to(null).equals("");
    }

    @Test
    @org.junit.Ignore("Lambda内部类泛型作业测试")
    /**
     * test003方法。
     */
    public void test003() {
        IConverter<Map<String, String>, String> converter = new IConverter<Map<String, String>, String>() {
            @Override
            /**
             * to方法。
             *      * @param value MapString,String类型参数
             * @param defaultValue String类型参数
             * @return String类型返回值
             */
            public String to(Map<String, String> value, String defaultValue) {
                return String.join(",", value.keySet());
            }
        };

        Converters.registerConverter(converter);

        Map<String, String> m = new HashMap<>();
        m.put("a", "a");
        m.put("b", "b");

        assert Converters.findConverter(Map.class, String.class).to(m).equals("a,b");
    }

    @Test
    /**
     * test004方法。
     */
    public void test004() {
        IConverter<Map<String, String>, String> converter = (value, defaultValue) -> String.join(",", value.keySet());
        Converters.registerConverter(converter, Map.class, String.class);
//        Converters.registerConverter(converter);

        Map<String, String> m = new HashMap<>();
        m.put("a", "a");
        m.put("b", "b");

        assert Converters.findConverter(Map.class, String.class).to(m).equals("a,b");
    }
}
