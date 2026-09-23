package com.zifang.util.expr.obj;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 模板求值：把 spec 里的结构片段物化成产出对象。
 * <p>
 * 两种槽位要分清：{@code select.fields} / {@code where.expr} / {@code agg} 里写的是 EL 原文；
 * 模板槽位（{@code into}、{@code value}）里的字符串是字面量，要取当前行的值必须写 {@code ${列名}}。
 * 整串只有一个 {@code ${}} 时保留原类型（数字还是数字），混在文字里则按字符串拼接。
 * 模板中出现 {@code {"op": "..."}} 即嵌套子程序，在当前行的上下文里递归执行 —— 高维结构就是这么长出来的。
 *
 * @author zifang
 */
final class Templates {

    private static final Pattern WHOLE = Pattern.compile("^\\s*\\$\\{(.*)}\\s*$", Pattern.DOTALL);

    private static final Pattern PIECE = Pattern.compile("\\$\\{([^}]*)}");

    private final ObjEngine engine;

    Templates(ObjEngine engine) {
        this.engine = engine;
    }

    @SuppressWarnings("unchecked")
    Object eval(Object template, Object context) {
        if (template instanceof String) {
            return interpolate((String) template, context);
        }
        if (template instanceof List) {
            List<Object> out = new ArrayList<>();
            for (Object item : (List<Object>) template) {
                out.add(eval(item, context));
            }
            return out;
        }
        if (template instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) template;
            if (map.get("op") instanceof String) {
                return engine.run(map, context);
            }
            Map<String, Object> out = new LinkedHashMap<>();
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                out.put(entry.getKey(), eval(entry.getValue(), context));
            }
            return out;
        }
        return template;
    }

    private Object interpolate(String template, Object context) {
        Matcher whole = WHOLE.matcher(template);
        if (whole.matches()) {
            return engine.expression(whole.group(1), context);
        }
        Matcher piece = PIECE.matcher(template);
        if (!piece.find()) {
            return template;
        }
        StringBuilder sb = new StringBuilder();
        int last = 0;
        do {
            sb.append(template, last, piece.start());
            Object v = engine.expression(piece.group(1), context);
            sb.append(v == null ? "" : String.valueOf(v));
            last = piece.end();
        } while (piece.find());
        sb.append(template.substring(last));
        return sb.toString();
    }
}
