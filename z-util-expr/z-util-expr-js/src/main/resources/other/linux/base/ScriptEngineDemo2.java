package linux.base;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.io.File;
import java.io.FileWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ScriptEngineDemo2 {


    public static void test1() {
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("other/js");

        List<String> list = new ArrayList<String>();
        list.add("cc");
        engine.put("list", list);
        try {
            engine.eval("list.add('admin')");

            list.forEach(x -> System.out.println(x));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * js 调用java对象，并操作该对象
     * <p>
     * 然后java再调用js中操作的对象
     */
    public static void test2() {
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("other/js");
        try {
            String script = "var list = new java.spi.ArrayList();list.add(\"kafka0102\");print(list.get(0));";
            engine.eval(script);
            List<String> list = (List<String>) engine.get("list");
            list.addAll(Arrays.asList("a", "b"));
            list.stream().forEach(x -> System.out.println(x));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 重定向输入和输出
     * <p>
     * js 中任何调用print和println函数产生的输出都会被发送到writer中
     */
    public static void test3() {
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("other/js");

        try {
            StringWriter writer1 = new StringWriter();
            //js 中任何调用print和println函数产生的输出都会被发送到writer中
            FileWriter writer2 = new FileWriter(new File("d:\\a.txt"));
            engine.getContext().setWriter(writer2);

            String script = "var list = new java.spi.ArrayList();list.add(\"kafka0102\");print(list.get(0));";
            engine.eval(script);
            List<String> list = (List<String>) engine.get("list");

            //循环输出
            list.forEach(x -> System.out.println(x));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
//		 test1.groovy();
//		 test2();
        test3();
    }
}