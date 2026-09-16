package com.zifang.util.core.system.properties;

import java.util.Properties;

/**
 * JVM 属性的快照值对象。创建时从 {@link System#getProperties()} 复制一份。
 * <p>
 * 使用示例：
 * <pre>{@code
 *   JvmProperties jp = JvmPropertiesUtil.getJvmProperties();
 *   String javaVersion = jp.getJavaVersion();
 *   String osName = jp.getOsName();
 * }</pre>
 *
 * @author zifang
 */
public class JvmProperties {

    private final String awtToolkit;
    private final String fileEncoding;
    private final String fileEncodingPkg;
    private final String fileSeparator;
    private final String gopherProxySet;
    private final String ideaTestCyclicBufferSize;
    private final String javaAwtGraphicsenv;
    private final String javaAwtPrinterjob;
    private final String javaClassPath;
    private final String javaClassVersion;
    private final String javaEndorsedDirs;
    private final String javaExtDirs;
    private final String javaHome;
    private final String javaIoTmpdir;
    private final String javaLibraryPath;
    private final String javaRuntimeName;
    private final String javaRuntimeVersion;
    private final String javaSpecificationName;
    private final String javaSpecificationVendor;
    private final String javaSpecificationVersion;
    private final String javaVendor;
    private final String javaVendorUrl;
    private final String javaVendorUrlBug;
    private final String javaVersion;
    private final String javaVmInfo;
    private final String javaVmName;
    private final String javaVmSpecificationName;
    private final String javaVmSpecificationVendor;
    private final String javaVmSpecificationVersion;
    private final String javaVmVendor;
    private final String javaVmVersion;
    private final String lineSeparator;
    private final String osArch;
    private final String osName;
    private final String osVersion;
    private final String pathSeparator;
    private final String sunArchDataModel;
    private final String sunBootClassPath;
    private final String sunBootLibraryPath;
    private final String sunCpuEndian;
    private final String sunCpuIsalist;
    private final String sunIoUnicodeEncoding;
    private final String sunJavaCommand;
    private final String sunJavaLauncher;
    private final String sunJnuEncoding;
    private final String sunManagementCompiler;
    private final String sunOsPatchLevel;
    private final String userCountry;
    private final String userCountryFormat;
    private final String userDir;
    private final String userHome;
    private final String userLanguage;
    private final String userName;
    private final String userTimezone;
    private final String visualvmId;

    /**
     * 构造时自动从 System.getProperties() 读取。
     */
    public JvmProperties() {
        Properties p = System.getProperties();
        this.awtToolkit = p.getProperty("awt.toolkit");
        this.fileEncoding = p.getProperty("file.encoding");
        this.fileEncodingPkg = p.getProperty("file.encoding.pkg");
        this.fileSeparator = p.getProperty("file.separator");
        this.gopherProxySet = p.getProperty("gopherProxySet");
        this.ideaTestCyclicBufferSize = p.getProperty("idea.test.cyclic.buffer.size");
        this.javaAwtGraphicsenv = p.getProperty("java.awt.graphicsenv");
        this.javaAwtPrinterjob = p.getProperty("java.awt.printerjob");
        this.javaClassPath = p.getProperty("java.class.path");
        this.javaClassVersion = p.getProperty("java.class.version");
        this.javaEndorsedDirs = p.getProperty("java.endorsed.dirs");
        this.javaExtDirs = p.getProperty("java.ext.dirs");
        this.javaHome = p.getProperty("java.home");
        this.javaIoTmpdir = p.getProperty("java.io.tmpdir");
        this.javaLibraryPath = p.getProperty("java.library.path");
        this.javaRuntimeName = p.getProperty("java.runtime.name");
        this.javaRuntimeVersion = p.getProperty("java.runtime.version");
        this.javaSpecificationName = p.getProperty("java.specification.name");
        this.javaSpecificationVendor = p.getProperty("java.specification.vendor");
        this.javaSpecificationVersion = p.getProperty("java.specification.version");
        this.javaVendor = p.getProperty("java.vendor");
        this.javaVendorUrl = p.getProperty("java.vendor.url");
        this.javaVendorUrlBug = p.getProperty("java.vendor.url.bug");
        this.javaVersion = p.getProperty("java.version");
        this.javaVmInfo = p.getProperty("java.vm.info");
        this.javaVmName = p.getProperty("java.vm.name");
        this.javaVmSpecificationName = p.getProperty("java.vm.specification.name");
        this.javaVmSpecificationVendor = p.getProperty("java.vm.specification.vendor");
        this.javaVmSpecificationVersion = p.getProperty("java.vm.specification.version");
        this.javaVmVendor = p.getProperty("java.vm.vendor");
        this.javaVmVersion = p.getProperty("java.vm.version");
        this.lineSeparator = p.getProperty("line.separator");
        this.osArch = p.getProperty("os.arch");
        this.osName = p.getProperty("os.name");
        this.osVersion = p.getProperty("os.version");
        this.pathSeparator = p.getProperty("path.separator");
        this.sunArchDataModel = p.getProperty("sun.arch.data.model");
        this.sunBootClassPath = p.getProperty("sun.boot.class.path");
        this.sunBootLibraryPath = p.getProperty("sun.boot.library.path");
        this.sunCpuEndian = p.getProperty("sun.cpu.endian");
        this.sunCpuIsalist = p.getProperty("sun.cpu.isalist");
        this.sunIoUnicodeEncoding = p.getProperty("sun.io.unicode.encoding");
        this.sunJavaCommand = p.getProperty("sun.java.command");
        this.sunJavaLauncher = p.getProperty("sun.java.launcher");
        this.sunJnuEncoding = p.getProperty("sun.jnu.encoding");
        this.sunManagementCompiler = p.getProperty("sun.management.compiler");
        this.sunOsPatchLevel = p.getProperty("sun.os.patch.level");
        this.userCountry = p.getProperty("user.country");
        this.userCountryFormat = p.getProperty("user.country.format");
        this.userDir = p.getProperty("user.dir");
        this.userHome = p.getProperty("user.home");
        this.userLanguage = p.getProperty("user.language");
        this.userName = p.getProperty("user.name");
        this.userTimezone = p.getProperty("user.timezone");
        this.visualvmId = p.getProperty("visualvm.id");
    }

