package com.zifang.util.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * README 里每一条定量主张都必须能从源码重算出来 —— 广告出去的话就是承诺。
 *
 * <p>为什么要有这一层：z-util 的 README 把版本号、根 POM 的模块清单、模块数、
 * 第三方依赖版本、入口类名都写死了，而每一次改代码都会让其中某个数字过期。
 * 没有任何尺子去问源码「现在还是吗」，那这些数字的保质期就是上一次编辑的时刻。</p>
 *
 * <p>口径五条：</p>
 * <ol>
 *   <li><b>事实对象只装源码侧</b>：版本、模块清单、依赖版本、类名一律现读 pom / 源码；
 *       README 侧的数字必须在核对当下从被核对的那份 README 里现解析。
 *       否则尺子做的是恒等式比较 —— 改了 README 也不红，尺子等于没有。</li>
 *   <li><b>主张抠不到就红</b>：不许「正则没匹配上就跳过」，那正是空跑。</li>
 *   <li><b>按 README 结构定位，不按数字定位</b>：数字是结论不是锚点，
 *       短数字是长数字的子串（「6 个子模块」⊂「16 个子模块」），按数字定位会匹配到别人的行上。</li>
 *   <li><b>根 pom 走 XML 解析</b>：正则会抓到 {@code <parent>} 里的 version、
 *       躲不掉被 {@code <!-- -->} 注释掉的 module；CI-friendly 版本要读
 *       {@code <properties><revision>}。</li>
 *   <li><b>历史叙述不钉</b>：带日期 / 版本锚点 / 发布件字样的实测记录（2026-10-19 那次
 *       repo1 逐个 curl）不是「当前态断言」，改了就是篡改历史测量，守卫也不去动它。</li>
 * </ol>
 *
 * <p>数值不等 = 红且把<b>重算值</b>打进消息（红了要知道真数是多少，否则只是又一处猜）。
 * 尺子的牙口由 {@link #rulerActuallyCondemnsWrongNumbers()} 用合成 README 证明，
 * 而「不许冤枉健康页」由 {@link #readmeClaimsMatchTheCode()} 本身证明。</p>
 */
class ReadmeClaimsTest {

    /** 本仓自己的 groupId：自家坐标的字面版本不计入「第三方字面版本钉」。 */
    private static final String OWN_GROUP = "io.github.yuku123";

    /** XML 注释：根 pom 里全是历史比对说明，解析前必须剥掉，否则会读到注释里的字样。 */
    private static final Pattern XML_COMMENT = Pattern.compile("<!--.*?-->", Pattern.DOTALL);

    /** 版本号：1.2.8 / 4.1.138.Final / 3.32.0-GA / 33.6.0-jre 都得能吃下。 */
    private static final Pattern VERSION = Pattern.compile("[0-9][0-9A-Za-z.\\-]*");

    /** 根 pom 里被钉住的第三方版本（README「技术栈」表逐行对照）。 */
    private static final String[][] TECH_STACK_VERSIONS = {
            // {技术栈表行标签, README 里写的名字, 源码侧坐标（artifactId）}
            {"关键第三方版本", "log4j", "log4j-api"},
            {"关键第三方版本", "slf4j", "slf4j-api"},
            {"关键第三方版本", "junit", "junit-jupiter"},
            {"关键第三方版本", "okhttp", "okhttp"},
            {"关键第三方版本", "netty", "netty-all"},
            {"关键第三方版本", "protobuf-java", "protobuf-java"},
            {"解析", "ANTLR", "antlr4-runtime"},
            {"解析", "jsoup", "jsoup"},
            {"解析", "Guava", "guava"},
            {"Office", "POI", "poi"},
            {"Office", "PDFBox", "pdfbox"},
            {"字节码", "ASM", "asm"},
            {"字节码", "cglib", "cglib"},
            {"字节码", "javassist", "javassist"},
            {"字节码", "javaparser", "javaparser-core"},
            {"字节码", "jol-core", "jol-core"},
            {"数据", "Druid", "druid"},
            {"数据", "mysql-connector-j", "mysql-connector-j"},
            {"数据", "jakarta.persistence-api", "jakarta.persistence-api"},
            {"数据", "MyBatis", "mybatis"},
            {"数据", "H2", "h2"},
            {"运维", "github-api", "github-api"},
            {"运维", "JGit", "org.eclipse.jgit"},
    };

    /** 子模块表：{README 小节标题里点的模块名, 模块目录名}（子模块表只对名单集合，不对顺序）。 */
    private static final String[][] SUB_TABLES = {
            {"z-util-parser", "z-util-parser"},
            {"z-util-expr", "z-util-expr"},
            {"z-util-devops", "z-util-devops"},
            {"z-util-serialize", "z-util-serialize"},
    };

    // ------------------------------------------------------------------ 基础设施

    /** 仓根：向上找同时有 README.md 与 pom.xml 的目录（测试跑在模块目录，user.dir 不是仓根）。 */
    private static Path repoRoot() {
        Path p = Paths.get(System.getProperty("user.dir")).toAbsolutePath().normalize();
        for (int i = 0; i < 5 && p != null; i++, p = p.getParent()) {
            if (Files.isRegularFile(p.resolve("README.md")) && Files.isRegularFile(p.resolve("pom.xml"))) {
                return p;
            }
        }
        throw new IllegalStateException("定位仓根失败：user.dir=" + System.getProperty("user.dir"));
    }

    private static String read(Path f) {
        try {
            return new String(Files.readAllBytes(f), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static String norm(Path p) {
        return p.toString().replace('\\', '/');
    }

    /** 解析 pom 的项目元素。先剥 XML 注释，再走 DOM（正则会抓到注释里的字样）。 */
    private static Element projectElement(Path pom) {
        try {
            String xml = XML_COMMENT.matcher(read(pom)).replaceAll("");
            DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
            f.setNamespaceAware(false);
            Document doc = f.newDocumentBuilder().parse(new InputSource(new StringReader(xml)));
            return doc.getDocumentElement();
        } catch (Exception e) {
            throw new IllegalStateException("解析 pom 失败：" + pom, e);
        }
    }

    private static Element child(Element parent, String name) {
        if (parent == null) {
            return null;
        }
        NodeList children = parent.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node n = children.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE && name.equals(n.getNodeName())) {
                return (Element) n;
            }
        }
        return null;
    }

    private static List<Element> children(Element parent, String name) {
        List<Element> out = new ArrayList<>();
        if (parent == null) {
            return out;
        }
        NodeList children = parent.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node n = children.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE && name.equals(n.getNodeName())) {
                out.add((Element) n);
            }
        }
        return out;
    }

    /** 全部子元素（不看名字）—— {@code <properties>} 里的属性名是任意的。 */
    private static List<Element> childElements(Element parent) {
        List<Element> out = new ArrayList<>();
        if (parent == null) {
            return out;
        }
        NodeList children = parent.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node n = children.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                out.add((Element) n);
            }
        }
        return out;
    }

    private static String text(Element e) {
        if (e == null) {
            return null;
        }
        String t = e.getTextContent();
        return t == null ? null : t.trim();
    }

    private static String textOf(Element parent, String name) {
        return text(child(parent, name));
    }

    /** 解析 ${...} 占位（CI-friendly 版本、第三方版本属性）。 */
    private static String resolve(String value, Map<String, String> props) {
        if (value == null) {
            return null;
        }
        String v = value;
        for (int i = 0; i < 5; i++) {
            Matcher m = Pattern.compile("\\$\\{([^}]+)}").matcher(v);
            if (!m.find()) {
                break;
            }
            StringBuilder sb = new StringBuilder();
            int last = 0;
            do {
                sb.append(v, last, m.start());
                String key = m.group(1).trim();
                String repl = props.get(key);
                sb.append(repl == null ? m.group() : repl);
                last = m.end();
            } while (m.find());
            sb.append(v.substring(last));
            v = sb.toString();
        }
        return v;
    }

    // ------------------------------------------------------------------ 事实对象（只装源码侧）

    /**
     * 一次重算出来的全部事实。**只装源码侧** —— README 侧的数字必须现解析，不许进这里。
     */
    private static final class Facts {
        String revision;
        String groupId;
        List<String> modules = new ArrayList<>();
        boolean hasParent;
        boolean hasImportedBom;
        String compileVersion;
        String compilerSource;
        String compilerTarget;
        int modulePomCount;
        int jarPomCount;
        int pomPomCount;
        /** 根 DM 里自家坐标的条数。 */
        int rootDmOwnCount;
        /** 根 DM 里自家坐标中指向 jar 打包件的条数。 */
        int rootDmOwnJarCount;
        /** z-util-all 的 DM 里自家坐标的条数。 */
        int allDmOwnCount;
        /** 模块 POM 里第三方字面版本（坐标非自家、版本不是 ${...}）的处数。 */
        int literalThirdPartyVersions;
        /** artifactId → 版本（根 DM 解析后 + 模块 POM 字面）。 */
        Map<String, String> versions = new LinkedHashMap<>();
        /** 模块目录名 → 该 pom 的 <module> 条目（顺序）。 */
        Map<String, List<String>> subModules = new LinkedHashMap<>();
        /** 全仓 src/main 下的全限定类名。 */
        Set<String> fqcn = new LinkedHashSet<>();
        /** 全仓 src/main 下的简单类名。 */
        Set<String> simpleNames = new LinkedHashSet<>();
        /** 全仓 src/main 下的包名。 */
        Set<String> packages = new LinkedHashSet<>();
        /** z-util-core/src/main 下的包数（distinct package 声明）。 */
        int corePackageCount;
    }

    /**
     * 重算事实。事实只来自源码/pom，一次 JVM 生命周期内不变 ⇒ 记一份缓存，
     * 省得每个用例都把 70 个 pom 和整棵源码树重扫一遍（纯性能，判定结果不变）。
     */
    private static Facts recomputed() {
        if (factsCache == null) {
            factsCache = computeFacts();
        }
        return factsCache;
    }

    private static Facts factsCache;

    private static Facts computeFacts() {
        Path root = repoRoot();
        Facts f = new Facts();

        Element project = projectElement(root.resolve("pom.xml"));
        Element props = child(project, "properties");
        Map<String, String> propMap = new LinkedHashMap<>();
        for (Element p : childElements(props)) {
            propMap.put(p.getNodeName(), text(p));
        }
        f.revision = textOf(props, "revision");
        if (f.revision == null) {
            f.revision = propMap.get("revision");
        }
        f.groupId = textOf(project, "groupId");
        f.hasParent = child(project, "parent") != null;

        for (Element m : children(child(project, "modules"), "module")) {
            f.modules.add(text(m));
        }

        // 根 DM：自家坐标条数 + 全部坐标的版本
        Element dm = child(project, "dependencyManagement");
        Element dmDeps = child(dm, "dependencies");
        List<Element> rootDeps = children(dmDeps, "dependency");
        for (Element d : rootDeps) {
            String g = textOf(d, "groupId");
            String a = textOf(d, "artifactId");
            String v = resolve(textOf(d, "version"), propMap);
            if (a == null || v == null) {
                continue;
            }
            if ("import".equals(textOf(d, "scope"))) {
                f.hasImportedBom = true;
            }
            f.versions.putIfAbsent(a, v);
            if (OWN_GROUP.equals(g)) {
                f.rootDmOwnCount++;
            }
        }

        // compile.version + 编译插件的 source/target
        f.compileVersion = propMap.get("compile.version");
        Element compilerCfg = pluginConfiguration(project, "maven-compiler-plugin");
        f.compilerSource = resolve(textOf(compilerCfg, "source"), propMap);
        f.compilerTarget = resolve(textOf(compilerCfg, "target"), propMap);

        // 全仓 pom：模块数、jar/pom 拆分、模块字面第三方版本
        for (Path pom : modulePoms(root)) {
            Element p = projectElement(pom);
            String packaging = textOf(p, "packaging");
            f.modulePomCount++;
            if ("pom".equals(packaging)) {
                f.pomPomCount++;
            } else {
                f.jarPomCount++;
            }
            Element deps = child(p, "dependencies");
            for (Element d : children(deps, "dependency")) {
                String g = textOf(d, "groupId");
                String a = textOf(d, "artifactId");
                String v = textOf(d, "version");
                if (g == null || a == null || v == null) {
                    continue;
                }
                if (OWN_GROUP.equals(g) || v.contains("${")) {
                    continue;
                }
                f.literalThirdPartyVersions++;
                f.versions.putIfAbsent(a, v);
            }
        }
        // 根 DM 里指向 pom 打包件的自家坐标 → jar 件数
        for (Element d : rootDeps) {
            if (!OWN_GROUP.equals(textOf(d, "groupId"))) {
                continue;
            }
            String a = textOf(d, "artifactId");
            if (!"pom".equals(packagingOf(root, a))) {
                f.rootDmOwnJarCount++;
            }
        }

        // z-util-all 的 DM 条数
        Path allPom = root.resolve("z-util-all/pom.xml");
        if (Files.isRegularFile(allPom)) {
            Element all = projectElement(allPom);
            Element allDm = child(child(all, "dependencyManagement"), "dependencies");
            for (Element d : children(allDm, "dependency")) {
                if (OWN_GROUP.equals(textOf(d, "groupId"))) {
                    f.allDmOwnCount++;
                }
            }
        }

        // 各聚合件的 <module>（子模块表对账用）
        for (String[] pair : SUB_TABLES) {
            Path dir = moduleDir(root, pair[1]);
            if (dir == null) {
                continue;
            }
            Element p = projectElement(dir.resolve("pom.xml"));
            List<String> mods = new ArrayList<>();
            for (Element m : children(child(p, "modules"), "module")) {
                mods.add(text(m));
            }
            f.subModules.put(pair[1], mods);
        }

        // 全仓 src/main 的类与包
        Set<String> corePackages = new TreeSet<>();
        try (Stream<Path> s = Files.walk(root)) {
            List<Path> files = s.filter(Files::isRegularFile)
                    .filter(p -> norm(p).endsWith(".java"))
                    .filter(p -> norm(p).contains("/src/main/java/"))
                    .filter(p -> !norm(p).contains("/target/"))
                    .collect(Collectors.toList());
            for (Path p : files) {
                Matcher m = Pattern.compile("(?m)^\\s*package\\s+([\\w.]+)\\s*;").matcher(read(p));
                String simple = p.getFileName().toString().replace(".java", "");
                if (m.find()) {
                    String pkg = m.group(1);
                    f.fqcn.add(pkg + "." + simple);
                    f.simpleNames.add(simple);
                    f.packages.add(pkg);
                    if (norm(p).startsWith(norm(root.resolve("z-util-core")) + "/")) {
                        corePackages.add(pkg);
                    }
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        f.corePackageCount = corePackages.size();
        return f;
    }

    /** 按 artifactId 找插件的 {@code <configuration>}（build/plugins 与 pluginManagement 两处都要认）。 */
    private static Element pluginConfiguration(Element project, String artifactId) {
        Element build = child(project, "build");
        List<Element> containers = new ArrayList<>();
        containers.add(child(child(build, "pluginManagement"), "plugins"));
        containers.add(child(build, "plugins"));
        containers.add(child(child(project, "pluginManagement"), "plugins"));
        for (Element container : containers) {
            for (Element plugin : children(container, "plugin")) {
                if (artifactId.equals(textOf(plugin, "artifactId"))) {
                    return child(plugin, "configuration");
                }
            }
        }
        return null;
    }

    /** 仓内全部模块 pom（排除根 pom 与 target 下的）。 */
    private static List<Path> modulePoms(Path root) {
        try (Stream<Path> s = Files.walk(root)) {
            return s.filter(Files::isRegularFile)
                    .filter(p -> "pom.xml".equals(p.getFileName().toString()))
                    .filter(p -> !norm(p).contains("/target/"))
                    .filter(p -> !p.normalize().equals(root.resolve("pom.xml").normalize()))
                    .sorted()
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /** 按 artifactId 找模块的 packaging（默认 jar）。 */
    private static String packagingOf(Path root, String artifactId) {
        for (Path pom : modulePoms(root)) {
            Element p = projectElement(pom);
            if (artifactId.equals(textOf(p, "artifactId"))) {
                String pk = textOf(p, "packaging");
                return pk == null ? "jar" : pk;
            }
        }
        return "jar";
    }

    /** 按目录名找模块目录（多模块仓里不要按目录名统计定位，但这里找的是唯一同名模块）。 */
    private static Path moduleDir(Path root, String name) {
        try (Stream<Path> s = Files.walk(root, 3)) {
            return s.filter(Files::isDirectory)
                    .filter(p -> !norm(p).contains("/target/"))
                    .filter(p -> p.getFileName().toString().equals(name))
                    .filter(p -> Files.isRegularFile(p.resolve("pom.xml")))
                    .findFirst()
                    .orElse(null);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    // ------------------------------------------------------------------ Markdown 表格解析

    /** 把一行表格切成单元格。 */
    private static String[] splitRow(String line) {
        String t = line.trim();
        if (t.startsWith("|")) {
            t = t.substring(1);
        }
        if (t.endsWith("|")) {
            t = t.substring(0, t.length() - 1);
        }
        String[] cells = t.split("\\|", -1);
        for (int i = 0; i < cells.length; i++) {
            cells[i] = cells[i].trim();
        }
        return cells;
    }

    /** 读出所有表格：每个表格 = {表头单元格...} + 数据行。 */
    private static List<List<String[]>> allTables(String readme) {
        List<List<String[]>> tables = new ArrayList<>();
        List<String[]> current = null;
        for (String line : readme.split("\n", -1)) {
            String t = line.trim();
            if (t.startsWith("|") && t.endsWith("|")) {
                String[] cells = splitRow(t);
                if (isSeparator(cells)) {
                    continue;
                }
                if (current == null) {
                    current = new ArrayList<>();
                    tables.add(current);
                }
                current.add(cells);
            } else {
                current = null;
            }
        }
        return tables;
    }

    private static boolean isSeparator(String[] cells) {
        for (String c : cells) {
            if (!c.matches(":?-{2,}:?")) {
                return false;
            }
        }
        return cells.length > 0;
    }

    /** 按表头定位表格；定位不到就抛（抠不到就红）。表头里任何一格命中即算。 */
    private static List<String[]> tableByHeader(String readme, String headerContains) {
        for (List<String[]> t : allTables(readme)) {
            if (t.isEmpty()) {
                continue;
            }
            String head = String.join(" ", t.get(0));
            if (head.contains(headerContains)) {
                return t.subList(1, t.size());
            }
        }
        throw new AssertionError("README 里找不到表头含「" + headerContains + "」的表格（表被改过形？还是这节被挪走了？）");
    }

    /** 反引号里的第一段内容。 */
    private static String backticked(String cell) {
        Matcher m = Pattern.compile("`([^`]+)`").matcher(cell);
        return m.find() ? m.group(1).trim() : null;
    }

    // ------------------------------------------------------------------ 主张核对

    /** 逐条核对 README 主张与源码事实；返回不一致清单（空 = 全对）。 */
    private static List<String> checkClaims(String readme, Facts f) {
        List<String> problems = new ArrayList<>();

        // 主张 1：当前版本 ↔ 根 pom <properties><revision>
        String versionCell = basicInfo(readme, "当前版本");
        String readmeVersion = null;
        Matcher vm = Pattern.compile("`(\\d+\\.\\d+\\.\\d+)`").matcher(versionCell);
        if (vm.find()) {
            readmeVersion = vm.group(1);
        }
        if (readmeVersion == null) {
            problems.add("[当前版本] 基本信息表的「当前版本」行抠不出 `X.Y.Z`： " + versionCell);
        } else if (!readmeVersion.equals(f.revision)) {
            problems.add("[当前版本] README 写 " + readmeVersion + "，根 pom <properties><revision> 是 " + f.revision);
        }

        // 主张 2：Maven 坐标里的 groupId ↔ 根 pom groupId
        String coordCell = basicInfo(readme, "Maven 坐标");
        Matcher gm = Pattern.compile("根聚合\\s*`([\\w.\\-]+):").matcher(coordCell);
        String readmeGroup = gm.find() ? gm.group(1) : null;
        if (readmeGroup == null) {
            problems.add("[Maven 坐标] 基本信息表的「Maven 坐标」行抠不出 groupId： " + coordCell);
        } else if (!readmeGroup.equals(f.groupId)) {
            problems.add("[Maven 坐标] README 写 groupId " + readmeGroup + "，根 pom 是 " + f.groupId);
        }

        // 主张 3：父项目 = 无 ↔ 根 pom 确实没有 <parent>、也不 import 外部 BOM
        String parentCell = basicInfo(readme, "父项目");
        Matcher pm = Pattern.compile("\\*\\*([^*]+)\\*\\*").matcher(parentCell);
        String readmeParent = pm.find() ? pm.group(1).trim() : null;
        if (readmeParent == null) {
            problems.add("[父项目] 基本信息表的「父项目」行抠不出结论： " + parentCell);
        } else {
            if (!"无".equals(readmeParent)) {
                problems.add("[父项目] README 写「" + readmeParent + "」，但本仓口径是「无」");
            }
            if (f.hasParent) {
                problems.add("[父项目] 根 pom 里出现了 <parent>，README 说的是「无」");
            }
            if (f.hasImportedBom) {
                problems.add("[父项目] 根 pom 里 import 了外部 BOM，README 说的是「不 import 任何外部 BOM」");
            }
        }

        // 主张 4：运行口径 Java 8 ↔ compile.version + 编译插件的 source/target
        String javaCell = basicInfo(readme, "运行口径");
        Matcher jm = Pattern.compile("Java\\s+(\\d+)").matcher(javaCell);
        String readmeJava = jm.find() ? jm.group(1) : null;
        Matcher cm = Pattern.compile("`compile\\.version=(\\d+)`").matcher(javaCell);
        String readmeCompile = cm.find() ? cm.group(1) : null;
        if (readmeJava == null || readmeCompile == null) {
            problems.add("[运行口径] 基本信息表的「运行口径」行抠不出「Java N」或 `compile.version=N`： " + javaCell);
        } else {
            if (!readmeJava.equals(f.compileVersion)) {
                problems.add("[运行口径] README 写 Java " + readmeJava + "，根 pom compile.version 是 " + f.compileVersion);
            }
            if (!readmeCompile.equals(f.compileVersion)) {
                problems.add("[运行口径] README 写 compile.version=" + readmeCompile + "，根 pom 是 " + f.compileVersion);
            }
            if (!f.compileVersion.equals(f.compilerSource) || !f.compileVersion.equals(f.compilerTarget)) {
                problems.add("[运行口径] 根 pom maven-compiler-plugin 的 source/target 是 "
                        + f.compilerSource + "/" + f.compilerTarget + "，与 compile.version=" + f.compileVersion + " 不一致");
            }
        }

        // 主张 5：根 POM <modules> 条数（基本信息表）
        String moduleCell = basicInfo(readme, "模块数");
        Matcher mm = Pattern.compile("<modules>`?\\s*\\*\\*(\\d+)\\*\\*").matcher(moduleCell);
        String readmeModules = mm.find() ? mm.group(1) : null;
        if (readmeModules == null) {
            problems.add("[模块数] 基本信息表的「模块数」行抠不出根 POM <modules> 的条数： " + moduleCell);
        } else if (!readmeModules.equals(String.valueOf(f.modules.size()))) {
            problems.add("[模块数] README 写根 POM <modules> " + readmeModules
                    + " 个条目，pom 实际是 " + f.modules.size() + " 个");
        }

        // 主张 6：徽章上的模块数
        Matcher bm = Pattern.compile("img\\.shields\\.io/badge/modules-(\\d+)-").matcher(readme);
        if (!bm.find()) {
            problems.add("[模块徽章] README 顶部的 modules 徽章抠不出数字");
        } else if (!bm.group(1).equals(String.valueOf(f.modules.size()))) {
            problems.add("[模块徽章] 徽章写 modules-" + bm.group(1) + "，根 POM <modules> 是 " + f.modules.size() + " 个");
        }

        // 主张 7：模块清单表 ↔ 根 POM <modules>（逐行、逐名、逐序）
        List<String[]> moduleRows = tableByHeader(readme, "`<module>`");
        if (moduleRows.size() != f.modules.size()) {
            problems.add("[模块清单表] 表里 " + moduleRows.size() + " 行，根 POM <modules> 是 "
                    + f.modules.size() + " 条");
        }
        int n = Math.min(moduleRows.size(), f.modules.size());
        for (int i = 0; i < n; i++) {
            String name = backticked(moduleRows.get(i)[0]);
            if (name == null) {
                problems.add("[模块清单表] 第 " + (i + 1) + " 行第一格抠不出模块名： " + moduleRows.get(i)[0]);
            } else if (!name.equals(f.modules.get(i))) {
                problems.add("[模块清单表] 第 " + (i + 1) + " 行是 " + name
                        + "，根 POM <modules> 第 " + (i + 1) + " 条是 " + f.modules.get(i));
            }
        }

        // 主张 8：展开后模块 POM 数（总数 / jar / pom）
        Matcher pm2 = Pattern.compile("\\*\\*(\\d+) 个模块 POM = (\\d+) 个 jar \\+ (\\d+) 个 pom\\*\\*").matcher(moduleCell);
        if (!pm2.find()) {
            problems.add("[模块 POM 数] 基本信息表的「模块数」行抠不出「N 个模块 POM = M 个 jar + K 个 pom」： " + moduleCell);
        } else {
            int total = Integer.parseInt(pm2.group(1));
            int jar = Integer.parseInt(pm2.group(2));
            int pom = Integer.parseInt(pm2.group(3));
            if (total != f.modulePomCount) {
                problems.add("[模块 POM 数] README 写 " + total + " 个模块 POM，仓里实数 " + f.modulePomCount + " 个");
            }
            if (jar != f.jarPomCount) {
                problems.add("[模块 POM 数] README 写 " + jar + " 个 jar，实数 " + f.jarPomCount + " 个");
            }
            if (pom != f.pomPomCount) {
                problems.add("[模块 POM 数] README 写 " + pom + " 个 pom，实数 " + f.pomPomCount + " 个");
            }
        }

        // 主张 9：四张子模块表 —— 标题里的 N == 表行数 == 该 pom 的 <modules> 条数
        String[] lines = readme.split("\n", -1);
        for (String[] pair : SUB_TABLES) {
            String heading = "### `" + pair[0] + "` 的 ";
            int lineIndex = -1;
            for (int i = 0; i < lines.length; i++) {
                if (lines[i].startsWith(heading)) {
                    lineIndex = i;
                    break;
                }
            }
            if (lineIndex < 0) {
                problems.add("[子模块表] README 里找不到小节标题：" + heading.trim());
                continue;
            }
            Matcher nm = Pattern.compile("的\\s*(\\d+)\\s*个子模块").matcher(lines[lineIndex]);
            if (!nm.find()) {
                problems.add("[子模块表] " + pair[0] + " 的小节标题抠不出「N 个子模块」");
                continue;
            }
            int claimed = Integer.parseInt(nm.group(1));
            List<String> names = subTableNames(lines, lineIndex);
            List<String> real = f.subModules.get(pair[1]);
            if (real == null) {
                problems.add("[子模块表] 仓里找不到模块目录：" + pair[1]);
                continue;
            }
            if (claimed != names.size()) {
                problems.add("[子模块表] " + pair[0] + " 标题写 " + claimed + " 个子模块，表里只有 " + names.size() + " 行");
            }
            if (claimed != real.size()) {
                problems.add("[子模块表] " + pair[0] + " 标题写 " + claimed + " 个子模块，该 pom 的 <modules> 是 "
                        + real.size() + " 条");
            }
            Set<String> missing = new TreeSet<>(real);
            missing.removeAll(names);
            Set<String> extra = new TreeSet<>(names);
            extra.removeAll(real);
            if (!missing.isEmpty()) {
                problems.add("[子模块表] " + pair[0] + " 的子模块没列进 README：" + missing);
            }
            if (!extra.isEmpty()) {
                problems.add("[子模块表] " + pair[0] + " 的 README 列了 pom 里没有的子模块：" + extra);
            }
        }

        // 主张 10：技术栈表里的第三方版本 ↔ 根 pom / 模块 pom
        List<String[]> techRows = tableByHeader(readme, "层级");
        Map<String, String> techCells = new LinkedHashMap<>();
        for (String[] row : techRows) {
            techCells.put(row[0], row.length > 1 ? row[1] : "");
        }
        for (String[] spec : TECH_STACK_VERSIONS) {
            String rowLabel = spec[0];
            String readmeName = spec[1];
            String artifactId = spec[2];
            String cell = techCells.get(rowLabel);
            if (cell == null) {
                problems.add("[技术栈版本] 技术栈表里找不到行「" + rowLabel + "」");
                continue;
            }
            String readmeVer = versionAfter(cell, readmeName);
            String realVer = f.versions.get(artifactId);
            if (realVer == null) {
                problems.add("[技术栈版本] 源码里找不到坐标 " + artifactId + " 的版本（README「" + rowLabel
                        + "」行却写着 " + readmeName + " " + readmeVer + "）");
                continue;
            }
            if (readmeVer == null) {
                problems.add("[技术栈版本] 技术栈表「" + rowLabel + "」行抠不出「" + readmeName + "」后面的版本号： " + cell);
            } else if (!readmeVer.equals(realVer)) {
                problems.add("[技术栈版本] " + rowLabel + " 行的 " + readmeName + " 版本 README 写 " + readmeVer
                        + "，源码是 " + realVer + "（坐标 " + artifactId + "）");
            }
        }

        // 主张 11：模块内字面版本钉的处数
        String literalCell = techCells.get("模块内字面版本钉（现状）");
        if (literalCell == null) {
            literalCell = techCellByPrefix(techCells, "模块内字面版本钉");
        }
        if (literalCell == null) {
            problems.add("[模块内字面版本钉] 技术栈表里找不到「模块内字面版本钉」这一行");
        } else {
            Matcher lm = Pattern.compile("(\\d+)\\s*处").matcher(literalCell);
            if (!lm.find()) {
                problems.add("[模块内字面版本钉] 抠不出「N 处」： " + literalCell);
            } else {
                int claimed = Integer.parseInt(lm.group(1));
                if (claimed != f.literalThirdPartyVersions) {
                    problems.add("[模块内字面版本钉] README 写 " + claimed + " 处，模块 POM 里实数 "
                            + f.literalThirdPartyVersions + " 处");
                }
            }
        }

        // 主张 12：z-util-all 的版本表条数 + 根 DM 的自家构件数
        String allRow = null;
        for (String[] row : moduleRows) {
            if (row[0].contains("z-util-all")) {
                allRow = String.join(" | ", row);
            }
        }
        if (allRow == null) {
            problems.add("[z-util-all 版本表] 模块清单表里找不到 z-util-all 那一行");
        } else {
            Matcher am = Pattern.compile("版本表[^|]{0,40}?覆盖\\s*\\*{0,2}(\\d+)\\*{0,2}\\s*条").matcher(allRow);
            if (!am.find()) {
                problems.add("[z-util-all 版本表] z-util-all 那一行抠不出「版本表覆盖 **N 条**」： " + allRow);
            } else if (Integer.parseInt(am.group(1)) != f.allDmOwnCount) {
                problems.add("[z-util-all 版本表] README 写 " + am.group(1) + " 条，z-util-all 的 <dependencyManagement> 里是 "
                        + f.allDmOwnCount + " 条自家坐标");
            }
        }
        Matcher dmm = Pattern.compile("那里覆盖\\s*\\*{0,2}(\\d+)\\*{0,2}\\s*个自家构件，其中\\s*jar\\s*件\\s*\\*{0,2}(\\d+)\\*{0,2}")
                .matcher(readme);
        if (!dmm.find()) {
            problems.add("[根 DM 自家构件数] README 里抠不出「根 `z-util` POM 的 DM（那里覆盖 **N** 个自家构件，其中 jar 件 **M** 个）");
        } else {
            int own = Integer.parseInt(dmm.group(1));
            int jar = Integer.parseInt(dmm.group(2));
            if (own != f.rootDmOwnCount) {
                problems.add("[根 DM 自家构件数] README 写 " + own + " 个，根 pom 的 <dependencyManagement> 里是 "
                        + f.rootDmOwnCount + " 条自家坐标");
            }
            if (jar != f.rootDmOwnJarCount) {
                problems.add("[根 DM 自家构件数] README 写 jar 件 " + jar + " 个，实数 " + f.rootDmOwnJarCount + " 个");
            }
        }

        // 主张 13：常用入口表里点名的类 ↔ 源码真实存在
        List<String[]> entryRows = tableByHeader(readme, "入口类");
        Set<String> missing = new TreeSet<>();
        for (String[] row : entryRows) {
            String cell = row.length > 1 ? row[1] : "";
            for (String token : classTokens(cell)) {
                if (!classExists(token, f)) {
                    missing.add(token);
                }
            }
        }
        if (!missing.isEmpty()) {
            problems.add("[常用入口类] README 点名但源码里找不到的类：" + missing);
        }
        if (entryRows.isEmpty()) {
            problems.add("[常用入口类] 常用入口表里一行都没有");
        }

        // 主张 14：结构树里 z-util-core 的包数
        Matcher tree = Pattern.compile("(?m)^[ \\t]*(?:├──|└──)[ \\t]*z-util-core(?:/|(?=[ \\t#]))[^\\n]*$")
                .matcher(readme);
        if (!tree.find()) {
            problems.add("[z-util-core 包数] README 的结构树里找不到 z-util-core 那一行");
        } else {
            String line = tree.group();
            Matcher pm3 = Pattern.compile("(\\d+)\\s*个包").matcher(line);
            if (!pm3.find()) {
                problems.add("[z-util-core 包数] 结构树那一行抠不出「N 个包」： " + line);
            } else if (Integer.parseInt(pm3.group(1)) != f.corePackageCount) {
                problems.add("[z-util-core 包数] README 写 " + pm3.group(1) + " 个包，"
                        + "z-util-core/src/main 下实数 " + f.corePackageCount + " 个（distinct package 声明）");
            }
        }

        return problems;
    }

    /** 基本信息表里某个字段的值。 */
    private static String basicInfo(String readme, String field) {
        List<String[]> rows = tableByHeader(readme, "字段");
        for (String[] row : rows) {
            String key = row[0].replace("*", "").trim();
            if (key.equals(field)) {
                return row.length > 1 ? row[1] : "";
            }
        }
        throw new AssertionError("README 的基本信息表里找不到「" + field + "」这一行");
    }

    /** 基本信息表「模块数」行里写的根 modules 条数（抠不到就返回 null）。 */
    private static String readmeModulesCount(String readme) {
        String cell = basicInfo(readme, "模块数");
        Matcher m = Pattern.compile("<modules>`?\\s*\\*\\*(\\d+)\\*\\*").matcher(cell);
        return m.find() ? m.group(1) : null;
    }

    /** 技术栈表里按行标签前缀找一行的值。 */
    private static String techCellByPrefix(Map<String, String> techCells, String prefix) {
        for (Map.Entry<String, String> e : techCells.entrySet()) {
            if (e.getKey().startsWith(prefix)) {
                return e.getValue();
            }
        }
        return null;
    }

    /** 某个子模块小节下面的表格第一列（子模块名）。 */
    private static List<String> subTableNames(String[] lines, int headingLine) {
        List<String> names = new ArrayList<>();
        boolean headerSeen = false;
        for (int i = headingLine + 1; i < lines.length; i++) {
            String t = lines[i].trim();
            if (!t.startsWith("|") || !t.endsWith("|")) {
                if (!names.isEmpty() || headerSeen) {
                    break;              // 表结束
                }
                if (t.startsWith("### ")) {
                    break;              // 撞到下一节
                }
                continue;
            }
            String[] cells = splitRow(t);
            if (isSeparator(cells)) {
                continue;
            }
            if (!headerSeen) {
                headerSeen = true;      // 表头行
                continue;
            }
            String name = backticked(cells[0]);
            if (name == null) {
                throw new AssertionError("子模块表第一格抠不出子模块名：" + cells[0]);
            }
            names.add(name);
        }
        return names;
    }

    /** 单元格里的类名 token（限定类名 / 简单类名 / 包通配）。 */
    private static List<String> classTokens(String cell) {
        List<String> out = new ArrayList<>();
        Matcher m = Pattern.compile("`([^`]+)`").matcher(cell);
        while (m.find()) {
            String t = m.group(1).trim();
            if (t.endsWith(".*")) {
                out.add(t);
            } else if (t.matches("[a-z][\\w$]*(\\.[a-z][\\w$]*)*\\.[A-Z][\\w$]*")
                    || t.matches("[A-Z][\\w$]*")) {
                out.add(t);
            }
        }
        return out;
    }

    /** 判定 README 点名的类在源码里是否真实存在。 */
    private static boolean classExists(String token, Facts f) {
        if (token.endsWith(".*")) {
            String pkg = token.substring(0, token.length() - 2);
            for (String p : f.packages) {
                if (p.equals(pkg) || p.startsWith(pkg + ".")) {
                    return true;
                }
            }
            return false;
        }
        if (token.contains(".")) {
            if (f.fqcn.contains(token)) {
                return true;
            }
            if (token.startsWith("core.")) {
                return f.fqcn.contains("com.zifang.util." + token);
            }
            return false;
        }
        return f.simpleNames.contains(token);
    }

    /** 单元格里「名字 后面紧跟的版本号」。 */
    private static String versionAfter(String cell, String name) {
        Matcher m = Pattern.compile(Pattern.quote(name) + "[`\\s]*(?:\\+\\s*)?([0-9][0-9A-Za-z.\\-]*)",
                Pattern.CASE_INSENSITIVE).matcher(cell);
        return m.find() ? m.group(1) : null;
    }

    // ------------------------------------------------------------------ 牙口

    /** 把「当前版本」那一格里的版本改成别的值。 */
    private static String tamperVersion(String readme, String newVersion) {
        Matcher m = Pattern.compile("(\\| \\*\\*当前版本\\*\\* \\| `)(\\d+\\.\\d+\\.\\d+)(`)").matcher(readme);
        if (!m.find()) {
            throw new IllegalStateException("篡改失败：找不到「当前版本」行");
        }
        if (m.group(2).equals(newVersion)) {
            throw new IllegalStateException("篡改失败：「当前版本」本来就是 " + newVersion + "，换个值再改");
        }
        return readme.substring(0, m.start()) + m.group(1) + newVersion + m.group(3) + readme.substring(m.end());
    }

    /** 把基本信息表「模块数」行里的根 modules 条数改成别的值。 */
    private static String tamperModuleCount(String readme, String newCount) {
        Matcher m = Pattern.compile("(\\| \\*\\*模块数\\*\\* \\| 根 POM `<modules>` \\*\\*)(\\d+)(\\*\\*)").matcher(readme);
        if (!m.find()) {
            throw new IllegalStateException("篡改失败：找不到「模块数」行");
        }
        if (m.group(2).equals(newCount)) {
            throw new IllegalStateException("篡改失败：「模块数」本来就是 " + newCount + "，换个值再改");
        }
        return readme.substring(0, m.start()) + m.group(1) + newCount + m.group(3) + readme.substring(m.end());
    }

    /** 把技术栈表里某个第三方版本改成别的值。 */
    private static String tamperTechVersion(String readme, String name, String newVersion) {
        Matcher m = Pattern.compile("`" + Pattern.quote(name) + "\\s+(\\d[0-9A-Za-z.\\-]*)`").matcher(readme);
        if (!m.find()) {
            throw new IllegalStateException("篡改失败：找不到 " + name + " 的版本");
        }
        if (m.group(1).equals(newVersion)) {
            throw new IllegalStateException("篡改失败：" + name + " 的版本本来就是 " + newVersion + "，换个值再改");
        }
        String hit = m.group();
        String tampered = hit.replace(m.group(1), newVersion);
        return readme.substring(0, m.start()) + tampered + readme.substring(m.end());
    }

    /** 把常用入口表里点名的类改成源码里没有的类。 */
    private static String tamperClassName(String readme, String oldName, String newName) {
        if (!readme.contains(oldName)) {
            throw new IllegalStateException("篡改失败：README 里找不到 " + oldName);
        }
        return readme.replace(oldName, newName);
    }

    // ------------------------------------------------------------------ 用例

    /** 逐条核对 README 主张与源码事实；不一致就抛 AssertionError 并把真数打进去。 */
    private static void assertClaimsHold(String readme, Facts f) {
        List<String> problems = checkClaims(readme, f);
        if (!problems.isEmpty()) {
            throw new AssertionError("README 的定量主张与源码重算值对不上（" + problems.size() + " 条）：\n  - "
                    + String.join("\n  - ", problems));
        }
    }

    @Test
    @DisplayName("README 的定量主张与源码重算值逐条对账")
    void readmeClaimsMatchTheCode() {
        Path root = repoRoot();
        Facts f = recomputed();
        assertClaimsHold(read(root.resolve("README.md")), f);
    }

    @Test
    @DisplayName("尺子的牙口：把 README 的数改错，判定必须判红")
    void rulerActuallyCondemnsWrongNumbers() {
        String readme = read(repoRoot().resolve("README.md"));
        Facts f = recomputed();

        // 变异 1：当前版本改错（目标值与 README 现值必不同，否则这路变异是空改）
        String fakeVersion = "9.9.9".equals(f.revision) ? "8.8.8" : "9.9.9";
        String tamperedVersion = tamperVersion(readme, fakeVersion);
        assertNotEquals(readme, tamperedVersion, "篡改失败：当前版本没被改？");
        AssertionError e1 = assertThrows(AssertionError.class, () -> assertClaimsHold(tamperedVersion, f));
        assertTrue(e1.getMessage().contains("[当前版本]") && e1.getMessage().contains(f.revision),
                "报错要指向「当前版本」并打出重算值，实际是：" + e1.getMessage());

        // 变异 2：根 modules 条数改错（目标值既不等于源码值，也不等于 README 现值）
        int fakeModules = f.modules.size() + 1;
        String currentModules = readmeModulesCount(readme);
        if (String.valueOf(fakeModules).equals(currentModules)) {
            fakeModules = f.modules.size() + 2;
        }
        String tamperedModules = tamperModuleCount(readme, String.valueOf(fakeModules));
        assertNotEquals(readme, tamperedModules, "篡改失败：模块数没被改？");
        AssertionError e2 = assertThrows(AssertionError.class, () -> assertClaimsHold(tamperedModules, f));
        assertTrue(e2.getMessage().contains("[模块数]") && e2.getMessage().contains(String.valueOf(f.modules.size())),
                "报错要指向「模块数」并打出重算值，实际是：" + e2.getMessage());

        // 变异 3：技术栈里的第三方版本改错
        String fakeLog4j = "2.99.9".equals(f.versions.get("log4j-api")) ? "2.88.8" : "2.99.9";
        String tamperedTech = tamperTechVersion(readme, "log4j", fakeLog4j);
        assertNotEquals(readme, tamperedTech, "篡改失败：技术栈版本没被改？");
        AssertionError e3 = assertThrows(AssertionError.class, () -> assertClaimsHold(tamperedTech, f));
        assertTrue(e3.getMessage().contains("[技术栈版本]") && e3.getMessage().contains("log4j"),
                "报错要指向「技术栈版本」里的 log4j，实际是：" + e3.getMessage());

        // 变异 4：点名的类改成源码里没有的类
        String oldClass = readme.contains("com.zifang.util.core.jwt.Jwt")
                ? "com.zifang.util.core.jwt.Jwt" : "com.zifang.util.core.meta.page.PageResult";
        String newClass = oldClass + "X";
        String tamperedClass = tamperClassName(readme, oldClass, newClass);
        assertNotEquals(readme, tamperedClass, "篡改失败：类名没被改？");
        AssertionError e4 = assertThrows(AssertionError.class, () -> assertClaimsHold(tamperedClass, f));
        assertTrue(e4.getMessage().contains("[常用入口类]") && e4.getMessage().contains(newClass),
                "报错要指向「常用入口类」并打出那个不存在的类，实际是：" + e4.getMessage());
    }

    @Test
    @DisplayName("不许冤枉健康页：真实 README 不得被判红")
    void healthyReadmeIsNotCondemned() {
        String readme = read(repoRoot().resolve("README.md"));
        List<String> problems = checkClaims(readme, recomputed());
        assertTrue(problems.isEmpty(), "健康页被冤枉了：" + String.join(" / ", problems));
    }
}
