package com.zifang.util.core.lang.beans;

import junit.framework.TestCase;

import java.beans.PropertyEditorSupport;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * TestPropertyEditorSupport类。
 */
public class TestPropertyEditorSupport extends TestCase {

    /**
     * testPropertyEditorSupport方法。
     */
    public void testPropertyEditorSupport() {
        PropertyEditorSupport support = new UserPropertyEditorSupport();
        support.setAsText("User [age=18, name=yang]");
        System.out.println(support.getValue());
    }

    static class UserPropertyEditorSupport extends PropertyEditorSupport {
        private static final Pattern pattern = Pattern.compile("User \\[age=(\\d+), name=(\\S+)\\]");

        @Override
        /**
         * setAsText方法。
         *      * @param text String类型参数
         */
        public void setAsText(String text) throws IllegalArgumentException {
            if (text == null) {
                return;
            }
            Matcher matcher = pattern.matcher(text);
            if (matcher.matches()) {
                int age = Integer.parseInt(matcher.group(1));
                String name = matcher.group(2);

                User user = new User(age, name);
                setValue(user);
            }
        }
    }

    static class User {
        private int age;
        private String name;

        /**
         * User方法。
         */
        public User() {
            super();
        }

        /**
         * User方法。
         * * @param age int类型参数
         *
         * @param name String类型参数
         */
        public User(int age, String name) {
            this.age = age;
            this.name = name;
        }

        /**
         * getAge方法。
         *
         * @return int类型返回值
         */
        public int getAge() {
            return age;
        }

        /**
         * setAge方法。
         * * @param age int类型参数
         */
        public void setAge(int age) {
            this.age = age;
        }

        /**
         * getName方法。
         *
         * @return String类型返回值
         */
        public String getName() {
            return name;
        }

        /**
         * setName方法。
         * * @param name String类型参数
         */
        public void setName(String name) {
            this.name = name;
        }
    }
}