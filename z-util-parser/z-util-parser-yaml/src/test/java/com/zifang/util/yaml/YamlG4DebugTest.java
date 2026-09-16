package com.zifang.util.yaml;

import com.zifang.util.dsl.g4.G4FileParser;
import com.zifang.util.dsl.g4.model.G4Rule;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

/**
 * 调试 G4 文件加载的测试。
 */
/**
 * YamlG4DebugTest类。
 */

/**
 * YamlG4DebugTest类。
 */
public class YamlG4DebugTest {

    @Test
    /**
     * testExtractRulesFromYamlParserG4方法。
     */
    /**
     * testExtractRulesFromYamlParserG4方法。
     */
    public void testExtractRulesFromYamlParserG4() {
        String parserG4 = "";
        try {
            java.io.InputStream is = getClass().getClassLoader().getResourceAsStream("YamlParser.g4");
            if (is != null) {
                byte[] bytes = com.zifang.util.core.io.FileUtil.readAllBytes(is);
                is.close();
                parserG4 = new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
            }
        } catch (Exception e) {
            fail("Failed to load YamlParser.g4: " + e.getMessage());
        }

        System.out.println("=== YamlParser.g4 content ===");
        System.out.println(parserG4);
        System.out.println("=== END ===");

        List<G4Rule> rules = G4FileParser.extractRules(parserG4);
        System.out.println("Total rules extracted: " + rules.size());
        for (G4Rule r : rules) {
            System.out.println("  [" + r.getType() + "] " + r.getName() + " : " + r.getBody());
        }
        assertFalse("Should have parser rules", rules.isEmpty());
    }

    @Test
    /**
     * testExtractRulesFromYamlLexerG4方法。
     */
    /**
     * testExtractRulesFromYamlLexerG4方法。
     */
    public void testExtractRulesFromYamlLexerG4() {
        String lexerG4 = "";
        try {
            java.io.InputStream is = getClass().getClassLoader().getResourceAsStream("YamlLexer.g4");
            if (is != null) {
                byte[] bytes = com.zifang.util.core.io.FileUtil.readAllBytes(is);
                is.close();
                lexerG4 = new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
            }
        } catch (Exception e) {
            fail("Failed to load YamlLexer.g4: " + e.getMessage());
        }

        List<G4Rule> rules = G4FileParser.extractRules(lexerG4);
        System.out.println("Lexer rules extracted: " + rules.size());
        for (G4Rule r : rules) {
            System.out.println("  [" + r.getType() + "] " + r.getName());
        }
        assertFalse("Should have lexer rules", rules.isEmpty());
    }
}