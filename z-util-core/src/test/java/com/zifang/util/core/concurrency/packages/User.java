package com.zifang.util.core.concurrency.packages;

/**
 * User类。
 */
public class User implements Cloneable {

    private String name;
    private int age;

    /**
     * User方法。
     * * @param name String类型参数
     *
     * @param age int类型参数
     */
    public User(String name, int age) {
        super();
        this.name = name;
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

    @Override
    /**
     * toString方法。
     * @return String类型返回值
     */
    public String toString() {
        return "User [name=" + name + ", age=" + age + "]";
    }

    @Override
    /**
     * hashCode方法。
     * @return int类型返回值
     */
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + age;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    /**
     * equals方法。
     *      * @param obj Object类型参数
     * @return boolean类型返回值
     */
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        User other = (User) obj;
        if (age != other.age)
            return false;
        if (name == null) {
            return other.name == null;
        } else return name.equals(other.name);
    }

}
