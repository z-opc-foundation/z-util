package com.zifang.util.db.define;

import java.lang.annotation.*;

/**
 * 更新操作注解。
 *
 * <h3>使用</h3>
 * <pre>{@code
 *   public interface UserMapper {
 *       &#64;Update("UPDATE user SET name = #{name}, age = #{age} WHERE id = #{id}")
 *       int updateUser(&#64;Param("name") String name, &#64;Param("age") int age, &#64;Param("id") long id);
 *   }
 * }</pre>
 *
 * @see Select
 * @see Insert
 * @see Delete
 * @see Param
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface Update {

    /**
     * SQL 语句，支持 #{paramName} 绑定。
     */
    String value();
}
