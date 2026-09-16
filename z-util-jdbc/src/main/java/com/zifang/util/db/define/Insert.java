package com.zifang.util.db.define;

import java.lang.annotation.*;

/**
 * 插入操作注解。
 * <p>
 * 与 {@link Select} / {@link Update} / {@link Delete} 配套使用，构成 MyBatis-lite 风格的 Mapper 注解。
 * SQL 中可通过 {@code #{paramName}} 引用方法参数。
 *
 * <h3>使用</h3>
 * <pre>{@code
 *   public interface UserMapper {
 *       &#64;Insert("INSERT INTO user(name, age) VALUES(#{name}, #{age})")
 *       int insertUser(&#64;Param("name") String name, &#64;Param("age") int age);
 *
 *       // 主键回填（DB 自增）
 *       &#64;Insert(value = "INSERT INTO ...", returnGeneratedKeys = true)
 *       int insertReturningKey(User user);
 *   }
 * }</pre>
 *
 * @see Select
 * @see Update
 * @see Delete
 * @see Param
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface Insert {

    /**
     * SQL 语句，支持 #{paramName} 绑定。
     */
    String value();

    /**
     * 是否回填自增主键到入参对象（需要入参是 POJO）。默认 false。
     */
    boolean returnGeneratedKeys() default false;
}
