package com.zifang.util.core.concurrency.packages;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * CollectionModifyExceptionTest类。
 */
public class CollectionModifyExceptionTest {

    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) {
        Collection<User> users = new ArrayList<User>();

        users.add(new User("张三", 28));
        users.add(new User("李四", 25));
        users.add(new User("王五", 31));
        Iterator<User> itUsers = users.iterator();
        while (itUsers.hasNext()) {
            User user = itUsers.next();
            if ("张三".equals(user.getName())) {
                users.remove(user);
//				itUsers.remove();
            } else {
                System.out.println(user);
            }
        }
    }
}
