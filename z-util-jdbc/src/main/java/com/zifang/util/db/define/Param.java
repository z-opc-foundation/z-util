package com.zifang.util.db.define;

import java.lang.annotation.*;

/**
 * SQL 参数注解。
 * <p>
 * 用于标识方法参数在 SQL 语句中的绑定名称，配合 {@link Select} / {@link Insert} / {@link Update} / {@link Delete} 一起使用。
 *
 * <h3>使用</h3>
 * <pre>{@code
 *   public interface UserMapper {
 *       &#64;Select("SELECT * FROM user WHERE name = #{name} AND age = #{age}")
 *       User findByNameAndAge(&#64;Param("name") String name, &#64;Param("age") int age);
 *   }
 * }</pre>
 *
 * <h3>简化规则</h3>
 * 若方法只有一个参数且没有 &#64;Param 注解，则参数名默认为 {@code "param"} / 形参名 / 占位符 {@code #{_parameter}}。
 *
 * @see Select
 * @see Insert
 * @see Update
 * @see Delete
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
@Documented
public @interface Param {
    /**
     * 参数名称，SQL 中通过 #{value} 引用。
     */
    String value();
}
