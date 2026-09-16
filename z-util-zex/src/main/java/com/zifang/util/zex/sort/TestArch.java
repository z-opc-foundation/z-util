package com.zifang.util.zex.sort;

import com.zifang.util.core.sys.ArchUtils;

/**
 * 排序架构测试类。
 * <p>
 * 此类用于测试不同排序算法的架构和性能。
 * 包含对各种排序算法实现的支持。
 *
 * @author zifang
 * @version 1.0
 */
public class TestArch {

    //ArchUtils 系统的信息导出

    /**
     * archUtils方法。
     *
     * @return static void类型返回值
     */
    public static void archUtils() {
        System.out.println(ArchUtils.getProcessor().getArch());
        System.out.println(ArchUtils.getProcessor().isX86());
        System.out.println(ArchUtils.getProcessor().getType());
        System.out.println(ArchUtils.getProcessor().isPPC());
    }

    /**
     * 数组工具类
     * <p>
     * add(boolean[] array, boolean element) 将给定的数据添加到指定的数组中，返回一个新的数组
     * <p>
     * ArrayUtils.add(null, true)          = [true]
     * ArrayUtils.add([true], false)       = [true, false]
     * ArrayUtils.add([true, false], true) = [true, false, true]
     * add(boolean[] array, int index, boolean element) 将给定的数据添加到指定的数组下标中，返回一个新的数组。
     * <p>
     * ArrayUtils.add(null, 0, true)          = [true]
     * ArrayUtils.add([true], 0, false)       = [false, true]
     * ArrayUtils.add([false], 1, true)       = [false, true]
     * ArrayUtils.add([true, false], 1, true) = [true, true, false]
     * byte, int, char, double, float, int, long ,short, T[] 同理
     * <p>
     * addAll(boolean[] array1, boolean... array2) 将给定的多个数据添加到指定的数组中，返回一个新的数组
     * <p>
     * ArrayUtils.addAll(array1, null)   = cloned copy of array1
     * ArrayUtils.addAll(null, array2)   = cloned copy of array2
     * ArrayUtils.addAll([], [])         = []
     * byte, int, char, double, float, int, long ,short, T[] 同理
     * <p>
     * clone(boolean[] array) 复制数组并返回 结果数组为空将返回空
     * <p>
     * byte, int, char, double, float, int, long ,short, T[] 同理
     * <p>
     * contains(boolean[] array, boolean valueToFind) 检查该数据在该数组中是否存在，返回一个boolean值
     * <p>
     * byte, int, char, double, float, int, long ,short, Object 同理
     * <p>
     * getLength(Object array) 返回该数组长度
     * <p>
     * ArrayUtils.getLength(null)            = 0
     * ArrayUtils.getLength([])              = 0
     * ArrayUtils.getLength([null])          = 1
     * ArrayUtils.getLength([true, false])   = 2
     * ArrayUtils.getLength([1, 2, 3])       = 3
     * ArrayUtils.getLength(["a", "b", "c"]) = 3
     * hashCode(Object array) 返回该数组的哈希Code码
     * <p>
     * indexOf(boolean[] array, boolean valueToFind) 从数组的第一位开始查询该数组中是否有指定的数值，存在返回index的数值，否则返回-1
     * <p>
     * indexOf(boolean[] array, boolean valueToFind, int startIndex) 从数组的第startIndex位开始查询该数组中是否有指定的数值，存在返回index的数值，否则返回-1
     * <p>
     * byte, int, char, double, float, int, long ,short 同理
     * <p>
     * insert(int index, boolean[] array, boolean... values) 向指定的位置往该数组添加指定的元素，返回一个新的数组
     * <p>
     * ArrayUtils.insert(index, null, null)      = null
     * ArrayUtils.insert(index, array, null)     = cloned copy of 'array'
     * ArrayUtils.insert(index, null, values)    = null
     * byte, int, char, double, float, int, long ,short, T[] 同理
     * <p>
     * isEmpty(boolean[] array) 判断该数组是否为空，返回一个boolean值
     * <p>
     * byte, int, char, double, float, int, long ,short, Object 同理
     * <p>
     * isNotEmpty(boolean[] array) 判断该数组是否为空，而不是null
     * <p>
     * byte, int, char, double, float, int, long ,short, T[] 同理
     * <p>
     * isSameLength(boolean[] array1, boolean[] array2) 判断两个数组的长度是否一样，当数组为空视长度为0。返回一个boolean值
     * <p>
     * isSameType(Object array1, Object array2) 判断两个数组的类型是否一样，返回一个boolean值
     * <p>
     * isSorted(boolean[] array) 判断该数组是否按照自然排列顺序排序，返回一个boolean值
     * <p>
     * byte, int, char, double, float, int, long ,short, T[] 同理
     * <p>
     * isSorted(T[] array, Comparator<T> comparator) 判断该数组是否按照比较器排列顺序排序，返回一个boolean值
     * <p>
     * lastIndexOf(boolean[] array, boolean valueToFind) 从数组的最后一位开始往前查询该数组中是否有指定的数值，存在返回index的数值，否则返回-1
     * <p>
     * lastIndexOf(boolean[] array, boolean valueToFind, int startIndex) 从数组的最后startIndex位开始往前查询该数组中是否有指定的数值，存在返回index的数值，否则返回-1
     * <p>
     * byte, int, char, double, float, int, long ,short, Object 同理
     * <p>
     * nullToEmpty(boolean[] array) 将null转换为空的数组,如果数组不为null,返回原数组,如果数组为null,返回一个空的数组
     * <p>
     * byte, int, char, double, float, int, long ,short, Object, T 同理
     * <p>
     * remove(boolean[] array, int index) 删除该数组指定位置上的元素，返回一个新的数组，所有后续元素左移（下标减1）
     * <p>
     * ArrayUtils.remove([true], 0)              = []
     * ArrayUtils.remove([true, false], 0)       = [false]
     * ArrayUtils.remove([true, false], 1)       = [true]
     * ArrayUtils.remove([true, true, false], 1) = [true, false]
     * byte, int, char, double, float, int, long ,short, T[] 同理
     * <p>
     * removeAll(boolean[] array, int... indices) 删除该数组多个指定位置上的元素，返回一个新的数组，所有后续元素左移（下标减1）
     * <p>
     * ArrayUtils.removeAll([true, false, true], 0, 2) = [false]
     * ArrayUtils.removeAll([true, false, true], 1, 2) = [true]
     * byte, int, char, double, float, int, long ,short, T[] 同理
     * <p>
     * removeAllOccurences(boolean[] array, boolean element) 从该数组中删除指定的元素，返回一个新的数组
     * <p>
     * byte, int, char, double, float, int, long ,short, T[] 同理
     * <p>
     * removeElement(boolean[] array, boolean element) 从该数组中删除指定的元素，返回一个新的数组
     * <p>
     * byte, int, char, double, float, int, long ,short, T[] 同理
     * <p>
     * removeElements(boolean[] array, boolean... values) 从该数组中删除指定数量的元素，返回一个新的数组
     * <p>
     * ArrayUtils.removeElements(null, true, false)               = null
     * ArrayUtils.removeElements([], true, false)                 = []
     * ArrayUtils.removeElements([true], false, false)            = [true]
     * ArrayUtils.removeElements([true, false], true, true)       = [false]
     * ArrayUtils.removeElements([true, false, true], true)       = [false, true]
     * ArrayUtils.removeElements([true, false, true], true, true) = [false]
     * byte, int, char, double, float, int, long ,short, T[] 同理
     * <p>
     * reverse(boolean[] array) 数组反转
     * <p>
     * reverse(boolean[] array, int startIndexInclusive, int endIndexExclusive) 数组从指定位置区间进行反转
     * <p>
     * byte, int, char, double, float, int, long ,short, Object 同理
     * <p>
     * shuffle(boolean[] array) 把数组中的元素按随机顺序重新排列
     * <p>
     * byte, int, char, double, float, int, long ,short, Object 同理
     * <p>
     * subarray(boolean[] array, int startIndexInclusive, int endIndexExclusive) 截取数组，按指定位置区间截取并返回一个新的数组
     * <p>
     * byte, int, char, double, float, int, long ,short, T[] 同理
     * <p>
     * swap(boolean[] array, int offset1, int offset2) 指定该数组的两个位置的元素交换进行交换
     * <p>
     * ArrayUtils.swap([1, 2, 3], 0, 2) -> [3, 2, 1]
     * ArrayUtils.swap([1, 2, 3], 0, 0) -> [1, 2, 3]
     * ArrayUtils.swap([1, 2, 3], 1, 0) -> [2, 1, 3]
     * ArrayUtils.swap([1, 2, 3], 0, 5) -> [1, 2, 3]
     * ArrayUtils.swap([1, 2, 3], -1, 1) -> [2, 1, 3]
     * byte, int, char, double, float, int, long ,short, Object 同理
     * <p>
     * toArray(T... items) 创建数组
     * <p>
     * String[] array = ArrayUtils.toArray("1", "2");
     * String[] emptyArray = ArrayUtils.<String>toArray();
     * toMap(Object[] array) 将二维数组转换成Map并返会Map
     * <p>
     * Map colorMap = ArrayUtils.toMap(new String[][] {
     * {"RED", "#FF0000"},
     * {"GREEN", "#00FF00"},
     * {"BLUE", "#0000FF"}}
     * );
     * toObject(boolean[] array) 将基本类型数组转换成对象类型数组并返回
     * <p>
     * byte, int, char, double, float, int, long ,short 同理
     * <p>
     * toPrimitive(Boolean[] array) 将对象类型数组转换成基本类型数组并返回
     * <p>
     * byte, int, char, double, float, int, long ,short 同理
     * <p>
     * toString(Object array) 将数组转换为string字符串并返回
     * <p>
     * toStringArray(Object[] array) 将Object数组转换为String数组类型
     */
    public static void arrayUtils() {

    }

