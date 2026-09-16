package com.zifang.util.core.system.properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author zifang
 */
public class JvmPropertiesDefine {

    public static final String awtToolkit = "awt.toolkit";
    public static final String fileEncoding = "file.encoding";
    public static final String fileEncodingPkg = "file.encoding.pkg";
    public static final String fileSeparator = "file.separator";
    public static final String gopherProxySet = "gopherProxySet";
    public static final String javaAwtGraphicsenv = "java.awt.graphicsenv";
    public static final String javaAwtPrinterjob = "java.awt.printerjob";
    public static final String javaClassPath = "java.class.path";
    public static final String javaClassVersion = "java.class.version";
    public static final String javaEndorsedDirs = "java.endorsed.dirs";
    public static final String javaExtDirs = "java.ext.dirs";
    public static final String javaHome = "java.home";
    public static final String javaIoTmpdir = "java.io.tmpdir";
    public static final String javaLibraryPath = "java.library.path";
    public static final String javaRuntimeName = "java.runtime.name";
    public static final String javaRuntimeVersion = "java.runtime.version";
    public static final String javaSpecificationName = "java.specification.name";
    public static final String javaSpecificationVendor = "java.specification.vendor";
    public static final String javaSpecificationVersion = "java.specification.version";
    public static final String javaVendor = "java.vendor";
    public static final String javaVendorUrl = "java.vendor.url";
    public static final String javaVendorUrlBug = "java.vendor.url.bug";
    public static final String javaVersion = "java.version";
    public static final String javaVmInfo = "java.vm.info";
    public static final String javaVmName = "java.vm.name";
    public static final String javaVmSpecificationName = "java.vm.specification.name";
    public static final String javaVmSpecificationVendor = "java.vm.specification.vendor";
    public static final String javaVmSpecificationVersion = "java.vm.specification.version";
    public static final String javaVmVendor = "java.vm.vendor";
    public static final String javaVmVersion = "java.vm.version";
    public static final String lineSeparator = "line.separator";
    public static final String osArch = "os.arch";
    public static final String osName = "os.name";
    public static final String osVersion = "os.version";
    public static final String pathSeparator = "path.separator";
    public static final String sunArchDataModel = "sun.arch.data.model";
    public static final String sunBootClassPath = "sun.boot.class.path";
    public static final String sunBootLibraryPath = "sun.boot.library.path";
    public static final String sunCpuEndian = "sun.cpu.endian";
    public static final String sunCpuIsalist = "sun.cpu.isalist";
    public static final String sunIoUnicodeEncoding = "sun.io.unicode.encoding";
    public static final String sunJavaCommand = "sun.java.command";
    public static final String sunJavaLauncher = "sun.java.launcher";
    public static final String sunJnuEncoding = "sun.jnu.encoding";
    public static final String sunManagementCompiler = "sun.management.compiler";
    public static final String sunOsPatchLevel = "sun.os.patch.level";
    public static final String userCountry = "user.country";
    public static final String userCountryFormat = "user.country.format";
    public static final String userDir = "user.dir";
    public static final String userHome = "user.home";
    public static final String userLanguage = "user.language";
    public static final String userName = "user.name";
    public static final String userTimezone = "user.timezone";
    public static final String visualvmId = "visualvm.id";
    private static final Logger log = LoggerFactory.getLogger(JvmPropertiesDefine.class);
    /**
     * LinkedHashMap<>方法。
     *
     * @return static Map<String, String> defineMap = new类型返回值
     */
    public static Map<String, String> defineMap = new LinkedHashMap<>();

    static {
        Field[] fields = JvmPropertiesDefine.class.getFields();
        for (Field field : fields) {
            if (field.getName().equals("defineMap")) {
                continue;
            }
            field.setAccessible(true);
            try {
                defineMap.put(field.getName(), (String) field.get(null));
            } catch (IllegalAccessException e) {
                log.warn("Failed to read property key '{}'", field.getName(), e);
            }
        }
    }
}
