package com.zifang.util.core.lang.primitive;

/**
 * @author: zifang
 * @time: 2021-11-23 16:24:00
 * @description: byte util
 * @version: JDK 1.8
 */
public class ByteUtil {

    /**
     * 短整型转字节数组
     *
     * @param number 短整型数值
     * @return 2字节数组
     */
    public static byte[] shortToByte(short number) {
        int temp = number;
        byte[] b = new byte[2];
        for (int i = 0; i < b.length; i++) {
            b[i] = (byte) (temp & 0xff);
            temp >>= 8;
        }
        return b;
    }

    /**
     * 字节数组转短整型
     *
     * @param b 2字节数组
     * @return 短整型数值
     */
    public static short byteToShort(byte[] b) {
        short s0 = (short) (b[0] & 0xff);
        short s1 = (short) (b[1] & 0xff);
        s1 <<= 8;
        return (short) (s0 | s1);
    }

    /**
     * 整型转字节数组
     *
     * @param i 整型数值
     * @return 4字节数组
     */
    public static byte[] intToByte(int i) {
        byte[] bt = new byte[4];
        bt[0] = (byte) (0xff & i);
        bt[1] = (byte) ((0xff00 & i) >> 8);
        bt[2] = (byte) ((0xff0000 & i) >> 16);
        bt[3] = (byte) ((0xff000000 & i) >> 24);
        return bt;
    }

    /**
     * 字节数组转整型
     *
     * @param bytes 4字节数组
     * @return 整型数值
     */
    public static int bytesToInt(byte[] bytes) {
        int num = bytes[0] & 0xFF;
        num |= ((bytes[1] << 8) & 0xFF00);
        num |= ((bytes[2] << 16) & 0xFF0000);
        num |= ((bytes[3] << 24) & 0xFF000000);
        return num;
    }

    /**
     * 整型数组转字节数组
     *
     * @param arr 整型数组
     * @return 字节数组
     */
    public static byte[] intToByte(int[] arr) {
        byte[] bt = new byte[arr.length * 4];
        for (int i = 0; i < arr.length; i++) {
            byte[] t = intToByte(arr[i]);
            System.arraycopy(t, 0, bt, i * 4, 4);
        }
        return bt;
    }

    /**
     * 长整型转字节数组
     *
     * @param number 长整型数值
     * @return 8字节数组
     */
    public static byte[] longToByte(long number) {
        long temp = number;
        byte[] b = new byte[8];
        for (int i = 0; i < b.length; i++) {
            b[i] = (byte) (temp & 0xff);
            temp >>= 8;
        }
        return b;
    }

    /**
     * 字节数组转长整型
     *
     * @param b 8字节数组
     * @return 长整型数值
     */
    public static long byteToLong(byte[] b) {
        long s0 = b[0] & 0xff;
        long s1 = b[1] & 0xff;
        long s2 = b[2] & 0xff;
        long s3 = b[3] & 0xff;
        long s4 = b[4] & 0xff;
        long s5 = b[5] & 0xff;
        long s6 = b[6] & 0xff;
        long s7 = b[7] & 0xff;
        s1 <<= 8;
        s2 <<= 16;
        s3 <<= 24;
        s4 <<= 32;
        s5 <<= 40;
        s6 <<= 48;
        s7 <<= 56;
        return s0 | s1 | s2 | s3 | s4 | s5 | s6 | s7;
    }

    /**
     * 将字节数组按指定字符进行百分号编码
     * <p>如 encodeBytes(bytes, '%') 将每字节转为 %XX 形式
     *
     * @param source 源字节数组
     * @param split  分隔字符（通常为 '%'）
     * @return 编码后的字节数组
     */
    public static byte[] encodeBytes(byte[] source, char split) {
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream(source.length);
        for (byte b : source) {
            if (b < 0) {
                b += 256;
            }
            bos.write(split);
            char hex1 = Character.toUpperCase(Character.forDigit((b >> 4) & 0xF, 16));
            char hex2 = Character.toUpperCase(Character.forDigit(b & 0xF, 16));
            bos.write(hex1);
            bos.write(hex2);
        }
        return bos.toByteArray();
    }

    /**
     * @author: zifang
     * @description: merge multiple byte array
     * @time: 2021-11-22 13:21
     * @params: [a, b] request
     * @return: byte[] response
     */
    public static byte[] merge(byte[] origin, byte[]... extra) {
        if (null == origin) {
            throw new RuntimeException("Origin data is null");
        }
        int newLength = origin.length;
        if (extra.length > 0) {
            for (byte[] bytes : extra) {
                if (null != bytes) {
                    newLength += bytes.length;
                }
            }
        }
        byte[] result = new byte[newLength];
        int position = origin.length;
        System.arraycopy(origin, 0, result, 0, position);
        if (extra.length > 0) {
            for (byte[] bytes : extra) {
                if (null != bytes) {
                    int currentLength = bytes.length;
                    System.arraycopy(bytes, 0, result, position, currentLength);
                    position += currentLength;
                }
            }
        }

        return result;
    }

    /**
     * rightPaddingZero方法。
     * * @param data byte[]类型参数
     *
     * @param length int类型参数
     * @return static byte[]类型返回值
     */
    public static byte[] rightPaddingZero(byte[] data, int length) {
        if (length == 0) {
            throw new RuntimeException("Length don't allow is zero");
        }
        byte[] dataByte = data;

        if (data.length % length != 0) {
            byte[] blankBytes = new byte[length - data.length % length];
            dataByte = merge(data, blankBytes);
        }
        return dataByte;
    }


    /**
     * bytesToHexString方法。
     * * @param src byte[]类型参数
     *
     * @return static String类型返回值
     */
    public static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder();
        if (src == null || src.length <= 0) {
            return null;
        }
        for (byte b : src) {
            int value = b & 0xFF;
            String hexValue = Integer.toHexString(value);
            if (hexValue.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hexValue);
        }
        return stringBuilder.toString();
    }


    /**
     * hexStringToBytes方法。
     * * @param hexString String类型参数
     *
     * @return static byte[]类型返回值
     */
    public static byte[] hexStringToBytes(String hexString) {
        if (null == hexString || hexString.isEmpty()) {
            return null;
        }
        hexString = hexString.toLowerCase();
        final byte[] byteArray = new byte[hexString.length() >> 1];
        int index = 0;
        for (int i = 0; i < hexString.length(); i++) {
            if (index > hexString.length() - 1) {
                return byteArray;
            }
            byte highDit = (byte) (Character.digit(hexString.charAt(index), 16) & 0xFF);
            byte lowDit = (byte) (Character.digit(hexString.charAt(index + 1), 16) & 0xFF);
            byteArray[i] = (byte) (highDit << 4 | lowDit);
            index += 2;
        }
        return byteArray;
    }

    /**
     * parseByte方法。
     * * @param object Object类型参数
     *
     * @return static Byte类型返回值
     */
    public static Byte parseByte(Object object) {
        if (null == object) {
            return null;
        }
        return Byte.parseByte(object.toString());
    }

    /**
     * parseByteOrDefault方法。
     * * @param object Object类型参数
     *
     * @param defaultValue byte类型参数
     * @return static Byte类型返回值
     */
    public static Byte parseByteOrDefault(Object object, Byte defaultValue) {
        if (null == object) {
            return defaultValue;
        }
        return Byte.parseByte(object.toString());
    }

}
