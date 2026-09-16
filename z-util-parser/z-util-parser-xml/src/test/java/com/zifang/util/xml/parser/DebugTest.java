package com.zifang.util.xml.parser;

import com.zifang.util.xml.XmlUtil;
import com.zifang.util.xml.model.XDocument;
import org.junit.Test;

/**
 * DebugTest类。
 */
public class DebugTest {
    @Test
    /**
     * testDebug方法。
     */
    public void testDebug() throws Exception {
        String xml = "<root id=\"123\" name=\"test\"/>";
        System.out.println("Input: " + xml);
        XDocument doc = XmlUtil.parse(xml);
        System.out.println("Root: " + doc.getRoot().getName());
        System.out.println("Attrs: " + doc.getRoot().getAttributes());
    }
}
