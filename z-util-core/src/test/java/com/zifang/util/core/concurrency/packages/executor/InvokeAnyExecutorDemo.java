package com.zifang.util.core.concurrency.packages.executor;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;

/**
 * 4.5　运行多个任务并处理第一个结果
 * <p>
 * 并发编程比较常见的一个问题是，当采用多个并发任务来解决一个问题时，往往只关心这些任务中的第一个结果。比如，对一个数组进行排序有很多种算法，可以并发启动所有算法，但是对于一个给定的数组，第一个得到排序结果的算法就是最快的排序算法。
 * 在本节，我们将学习如何使用 ThreadPoolExecutor 类来实现这个场景。范例允许用户可以通过两种验证机制进行验证，但是，只要有一种机制验证成功，那么这个用户就被验证通过了。
 */

/**
 * InvokeAnyExecutorDemo类。
 */
public class InvokeAnyExecutorDemo {
    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) {
        /// 17．创建两个 String 对象，分别取名为 username 和 password，并初始化这两个属性值为test。
        String username = "test";
        String password = "test";
        // 18．创建两个 UserValidator 对象，分别取名为 ldapValidator 和 dbValidator。
        UserValidator ldapValidator = new UserValidator("LDAP");
        UserValidator dbValidator = new UserValidator("DataBase");
        // 19．创建两个TaskValidator对象，分别取名为ldapTask和dbTask，并分别用ldapValidator
        // 和dbValidator来初始化他们。
        TaskValidator ldapTask = new TaskValidator(ldapValidator, username, password);
        TaskValidator dbTask = new TaskValidator(dbValidator, username, password);
        // 20．创建一个名为 taksList 的 TaskValidator 类型列表，并将 ldapTask 和 dbTask 添加到列表中。
        List<TaskValidator> taskList = new ArrayList<>();
        taskList.add(ldapTask);
        taskList.add(dbTask);
        // 21．通过Executors工厂类的newCachedThreadPool()方法创建一个新的 ThreadPoolExecutor
        // 执行器对象，并创建一个名为 result 的 String 对象。
        ExecutorService executor = Executors.newCachedThreadPool();
        String result;
        // 22．调用执行器的 invokeAny() 方法。这个方法接收 taskList 作为参数，并返回String
        // 对象。然后，在控制台上输出这个方法返回的 String 对象。
        try {
            result = executor.invokeAny(taskList);
            System.out.printf("Main: Result: %s\n", result);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        // 23．通过shutdown()方法来终止执行器，并在控制台输出信息表示程序已经执行结束。
        executor.shutdown();
        System.out.printf("Main: End of the Execution\n");
    }

    /// 1．创建一个名为 UserValidator 的类，它将实现用户验证的过程。
    static class UserValidator {
        /// 2．声明一个名为 name 的私有 String 属性，用来存储用户验证系统的名称。
        private String name;

        // 3．实现类的构造器，用来初始化类的属性。

        /**
         * UserValidator方法。
         * * @param name String类型参数
         */
        public UserValidator(String name) {
            this.name = name;
        }

        // 4．实现 validate() 方法。它接收两个 String 参数，分别取名为用户名name 和密码
        // password，这两个参数也将被用来进行用户验证。

        /**
         * validate方法。
         * * @param name String类型参数
         *
         * @param password String类型参数
         * @return boolean类型返回值
         */
        public boolean validate(String name, String password) {
            /// 5．创建一个名为 random 的 Random 类型的随机对象。
            Random random = new Random();
            // 6．等待一段随机时间来模拟用户验证的过程。
            try {
                long duration = (long) (Math.random() * 10);
                System.out.printf("Validator %s: Validating a user during %d seconds\n", this.name, duration);
                TimeUnit.SECONDS.sleep(duration);
            } catch (InterruptedException e) {
                return false;
            }
            // 7．返回随机的 boolean 值。当用户通过验证时，这个方法返回 true 值，如果用户没有通过验证则返回 false 值。
            return random.nextBoolean();
        }

        // 8．实现 getName() 方法。这个方法返回 name 属性值。

        /**
         * getName方法。
         *
         * @return String类型返回值
         */
        public String getName() {
            return name;
        }
    }
    // 16．实现范例的主类，创建 Main 主类，并实现 main() 方法。

    // 9．创建一个名为 TaskValidator 的类，它将通过 UserValidation
    // 对象作为并发任务来执行用户验证的过程。这个类实现了带有 String 泛型参数的Callable 接口。
    static class TaskValidator implements Callable<String> {
        /// 10．声明一个名为 validator 的私有 UserValidator 属性。
        private UserValidator validator;
        // 11．声明两个私有的 String 属性，分别为用户名 user 和密码 password 。
        private String user;

        private String password;

        // 12．实现类的构造器，用来初始化类的属性。

        /**
         * TaskValidator方法。
         * * @param validator UserValidator类型参数
         *
         * @param user     String类型参数
         * @param password String类型参数
         */
        public TaskValidator(UserValidator validator, String user, String password) {
            this.validator = validator;
            this.user = user;
            this.password = password;
        }

        // 13．实现call()方法，并返回String对象。
        @Override
        /**
         * call方法。
         * @return String类型返回值
         */
        public String call() throws Exception {
            /// 14．如果用户没有通过 UserValidator 对象的验证，就在控制台输出没有找到这个用户，表明该用户未通过验证，并抛出
            /// Exception 类型的异常。
            if (!validator.validate(user, password)) {
                System.out.printf("%s: The user has not been found\n", validator.getName());
                throw new Exception("Error validating user");
            }
            // 15．否则，就在控制台输出用户已经找到，表明该用户已经通过验证，然后返回 UserValidator 对象的名称。
            System.out.printf("%s: The user has been found\n", validator.getName());

            return validator.getName();
        }
    }
}

