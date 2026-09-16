package com.zifang.util.core.concurrency.packages.atomic;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 在Java 1.5中就引入了原子变量，它提供对单个变量的原子操作。当你在操作一个普通变量时，你在Java实现的每个操作，在程序编译时会被转换成几个机器能读懂的指令。例如，当你分配一个值给变量，在Java你只使用了一个指令，但是当你编译这个程序时，这个指令就被转换成多个JVM 语言指令。这样子的话当你在操作多个线程且共享一个变量时，就会导致数据不一致的错误。
 * 为了避免这样的问题，Java引入了原子变量。当一个线程正在操作一个原子变量时，即使其他线程也想要操作这个变量，类的实现中含有一个检查那步骤操作是否完成的机制。 基本上，操作获取变量的值，改变本地变量值，然后尝试以新值代替旧值。如果旧值还是一样，那么就改变它。如果不一样，方法再次开始操作。这个操作称为 Compare and Set（校对注：简称CAS，比较并交换的意思）。
 * <p>
 * 原子变量不使用任何锁或者其他同步机制来保护它们的值的访问。他们的全部操作都是基于CAS操作。它保证几个线程可以同时操作一个原子对象也不会出现数据不一致的错误，并且它的性能比使用受同步机制保护的正常变量要好。
 * 在这个指南，你将学习怎样使用原子变量实现一个银行账户和2个不同的任务：一个存钱到账户和另一个从账户提取钱。在例子的实现中，你将使用 AtomicLong 类。
 */

/**
 * AtomicLongDemo类。
 */
public class AtomicLongDemo {
    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) {
        // 17. 创建一个 Account 对象，设置它的余额为 1000。
        Account account = new Account();
        account.setBalance(1000);
        // 18. 创建新的 Company 任务和一个线程运行它。
        Company company = new Company(account);
        Thread companyThread = new Thread(company);
        // 创建一个新的 Bank t任务和一个线程运行它。
        Bank bank = new Bank(account);
        Thread bankThread = new Thread(bank);
        // 19. 在操控台写上账号的初始余额。
        System.out.printf("Account : Initial Balance: %d\n", account.getBalance());
        // 20. 开始线程。
        companyThread.start();
        bankThread.start();
        // 21. 使用 join() 方法等待线程的完结并把账号最终余额写入操控台。
        try {
            companyThread.join();
            bankThread.join();
            System.out.printf("Account : Final Balance: %d\n", account.getBalance());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // 1. 创建一个类，名为 Account，来模拟银行账号。
    static class Account {
        // 2. 声明一个私有 AtomicLong 属性，名为 balance，用来储存账号的余额。
        private AtomicLong balance;

        // 3. 实现类的构造函数，初始化它的属性值。

        /**
         * Account方法。
         */
        public Account() {
            balance = new AtomicLong();
        }

        // 4. 实现一个方法，名为 getBalance()，用来返回余额属性值。

        /**
         * getBalance方法。
         *
         * @return long类型返回值
         */
        public long getBalance() {
            return balance.get();
        }

        // 5. 实现一个方法，名为 setBalance()，用来设置余额属性值。

        /**
         * setBalance方法。
         * * @param balance long类型参数
         */
        public void setBalance(long balance) {
            this.balance.set(balance);
        }

        // 6. 实现一个方法，名为 addAmount()，来增加余额属性值。

        /**
         * addAmount方法。
         * * @param amount long类型参数
         */
        public void addAmount(long amount) {
            this.balance.getAndAdd(amount);
        }

        // 7. 实现一个方法，名为 substractAmount() 来减少余额属性值。

        /**
         * subtractAmount方法。
         * * @param amount long类型参数
         */
        public void subtractAmount(long amount) {
            this.balance.getAndAdd(-amount);
        }
    }

    // 8. 创建一个类，名为 并一定实现 Runnable 接口。这个类会模拟公司付款。
    static class Company implements Runnable {
        // 9. 声明一个私有 Account 属性，名为 account。
        private Account account;

        // 10. 实现类的构造函数，初始化它的属性值。

        /**
         * Company方法。
         * * @param account Account类型参数
         */
        public Company(Account account) {
            this.account = account;
        }

        // 11. 实现任务的 run() 方法。 使用 account 的
        // addAmount()方法来让它的余额做10次的递增，递增额为1000。
        @Override
        /**
         * run方法。
         */
        public void run() {
            for (int i = 0; i < 10; i++) {
                account.addAmount(1000);
            }
        }
    }

    // 16. 创建例子的主类通过创建一个类，名为 Main 并添加 main()方法。

    // 12. 创建一个类，名为 Bank，并一定实现 Runnable 接口。这个类会模拟从一个账号提款。
    static class Bank implements Runnable {
        // 13. 声明一个私有 Account 属性，名为 account。
        private Account account;

        // 14. 实现类的构造函数，初始化它的属性值。

        /**
         * Bank方法。
         * * @param account Account类型参数
         */
        public Bank(Account account) {
            this.account = account;
        }

        // 15. 实现任务的 run() 方法。使用 account 的 subtractAmount()
        // 方法来让它的余额做10次的递减，递减额为1000。
        @Override
        /**
         * run方法。
         */
        public void run() {
            for (int i = 0; i < 10; i++) {
                account.subtractAmount(1000);
            }
        }
    }
}

/**
 * 它是怎么工作的…
 * 这个例子的关键是 Account 类。在这个类，我们声明了一个 AtomicLong 属性，名为 balance，用来储存账户余额，然后我们使用 AtomicLong 类提供的方法实现了操作余额的方法。为了实现 getBalance() 方法，返回余额的属性值，你要使用 AtomicLong 类的 get() 方法。为了实现 setBalance() 方法，设立余额值，你要使用 AtomicLong 类的 set() 方法。为了实现 addAmount()方法，为余额值加上收入，你要使用 AtomicLong 类的getAndAdd() 方法，用特定的参数值增加它并返回值。最后，为了实现 subtractAmount() 方法，减少余额值，你也要使用 getAndAdd() 方法。
 * 接着，你实现了2个不同的任务：
 * Company 类模拟了一个公司，增加余额值。这个类的每次任务会做10次的递增，递增值为1000。
 * Bank 类模拟了一个银行，银行作为账号的拥有者而收取费用。这个类的每次任务会做10次的递减，递减值为1000。
 * 在 Main 类，你创建了一个有1000余额的 Account 对象。然后，你运行一个银行任务和一个公司任务，所以最终的账号余额一定是等同于初始余额。
 */