package com.zifang.util.core.lang.reflect;

import com.zifang.util.core.lang.ArraysUtil;
import com.zifang.util.core.lang.validator.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.net.*;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static com.zifang.util.core.lang.reflect.ClassLoaderUtil.loadClass;

/**
 * 类反射工具类。
 * <p>
 * 提供类扫描、字段获取、方法获取、泛型类型解析等反射相关操作。
 *
 * @author zifang
 */
public class ClassUtil {

    private static final Logger log = LoggerFactory.getLogger(ClassUtil.class);
    private static final String JAR = "jar";
    private static final String FILE = "file";
    private static final String defaultClassPath = ClassUtil.class.getResource("/").getPath();
    public static Class<?>[] EMPTY_CLASS_ARRAY = new Class<?>[0];

    public static String METHOD_GET_PREFIX = "get";
    public static String METHOD_IS_PREFIX = "is";
    public static String METHOD_SET_PREFIX = "set";
    static {
    }

    /**
     * 获取指定目录下所有的类名
     */
    public static boolean isAllAssignableFrom(Class<?>[] typesTarget, Class<?>[] typesFrom) {
        if (typesTarget.length == typesFrom.length) {
            for (int i = 0; i < typesTarget.length; i++) {
                if (!typesTarget[i].isAssignableFrom(typesFrom[i])) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    /**
     * getAccessibleFields方法。
     * * @param clazz Class?类型参数
     *
     * @return static Field[]类型返回值
     */
    public static Field[] getAccessibleFields(Class<?> clazz) {
        return getAccessibleFields(clazz, Object.class);
    }

    /**
     * getAccessibleFields方法。
     * * @param clazz Class?类型参数
     *
     * @param limit Class?类型参数
     * @return static Field[]类型返回值
     */
    public static Field[] getAccessibleFields(Class<?> clazz, Class<?> limit) {
        Package topPackage = clazz.getPackage();
        List<Field> fieldList = new ArrayList<>();
        int topPackageHash = topPackage == null ? 0 : topPackage.hashCode();
        boolean top = true;
        do {
            if (clazz == null) {
                break;
            }
            Field[] declaredFields = clazz.getDeclaredFields();
            for (Field field : declaredFields) {
                if (top) {                // add all top declared fields
                    fieldList.add(field);
                    continue;
                }
                int modifier = field.getModifiers();
                if (Modifier.isPrivate(modifier)) {
                    continue;                                        // ignore super private fields
                }
                if (Modifier.isPublic(modifier)) {
                    addFieldIfNotExist(fieldList, field);            // add super public methods
                    continue;
                }
                if (Modifier.isProtected(modifier)) {
                    addFieldIfNotExist(fieldList, field);            // add super protected methods
                    continue;
                }
                // add super default methods from the same package
                Package pckg = field.getDeclaringClass().getPackage();
                int pckgHash = pckg == null ? 0 : pckg.hashCode();
                if (pckgHash == topPackageHash) {
                    addFieldIfNotExist(fieldList, field);
                }
            }
            top = false;
        } while ((clazz = clazz.getSuperclass()) != limit);

        Field[] fields = new Field[fieldList.size()];
        for (int i = 0; i < fields.length; i++) {
            fields[i] = fieldList.get(i);
        }
        return fields;
    }

    // ---------------------------------------------------------------- supported methods

    private static void addFieldIfNotExist(List<Field> allFields, Field newField) {
        for (Field f : allFields) {
            if (compareSignatures(f, newField)) {
                return;
            }
        }
        allFields.add(newField);
    }

    /**
     * 是否为标准的类<br>
     * 这个类必须：
     * <p>
     * 1、非接口
     * 2、非抽象类
     * 3、非Enum枚举
     * 4、非数组
     * 5、非注解
     * 6、非原始类型（int, long等）
     * </pre>
     *
     * @param clazz 类
     * @return 是否为标准类
     */
    public static boolean isNormalClass(Class<?> clazz) {
        return null != clazz //
                && !clazz.isInterface() //
                && !Modifier.isAbstract(clazz.getModifiers()) //
                && !clazz.isEnum() //
                && !clazz.isArray() //
                && !clazz.isAnnotation() //
                && !clazz.isSynthetic() //
                && !clazz.isPrimitive();//
    }

    /**
     * getSupportedMethods方法。
     * * @param clazz Class类型参数
     *
     * @return static Method[]类型返回值
     */
    public static Method[] getSupportedMethods(Class clazz) {
        return getSupportedMethods(clazz, Object.class);
    }

    /**
     * Returns a <code>Method</code> array of the methods to which instances of the specified
     * respond except for those methods defined in the class specified by limit
     * or any of its superclasses. Note that limit is usually used to eliminate
     * them methods defined by <code>java.lang.Object</code>. If limit is <code>null</code> then all
     * methods are returned.
     */
    public static Method[] getSupportedMethods(Class clazz, Class limit) {
        ArrayList<Method> supportedMethods = new ArrayList<>();
        for (Class c = clazz; c != limit && c != null; c = c.getSuperclass()) {
            Method[] methods = c.getDeclaredMethods();
            for (Method method : methods) {
                boolean found = false;
                for (Method supportedMethod : supportedMethods) {
                    if (compareSignatures(method, supportedMethod)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    supportedMethods.add(method);
                }
            }
        }
        return supportedMethods.toArray(new Method[0]);
    }


    // ---------------------------------------------------------------- compare

    /**
     * getSupportedFields方法。
     * * @param clazz Class类型参数
     *
     * @return static Field[]类型返回值
     */
    public static Field[] getSupportedFields(Class clazz) {
        return getSupportedFields(clazz, Object.class);
    }

    /**
     * getSupportedFields方法。
     * * @param clazz Class类型参数
     *
     * @param limit Class类型参数
     * @return static Field[]类型返回值
     */
    public static Field[] getSupportedFields(Class clazz, Class limit) {
        ArrayList<Field> supportedFields = new ArrayList<>();
        for (Class c = clazz; c != limit && c != null; c = c.getSuperclass()) {
            Field[] fields = c.getDeclaredFields();
            for (Field field : fields) {
                boolean found = false;
                for (Field supportedField : supportedFields) {
                    if (compareSignatures(field, supportedField)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    supportedFields.add(field);
                }
            }
        }
        return supportedFields.toArray(new Field[0]);
    }

    /**
     * Compares method declarations: signature and return types.
     */
    public static boolean compareDeclarations(Method first, Method second) {
        if (first.getReturnType() != second.getReturnType()) {
            return false;
        }
        return compareSignatures(first, second);
    }

    /**
     * Compares method signatures: names and parameters.
     */
    public static boolean compareSignatures(Method first, Method second) {
        if (!first.getName().equals(second.getName())) {
            return false;
        }
        return compareParameters(first.getParameterTypes(), second.getParameterTypes());
    }

    /**
     * Compares constructor signatures: names and parameters.
     */
    public static boolean compareSignatures(Constructor first, Constructor second) {
        if (!first.getName().equals(second.getName())) {
            return false;
        }
        return compareParameters(first.getParameterTypes(), second.getParameterTypes());
    }

    // ---------------------------------------------------------------- force

    /**
     * compareSignatures方法。
     * * @param first Field类型参数
     *
     * @param second Field类型参数
     * @return static boolean类型返回值
     */
    public static boolean compareSignatures(Field first, Field second) {
        return first.getName().equals(second.getName());
    }


    // ---------------------------------------------------------------- generics

    /**
     * Compares classes, usually method or ctor parameters.
     */
    public static boolean compareParameters(Class[] first, Class[] second) {
        if (first.length != second.length) {
            return false;
        }
        for (int i = 0; i < first.length; i++) {
            if (first[i] != second[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Suppress access check against a reflection object. SecurityException is silently ignored.
     * Checks first if the object is already accessible.
     */
    public static void forceAccess(AccessibleObject accObject) {
        try {
            if (!accObject.isAccessible()) {
                accObject.setAccessible(true);
            }
        } catch (SecurityException sex) {
            // ignore
        }
    }

    /**
     * Returns single component type. Index is used when type consist of many
     * components. If negative, index will be calculated from the end of the
     * returned array. Returns <code>null</code> if component type
     * does not exist or if index is out of bounds.
     *
     * @see #getComponentTypes(Type)
     */
    public static Class getComponentType(Type type, int index) {
        return getComponentType(type, null, index);
    }

    /**
     * Returns single component type for given type and implementation.
     * Index is used when type consist of many
     * components. If negative, index will be calculated from the end of the
     * returned array.  Returns <code>null</code> if component type
     * does not exist or if index is out of bounds.
     * <p>
     *
     * @see #getComponentTypes(Type, Class)
     */
    public static Class getComponentType(Type type, Class implClass, int index) {
        Class[] componentTypes = getComponentTypes(type, implClass);
        if (componentTypes == null) {
            return null;
        }

        if (index < 0) {
            index += componentTypes.length;
        }

        if (index >= componentTypes.length) {
            return null;
        }

        return componentTypes[index];
    }

    /**
     * @see #getComponentTypes(Type, Class)
     */
    public static Class[] getComponentTypes(Type type) {
        return getComponentTypes(type, null);
    }

    /**
     * Returns all component types of the given type.
     * For example the following types all have the
     * component-type MyClass:
     * <ul>
     * <li>MyClass[]</li>
     * <li>List&lt;MyClass&gt;</li>
     * <li>Foo&lt;? extends MyClass&gt;</li>
     * <li>Bar&lt;? super MyClass&gt;</li>
     * <li>&lt;T extends MyClass&gt; T[]</li>
     * </ul>
     */
    public static Class[] getComponentTypes(Type type, Class implClass) {
        if (type instanceof Class) {
            Class clazz = (Class) type;
            if (clazz.isArray()) {
                return new Class[]{clazz.getComponentType()};
            }
        } else if (type instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) type;

            Type[] generics = pt.getActualTypeArguments();

            if (generics.length == 0) {
                return null;
            }

            Class[] types = new Class[generics.length];

            for (int i = 0; i < generics.length; i++) {
                types[i] = getRawType(generics[i], implClass);
            }
            return types;
        } else if (type instanceof GenericArrayType) {
            GenericArrayType gat = (GenericArrayType) type;

            Class rawType = getRawType(gat.getGenericComponentType(), implClass);
            if (rawType == null) {
                return null;
            }

            return new Class[]{rawType};
        }
        return null;
    }

    /**
     * Shortcut for <code>getComponentTypes(type.getGenericSuperclass())</code>.
     *
     * @see #getComponentTypes(Type)
     */
    public static Class[] getGenericSupertypes(Class type) {
        return getComponentTypes(type.getGenericSuperclass());
    }

    /**
     * Shortcut for <code>getComponentType(type.getGenericSuperclass())</code>.
     *
     * @see #getComponentType(Type, int)
     */
    public static Class getGenericSupertype(Class type, int index) {
        return getComponentType(type.getGenericSuperclass(), index);
    }

    /**
     * Returns raw class for given <code>type</code>. Use this method with both
     * regular and generic types.
     *
     * @param type the type to convert
     * @return the closest class representing the given <code>type</code>
     * @see #getRawType(Type, Class)
     */
    public static Class getRawType(Type type) {
        return getRawType(type, null);
    }

    /**
     * Returns raw class for given <code>type</code> when implementation class is known
     * and it makes difference.
     *
     * @see #resolveVariable(TypeVariable, Class)
     */
    public static Class<?> getRawType(Type type, Class implClass) {
        if (type instanceof Class) {
            return (Class) type;
        }
        if (type instanceof ParameterizedType) {
            ParameterizedType pType = (ParameterizedType) type;
            return getRawType(pType.getRawType(), implClass);
        }
        if (type instanceof WildcardType) {
            WildcardType wType = (WildcardType) type;

            Type[] lowerTypes = wType.getLowerBounds();
            if (lowerTypes.length > 0) {
                return getRawType(lowerTypes[0], implClass);
            }

            Type[] upperTypes = wType.getUpperBounds();
            if (upperTypes.length != 0) {
                return getRawType(upperTypes[0], implClass);
            }

            return Object.class;
        }
        if (type instanceof GenericArrayType) {
            Type genericComponentType = ((GenericArrayType) type).getGenericComponentType();
            Class<?> rawType = getRawType(genericComponentType, implClass);
            // this is sort of stupid, but there seems no other way (consider don't creating new instances each time)...
            return Array.newInstance(rawType, 0).getClass();
        }
        if (type instanceof TypeVariable) {
            TypeVariable<?> varType = (TypeVariable<?>) type;
            if (implClass != null) {
                Type resolvedType = resolveVariable(varType, implClass);
                if (resolvedType != null) {
                    return getRawType(resolvedType, null);
                }
            }
            Type[] boundsTypes = varType.getBounds();
            if (boundsTypes.length == 0) {
                return Object.class;
            }
            return getRawType(boundsTypes[0], implClass);
        }
        return null;
    }

    /**
     * Resolves <code>TypeVariable</code> with given implementation class.
     */
    public static Type resolveVariable(TypeVariable variable, Class implClass) {
        Class rawType = getRawType(implClass, null);

        int index = ArraysUtil.indexOf(rawType.getTypeParameters(), variable);
        if (index >= 0) {
            return variable;
        }

        Class[] interfaces = rawType.getInterfaces();
        Type[] genericInterfaces = rawType.getGenericInterfaces();

        for (int i = 0; i <= interfaces.length; i++) {
            Class rawInterface;

            if (i < interfaces.length) {
                rawInterface = interfaces[i];
            } else {
                rawInterface = rawType.getSuperclass();
                if (rawInterface == null) {
                    continue;
                }
            }

            Type resolved = resolveVariable(variable, rawInterface);
            if (resolved instanceof Class || resolved instanceof ParameterizedType) {
                return resolved;
            }

            if (resolved instanceof TypeVariable) {
                TypeVariable typeVariable = (TypeVariable) resolved;
                index = ArraysUtil.indexOf(rawInterface.getTypeParameters(), typeVariable);

                if (index < 0) {
                    throw new IllegalArgumentException("Invalid type variable:" + typeVariable);
                }

                Type type = i < genericInterfaces.length ? genericInterfaces[i] : rawType.getGenericSuperclass();

                if (type instanceof Class) {
                    return Object.class;
                }

                if (type instanceof ParameterizedType) {
                    return ((ParameterizedType) type).getActualTypeArguments()[index];
                }

                throw new IllegalArgumentException("Unsupported type: " + type);
            }
        }
        return null;
    }

    // ---------------------------------------------------------------- annotations

    /**
     * Converts <code>Type</code> to a <code>String</code>. Supports successor interfaces:
     * <ul>
     * <li><code>java.lang.Class</code> - represents usual class</li>
     * <li><code>java.lang.reflect.ParameterizedType</code> - class with generic parameter (e.g. <code>List</code>)</li>
     * <li><code>java.lang.reflect.TypeVariable</code> - generic type literal (e.g. <code>List</code>, <code>T</code> - type variable)</li>
     * <li><code>java.lang.reflect.WildcardType</code> - wildcard type (<code>List&lt;? extends Number&gt;</code>, <code>"? extends Number</code> - wildcard type)</li>
     * <li><code>java.lang.reflect.GenericArrayType</code> - type for generic array (e.g. <code>T[]</code>, <code>T</code> - array type)</li>
     * </ul>
     */
    public static String typeToString(Type type) {
        StringBuilder sb = new StringBuilder();
        typeToString(sb, type, new HashSet<Type>());
        return sb.toString();
    }

    // ---------------------------------------------------------------- caller

    private static void typeToString(StringBuilder sb, Type type, Set<Type> visited) {
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            Class<?> rawType = (Class<?>) parameterizedType.getRawType();
            sb.append(rawType.getName());
            boolean first = true;
            for (Type typeArg : parameterizedType.getActualTypeArguments()) {
                if (first) {
                    first = false;
                } else {
                    sb.append(", ");
                }
                sb.append('<');
                typeToString(sb, typeArg, visited);
                sb.append('>');
            }
        } else if (type instanceof WildcardType) {
            WildcardType wildcardType = (WildcardType) type;
            sb.append('?');

            // According to JLS(http://java.sun.com/docs/books/jls/third_edition/html/typesValues.html#4.5.1):
            // - Lower and upper can't coexist: (for instance, this is not allowed: <? extends List<String> & super MyInterface>)
            // - Multiple bounds are not supported (for instance, this is not allowed: <? extends List<String> & MyInterface>)

            Type bound;
            if (wildcardType.getLowerBounds().length != 0) {
                sb.append(" super ");
                bound = wildcardType.getLowerBounds()[0];
            } else {
                sb.append(" extends ");
                bound = wildcardType.getUpperBounds()[0];
            }
            typeToString(sb, bound, visited);
        } else if (type instanceof TypeVariable<?>) {
            TypeVariable<?> typeVariable = (TypeVariable<?>) type;
            sb.append(typeVariable.getName());

            // prevent cycles in case: <T extends List<T>>

            if (!visited.contains(type)) {
                visited.add(type);
                sb.append(" extends ");
                boolean first = true;
                for (Type bound : typeVariable.getBounds()) {
                    if (first) {
                        first = false;
                    } else {
                        sb.append(" & ");
                    }
                    typeToString(sb, bound, visited);
                }
                visited.remove(type);
            }
        } else if (type instanceof GenericArrayType) {
            GenericArrayType genericArrayType = (GenericArrayType) type;
            typeToString(genericArrayType.getGenericComponentType());
            sb.append(genericArrayType.getGenericComponentType());
            sb.append("[]");
        } else if (type instanceof Class) {
            Class<?> typeClass = (Class<?>) type;
            sb.append(typeClass.getName());
        } else {
            throw new IllegalArgumentException("Unsupported type: " + type);
        }
    }

    /**
     * Reads annotation value. Returns <code>null</code> on error
     * (e.g. when value name not found).
     */
    public static Object readAnnotationValue(Annotation annotation, String name) {
        try {
            Method method = annotation.annotationType().getDeclaredMethod(name);
            return method.invoke(annotation);
        } catch (Exception ignore) {
            return null;
        }
    }

    /**
     * Emulates <code>Reflection.getCallerClass</code> using standard API.
     * This implementation uses custom <code>SecurityManager</code>
     * and it is the fastest. Other implementations are:
     * <ul>
     * <li><code>new Throwable().getStackTrace()[callStackDepth]</code></li>
     * <li><code>Thread.currentThread().getStackTrace()[callStackDepth]</code> (the slowest)</li>
     * </ul>
     */
    public static Class getCallerClass(int framesToSkip) {
        StackTraceElement[] stackTraceElements = new Throwable().getStackTrace();

        if (framesToSkip >= 2) {
            framesToSkip += 4;
        }

        String className = stackTraceElements[framesToSkip].getClassName();

        try {
            return Thread.currentThread().getContextClassLoader().loadClass(className);
        } catch (ClassNotFoundException cnfex) {
            throw new UnsupportedOperationException(className + " not found.");
        }
    }

    /**
     * Returns <code>enum</code> class or <code>null</code> if class is not an enum.
     */
    public static Class findEnum(Class target) {
        if (target.isPrimitive()) {
            return null;
        }
        while (target != Object.class) {
            if (target.isEnum()) {
                return target;
            }

            target = target.getSuperclass();
        }

        return null;
    }

    // ---------------------------------------------------------------- enum

    /**
     * Returns the class of the immediate subclass of the given parent class for
     * the given object instance; or null if such immediate subclass cannot be
     * uniquely identified for the given object instance.
     */
    public static Class<?> childClassOf(Class<?> parentClass, Object instance) {

        if (instance == null || instance == Object.class) {
            return null;
        }

        if (parentClass != null) {
            if (parentClass.isInterface()) {
                return null;
            }
        }

        Class<?> childClass = instance.getClass();
        while (true) {
            Class<?> parent = childClass.getSuperclass();
            if (parent == parentClass) {
                return childClass;
            }
            if (parent == null) {
                return null;
            }
            childClass = parent;
        }
    }


    // ---------------------------------------------------------------- misc

    /**
     * Returns the jar file from which the given class is loaded; or null
     * if no such jar file can be located.
     */
    public static JarFile jarFileOf(Class<?> klass) throws UnsupportedEncodingException {
        URL url = klass.getResource(
                "/" + klass.getName().replace('.', '/') + ".class");

        if (url == null) {
            return null;
        }

        String s = url.getFile();
        int beginIndex = s.indexOf("file:") + "file:".length();
        int endIndex = s.indexOf(".jar!");
        if (endIndex == -1) {
            return null;
        }

        endIndex += ".jar".length();
        String f = s.substring(beginIndex, endIndex);
        // decode URL string - it may contain encoded chars (e.g. whitespaces) which are not supported for file-instances
        f = URLDecoder.decode(f, "UTF-8");
        File file = new File(f);

        try {
            return file.exists() ? new JarFile(file) : null;
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Resolves class file name from class name by replacing dot's with '/' separator
     * and adding class extension at the end. If array, component type is returned.
     */
    public static String convertClassNameToFileName(Class clazz) {
        if (clazz.isArray()) {
            clazz = clazz.getComponentType();
        }
        return convertClassNameToFileName(clazz.getName());
    }

    // ---------------------------------------------------------------- class names

    /**
     * Resolves class file name from class name by replacing dot's with '/' separator.
     */
    public static String convertClassNameToFileName(String className) {
        return className.replace('.', '/') + ".class";
    }

    /**
     * 得到类的短名
     */
    public static String getShortClassName(Class clazz) {
        return getShortClassName(clazz, 1);
    }

    /**
     * 处理class名字
     *
     * @param clazz     等待处理的class
     * @param shortUpTo 截断到第几位
     * @return 处理之后的数据
     */
    private static String getShortClassName(Class clazz, int shortUpTo) {
        return ""; // todo
    }

    /**
     * getClassNameByFile方法。
     * * @param filePath String类型参数
     *
     * @param childPackage boolean类型参数
     * @return static List<String>类型返回值
     */
    public static List<String> getClassNameByFile(String filePath, boolean childPackage) {
        List<String> myClassName = new ArrayList<>();
        List<File> files = new ArrayList<>();
//        List<File> files = FileUtil.listFile(filePath, childPackage);
        for (File file : files) {
            if (file.getName().endsWith(".class")) {
                String childFilePath = file.getPath();
                int index = filePath.replaceAll("\\\\", ".").length();
                childFilePath = childFilePath.replaceAll("\\\\", ".").substring(index + 1, childFilePath.length());
                myClassName.add(childFilePath);
            }
        }
        return myClassName;
    }

    /**
     * 将class导出到文件目录上
     */
//    public static void saveClassFile(Class<?> clazz) {
//        //生成class的字节数组，此处生成的class与proxy.newProxyInstance中生成的class除了代理类的名字不同，其它内容完全一致
//        byte[] classFile = ProxyGenerator.generateProxyClass(clazz.getSimpleName(), clazz.getInterfaces());
//        String path = clazz.getResource(".").getPath();
//        FileOutputStream fos = null;
//        try {
//            fos = new FileOutputStream(path + clazz.getSimpleName() + ".class");
//            fos.write(classFile);
//            fos.flush();
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            try {
//                fos.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }
//
//    public static void aa(Class clazz) {
//        byte[] a = ProxyGenerator.generateProxyClass("", new Class[]{clazz});
//        FileOutputStream os = null;
//        try {
//            os = new FileOutputStream("aa.class");
//            os.write(a);
//            os.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }


//    /**
//     * 获取指定目录下所有的类名
//     *
//     * @param classPath class文件路径
//     * @param jarPath jar文件路径
//     */
////    public static List<String> getClassName(String classPath, String jarPath) {
////        List<String> fileNames = new ArrayList<>();
////        List<File> jarList = FileHelper.listFileSuffix(new File(jarPath), "jar");
////        for(File file:jarList){
////            fileNames.addAll(getClassNameByJar(file.getAbsolutePath()));
////        }
////        //fileNames.addAll(getClassNameByFile(classPath,true));
////
////        return fileNames;
////    }

    /**
     * getClassNameByJar方法。
     * * @param jarPath String类型参数
     *
     * @return static List<String>类型返回值
     */
    public static List<String> getClassNameByJar(String jarPath) {
        List<String> myClassName = new ArrayList<>();
        try (JarFile jarFile = new JarFile(jarPath)) {
            Enumeration<JarEntry> entrys = jarFile.entries();
            while (entrys.hasMoreElements()) {
                JarEntry jarEntry = entrys.nextElement();
                String entryName = jarEntry.getName();
                if (entryName.endsWith(".class")) {
                    entryName = entryName.replace("/", ".");
                    myClassName.add(entryName);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return myClassName;
    }

    /**
     * getSuperClassChian方法。
     * * @param className String类型参数
     *
     * @return static String[]类型返回值
     */
    public static String[] getSuperClassChian(String className) {
        Class classz = loadClass(className);
        List<String> list = new ArrayList<>();
        Class superclass = classz.getSuperclass();
        String superName = superclass.getName();
        if (!"java.lang.Object".equals(superName)) {
            list.add(superName);
            list.addAll(Arrays.asList(getSuperClassChian(superName)));
        } else {
            list.add(superName);
        }
        return list.toArray(new String[list.size()]);
    }

//
//    public static List<String> getResourceNameByJar(String jarPath) {
//        List<String> resource = new ArrayList<>();
//        try (JarFile jarFile = new JarFile(jarPath)) {
//            Enumeration<JarEntry> entrys = jarFile.entries();
//            while (entrys.hasMoreElements()) {
//                JarEntry jarEntry = entrys.nextElement();
//                String entryName = jarEntry.getName();
//                if (!entryName.endsWith(".class") && !entryName.endsWith("/")) {
//                    resource.add(SysHepler.commandPath(jarPath) + "!" + entryName);
//                }
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return resource;
//    }
//
//
//    public static List<String> getResourceNameByJar(String jarPath, String suffix) {
//        List<String> resource = new ArrayList<>();
//        try (JarFile jarFile = new JarFile(jarPath)) {
//            Enumeration<JarEntry> entrys = jarFile.entries();
//            while (entrys.hasMoreElements()) {
//                JarEntry jarEntry = entrys.nextElement();
//                String entryName = jarEntry.getName();
//                if (entryName.endsWith(suffix)) {
//                    resource.add(SysHepler.commandPath(jarPath) + "!" + entryName);
//                }
//            }
//        } catch (IOException e) {
//            log.error(ExceptionUtil.stackTraceToString(e, "com.opslab.util"));
//        }
//        return resource;
//    }

    /**
     * 获取资源文件
     */
    public static File getFile(String resourceLocation) throws FileNotFoundException {
        Validator.notNull(resourceLocation, "Resource location must not be null");
        if (resourceLocation.startsWith("classpath:")) {
            String path = resourceLocation.substring("classpath:".length());
            String description = "class path resource [" + path + "]";
            ClassLoader cl = ClassLoaderUtil.getContextClassLoader();
            URL url = cl != null ? cl.getResource(path) : ClassLoader.getSystemResource(path);
            if (url == null) {
                throw new FileNotFoundException(description + " cannot be resolved to absolute file path because it does not exist");
            } else {
                return getFile(url, description);
            }
        } else {
            try {
                return getFile(new URL(resourceLocation));
            } catch (MalformedURLException var5) {
                return new File(resourceLocation);
            }
        }
    }

    /**
     * getFile方法。
     * * @param resourceUrl URL类型参数
     *
     * @return static File类型返回值
     */
    public static File getFile(URL resourceUrl) throws FileNotFoundException {
        return getFile(resourceUrl, "URL");
    }

    /**
     * getFile方法。
     * * @param resourceUrl URL类型参数
     *
     * @param description String类型参数
     * @return static File类型返回值
     */
    public static File getFile(URL resourceUrl, String description) throws FileNotFoundException {
        Validator.notNull(resourceUrl, "Resource URL must not be null");
        if (!"file".equals(resourceUrl.getProtocol())) {
            throw new FileNotFoundException(description + " cannot be resolved to absolute file path because it does not reside in the file system: " + resourceUrl);
        } else {
            try {
                return new File(toURI(resourceUrl).getSchemeSpecificPart());
            } catch (URISyntaxException var3) {
                return new File(resourceUrl.getFile());
            }
        }
    }

    /**
     * toURI方法。
     * * @param url URL类型参数
     *
     * @return static URI类型返回值
     */
    public static URI toURI(URL url) throws URISyntaxException {
        return toURI(url.toString());
    }

    /**
     * toURI方法。
     * * @param location String类型参数
     *
     * @return static URI类型返回值
     */
    public static URI toURI(String location) throws URISyntaxException {
        return new URI(location.replace(" ", "%20"));
    }
}