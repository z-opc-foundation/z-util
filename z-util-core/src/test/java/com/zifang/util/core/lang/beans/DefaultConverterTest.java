package com.zifang.util.core.lang.beans;

/**
 * DefaultConverterTest类。
 */
public class DefaultConverterTest {

    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) {
        String[] from = new String[]{"Byte", "Short", "Integer", "Long", "Float", "Double", "Character", "String"};
        String[] to = new String[]{"Byte", "Short", "Integer", "Long", "Float", "Double", "Character", "String"};

        for (int i = 0; i < from.length; i++) {
            String f = from[i];
            System.out.println("//--------------------" + f + "-----------------------");
            for (int j = 0; j < to.length; j++) {
                String t = to[j];
                StringBuffer stringBuffer = new StringBuffer();
                stringBuffer.append(String.format("/**" + "\n"));
                stringBuffer.append(String.format(String.format(" * %s -> %s", f, t) + "\n"));
                stringBuffer.append(String.format(" **/" + "\n"));
                stringBuffer.append(String.format("public %s to(%s value, %s defaultValue){", t, f, t) + "\n");
                stringBuffer.append(String.format("    log.info(\"call %s to(%s value, %s defaultValue\");", t, f, t) + "\n");
                stringBuffer.append(String.format("    if(value == null){") + "\n");
                stringBuffer.append(String.format("        return defaultValue;") + "\n");
                stringBuffer.append(String.format("     }") + "\n");
                stringBuffer.append(String.format("    return null;") + "\n");
                stringBuffer.append(String.format("}") + "\n");

                System.out.println(stringBuffer.toString());
            }

        }
    }
}
