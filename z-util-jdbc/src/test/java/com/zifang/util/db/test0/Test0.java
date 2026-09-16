package com.zifang.util.db.test0;

import com.zifang.util.core.util.GsonUtil;
import com.zifang.util.db.context.DataSourceContext;
import com.zifang.util.db.context.DatasourceContextManager;
import com.zifang.util.db.respository.RepositoryProxy;
import com.zifang.util.db.transaction.TransactionManager;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Map;


/**
 * Test0类。
 */
public class Test0 {

    private static final Logger log = LoggerFactory.getLogger(Test0.class);

    /**
     * 创建一个不真正连库的 DataSource 桩对象，供仅测试 DataSourceContext 装配使用。
     */
    private static DataSource mockDataSource() {
        return (DataSource) Proxy.newProxyInstance(
                DataSource.class.getClassLoader(),
                new Class<?>[]{DataSource.class},
                (proxy, method, args) -> null);
    }

    @Before
    /**
     * init方法。
     */
    public void init() {

        // 数据库上下文
        DataSourceContext dataSourceContext = new DataSourceContext()
                .scanPackage("com.zifang.util.db")
                .transationManager(new TransactionManager(mockDataSource()))
                .dataSourceFactory(new MysqlDatasourceFactory());

        // 注册数据库信息
        DatasourceContextManager.register(DatasourceContextManager.DEFAULT, dataSourceContext);
    }

    @Test
    @org.junit.Ignore("需要 MySQL 数据库连接")
    /**
     * test方法。
     */
    public void test() {

        ResourceItemRepository resourceItemRepository = RepositoryProxy.proxy(ResourceItemRepository.class);

        List<ResourceItem> r1 = resourceItemRepository.findByNameList("5e8888e8be7fff746fb26b5a", 0);
        List<Map<String, Object>> r2 = resourceItemRepository.findByNameListMap("5e8888e8be7fff746fb26b5a", 0);
        ResourceItem r3 = resourceItemRepository.findByNameBean("5e8888e8be7fff746fb26b5a", 0);
        Map<String, Object> r4 = resourceItemRepository.findByNameMap("5e8888e8be7fff746fb26b5a", 0);

        log.info(GsonUtil.objectToJsonStr(r1));
        log.info(GsonUtil.objectToJsonStr(r2));
        log.info(GsonUtil.objectToJsonStr(r3));
        log.info(GsonUtil.objectToJsonStr(r4));

        log.info("结束");

    }

    @Test
    @org.junit.Ignore("需要 MySQL 数据库连接")
    /**
     * test1方法。
     */
    public void test1() {
        ResourceItemRepository resourceItemRepository = RepositoryProxy.proxy(ResourceItemRepository.class);

        ResourceItem resourceItem = new ResourceItem();
        resourceItem.setCmsId("xxxxx");
        resourceItem.setShared(false);
        resourceItemRepository.save(resourceItem);
        resourceItemRepository.findById(resourceItem.getId());
        resourceItemRepository.deleteById(resourceItem.getId());
    }
}
