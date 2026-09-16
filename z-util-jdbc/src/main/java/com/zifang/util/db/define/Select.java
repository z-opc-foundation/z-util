package com.zifang.util.db.define;

import java.lang.annotation.*;

/**
 * 查询操作注解
 * <p>
 * 用于标识需要进行数据库查询操作的方法，通常与MyBatis等ORM框架配合使用。
 * 标记在Mapper接口的方法上，表示该方法执行数据库查询操作。
 *
 * <p>使用示例：
 * <pre>
 * public interface UserMapper {
 *     &#64;Select("SELECT * FROM user WHERE id = #{id}")
 *     User findUserById(long id);
 *
 *     &#64;Select("SELECT * FROM user WHERE name = #{name} AND age = #{age}")
 *     List&lt;User&gt; findUsersByNameAndAge(&#64;Param("name") String name, &#64;Param("age") int age);
 * }
 * </pre>
 *
 * <p>该注解可以与&#64;Param注解配合使用，为方法参数指定名称，
 * 以便在SQL语句中通过#{paramName}方式引用。
 *
 * @see Insert
 * @see Update
 * @see Delete
 * @see Param
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
/**
 * Select注解。
 */
public @interface Select {
    /**
     * 指定查询SQL语句
     *
     * @return SQL查询语句，支持#{paramName}和${paramName}参数绑定
     */
    String value();
}
