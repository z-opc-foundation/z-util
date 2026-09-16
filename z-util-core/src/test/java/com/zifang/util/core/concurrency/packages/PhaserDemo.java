package com.zifang.util.core.concurrency.packages;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Phaser;
import java.util.concurrent.TimeUnit;

// 1. 创建一个类名为FileSearch并一定实现Runnable
// 接口。这个类实现的操作是在文件夹和其子文件夹中搜索确定的扩展名并在24小时内修改的文件。
class FileSearch implements Runnable {

    // 2. 声明一个私有 String 属性来储存搜索开始的时候的文件夹。
    private String initPath;

    // 3. 声明另一个私有 String 属性来储存我们要寻找的文件的扩展名。
    private String end;

    // 4. 声明一个私有 List 属性来储存我们找到的符合条件的文件的路径。
    private List<String> results;

    // 5. 最后，声明一个私有 Phaser 属性来控制任务的不同phaser的同步。
    private Phaser phaser;

    // 6. 实现类的构造函数，初始化类的属性们。它接收初始文件夹的路径，文件的扩展名，和phaser 作为参数。

    /**
     * FileSearch方法。
     * * @param initPath String类型参数
     *
     * @param end    String类型参数
     * @param phaser Phaser类型参数
     */
    public FileSearch(String initPath, String end, Phaser phaser) {
        this.initPath = initPath;
        this.end = end;
        this.phaser = phaser;
        results = new ArrayList<String>();
    }

    // 7. 现在，你必须实现一些要给run() 方法用的辅助方法。第一个是 directoryProcess()
    // 方法。它接收File对象作为参数并处理全部的文件和子文件夹。对于每个文件夹，此方法会递归调用并传递文件夹作为参数。对于每个文件，此方法会调用fileProcess()
    // 方法。
    private void directoryProcess(File file) {

        File[] list = file.listFiles();
        if (list != null) {
            for (int i = 0; i < list.length; i++) {

                if (list[i].isDirectory()) {
                    directoryProcess(list[i]);
                } else {
                    fileProcess(list[i]);
                }
            }
        }
    }

    // 8. 现在，实现 fileProcess() 方法。它接收 File
    // 对象作为参数并检查它的扩展名是否是我们正在查找的。如果是，此方法会把文件的绝对路径写入结果列表内。
    private void fileProcess(File file) {
        if (file.getName().endsWith(end)) {
            results.add(file.getAbsolutePath());
        }
    }

    // 9. 现在，实现 filterResults()
    // 方法。不接收任何参数。它过滤在第一阶段获得的文件列表，并删除修改超过24小时的文件。首先，创建一个新的空list并获得当前时间。
    private void filterResults() {
        List<String> newResults = new ArrayList<String>();
        long actualDate = new Date().getTime();

        // 10. 然后，浏览结果list里的所有元素。对于每个路径，为文件创建File对象 go through all the elements
        // of the results list. For each path in the list of results, create a
        // File object for that file and get the last modified date for it.
        for (int i = 0; i < results.size(); i++) {
            File file = new File(results.get(i));
            long fileDate = file.lastModified();

            // 11. 然后， 对比与真实日期对比，如果相差小于一天，把文件的路径加入到新的结果列表。
            if (actualDate - fileDate < TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS)) {
                newResults.add(results.get(i));
            }
        }

