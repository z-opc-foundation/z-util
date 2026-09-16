package linux.base;

import javax.script.*;
import java.util.List;

/**
 * 之前我们提到Java，总说其最大的特点是跨平台，是一次编写到处运行。但最近几年，Java领域最大的变化就是基于JVM的语言正在开始流行，
 * Java已经进入了混合编程时代。今天我们要向您介绍的就是Java在多语言方面的一个尝试，在Java中使用脚本语言。
 */
public class ScriptEngineDemo {

    /**
     * 1、可用的脚本引擎
     * <p>
     * Java 6提供对执行脚本语言的支持，这个支持来自于JSR223规范，对应的包是javax.script。默认情况下，
     * Java6只支持JavaScript脚本，它底层的实现是Mozilla Rhino，它是个纯Java的JavaScript实现。
     * 可以通过下面的代码列出当前环境中支持的脚本引擎。
     *
     * @date 2019年12月5日 下午12:05:18
     */
    public static void test1() {
        ScriptEngineManager manager = new ScriptEngineManager();
        List<ScriptEngineFactory> factories = manager.getEngineFactories();
        for (ScriptEngineFactory f : factories) {
            //查看是否支持多线程并发执行js
            /**
             * null:并发执行不安全
             * MULTITHREADED：并发执行安全
             * THREAD-ISOLATED：除了“MULTITHREADED”之外，会为每个线程维护不同的变量绑定。
             * STATELESS：除了“THREAD_ISOLATED”之外，脚本不会改变变量的绑定
             */
            System.out.println(f.getParameter("THREADING"));


            System.out.println("egine name:" + f.getEngineName());
            System.out.println("engine version:" + f.getEngineVersion());
            System.out.println("language name:" + f.getLanguageName());
            System.out.println("language version:" + f.getLanguageVersion());
            System.out.println("names:" + f.getNames());
            System.out.println("mime:" + f.getMimeTypes());
            System.out.println("extension:" + f.getExtensions());
        }
        /**
         * 可以看到，Java内置只支持JavaScript一种脚本。但是，只要遵循
         * JSR223，便可以扩展支持多种脚本语言，可以从https://scripting.dev.java.net/上查找当前已被支持的脚本的第三方库。
         */
    }

    /**
     * 2:Hello wordl实例： 接下来给出在Java中使用JavaScript的Hello world示例：
     * <p>
     * 使用的API还是很简单的，ScriptEngineManager是ScriptEngine的工厂，实例化该工厂的时候会加载可用的所有脚本引擎。
     * 从工厂中创建ScriptEngine可以使用getEngineByName、getEngineByExtension或
     * getEngineByMimeType来得到，只要参数名字能对上。执行脚本调用eval方法即可（效果等同于JavaScript中的eval）。
     *
     * @date 2019年12月5日 下午12:09:47
     */
    public static void test2() {
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("other/js");
        String script = "print ('hello script')";
        try {
            engine.eval(script);
        } catch (ScriptException e) {
            e.printStackTrace();
        }
    }

    /**
     * 3、传递变量:可以向脚本中传递变量，使得Java代码可以和脚本代码交互，示例如下：
     * <p>
     * 对于put的变量，它作用于自身engine范围内，也就是ScriptContext.ENGINE_SCOPE，
     * put的变量放到一个叫Bindings的Map中，可以通过
     * engine.getBindings(ScriptContext.ENGINE_SCOPE).get(“a”); 得到put的内容。
     * 和ENGINE_SCOPE相对，还有个ScriptContext.GLOBAL_SCOPE
     * 作用域，其作用的变量是由同一ScriptEngineFactory创建的所有ScriptEngine共享的全局作用域。
     *
     * @date 2019年12月5日 下午12:11:16
     */
    public static void test3() {
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("other/js");
        engine.put("a", 4);
        engine.put("b", 6);
        try {
            Object maxNum = engine.eval("function max_num(a,b){return (a>b)?a:b;}max_num(a,b);");
            System.out.println("max_num:" + maxNum);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 4、动态调用
     * <p>
     * 上面的例子中定义了一个JavaScript函数max_num，可以通过Invocable接口来多次调用脚本库中的函数，Invocable接口是
     * ScriptEngine可选实现的接口。下面是个使用示例：
     *
     * @date 2019年12月5日 下午12:14:27
     */
    public static void test4() {
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("other/js");
        try {
            engine.eval("function max_num(a,b){return (a>b)?a:b;}");
            Invocable invoke = (Invocable) engine;
            Object maxNum = invoke.invokeFunction("max_num", 4, 6);
            System.out.println(maxNum);
            maxNum = invoke.invokeFunction("max_num", 7, 6);
            System.out.println(maxNum);
        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    /**
     * 上面的invokeFunction，第一个参数调用的脚本函数名，后面跟的可变参数是对应的脚本函数参数。
     * <p>
     * Invocable还有个很酷的功能，就是动态实现接口，它可以从脚本引擎中得到Java Interface
     * 的实例；也就是说，可以定义个一个Java接口，其实现是由脚本完成。以上面的例子为例，定义接口JSLib，该接口中的函数和JavaScript中的函数签名保持一致：
     *
     * @date 2019年12月5日 下午12:17:59
     */
    public static void test41() {
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("other/js");
        try {
            engine.eval("function max_num(a,b){return (a>b)?a:b;}");
            Invocable invoke = (Invocable) engine;
            JSLib jslib = invoke.getInterface(JSLib.class);
            int maxNum = jslib.max_num(4, 6);
            System.out.println(maxNum);
        } catch (Exception e) {
        }
    }

    /**
     * 5、使用Java对象
     * <p>
     * 可以在JavaScript中使用Java代码，这确实是很酷的事情。在Rhino中，可以通过importClass导入一个类，
     * 也可以通过importPackage导入一个包，也可以直接使用全路经的类。在创建对象时，new也不是必须的。 示例代码如下：
     *
     * @date 2019年12月5日 下午12:46:06
     */
    public static void test5() {
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("other/js");
        try {
            String script = "var list = new java.spi.ArrayList();list.add(\"kafka0102\");print(list.get(0));";
            engine.eval(script);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 6、编译执行
     * <p>
     * 脚本引擎默认是解释执行的，如果需要反复执行脚本，可以使用它的可选接口Compilable来编译执行脚本，以获得更好的性能，示例代码如下：
     *
     * @date 2019年12月5日 下午12:46:39 @throws
     */
    public static void test6() {
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("other/js");
        try {
            Compilable compEngine = (Compilable) engine;
            CompiledScript script = compEngine.compile("function max_num(a,b){return (a>b)?a:b;}");
            script.eval();
            Invocable invoke = (Invocable) engine;
            Object maxNum = invoke.invokeFunction("max_num", 4, 6);
            System.out.println(maxNum);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
//		 test1.groovy();
        // test2();
        // test3();
        // test4();
        // test41();
        test5();
//		test6();
    }


    interface JSLib {
        int max_num(int a, int b);
    }
}