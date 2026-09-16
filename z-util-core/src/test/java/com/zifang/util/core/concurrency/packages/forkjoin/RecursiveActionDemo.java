package com.zifang.util.core.concurrency.packages.forkjoin;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.TimeUnit;

/**
 * RecursiveActionDemo类。
 */
public class RecursiveActionDemo {

    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) {
        // 19．使用ProductListGenerator类创建一个有10,000个产品的列表
        ProductListGenerator generator = new ProductListGenerator();
        List<Product> products = generator.generate(10000);
        // 20．创建一个新的Task对象用来更新列表中的所有产品。参数first为0，参数last为产品列表的大小，即10,000。
        Task task = new Task(products, 0, products.size(), 0.20);
        // 21．通过无参的类构造器创建一个ForkJoinPool对象。
        ForkJoinPool pool = new ForkJoinPool();
        // 22．调用execute()方法执行任务。
        pool.execute(task);
        // 23．实现代码块，显示关于线程池演变的信息，每5毫秒在控制台上输出线程池的一些参数值，直到任务执行结束。
        do {
            System.out.printf("Main: Thread Count: %d\n", pool.getActiveThreadCount());
            System.out.printf("Main: Thread Steal: %d\n", pool.getStealCount());
            System.out.printf("Main: Parallelism: %d\n", pool.getParallelism());
            try {
                TimeUnit.MILLISECONDS.sleep(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } while (!task.isDone());
        // 24．调用shutdown()方法关闭线程池。
        pool.shutdown();
        // 25．调用isCompletedNormally()方法，检查任务是否已经完成并且没有错误，在这个示例中，在控制台输出信息表示任务已经处理结束。
        if (task.isCompletedNormally()) {
            System.out.printf("Main: The process has completed normally.\n");
        }
        // 26．在增加之后，所有产品的期望价格是12元。在控制台输出所有产品的名称和价格，如果产品的价格不是12元，就将产品信息打印出来，以便确认所有的产品价格都正确地增加了。
        for (int i = 0; i < products.size(); i++) {
            Product product = products.get(i);
            if (product.getPrice() != 12) {
                System.out.printf("Product %s: %f\n", product.getName(), product.getPrice());
            }
        }
    }

    // 1.创建一个名为Product的类，用来存储产品的名称和价格。
    static class Product {
        // 2.声明一个名为name的私有String属性，一个名为price的私有double属性。
        private String name;
        private double price;

        // 3.实现两个属性各自的设值与取值方法。

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
         * getPrice方法。
         *
         * @return double类型返回值
         */
        public double getPrice() {
            return price;
        }

        /**
         * setPrice方法。
         * * @param price double类型参数
         */
        public void setPrice(double price) {
            this.price = price;
        }
    }

    // 4．创建一个名为ProductListGenerator的类，用来生成一个随机产品列表。
    static class ProductListGenerator {
        // 5．实现generate()方法。接收一个表示列表大小的int参数，并返回一个生成产品的List<Product>列表。

        /**
         * generate方法。
         * * @param size int类型参数
         *
         * @return List<Product>类型返回值
         */
        public List<Product> generate(int size) {
            // 6．创建返回产品列表的对象ret。
            List<Product> ret = new ArrayList<Product>();
            // 7．生成产品列表，给所有的产品分配相同的价格，比如可以检查程序是否运行良好的数字10。
            for (int i = 0; i < size; i++) {
                Product product = new Product();
                product.setName("Product " + i);
                product.setPrice(10);
                ret.add(product);
            }
            return ret;
        }
    }

    // 18．实现范例的主类，创建Main主类，并实现main()方法。

    // 8．创建一个名为Task的类，并继承RecursiveAction类。
    static class Task extends RecursiveAction {
        // 9．声明这个类的serialVersionUID属性。这个元素是必需的，因为RecursiveAction的父类ForkJoinTask实现了Serializable接口。
        private static final long serialVersionUID = 1L;
        // 10．声明一个名为products私有的List<Product>属性。
        private List<Product> products;
        // 11．声明两个私有的int属性，分别命名为first和last。这两个属性将决定任务执行时对产品的分块。
        private int first;

        private int last;
        // 12．声明一个名为increment的私有double属性，用来存储产品价格的增加额。
        private double increment;

        // 13．实现类的构造器，用来初始化类的这些属性。

        /**
         * Task方法。
         * * @param products ListProduct类型参数
         *
         * @param first     int类型参数
         * @param last      int类型参数
         * @param increment double类型参数
         */
        public Task(List<Product> products, int first, int last, double increment) {
            this.products = products;
            this.first = first;
            this.last = last;
            this.increment = increment;
        }

        // 14．实现compute()方法，实现任务的执行逻辑。
        @Override
        /**
         * compute方法。
         */
        protected void compute() {
            // 15．如果last和first属性值的差异小于10（一个任务只能更新少于10件产品的价格），则调用updatePrices()方法增加这些产品的价格。
            if (last - first < 10) {
                updatePrices();
                // 16．如果last和first属性值的差异大于或等于10，就创建两个新的Task对象，一个处理前一半的产品，另一个处理后一半的产品，然后调用ForkJoinPool的invokeAll()方法来执行这两个新的任务。
            } else {
                int middle = (last + first) / 2;
                System.out.printf("Task: Pending tasks:%s\n", getQueuedTaskCount());
                Task t1 = new Task(products, first, middle + 1, increment);
                Task t2 = new Task(products, middle + 1, last, increment);
                invokeAll(t1, t2);
            }
        }

        // 17．实现updatePrices()方法。这个方法用来更新在产品列表中处于first和last属性之间的产品。
        private void updatePrices() {
            for (int i = first; i < last; i++) {
                Product product = products.get(i);
                product.setPrice(product.getPrice() * (1 + increment));
            }
        }
    }
}
/**
 * 工作原理 在这个范例中，我们创建了ForkJoinPool对象，和一个将在线程池中执行的ForkJoinTask的子类。
 * 使用了无参的类构造器创建了ForkJoinPool对象，因此它将执行默认的配置。创建一个线程数等于计算机CPU数目的线程池，
 * 创建好ForkJoinPool对象之后，那些线程也创建就绪了，在线程池中等待任务的到达，然后开始执行。
 * 由于Task类继承了RecursiveAction类，因此不返回结果。在本节，我们使用了推荐的结构来实现任务。如果任务需要更新大于10个产品，
 * 它将拆分这些元素为两部分，创建两个任务，并将拆分的部分相应地分配给新创建的任务。通过使用Task类的first和last属性，
 * 来获知任务将要更新的产品列表所在的位置范围。我们已经使用first和last属性，来操作产品列表中仅有的一份副本，而没有为每一个任务去创建不同的产品列表。
 * 调用invokeAll()方法来执行一个主任务所创建的多个子任务。这是一个同步调用，这个任务将等待子任务完成，然后继续执行（也可能是结束）。
 * 当一个主任务等待它的子任务时，执行这个主任务的工作者线程接收另一个等待执行的任务并开始执行。正因为有了这个行为，所以说Fork／
 * Join框架提供了一种比Runnable和Callable对象更加高效的任务管理机制。
 * ForkJoinTask类的invokeAll()方法是执行器框架（ExecutorFramework）和Fork／Join框架之间的主要差异之一。
 * 在执行器框架中，所有的任务必须发送给执行器，然而，在这个示例中，线程池中包含了待执行方法的任务，任务的控制也是在线程池中进行的。
 * 我们在Task类中使用了invokeAll()方法，Task类继承了RecursiveAction类，
 * 而RecursiveAction类则继承了ForkJoinTask类。
 * 我们已经发送一个唯一的任务到线程池中，通过使用execute()方法来更新所有产品的列表。在这个示例中，它是一个同步调用，主线程一直等待调用的执行。
 * 我们已经使用了ForkJoinPool类的一些方法，来检查正在运行的任务的状态和演变情况。这个类包含更多的方法，可以用于任务状态的检测。参见8.
 * 5节介绍的这些方法的完整列表。 最后，像执行器框架一样，必须调用shutdown()方法来结束ForkJoinPool的执行。
 * <p>
 * ForkJoinPool类还提供了以下方法用于执行任务。 execute
 * (Runnabletask)：这是本范例中使用的execute()方法的另一种版本。这个方法发送一个Runnable任务给ForkJoinPool类。
 * 需要注意的是，使用Runnable对象时ForkJoinPool类就不采用工作窃取算法（Work-StealingAlgorithm），
 * ForkJoinPool类仅在使用ForkJoinTask类时才采用工作窃取算法。 invoke(ForkJoinTask
 * <T>task)：正如范例所示，ForkJoinPool类的execute()方法是异步调用的，而ForkJoinPool类的invoke()
 * 方法则是同步调用的。这个方法直到传递进来的任务执行结束后才会返回。
 * 也可以使用在ExecutorService类中声明的invokeAll()和invokeAny()方法，这些方法接收Callable对象作为参数。
 * 使用Callable对象时ForkJoinPool类就不采用工作窃取算法（Work-StealingAlgorithm），因此，
 * 最好使用执行器来执行Callable对象。 ForkJoinTask类也包含了在范例中所使用的invokeAll()方法的其他版本，这些版本如下。
 * invokeAll(ForkJoinTask<?>…
 * tasks)：这个版本的方法接收一个可变的参数列表，可以传递尽可能多的ForkJoinTask对象给这个方法作为参数。
 * invokeAll(Collection
 * <T>tasks)：这个版本的方法接受一个泛型类型T的对象集合（比如，ArrayList对象、LinkedList对象或者TreeSet对象）。
 * 这个泛型类型T必须是ForkJoinTask类或者它的子类。
 * 虽然ForkJoinPool类是设计用来执行ForkJoinTask对象的，但也可以直接用来执行Runnable和Callable对象。当然，
 * 也可以使用ForkJoinTask类的adapt()方法来接收一个Callable对象或者一个Runnable对象，
 * 然后将之转化为一个ForkJoinTask对象，然后再去执行。
 * <p>
 * ForkJoinPool类还提供了以下方法用于执行任务。 execute
 * (Runnabletask)：这是本范例中使用的execute()方法的另一种版本。这个方法发送一个Runnable任务给ForkJoinPool类。
 * 需要注意的是，使用Runnable对象时ForkJoinPool类就不采用工作窃取算法（Work-StealingAlgorithm），
 * ForkJoinPool类仅在使用ForkJoinTask类时才采用工作窃取算法。 invoke(ForkJoinTask
 * <T>task)：正如范例所示，ForkJoinPool类的execute()方法是异步调用的，而ForkJoinPool类的invoke()
 * 方法则是同步调用的。这个方法直到传递进来的任务执行结束后才会返回。
 * 也可以使用在ExecutorService类中声明的invokeAll()和invokeAny()方法，这些方法接收Callable对象作为参数。
 * 使用Callable对象时ForkJoinPool类就不采用工作窃取算法（Work-StealingAlgorithm），因此，
 * 最好使用执行器来执行Callable对象。 ForkJoinTask类也包含了在范例中所使用的invokeAll()方法的其他版本，这些版本如下。
 * invokeAll(ForkJoinTask<?>…
 * tasks)：这个版本的方法接收一个可变的参数列表，可以传递尽可能多的ForkJoinTask对象给这个方法作为参数。
 * invokeAll(Collection
 * <T>tasks)：这个版本的方法接受一个泛型类型T的对象集合（比如，ArrayList对象、LinkedList对象或者TreeSet对象）。
 * 这个泛型类型T必须是ForkJoinTask类或者它的子类。
 * 虽然ForkJoinPool类是设计用来执行ForkJoinTask对象的，但也可以直接用来执行Runnable和Callable对象。当然，
 * 也可以使用ForkJoinTask类的adapt()方法来接收一个Callable对象或者一个Runnable对象，
 * 然后将之转化为一个ForkJoinTask对象，然后再去执行。
 * <p>
 * ForkJoinPool类还提供了以下方法用于执行任务。 execute
 * (Runnabletask)：这是本范例中使用的execute()方法的另一种版本。这个方法发送一个Runnable任务给ForkJoinPool类。
 * 需要注意的是，使用Runnable对象时ForkJoinPool类就不采用工作窃取算法（Work-StealingAlgorithm），
 * ForkJoinPool类仅在使用ForkJoinTask类时才采用工作窃取算法。 invoke(ForkJoinTask
 * <T>task)：正如范例所示，ForkJoinPool类的execute()方法是异步调用的，而ForkJoinPool类的invoke()
 * 方法则是同步调用的。这个方法直到传递进来的任务执行结束后才会返回。
 * 也可以使用在ExecutorService类中声明的invokeAll()和invokeAny()方法，这些方法接收Callable对象作为参数。
 * 使用Callable对象时ForkJoinPool类就不采用工作窃取算法（Work-StealingAlgorithm），因此，
 * 最好使用执行器来执行Callable对象。 ForkJoinTask类也包含了在范例中所使用的invokeAll()方法的其他版本，这些版本如下。
 * invokeAll(ForkJoinTask<?>…
 * tasks)：这个版本的方法接收一个可变的参数列表，可以传递尽可能多的ForkJoinTask对象给这个方法作为参数。
 * invokeAll(Collection
 * <T>tasks)：这个版本的方法接受一个泛型类型T的对象集合（比如，ArrayList对象、LinkedList对象或者TreeSet对象）。
 * 这个泛型类型T必须是ForkJoinTask类或者它的子类。
 * 虽然ForkJoinPool类是设计用来执行ForkJoinTask对象的，但也可以直接用来执行Runnable和Callable对象。当然，
 * 也可以使用ForkJoinTask类的adapt()方法来接收一个Callable对象或者一个Runnable对象，
 * 然后将之转化为一个ForkJoinTask对象，然后再去执行。
 * <p>
 * ForkJoinPool类还提供了以下方法用于执行任务。 execute
 * (Runnabletask)：这是本范例中使用的execute()方法的另一种版本。这个方法发送一个Runnable任务给ForkJoinPool类。
 * 需要注意的是，使用Runnable对象时ForkJoinPool类就不采用工作窃取算法（Work-StealingAlgorithm），
 * ForkJoinPool类仅在使用ForkJoinTask类时才采用工作窃取算法。 invoke(ForkJoinTask
 * <T>task)：正如范例所示，ForkJoinPool类的execute()方法是异步调用的，而ForkJoinPool类的invoke()
 * 方法则是同步调用的。这个方法直到传递进来的任务执行结束后才会返回。
 * 也可以使用在ExecutorService类中声明的invokeAll()和invokeAny()方法，这些方法接收Callable对象作为参数。
 * 使用Callable对象时ForkJoinPool类就不采用工作窃取算法（Work-StealingAlgorithm），因此，
 * 最好使用执行器来执行Callable对象。 ForkJoinTask类也包含了在范例中所使用的invokeAll()方法的其他版本，这些版本如下。
 * invokeAll(ForkJoinTask<?>…
 * tasks)：这个版本的方法接收一个可变的参数列表，可以传递尽可能多的ForkJoinTask对象给这个方法作为参数。
 * invokeAll(Collection
 * <T>tasks)：这个版本的方法接受一个泛型类型T的对象集合（比如，ArrayList对象、LinkedList对象或者TreeSet对象）。
 * 这个泛型类型T必须是ForkJoinTask类或者它的子类。
 * 虽然ForkJoinPool类是设计用来执行ForkJoinTask对象的，但也可以直接用来执行Runnable和Callable对象。当然，
 * 也可以使用ForkJoinTask类的adapt()方法来接收一个Callable对象或者一个Runnable对象，
 * 然后将之转化为一个ForkJoinTask对象，然后再去执行。
 * <p>
 * ForkJoinPool类还提供了以下方法用于执行任务。 execute
 * (Runnabletask)：这是本范例中使用的execute()方法的另一种版本。这个方法发送一个Runnable任务给ForkJoinPool类。
 * 需要注意的是，使用Runnable对象时ForkJoinPool类就不采用工作窃取算法（Work-StealingAlgorithm），
 * ForkJoinPool类仅在使用ForkJoinTask类时才采用工作窃取算法。 invoke(ForkJoinTask
 * <T>task)：正如范例所示，ForkJoinPool类的execute()方法是异步调用的，而ForkJoinPool类的invoke()
 * 方法则是同步调用的。这个方法直到传递进来的任务执行结束后才会返回。
 * 也可以使用在ExecutorService类中声明的invokeAll()和invokeAny()方法，这些方法接收Callable对象作为参数。
 * 使用Callable对象时ForkJoinPool类就不采用工作窃取算法（Work-StealingAlgorithm），因此，
 * 最好使用执行器来执行Callable对象。 ForkJoinTask类也包含了在范例中所使用的invokeAll()方法的其他版本，这些版本如下。
 * invokeAll(ForkJoinTask<?>…
 * tasks)：这个版本的方法接收一个可变的参数列表，可以传递尽可能多的ForkJoinTask对象给这个方法作为参数。
 * invokeAll(Collection
 * <T>tasks)：这个版本的方法接受一个泛型类型T的对象集合（比如，ArrayList对象、LinkedList对象或者TreeSet对象）。
 * 这个泛型类型T必须是ForkJoinTask类或者它的子类。
 * 虽然ForkJoinPool类是设计用来执行ForkJoinTask对象的，但也可以直接用来执行Runnable和Callable对象。当然，
 * 也可以使用ForkJoinTask类的adapt()方法来接收一个Callable对象或者一个Runnable对象，
 * 然后将之转化为一个ForkJoinTask对象，然后再去执行。
 * <p>
 * ForkJoinPool类还提供了以下方法用于执行任务。 execute
 * (Runnabletask)：这是本范例中使用的execute()方法的另一种版本。这个方法发送一个Runnable任务给ForkJoinPool类。
 * 需要注意的是，使用Runnable对象时ForkJoinPool类就不采用工作窃取算法（Work-StealingAlgorithm），
 * ForkJoinPool类仅在使用ForkJoinTask类时才采用工作窃取算法。 invoke(ForkJoinTask
 * <T>task)：正如范例所示，ForkJoinPool类的execute()方法是异步调用的，而ForkJoinPool类的invoke()
 * 方法则是同步调用的。这个方法直到传递进来的任务执行结束后才会返回。
 * 也可以使用在ExecutorService类中声明的invokeAll()和invokeAny()方法，这些方法接收Callable对象作为参数。
 * 使用Callable对象时ForkJoinPool类就不采用工作窃取算法（Work-StealingAlgorithm），因此，
 * 最好使用执行器来执行Callable对象。 ForkJoinTask类也包含了在范例中所使用的invokeAll()方法的其他版本，这些版本如下。
 * invokeAll(ForkJoinTask<?>…
 * tasks)：这个版本的方法接收一个可变的参数列表，可以传递尽可能多的ForkJoinTask对象给这个方法作为参数。
 * invokeAll(Collection
 * <T>tasks)：这个版本的方法接受一个泛型类型T的对象集合（比如，ArrayList对象、LinkedList对象或者TreeSet对象）。
 * 这个泛型类型T必须是ForkJoinTask类或者它的子类。
 * 虽然ForkJoinPool类是设计用来执行ForkJoinTask对象的，但也可以直接用来执行Runnable和Callable对象。当然，
 * 也可以使用ForkJoinTask类的adapt()方法来接收一个Callable对象或者一个Runnable对象，
 * 然后将之转化为一个ForkJoinTask对象，然后再去执行。
 * <p>
 * ForkJoinPool类还提供了以下方法用于执行任务。 execute
 * (Runnabletask)：这是本范例中使用的execute()方法的另一种版本。这个方法发送一个Runnable任务给ForkJoinPool类。
 * 需要注意的是，使用Runnable对象时ForkJoinPool类就不采用工作窃取算法（Work-StealingAlgorithm），
 * ForkJoinPool类仅在使用ForkJoinTask类时才采用工作窃取算法。 invoke(ForkJoinTask
 * <T>task)：正如范例所示，ForkJoinPool类的execute()方法是异步调用的，而ForkJoinPool类的invoke()
 * 方法则是同步调用的。这个方法直到传递进来的任务执行结束后才会返回。
 * 也可以使用在ExecutorService类中声明的invokeAll()和invokeAny()方法，这些方法接收Callable对象作为参数。
 * 使用Callable对象时ForkJoinPool类就不采用工作窃取算法（Work-StealingAlgorithm），因此，
 * 最好使用执行器来执行Callable对象。 ForkJoinTask类也包含了在范例中所使用的invokeAll()方法的其他版本，这些版本如下。
 * invokeAll(ForkJoinTask<?>…
 * tasks)：这个版本的方法接收一个可变的参数列表，可以传递尽可能多的ForkJoinTask对象给这个方法作为参数。
 * invokeAll(Collection
 * <T>tasks)：这个版本的方法接受一个泛型类型T的对象集合（比如，ArrayList对象、LinkedList对象或者TreeSet对象）。
 * 这个泛型类型T必须是ForkJoinTask类或者它的子类。
 * 虽然ForkJoinPool类是设计用来执行ForkJoinTask对象的，但也可以直接用来执行Runnable和Callable对象。当然，
 * 也可以使用ForkJoinTask类的adapt()方法来接收一个Callable对象或者一个Runnable对象，
 * 然后将之转化为一个ForkJoinTask对象，然后再去执行。
 * <p>
 * ForkJoinPool类还提供了以下方法用于执行任务。 execute
 * (Runnabletask)：这是本范例中使用的execute()方法的另一种版本。这个方法发送一个Runnable任务给ForkJoinPool类。
 * 需要注意的是，使用Runnable对象时ForkJoinPool类就不采用工作窃取算法（Work-StealingAlgorithm），
 * ForkJoinPool类仅在使用ForkJoinTask类时才采用工作窃取算法。 invoke(ForkJoinTask
 * <T>task)：正如范例所示，ForkJoinPool类的execute()方法是异步调用的，而ForkJoinPool类的invoke()
 * 方法则是同步调用的。这个方法直到传递进来的任务执行结束后才会返回。
 * 也可以使用在ExecutorService类中声明的invokeAll()和invokeAny()方法，这些方法接收Callable对象作为参数。
 * 使用Callable对象时ForkJoinPool类就不采用工作窃取算法（Work-StealingAlgorithm），因此，
 * 最好使用执行器来执行Callable对象。 ForkJoinTask类也包含了在范例中所使用的invokeAll()方法的其他版本，这些版本如下。
 * invokeAll(ForkJoinTask<?>…
 * tasks)：这个版本的方法接收一个可变的参数列表，可以传递尽可能多的ForkJoinTask对象给这个方法作为参数。
 * invokeAll(Collection
 * <T>tasks)：这个版本的方法接受一个泛型类型T的对象集合（比如，ArrayList对象、LinkedList对象或者TreeSet对象）。
 * 这个泛型类型T必须是ForkJoinTask类或者它的子类。
 * 虽然ForkJoinPool类是设计用来执行ForkJoinTask对象的，但也可以直接用来执行Runnable和Callable对象。当然，
 * 也可以使用ForkJoinTask类的adapt()方法来接收一个Callable对象或者一个Runnable对象，
 * 然后将之转化为一个ForkJoinTask对象，然后再去执行。
 */

/**
 * ForkJoinPool类还提供了以下方法用于执行任务。 execute
 * (Runnabletask)：这是本范例中使用的execute()方法的另一种版本。这个方法发送一个Runnable任务给ForkJoinPool类。
 * 需要注意的是，使用Runnable对象时ForkJoinPool类就不采用工作窃取算法（Work-StealingAlgorithm），
 * ForkJoinPool类仅在使用ForkJoinTask类时才采用工作窃取算法。 invoke(ForkJoinTask
 * <T>task)：正如范例所示，ForkJoinPool类的execute()方法是异步调用的，而ForkJoinPool类的invoke()
 * 方法则是同步调用的。这个方法直到传递进来的任务执行结束后才会返回。
 * 也可以使用在ExecutorService类中声明的invokeAll()和invokeAny()方法，这些方法接收Callable对象作为参数。
 * 使用Callable对象时ForkJoinPool类就不采用工作窃取算法（Work-StealingAlgorithm），因此，
 * 最好使用执行器来执行Callable对象。 ForkJoinTask类也包含了在范例中所使用的invokeAll()方法的其他版本，这些版本如下。
 * invokeAll(ForkJoinTask<?>…
 * tasks)：这个版本的方法接收一个可变的参数列表，可以传递尽可能多的ForkJoinTask对象给这个方法作为参数。
 * invokeAll(Collection
 * <T>tasks)：这个版本的方法接受一个泛型类型T的对象集合（比如，ArrayList对象、LinkedList对象或者TreeSet对象）。
 * 这个泛型类型T必须是ForkJoinTask类或者它的子类。
 * 虽然ForkJoinPool类是设计用来执行ForkJoinTask对象的，但也可以直接用来执行Runnable和Callable对象。当然，
 * 也可以使用ForkJoinTask类的adapt()方法来接收一个Callable对象或者一个Runnable对象，
 * 然后将之转化为一个ForkJoinTask对象，然后再去执行。
 */