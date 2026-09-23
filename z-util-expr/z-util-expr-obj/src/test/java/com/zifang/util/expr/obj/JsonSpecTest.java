package com.zifang.util.expr.obj;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zifang.util.core.json.JsonMapperFactory;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.zifang.util.expr.obj.Fixture.asLong;
import static com.zifang.util.expr.obj.Fixture.asMap;
import static com.zifang.util.expr.obj.Fixture.asRows;
import static com.zifang.util.expr.obj.Fixture.map;
import static com.zifang.util.expr.obj.Fixture.program;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * 证明"程序 = JSON"：同一份整形程序，写成 JSON 字符串与写成 Map 走的是同一条路。
 * <p>
 * spec 从数据库配置或 HTTP 请求体里来时就是这个形态，所以本语言不需要自己的语法解析器。
 * 这里用 Jackson（平台边界上的解析器）给出 {@code LinkedHashMap} 树，顺带钉住一件事：
 * {@code into} 模板的字段顺序就是产出的字段顺序，前端按列渲染才不会串列。
 *
 * @author zifang
 */
public class JsonSpecTest {

    private static final ObjectMapper MAPPER = JsonMapperFactory.newDefault();

    private static final String SALES = "["
            + "{\"city\":\"杭州\",\"region\":\"华东\",\"amount\":120},"
            + "{\"city\":\"上海\",\"region\":\"华东\",\"amount\":80},"
            + "{\"city\":\"深圳\",\"region\":\"华南\",\"amount\":300}]";

    private static final String SPEC = "["
            + " {\"op\": \"from\", \"rows\": " + SALES + "},"
            + " {\"op\": \"order\", \"by\": \"amount desc\"},"
            + " {\"op\": \"group\", \"by\": \"region\", \"items\": \"lines\","
            + "  \"agg\": {\"total\": \"SUM(amount)\"},"
            + "  \"into\": {\"region\": \"${region}\", \"total\": \"${total}\","
            + "             \"lines\": {\"op\": \"map\", \"of\": \"lines\","
            + "                        \"into\": {\"city\": \"${city}\"}}}}"
            + "]";

    @Test
    @SuppressWarnings("unchecked")
    public void jsonProgramShapesRowsIntoNestedDoc() throws Exception {
        List<Map<String, Object>> groups =
                (List<Map<String, Object>>) new ObjEngine().shape(MAPPER.readValue(SPEC, Object.class));
        assertEquals(2, groups.size());

        // 分组顺序 = 组内首现顺序（上游 order 已把 300 提到最前），明细数组沿用 order 的结果
        Map<String, Object> south = groups.get(0);
        assertEquals("华南", south.get("region"));
        assertEquals(300L, asLong(south.get("total")));

        Map<String, Object> east = groups.get(1);
        assertEquals("华东", east.get("region"));
        assertEquals(200L, asLong(east.get("total")));
        // 模板保序：JSON 里字段的书写顺序 = 产出的字段顺序
        assertEquals(Arrays.asList("region", "total", "lines"), Arrays.asList(east.keySet().toArray()));
        List<Object> lines = (List<Object>) east.get("lines");
        assertEquals("杭州", asMap(lines.get(0)).get("city"));
        assertEquals("上海", asMap(lines.get(1)).get("city"));
        // into 没写的列不该漏出来
        assertNull(asMap(lines.get(0)).get("amount"));
    }

    @Test
    public void jsonProgramAndMapProgramAgree() throws Exception {
        Object rows = MAPPER.readValue(SALES, Object.class);
        Object fromJson = new ObjEngine().shape(MAPPER.readValue(SPEC, Object.class));
        Object fromMap = new ObjEngine().shape(program(
                map("op", "order", "by", "amount desc"),
                map("op", "group", "by", "region", "items", "lines",
                        "agg", map("total", "SUM(amount)"),
                        "into", map("region", "${region}", "total", "${total}",
                                "lines", map("op", "map", "of", "lines",
                                        "into", map("city", "${city}"))))), rows);
        assertEquals("JSON 程序与 Map 程序必须产出同一结果", fromMap, fromJson);
    }

    @Test
    public void bareJsonTemplateAppliedToIncomingRows() throws Exception {
        Object rows = MAPPER.readValue(SALES, Object.class);
        Object out = new ObjEngine().shape(
                MAPPER.readValue("{\"label\": \"${city}\", \"amount\": \"${amount}\"}", Object.class), rows);
        List<Map<String, Object>> list = asRows(out);
        assertEquals(3, list.size());
        assertEquals("杭州", list.get(0).get("label"));
        // 整串只有一个 ${} 时保留 JSON 里的数字类型，不是拼成字符串
        assertEquals(120L, asLong(list.get(0).get("amount")));
    }
}
