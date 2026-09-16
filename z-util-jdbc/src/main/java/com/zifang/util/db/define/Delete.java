package com.zifang.util.db.define;

import java.lang.annotation.*;

/**
 * 删除操作注解。
 *
 * <h3>使用</h3>
 * <pre>{@code
 *   public interface UserMapper {
 *       &#64;Delete("DELETE FROM user WHERE id = #{id}")
 *       int deleteUserById(&#64;Param("id") long id);
 *   }
 * }</pre>
 *
 * @see Select
 * @see Insert
 * @see Update
 * @see Param
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface Delete {

    /**
     * SQL 语句，支持 #{paramName} 绑定。
     */
    String value();
}