        // 12. 最后，把旧的结果改为新的。
        results = newResults;
    }

    // 13. 现在，实现 checkResults() 方法。此方法在第一个和第二个phase的结尾被调用，并检查结果是否为空。此方法不接收任何参数。
    private boolean checkResults() {

        // 14. 首先，检查结果List的大小。如果为0，对象写信息到操控台表明情况，然后调用Phaser对象的
        // arriveAndDeregister() 方法通知此线程已经结束actual phase，并离开phased操作。
        if (results.isEmpty()) {
            System.out.printf("%s: Phase %d: 0 results.\n", Thread.currentThread().getName(), phaser.getPhase());
            System.out.printf("%s: Phase %d: End.\n", Thread.currentThread().getName(), phaser.getPhase());
            phaser.arriveAndDeregister();
            return false;

            // 15. 另一种情况，如果结果list有元素，那么对象写信息到操控台表明情况，调用 Phaser对象懂得
            // arriveAndAwaitAdvance() 方法并通知 actual phase，它会被阻塞直到phased
            // 操作的全部参与线程结束actual phase。

        } else {
            System.out.printf("%s: Phase %d: %d results.\n", Thread.currentThread().getName(), phaser.getPhase(),
                    results.size());
            phaser.arriveAndAwaitAdvance();
            return true;
        }
    }

    // 16. 最好一个辅助方法是 showInfo() 方法，打印results list 的元素到操控台。
    private void showInfo() {
        for (int i = 0; i < results.size(); i++) {
            File file = new File(results.get(i));
            System.out.printf("%s: %s\n", Thread.currentThread().getName(), file.getAbsolutePath());
        }
        phaser.arriveAndAwaitAdvance();
    }

    // 17. 现在，来实现 run() 方法，使用之前描述的辅助方法来执行，并使用Phaser对象控制phases间的改变。首先，调用phaser对象的
    // arriveAndAwaitAdvance() 方法。直到使用线程被创建完成，搜索行为才会开始。
    @Override
    /**
     * run方法。
     */
    public void run() {

        phaser.arriveAndAwaitAdvance();

        // 18. 然后，写信息到操控台表明搜索任务开始。

        System.out.printf("%s: Starting.\n", Thread.currentThread().getName());

        // 19. 查看 initPath 属性储存的文件夹名字并使用 directoryProcess()
        // 方法在文件夹和其子文件夹内查找带特殊扩展名的文件。
        File file = new File(initPath);
        if (file.isDirectory()) {
            directoryProcess(file);
        }

        // 20. 使用 checkResults() 方法检查是否有结果。如果没有任何结果，结束线程的执行并返回keyword。
        if (!checkResults()) {
            return;
        }

        // 21. 使用filterResults() 方法过滤结果list。
        filterResults();

        // 22. 再次使用checkResults() 方法检查是否有结果。如果没有，结束线程的执行并返回keyword。
        if (!checkResults()) {
            return;
        }

        // 23. 使用 showInfo() 方法打印最终的结果list到操控台，撤销线程的登记，并打印信息表明线程的终结。
        showInfo();
        phaser.arriveAndDeregister();
        System.out.printf("%s: Work completed.\n", Thread.currentThread().getName());

    }
}

