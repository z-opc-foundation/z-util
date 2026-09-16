package com.zifang.util.core.util;

import java.lang.reflect.Field;

public class UnsafeUtil {

    private static final Field STRING_VALUE_FIELD;
    private static final Field STRING_OFFSET_FIELD;
    private static final Field STRING_COUNT_FIELD;

    static {
        Field valueField = null;
        Field offsetField = null;
        Field countField = null;

        if (System.getProperty("java.version").startsWith("1.8")) {
            try {
                valueField = String.class.getDeclaredField("value");
                valueField.setAccessible(true);
            } catch (Throwable ignore) {
            }

            try {
                offsetField = String.class.getDeclaredField("offset");
                offsetField.setAccessible(true);
            } catch (Throwable ignore) {
            }

            try {
                countField = String.class.getDeclaredField("count");
                countField.setAccessible(true);
            } catch (Throwable ignore) {
            }
        }

        STRING_VALUE_FIELD = valueField;
        STRING_OFFSET_FIELD = offsetField;
        STRING_COUNT_FIELD = countField;
    }


    public static Object getUnsafeInstance() {
        return null;
    }

    public static Object getUnsageInstance() {
        return getUnsafeInstance();
    }

    static char[] unsafeGetChars(final String string) {
        if (STRING_VALUE_FIELD == null) {
            return string.toCharArray();
        }

        try {
            final char[] value = (char[]) STRING_VALUE_FIELD.get(string);

            if (STRING_OFFSET_FIELD != null && STRING_COUNT_FIELD != null) {
                final int offset = STRING_OFFSET_FIELD.getInt(string);
                final int count = STRING_COUNT_FIELD.getInt(string);

                if (offset == 0 && count == value.length) {
                    return value;
                } else {
                    final char[] result = new char[count];
                    System.arraycopy(value, offset, result, 0, count);
                    return result;
                }
            } else {
                return value;
            }
        } catch (Exception e) {
            return string.toCharArray();
        }
    }
}