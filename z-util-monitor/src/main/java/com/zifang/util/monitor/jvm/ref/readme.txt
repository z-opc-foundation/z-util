概述
	Java.lang.ref 是 Java 类库中比较特殊的一个包，它提供了与 Java 垃圾回收器密切相关的引用类。
这些引用类对象可以指向其它对象，但它们不同于一般的引用，因为它们的存在并不防碍 Java 垃圾回收器对它们所指向的
对象进行回收。其好处就在于使者可以保持对使用对象的引用，同时 JVM 依然可以在内存不够用的时候对使用对象进行回收。
因此这个包在用来实现与缓存相关的应用时特别有用。同时该包也提供了在对象的“可达”性发生改变时，进行提醒的机制。
本文通过对该包进行由浅入深的介绍与分析，使读者可以加深对该包的理解，从而更好地利用该包进行开发。


=======================引用类型特性总结================================
引用类型	|	取得目标对象方式	|	垃圾回收条件		|	是否可能内存泄漏
强引用		|	直接调用		|	不回收			|	可能
软引用		|	通过 get()方法	|	视内存情况回收	|	不可能
弱引用		|	通过 get()方法	|	永远回收		|	不可能
虚引用		|	无法取得		|	不回收			|	可能


类型					是否抛出异常						示例代码	运行结果
StrongReference		抛出异常						见清单 6	Exception in thread "main" java.lang.OutOfMemoryError: Java heap space
SoftReference		不抛异常，之前的引用自动清空并返回 null	见清单 7	null
WeakReference		同上							见清单 8	null
PhantomReference	抛出异常						见清单 9	Exception in thread "main" java.lang.OutOfMemoryError: Java heap space