    /**
     *
     * 三、BooleanUtils
     * 布尔工具类
     * <p>
     * and(boolean... array) 逻辑与
     * <p>
     * BooleanUtils.and(true, true)         = true
     * BooleanUtils.and(false, false)       = false
     * BooleanUtils.and(true, false)        = false
     * BooleanUtils.and(true, true, false)  = false
     * BooleanUtils.and(true, true, true)   = true
     * compare(boolean x, boolean y) 比较两个布尔值并返回int类型 如果x == y返回0， !x && y 返回小于 0 ，x && !y 返回大于0
     * <p>
     * isFalse(Boolean bool) 是否是假并返回boolean
     * <p>
     * isTrue(Boolean bool) 是否是真并返回boolean
     * <p>
     * negate(Boolean bool) 逻辑非
     * <p>
     * BooleanUtils.negate(Boolean.TRUE)  = Boolean.FALSE;
     * BooleanUtils.negate(Boolean.FALSE) = Boolean.TRUE;
     * BooleanUtils.negate(null)          = null;
     * or(boolean... array) 逻辑或
     * <p>
     * BooleanUtils.or(true, true)          = true
     * BooleanUtils.or(false, false)        = false
     * BooleanUtils.or(true, false)         = true
     * BooleanUtils.or(true, true, false)   = true
     * BooleanUtils.or(true, true, true)    = true
     * BooleanUtils.or(false, false, false) = false
     * toBoolean(Boolean bool) 将对象类型转换为基本数据类型并返回
     * <p>
     * BooleanUtils.toBoolean(Boolean.TRUE)  = true
     * BooleanUtils.toBoolean(Boolean.FALSE) = false
     * BooleanUtils.toBoolean(null)          = false
     * toBoolean(int value) 将int类型转换为boolean类型并返回
     * <p>
     * BooleanUtils.toBoolean(0) = false
     * BooleanUtils.toBoolean(1) = true
     * BooleanUtils.toBoolean(2) = true
     * toBoolean(String str) 将string类型转换为boolean类型并返回
     * <p>
     * BooleanUtils.toBoolean(null)    = false
     * BooleanUtils.toBoolean("true")  = true
     * BooleanUtils.toBoolean("TRUE")  = true
     * BooleanUtils.toBoolean("tRUe")  = true
     * BooleanUtils.toBoolean("on")    = true
     * BooleanUtils.toBoolean("yes")   = true
     * BooleanUtils.toBoolean("false") = false
     * BooleanUtils.toBoolean("x gti") = false
     * BooleanUtils.toBooleanObject("y") = true
     * BooleanUtils.toBooleanObject("n") = false
     * BooleanUtils.toBooleanObject("t") = true
     * BooleanUtils.toBooleanObject("f") = false
     * toInteger(boolean bool) 将boolean类型数据转换为int类型并返回
     * <p>
     * BooleanUtils.toInteger(true)  = 1
     * BooleanUtils.toInteger(false) = 0
     * toStringOnOff(boolean bool) 将boolean类型数据转换为String类型'on' or 'off'并返回
     * <p>
     * BooleanUtils.toStringOnOff(true)   = "on"
     * BooleanUtils.toStringOnOff(false)  = "off"
     * toStringTrueFalse(Boolean bool) 将boolean类型数据转换为String类型''true' or 'false'并返回
     * <p>
     * BooleanUtils.toStringTrueFalse(true)   = "true"
     * BooleanUtils.toStringTrueFalse(false)  = "false"
     * toStringYesNo(boolean bool) 将boolean类型数据转换为String类型'yes' or 'no'并返回
     * <p>
     * BooleanUtils.toStringYesNo(true)   = "yes"
     * BooleanUtils.toStringYesNo(false)  = "no"
     * xor(boolean... array) 异或
     * <p>
     * BooleanUtils.xor(true, true)   = false
     * BooleanUtils.xor(false, false) = false
     * BooleanUtils.xor(true, false)  = true
     * 四、ClassPathUtils
     * class路径工具
     * <p>
     * toFullyQualifiedName(Class<?> context, String resourceName) 返回一个由class包名+resourceName拼接的字符串
     * <p>
     * ClassPathUtils.toFullyQualifiedName(StringUtils.class, "StringUtils.properties") = "org.apache.commons.lang3.StringUtils.properties"
     * toFullyQualifiedName(Package context, String resourceName) 返回一个由class包名+resourceName拼接的字符串
     * <p>
     * ClassPathUtils.toFullyQualifiedName(StringUtils.class.getPackage(), "StringUtils.properties") = "org.apache.commons.lang3.StringUtils.properties"
     * toFullyQualifiedPath(Class<?> context, String resourceName) 返回一个由class包名+resourceName拼接的字符串
     * <p>
     * ClassPathUtils.toFullyQualifiedPath(StringUtils.class, "StringUtils.properties") = "org/apache/commons/lang3/StringUtils.properties"
     * toFullyQualifiedPath(Package context, String resourceName) 返回一个由class包名+resourceName拼接的字符串
     * <p>
     * ClassPathUtils.toFullyQualifiedPath(StringUtils.class, "StringUtils.properties") = "org/apache/commons/lang3/StringUtils.properties"
     * 五、EnumUtils
     * 枚举工具类
     * <p>
     * getEnum(Class<E> enumClass, String enumName) 通过类返回一个枚举，可能返回空
     * <p>
     * getEnumList(Class<E> enumClass) 通过类返回一个枚举集合
     * <p>
     * getEnumMap(Class<E> enumClass) 通过类返回一个枚举map
     * <p>
     * isValidEnum(Class<E> enumClass, String enumName) 验证enumName是否在枚举中，返回true false
     * <p>
     * demo
     * <p>
     * 枚举类
     * public enum EnumDemo {
     * AA("1"), BB("2");
     * private String value;
     * <p>
     * EnumDemo(String value) {
     * this.value = value;
     * }
     * <p>
     * public String getValue() {
     * return value;
     * }
     * }
     * <p>
     * 测试
     * EnumDemo enumDemo = EnumUtils.getEnum(EnumDemo.class, "");
     * System.out.println(enumDemo);
     * System.out.println("-----");
     * <p>
     * List<EnumDemo> list = EnumUtils.getEnumList(EnumDemo.class);
     * for (EnumDemo a : list) {
     * System.out.println(a + ":" + a.getValue());
     * }
     * System.out.println("-----");
     * <p>
     * Map<String, EnumDemo> enumMap = EnumUtils.
     *
     *
     *
     */
    public static void main(String[] args) {
        archUtils();
    }
}
