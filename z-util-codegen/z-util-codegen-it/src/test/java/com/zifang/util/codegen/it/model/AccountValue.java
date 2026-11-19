package com.zifang.util.codegen.it.model;

import com.zifang.util.codegen.annotation.ZImmutable;

/**
 * 抽象值类型：混合 record 风格与 bean 风格命名，验证两种都能被识别。
 */
@ZImmutable
public abstract class AccountValue {

    /** record 风格。 */
    public abstract String id();

    /** bean 风格。 */
    public abstract int balance();

    /** boolean 的 is 前缀。 */
    public abstract boolean active();
}
