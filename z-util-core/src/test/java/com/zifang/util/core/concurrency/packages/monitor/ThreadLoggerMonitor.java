package com.zifang.util.core.concurrency.packages.monitor;//package com.zifang.util.zex.demo.jdk.concurent.packages.monitor;
//
//import java.io.IOException;
//import java.util.Date;
//import java.util.logging.Formatter;
//import java.util.concurrent.TimeUnit;
//import java.util.logging.FileHandler;
//import java.util.logging.Handler;
//import java.util.logging.Level;
//import java.util.logging.LogRecord;
//import java.util.logging.log;
//
/// **
// * 写有效的日志
//
//	log工具提供了允许你写信息到一个或者多个目的地的机制。一个log是由以下这些组成：
//		1.一个或多个处理者: 一个处理者将决定目的地和log信息的格式。你可以把日志信息写入操控台，文档，或者数据库。
//		2.名字: 通常log使用的名字是基于类名或者它的包名。
//		3.等级: 日志信息有等级来表明它的重要性。log也有个等级是用来决定写什么样的信息。它只会写和这个等级一样重要的，或者更重要的信息。
//	为了以下2个主要目的，你应该使用log 系统：
//		1.当异常被捕捉，写尽可能多的信息。这个会帮助你定位错误并解决它。
//		2.写关于程序正在执行的类和方法的信息。
//	在这个指南，你将学习如何使用 java.spi.logging 包提供的类来添加一个log系统到你的并发应用。
// *
// */
//public class ThreadlogMonitor {
//
//	// 1. 创建一个类，名为MyFormatter ，扩展 java.spi.logging.Formatter 类。实现抽象方法
//	// format()。它接收一个 LogRecord 对象作为参数，并返回一个有着日志信息 String 对象。
//	static class MyFormatter extends Formatter {
//		@Override
//		public String format(LogRecord record) {
//			StringBuilder sb = new StringBuilder();
//			sb.append("[" + record.getLevel() + "] - ");
//			sb.append(new Date(record.getMillis()) + " : ");
//			sb.append(record.getSourceClassName() + "." + record.getSourceMethodName() + " : ");
//			sb.append(record.getMessage() + "\n");
//
//			return sb.toString();
//		}
//
//		// 2. 创建一个类，名为 Mylog。
//		static class Mylog {
//			// 3. 声明一个私有 static Handler 属性，名为 handler。
//			private static Handler handler;
//
//			// 4. 实现公共 static 方法 getlog() 来创建 log
//			// 对象，你将要使用它来写日志信息。它接收一个String 参数，名为 name。
//			public static log getlog(String name) {
//				// 5. 使用 log 类的getlog() 方法,获取与 java.spi.logging.log
//				// 相关联的 name 作为参数。
//				log log = log.getlog(name);
//				// 6. 使用 setLevel() 方法，确立用来写入全部信息的log级别。
//				log.setLevel(Level.ALL);
//				// 7. 如果处理者属性为null值，创建一个新的 FileHandler 对象在 recipe8.log
//				// 文件内写日志信息。使用 setFormatter()对象给处理者分配一个 MyFormatter 对象作为格式。
//				try {
//					if (handler == null) {
//						handler = new FileHandler("recipe8.log");
//						Formatter format = new MyFormatter();
//						handler.setFormatter(format);
//					}
//
//					// 8. If the 如果 log 对象还有一个与之相关联的处理者，使用 addHandler()
//					// 方法分配一个处理者。
//					if (log.getHandlers().length == 0) {
//						log.addHandler(handler);
//					}
//				} catch (SecurityException e) {
//					e.printStackTrace();
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//
//				// 9. 返回创建的 log 对象。
//				return log;
//			}
//		}
//
//		// 10. 创建一个类，名为Task，它实现Runnable 接口。它将是用来测试你的log对象的任务。
//		static class Task implements Runnable {
//
//			// 11. 实现 run() 方法。
//			@Override
//			public void run() {
//
//				// 12. 首先，声明一个 log 对象，名为 log。使用 Mylog 类的 getlog()
//				// 方法传递这个类的名字为参数来初始它。
//				log log = Mylog.getlog(this.getClass().getName());
//
//				// 13. 使用 entering() 方法写日志信息表明执行开始。
//				log.entering(Thread.currentThread().getName(), "run()");
//				// 休眠2秒。
//				try {
//					TimeUnit.SECONDS.sleep(2);
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}
//
//				// 14.使用 exiting() 方法写日志信息表明执行结束
//				log.exiting(Thread.currentThread().getName(), "run()", Thread.currentThread());
//			}
//		}
//
//		// 15. 创建例子的主类通过创建一个类，名为 Main 并添加 main()方法。
//
//		public static void main(String[] args) {
//
//			// 16. 声明一个 log 对象，名为 log。使用 Mylog 类的 getlog() 方法传递字符串
//			// Core 作为参数来初始它
//			log log = Mylog.getlog("Core");
//
//			// 17. 使用 entering() 方法写日志信息表明主程序开始执行。
//			log.entering("Core", "main()", args);
//
//			// 18. 创建 Thread array 来保存5个线程。
//            Thread[] threads = new Thread[5];
//
//			// 19. 创建5个Task对象和5个执行他们的线程。写日志信息表明，你将运行一个新的线程和表明你已经创建了线程。
//			for (int i = 0; i < threads.length; i++) {
//				log.log(Level.INFO, "Launching thread: " + i);
//				Task task = new Task();
//				threads[i] = new Thread(task);
//				log.log(Level.INFO, "Thread created: " + threads[i].getName());
//				threads[i].start();
//			}
//
//			// 20. 写日志信息表明你已经创建了线程。
//			log.log(Level.INFO, "Ten Threads created." + "Waiting for its finalization");
//
//			// 21. 使用 join() 方法等待5个线程的终结。在每个线程终结之后，写日志信息表明线程已经结束。
//			for (int i = 0; i < threads.length; i++) {
//				try {
//					threads[i].join();
//					log.log(Level.INFO, "Thread has finished its execution", threads[i]);
//				} catch (InterruptedException e) {
//					log.log(Level.SEVERE, "Exception", e);
//				}
//			}
//
//			// 22. 使用 exiting() 方法写一个日志信息表明主程序运行结束。
//			log.exiting("Core", "main()");
//		}
//	}
//}
//
//
/// **
//它是如何工作的…
//	在这个指南里，你已经使用 Java logging API 提供的log类 在并发应用中写日志信息。首先，你实现了 MyFormatter 类来给日志信息一个格式。这个类扩展 Formatter 类，声明了抽象方法 format()。此方法接收 LogRecord 对象的全部日志消息信息，并返回一条格式化的日志信息。在你的类里使用了 LogRecord类的以下这些方法来获取日志信息：
//		1.getLevel(): 返回的信息的级别。
//		2.getMillis():返回日期，当一条信息被发送给 log 对象。
//		3.getSourceClassName(): 返回发送信息给log的类的名字。
//		4.getSourceMessageName():返回发送信息给log的方法的名字
//		5.getMessage() 返回日志信息。Mylog 类实现了静态方法 getlog()， 创建 log 对象并分配 Handler 对象使用MyFormatter的格式在recipe8.log 文件中写入日志信息。你可以使用这个类的静态方法 getlog() 创建对象。此方法返回每个不同的对象作为参数传递的名字。 你只要创建一个Handler对象，全部的log对象都会使用它在同一个文件中写日志信息。你还配置了log写全部的日志信息，无论信息级别。
//	最后，你实现了 Task 对象和一个主程序在logfile写入不同的日志信息。你使用了以下的方法：
//		1.entering():写 FINER 等级的信息，表明方法开始运行
//		2.exiting(): 写 FINER 等级的信息，表明方法结束运行
//		3.log(): 写特定级别的信息
//        更多…
//	当你使用log类时，你必须考虑2个重要点：
//	写必需的信息：如果你写过少的信息，那么log就没有满足它的目的变的不是特别有作用。如果你写过多的信息，那么就会产生很大的日志文件，就不好管理且很难获取必要信息。
//	对信息使用适当的级别：如果你用高级别写入消息信息（information messages)，或者使用低级别来写入报错信息，那么你就会让看logfiles的人很困惑。就会变得很难了解到底发生了什么错误，或者有过多的信息来分析错误的主要原因。
//	还有很多其他库比 java.spi. logging 包提供了更完整的log系统，例如 Log4j 或者 slf4j libraries。但是 java.spi.logging 是Java API 的一部分，而且它的全部方法都是 multi-thread safe，所以在并发应用中使用它将不会遇到任何问题。
//
//*/