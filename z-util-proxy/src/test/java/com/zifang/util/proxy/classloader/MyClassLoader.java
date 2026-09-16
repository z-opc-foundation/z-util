package com.zifang.util.proxy.classloader;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * 首先实现一个自己的ClassLoader，该ClassLoader重写findClass方法。 从classpath中加载类资源。
 * 注意，不要重写loadClass方法。  因为在使用自定义的MyClassLoader加载Person类的时候 。
 * Person类中需要依赖的其他对象， 都会默认使用MyClassLoader的loadClass方法进行加载。
 * 如果重写了loadClass方法（像下面代码注释那样），就会导致jvm使用MyClassLoader来加载
 * Object、String等等一些类。  当然，这些类在classpath是找不到的。
 * 所以就会抛出ClassNotFoundException 。
 */

/**
 * MyClassLoader类。
 */
public class MyClassLoader extends ClassLoader {
//	@Override
//	public Class<?> loadClass(String name) throws ClassNotFoundException {
//		return super.loadClass(name);
//	}

    @Override
    /**
     * findClass方法。
     *      * @param name String类型参数
     * @return Class<?>类型返回值
     */
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        String classPath = MyClassLoader.class.getResource("/").getPath(); // 得到classpath
        String fileName = name.replace(".", "/") + ".class";
        File classFile = new File(classPath, fileName);
        if (!classFile.exists()) {
            throw new ClassNotFoundException(classFile.getPath() + " 不存在");
        }
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ByteBuffer bf = ByteBuffer.allocate(1024);
        FileInputStream fis = null;
        FileChannel fc = null;
        try {
            fis = new FileInputStream(classFile);
            fc = fis.getChannel();
            while (fc.read(bf) > 0) {
                bf.flip();
                bos.write(bf.array(), 0, bf.limit());
                bf.clear();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                fis.close();
                fc.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return defineClass(null, bos.toByteArray(), 0, bos.toByteArray().length);
    }
}