/**
 * 工作原理
 * 这个范例的关键点在 Main 主类中。ThreadPoolExecutor 类的 invokeAny() 方法接收到一个任务列表，然后运行任务，并返回第一个完成任务并且没有抛出异常的任务的执行结果。这个方法返回的类型与任务里的 call() 方法返回的类型相同，在这个范例中，它将返回 String 类型值。
 * 下面的截图展示了当范例运行后，有一个任务成功地验证了用户后的运行结果。
 * Java Concurrency Cook Book 4.3
 * 范例中有两个UserValidator对象，它们返回随机的boolean值。每一个UserValidator对象被TaskValidator对象使用，TaskValidator对象实现了Callable接口。如果 UserValidator类的validate()方法返回false值，那么TaskValidator类将抛出Exception异常。否则，返回true值。
 * 因此，我们有两个任务可以返回true值或抛出Exception异常。从而，可以有如下4种可能性。
 * 1.如果两个任务都返回true值，那么invokeAny()方法的结果就是首先完成任务的名称。
 * 2.如果第一个任务返回true值，第二个任务抛出Exception异常，那么invokeAny() 方法的结果就是第一个任务的名称。
 * 3.如果第一个任务抛出Exception异常，第二个任务返回true值，那么invokeAny() 方法的结果就是第二个任务的名称。
 * 4.如果两个任务都抛出Exception异常，那么invokeAny()方法将抛出 ExecutionException异常。
 * 将这个范例多运行几次，那么将得到如上所述的四种可能的结果。以下截图则显示当两个任务同时抛出异常时，应用程序得到的结果。
 * Java Concurrency Cook Book 4.4
 * 更多信息
 * ThreadPoolExecutor 类还提供了 invokeAny() 方法的其他版本：
 * invokeAny(Collection<? extends Callable<T>> tasks, long timeout，TimeUnit unit)：这个方法执行所有的任务，如果在给定的超时期满之前某个任务已经成功完成（也就是未抛出异常），则返回其结果。
 * TimeUnit是一个枚举类，有如下的常量：DAYS、HOURS、MICROSECONDS、MILLISECONDS、MINUTES、NANOSECONDS和SECONDS。
 */