/**
 * 它是怎么工作的…
 * 这程序开始创建的 Phaser对象是用来在每个phase的末端控制线程的同步。Phaser的构造函数接收参与者的数量作为参数。在这里，Phaser有3个参与者。
 * 这个数向Phaser表示Phaser改变phase之前执行 arriveAndAwaitAdvance() 方法的线程数，并叫醒正在休眠的线程。
 * 一旦Phaser被创建，我们运行3个线程分别执行3个不同的FileSearch对象。 在例子里，我们使用 Windows operating system
 * 的路径。如果你使用的是其他操作系统，那么修改成适应你的环境的路径。 FileSearch对象的 run() 方法中的第一个指令是调用Phaser对象的
 * arriveAndAwaitAdvance()方法。像之前提到的，Phaser知道我们要同步的线程的数量。当某个线程调用此方法，Phaser减少终结actual
 * phase的线程数，并让这个线程进入休眠 直到全部其余线程结束phase。在run() 方法前面调用此方法，没有任何 FileSearch
 * 线程可以开始他们的工作，直到全部线程被创建。
 * 在phase 1 和 phase 2 的末端，我们检查phase是否生成有元素的结果list,或者它没有生成结果且list为空。在第一个情况，checkResults() 方法之前提的调用
 * arriveAndAwaitAdvance()。在第二个情况，如果list为空，那就没有必要让线程继续了，就直接返回吧。但是你必须通知phaser，将会少一个参与者
 * 。为了这个，我们使用arriveAndDeregister()。它通知phaser线程结束了actual phase，
 * 但是它将不会继续参见后面的phases,所以请phaser不要再等待它了。
 * 在phase3的结尾实现了 showInfo() 方法, 调用了 phaser的 arriveAndAwaitAdvance()方法。这个调用，保证了全部线程在同一时间结束。当此方法结束执行，有一个调用phaser的arriveAndDeregister()
 * 方法。这个调用，我们撤销了对phaser线程的注册，所以当全部线程结束时，phaser 有0个参与者。 最后，main()方法等待3个线程的完成并调用phaser的 isTerminated() 方法。
 * 当phaser有0个参与者时，它进入termination状态，此状态与此调用将会打印true到操控台。
 * Phaser 对象可能是在这2中状态：
 * 1.Active: 当Phaser 接受新的参与者注册，它进入这个状态，并且在每个phase的末端同步。 在此状态，Phaser像在这个指南里解释的那样工作。此状态不在Java
 * 并发 API中。
 * 2.Termination: 默认状态，当Phaser里全部的参与者都取消注册，它进入这个状态，所以这时 Phaser有0个参与者。更具体的说，当onAdvance() 方法返回真值时，Phaser 是在这个状态里。如果你覆盖那个方法,你可以改变它的默认行为。当
 * Phaser 在这个状态，同步方法 arriveAndAwaitAdvance()会 立刻返回，不会做任何同步。
 * Phaser类的一个显著特点是你不需要控制任何与phaser相关的方法的异常。不像其他同步应用，线程们在phaser休眠不会响应任何中断也不会抛出
 * InterruptedException 异常。只有一个异常会在下面的‘更多’里解释。 下面的裁图是例子的运行结果：
 * <p>
 * 它展示了前2个phases的执行。你可以发现Apps线程在phase 2 结束它的运行由于list
 * 为空。当你执行例子，你会发现一些线程比其他的线程更快结束phase，但是他们必须等待其他全部结束然后才能继续。 更多… The
 * Phaser类还提供了其他相关方法来改变phase。他们是：
 * arrive(): 此方法示意phaser某个参与者已经结束actual phase了，但是他应该等待其他的参与者才能继续执行。小心使用此法，因为它并不能与其他线程同步。
 * awaitAdvance(int phase):如果我们传递的参数值等于phaser的actual phase，此方法让当前线程进入睡眠直到phaser的全部参与者结束当前的phase。如果参数值与phaser 的 actual
 * phase不等，那么立刻返回。
 * awaitAdvanceInterruptibly(int phaser):此方法等同与之前的方法，只是在线程正在此方法中休眠而被中断时候，它会抛出InterruptedException 异常。
 * Phaser的参与者的注册
 * 当你创建一个 Phaser 对象,你表明了参与者的数量。但是Phaser类还有2种方法来增加参与者的数量。他们是：
 * register():此方法为Phaser添加一个新的参与者。这个新加入者会被认为是还未到达 actual phase.
 * bulkRegister(int Parties):此方法为Phaser添加一个特定数量的参与者。
 * 这些新加入的参与都会被认为是还未到达 actual phase.
 * Phaser类提供的唯一一个减少参与者数量的方法是arriveAndDeregister() 方法，它通知phaser线程已经结束了actual
 * phase,而且他不想继续phased的操作了。 强制终止 Phaser
 * 当phaser有0个参与者，它进入一个称为Termination的状态。Phaser 类提供 forceTermination()
 * 来改变phaser的状态，让它直接进入Termination
 * 状态，不在乎已经在phaser中注册的参与者的数量。此机制可能会很有用在一个参与者出现异常的情况下来强制结束phaser. 当phaser在
 * Termination 状态， awaitAdvance() 和 arriveAndAwaitAdvance()
 * 方法立刻返回一个负值，而不是一般情况下的正值如果你知道你的phaser可能终止了，那么你可以用这些方法来确认他是否真的终止了。
 */

/**
 * PhaserDemo类。
 */
public class PhaserDemo {

    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) {
        // 25. 创建 含3个参与者的 Phaser 对象。
        Phaser phaser = new Phaser(3);

        // 26. 创建3个 FileSearch 对象，每个在不同的初始文件夹里搜索.log扩展名的文件。
        FileSearch system = new FileSearch("C:\\Windows", "log", phaser);
        FileSearch apps = new FileSearch("C:\\Program Files", "log", phaser);
        FileSearch documents = new FileSearch("C:\\Documents And Settings", "log", phaser);

        // 27. 创建并开始一个线程来执行第一个 FileSearch 对象。
        Thread systemThread = new Thread(system, "System");
        systemThread.start();

        // 28. 创建并开始一个线程来执行第二个 FileSearch 对象。
        Thread appsThread = new Thread(apps, "Apps");
        appsThread.start();

        // 29. 创建并开始一个线程来执行第三个 FileSearch 对象。
        Thread documentsThread = new Thread(documents, "Documents");
        documentsThread.start();

        // 30. 等待3个线程们的终结。

        try {
            systemThread.join();
            appsThread.join();
            documentsThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // 31. 使用isFinalized()方法把Phaser对象的结束标志值写入操控台。
        System.out.println("Terminated: " + phaser.isTerminated());

    }
}