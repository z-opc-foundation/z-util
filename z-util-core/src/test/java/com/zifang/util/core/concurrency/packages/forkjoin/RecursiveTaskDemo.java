package com.zifang.util.core.concurrency.packages.forkjoin;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.TimeUnit;

/**
 * RecursiveTaskDemo类。
 */
public class RecursiveTaskDemo {

    /**
     * main方法。
     * * @param args String[]类型参数
     *
     * @return static void类型返回值
     */
    public static void main(String[] args) {
        // 30．创建Document对象，包含100行，每行1,000个词。
        Document mock = new Document();

        String[][] document = mock.generateDocument(100, 1000, "the");
        // 31．创建一个DocumentTask对象，用来更新整个文档。传递数字0给参数start，以及数字100给参数end。
        DocumentTask task = new DocumentTask(document, 0, 100, "the");
        // 32．采用无参的构造器创建一个ForkJoinPool对象，然后调用execute()方法在线程池里执行这个任务。
        ForkJoinPool pool = new ForkJoinPool();
        pool.execute(task);
        // 33．实现代码块，显示线程池的进展信息，每秒钟在控制台输出线程池的一些参数，直到任务执行结束。
        do {
            System.out.printf("******************************************\n");
            System.out.printf("Main: Parallelism: %d\n", pool.getParallelism());
            System.out.printf("Main: Active Threads: %d\n", pool.getActiveThreadCount());
            System.out.printf("Main: Task Count: %d\n", pool.getQueuedTaskCount());
            System.out.printf("Main: Steal Count: %d\n", pool.getStealCount());
            System.out.printf("******************************************\n");
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } while (!task.isDone());
        // 34．调用shutdown()方法关闭线程池。
        pool.shutdown();
        // 35．调用awaitTermination()等待任务执行结束。
        try {
            pool.awaitTermination(1, TimeUnit.DAYS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // 36．在控制台输出文档中出现要查找的词的次数。检验这个数字与DocumentMock类输出的数字是否一致。
        try {
            System.out.printf("Main: The word appears %d in the document", task.get());
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    // 1．创建一个名为DocumentMock的类。它将生成一个字符串矩阵来模拟一个文档。
    static class Document {
        // 2．用一些词来创建一个字符串数组。这个数组将被用来生成字符串矩阵。
        private String[] words = {"the", "hello", "goodbye", "packt", "java", "thread", "pool", "random", "class",
                "main"};

        // 3．实现generateDocument()方法。它接收3个参数，分别是行数numLines，每一行词的个数numWords，和准备查找的词word。然后返回一个字符串矩阵。

        /**
         * generateDocument方法。
         * * @param numLines int类型参数
         *
         * @param numWords int类型参数
         * @param word     String类型参数
         * @return String[][]类型返回值
         */
        public String[][] generateDocument(int numLines, int numWords, String word) {
            // 4．创建用来生成文档所需要的对象：String矩阵，和用来生成随机数的Random对象。
            int counter = 0;
            String[][] document = new String[numLines][numWords];
            Random random = new Random();
            // 5．为字符串矩阵填上字符串。通过随机数取得数组words中的某一字符串，然后存入到字符串矩阵document对应的位置上，同时计算生成的字符串矩阵中将要查找的词出现的次数。这个值可以用来与后续程序运行查找任务时统计的次数相比较，检查两个值是否相同。
            for (int i = 0; i < numLines; i++) {
                for (int j = 0; j < numWords; j++) {
                    int index = random.nextInt(words.length);
                    document[i][j] = words[index];
                    if (document[i][j].equals(word)) {
                        counter++;
                    }
                }
            }
            // 6．在控制台输出这个词出现的次数，并返回生成的矩阵document。
            System.out.println("DocumentMock: The word appears " + counter + " times in the document");
            return document;
        }
    }

    // 7．创建名为DocumentTask的类，并继承RecursiveTask类，RecursiveTask类的泛型参数为Integer类型。这个DocumentTask类将实现一个任务，用来计算所要查找的词在行中出现的次数。
    static class DocumentTask extends RecursiveTask<Integer> {
        private static final long serialVersionUID = -6062187295810572604L;

        // 8．声明一个名为document的私有String矩阵，以及两个名为start和end的私有int属性，并声明一个名为word的私有String属性。
        private String[][] document;
        private int start, end;
        private String word;

        // 9．实现类的构造器，用来初始化类的所有属性。

        /**
         * DocumentTask方法。
         * * @param document String[][]类型参数
         *
         * @param start int类型参数
         * @param end   int类型参数
         * @param word  String类型参数
         */
        public DocumentTask(String[][] document, int start, int end, String word) {
            this.document = document;
            this.start = start;
            this.end = end;
            this.word = word;
        }

        // 10．实现compute()方法。如果end和start的差异小于10，则调用processLines()方法，来计算这两个位置之间要查找的词出现的次数。
        @Override
        /**
         * compute方法。
         * @return int类型返回值
         */
        protected Integer compute() {
            int result = 0;
            if (end - start < 10) {
                result = processLines(document, start, end, word);
                // 11．否则，拆分这些行成为两个对象，并创建两个新的DocumentTask对象来处理这两个对象，然后调用invokeAll()方法在线程池里执行它们。
            } else {
                int mid = (start + end) / 2;
                DocumentTask task1 = new DocumentTask(document, start, mid, word);
                DocumentTask task2 = new DocumentTask(document, mid, end, word);
                invokeAll(task1, task2);
                // 12．采用groupResults()方法将这两个任务返回的值相加。最后，返回任务计算的结果。
                try {
                    result = groupResults(task1.get(), task2.get());
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
            return result;
        }

        // 13．实现processLines()方法。接收4个参数，一个字符串document矩阵，start属性，end属性和任务将要查找的词word的属性。
        private Integer processLines(String[][] document, int start, int end, String word) {
            // 14．为任务所要处理的每一行，创建一个LineTask对象，然后存储在任务列表里。
            List<LineTask> tasks = new ArrayList<LineTask>();
            for (int i = start; i < end; i++) {
                LineTask task = new LineTask(document[i], 0, document[i].length, word);
                tasks.add(task);
            }
            // 15．调用invokeAll()方法执行列表中所有的任务。
            invokeAll(tasks);
            // 16．合计这些任务返回的值，并返回结果。
            int result = 0;
            for (int i = 0; i < tasks.size(); i++) {
                LineTask task = tasks.get(i);
                try {
                    result = result + task.get();
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
            return new Integer(result);
        }

        // 17．实现groupResults()方法。它将两个数字相加并返回结果。
        private Integer groupResults(Integer number1, Integer number2) {
            Integer result;
            result = number1 + number2;
            return result;
        }

    }

    // 29．实现范例的主类，创建Main主类，并实现main()方法。

    // 18．创建名为LineTask的类，并继承RecursiveTask类，RecursiveTask类的泛型参数为Integer类型。这个RecursiveTask类实现了一个任务，用来计算所要查找的词在一行中出现的次数。
    static class LineTask extends RecursiveTask<Integer> {
        // 19．声明类的serialVersionUID属性。这个元素是必需的，因为RecursiveTask的父类ForkJoinTask实现了Serializable接口。声明一个名为line的私有String数组属性和两个名为start和end的私有int属性。最后，声明一个名为word的私有String属性。
        private static final long serialVersionUID = 1L;
        private String[] line;
        private int start, end;
        private String word;

        // 20．实现类的构造器，用来初始化它的属性。

        /**
         * LineTask方法。
         * * @param line String[]类型参数
         *
         * @param start int类型参数
         * @param end   int类型参数
         * @param word  String类型参数
         */
        public LineTask(String[] line, int start, int end, String word) {
            this.line = line;
            this.start = start;
            this.end = end;
            this.word = word;
        }

        // 21．实现compute()方法。如果end和start属性的差异小于100，那么任务将采用count()方法，在由start与end属性所决定的行的片断中查找词。
        @Override
        /**
         * compute方法。
         * @return int类型返回值
         */
        protected Integer compute() {
            Integer result = null;
            if (end - start < 100) {
                result = count(line, start, end, word);
                // 22．如果end和start属性的差异不小于100，将这一组词拆分成两组，然后创建两个新的LineTask对象来处理这两个组，调用invokeAll()方法在线程池中执行它们。
            } else {
                int mid = (start + end) / 2;
                LineTask task1 = new LineTask(line, start, mid, word);
                LineTask task2 = new LineTask(line, mid, end, word);
                invokeAll(task1, task2);
                // 23．调用groupResults()方法将两个任务返回的值相加。最后返回任务计算的结果。
                try {
                    result = groupResults(task1.get(), task2.get());
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
            return result;
        }

        // 24．实现count()方法。它接收4个参数，一个完整行字符串line数组，start属性，end属性和任务将要查找的词word的属性。
        private Integer count(String[] line, int start, int end, String word) {
            // 25．将存储在start和end属性值之间的词与任务正在查找的word属性相比较。如果相同，那么将计数器counter变量加1。
            int counter;
            counter = 0;
            for (int i = start; i < end; i++) {
                if (line[i].equals(word)) {
                    counter++;
                }
            }
            // 26．为了延缓范例的执行，将任务休眠10毫秒。
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // 27．返回计数器counter变量的值。
            return counter;
        }

        // 28．实现groupResults()方法。计算两个数字之和并返回结果。.
        private Integer groupResults(Integer number1, Integer number2) {
            Integer result;
            result = number1 + number2;
            return result;
        }
    }
}

/**
 * 工作原理 在这个范例中，我们实现了两个不同的任务。
 * DocumentTask类：这个类的任务需要处理由start和end属性决定的文档行。如果这些行数小于10，那么，就每行创建一个LineTask对象，
 * 然后在任务执行结束后，合计返回的结果，并返回总数。如果任务要处理的行数大于10，那么，将任务拆分成两组，
 * 并创建两个DocumentTask对象来处理这两组对象。当这些任务执行结束后，同样合计返回的结果，并返回总数。
 * LineTask类：这个类的任务需要处理文档中一行的某一组词。如果一组词的个数小100，那么任务将直接在这一组词里搜索特定词，
 * 然后返回查找词在这一组词中出现的次数。否则，任务将拆分这些词为两组，并创建两个LineTask对象来处理这两组词。当这些任务执行完成后，合计返回的结果，
 * 并返回总数。
 * 在Main主类中，我们通过默认的构造器创建了ForkJoinPool对象，然后执行DocumentTask类，来处理一个共有100行，每行1000字的文档
 * 。这个任务将问题拆分成DocumentTask对象和LineTask对象，然后当所有的任务执行完成后，
 * 使用原始的任务来获取整个文档中所要查找的词出现的次数。由于任务继承了RecursiveTask类，因此能够返回结果。
 * 调用get()方法来获得Task返回的结果。这个方法声明在Future接口里，并由RecursiveTask类实现。
 * 执行程序时，在控制台上，我们可以比较第一行与最后一行的输出信息。第一行是文档生成时被查找的词出现的次数，最后一行则是通过Fork／
 * Join任务计算而来的被查找的词出现的次数，而且这两个数字相同。 更多信息
 * ForkJoinTask类提供了另一个complete()方法来结束任务的执行并返回结果。这个方法接收一个对象，
 * 对象的类型就是RecursiveTask类的泛型参数，然后在任务调用join()方法后返回这个对象作为结果。这一过程采用了推荐的异步任务来返回任务的结果。
 * 由于RecursiveTask类实现了Future接口，因此还有get()方法调用的其他版本： get(long timeout, TimeUnit
 * unit)：这个版本中，如果任务的结果未准备好，将等待指定的时间。如果等待时间超出，而结果仍未准备好，那方法就会返回null值。
 * TimeUnit是一个枚举类，有如下的常量：DAYS、HOURS、MICROSECONDS、MILLISECONDS、MINUTES、
 * NANOSECONDS和SECONDS。
 */