    /**
     * 从 System.getProperties() 初始化一份快照。供 {@link JvmPropertiesUtil} 调用。
     */
    public void init() {
        // Init is a no-op because the constructor already pulls all values.
        // Method kept for API backward compatibility with JvmPropertiesUtil.
    }

    public String getAwtToolkit() {
        return awtToolkit;
    }

    public String getFileEncoding() {
        return fileEncoding;
    }

    public String getFileEncodingPkg() {
        return fileEncodingPkg;
    }

    public String getFileSeparator() {
        return fileSeparator;
    }

    public String getGopherProxySet() {
        return gopherProxySet;
    }

    public String getIdeaTestCyclicBufferSize() {
        return ideaTestCyclicBufferSize;
    }

    public String getJavaAwtGraphicsenv() {
        return javaAwtGraphicsenv;
    }

    public String getJavaAwtPrinterjob() {
        return javaAwtPrinterjob;
    }

    public String getJavaClassPath() {
        return javaClassPath;
    }

    public String getJavaClassVersion() {
        return javaClassVersion;
    }

    public String getJavaEndorsedDirs() {
        return javaEndorsedDirs;
    }

    public String getJavaExtDirs() {
        return javaExtDirs;
    }

    public String getJavaHome() {
        return javaHome;
    }

    public String getJavaIoTmpdir() {
        return javaIoTmpdir;
    }

    public String getJavaLibraryPath() {
        return javaLibraryPath;
    }

    public String getJavaRuntimeName() {
        return javaRuntimeName;
    }

    public String getJavaRuntimeVersion() {
        return javaRuntimeVersion;
    }

    public String getJavaSpecificationName() {
        return javaSpecificationName;
    }

    public String getJavaSpecificationVendor() {
        return javaSpecificationVendor;
    }

    public String getJavaSpecificationVersion() {
        return javaSpecificationVersion;
    }

    public String getJavaVendor() {
        return javaVendor;
    }

    public String getJavaVendorUrl() {
        return javaVendorUrl;
    }

    public String getJavaVendorUrlBug() {
        return javaVendorUrlBug;
    }

    public String getJavaVersion() {
        return javaVersion;
    }

    public String getJavaVmInfo() {
        return javaVmInfo;
    }

    public String getJavaVmName() {
        return javaVmName;
    }

    public String getJavaVmSpecificationName() {
        return javaVmSpecificationName;
    }

    public String getJavaVmSpecificationVendor() {
        return javaVmSpecificationVendor;
    }

    public String getJavaVmSpecificationVersion() {
        return javaVmSpecificationVersion;
    }

    public String getJavaVmVendor() {
        return javaVmVendor;
    }

    public String getJavaVmVersion() {
        return javaVmVersion;
    }

    public String getLineSeparator() {
        return lineSeparator;
    }

    public String getOsArch() {
        return osArch;
    }

    public String getOsName() {
        return osName;
    }

    public String getOsVersion() {
        return osVersion;
    }

    public String getPathSeparator() {
        return pathSeparator;
    }

    public String getSunArchDataModel() {
        return sunArchDataModel;
    }

    public String getSunBootClassPath() {
        return sunBootClassPath;
    }

    public String getSunBootLibraryPath() {
        return sunBootLibraryPath;
    }

    public String getSunCpuEndian() {
        return sunCpuEndian;
    }

    public String getSunCpuIsalist() {
        return sunCpuIsalist;
    }

    public String getSunIoUnicodeEncoding() {
        return sunIoUnicodeEncoding;
    }

    public String getSunJavaCommand() {
        return sunJavaCommand;
    }

    public String getSunJavaLauncher() {
        return sunJavaLauncher;
    }

    public String getSunJnuEncoding() {
        return sunJnuEncoding;
    }

    public String getSunManagementCompiler() {
        return sunManagementCompiler;
    }

    public String getSunOsPatchLevel() {
        return sunOsPatchLevel;
    }

    public String getUserCountry() {
        return userCountry;
    }

    public String getUserCountryFormat() {
        return userCountryFormat;
    }

    public String getUserDir() {
        return userDir;
    }

    public String getUserHome() {
        return userHome;
    }

    public String getUserLanguage() {
        return userLanguage;
    }

    public String getUserName() {
        return userName;
    }

    public String getUserTimezone() {
        return userTimezone;
    }

    public String getVisualvmId() {
        return visualvmId;
    }
}