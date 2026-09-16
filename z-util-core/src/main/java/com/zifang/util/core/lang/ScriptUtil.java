package com.zifang.util.core.lang;

/**
 * @author: zifang
 * @time: 2022-06-13 17:03:13
 * @description: Expression util
 * @version: JDK 1.8
 */
public class ScriptUtil {
//
//    private static final Map<String, String> SCRIPT_TEMPLATE_CACHE = new ConcurrentHashMap<>();
//    private static final Map<String, ScriptEngine> SCRIPT_ENGINE_CACHE = new ConcurrentHashMap<>();
//    private static final String RETURN = "return";
//    private static ScriptEngineManager engineManager = new ScriptEngineManager();
//
//    public static Object calculate(ScriptLang lang, String expression,
//                                   Object param) {
//        if (StringUtil2.isEmpty(expression) || StringUtil2.isEmpty(lang)) {
//            return param;
//        }
//        // 替换$为$0, 实际脚本中为$0但为了使用简洁允许用户直接用$表示对象
//        expression = StringUtil2.replace(expression, SIMPLE_FUNCTION_PARAM, FUNCTION_PARAM);
//        try {
//            String langName = lang.getLangName();
//            String templatePath = lang.getTemplatePath();
//            if (StringUtil2.isEmpty(templatePath)) {
//                templatePath = langName + SCRIPT_TEMPLATE_SUFFIX;
//            }
//            ScriptEngine engine = SCRIPT_ENGINE_CACHE.computeIfAbsent(langName,
//                    value -> engineManager.getEngineByName(langName));
//            if (null == engine) {
//                throw new ExecuteScriptException("Not found script engine by " + langName);
//            }
//            String finalTemplatePath = templatePath;
//            String template = SCRIPT_TEMPLATE_CACHE.computeIfAbsent(templatePath, value -> {
//                InputStream templateStream = ThreadUtil.getResourceAsStream(finalTemplatePath);
//                if (null == templateStream) {
//                    templateStream = ScriptUtil.class.getClassLoader()
//                            .getResourceAsStream(finalTemplatePath);
//                }
//                return StringUtil2.parseSteamToString(templateStream);
//            });
//            if (!expression.contains(RETURN)) {
//                expression = "return " + expression;
//            }
//            String script = String.format(template, CALCULATE_METHOD_NAME, FUNCTION_PARAM,
//                    expression);
//            engine.eval(script);
//            Invocable invocable = (Invocable) engine;
//
//            try {
//                // invokeFunction没法自动识别Object[], 会导致脚本无法有效识别参数
//                if (param instanceof Object[]) {
//                    return invocable.invokeFunction(CALCULATE_METHOD_NAME, (Object[]) param);
//                } else {
//                    return invocable.invokeFunction(CALCULATE_METHOD_NAME, param);
//                }
//            } catch (Exception e) {
//                throw new ExecuteScriptException("Execute script exception: ", e);
//            }
//        } catch (ExecuteScriptException e) {
//            throw e;
//        } catch (Throwable e) {
//            throw new UnknownException("Unknown exception: ", e);
//        }
//    }
//
//    public static void setEngineManager(ClassLoader classLoader) {
//        engineManager = new ScriptEngineManager(classLoader);
//    }

}
