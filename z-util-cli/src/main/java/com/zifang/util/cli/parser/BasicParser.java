package com.zifang.util.cli.parser;

import com.zifang.util.cli.model.Options;

/**
 * Very simple parser that passes arguments through unchanged.
 *
 * @deprecated since 1.3, use {@link DefaultParser} instead.
 */
@Deprecated
/**
 * BasicParser类。
 */
public class BasicParser extends Parser {

    /**
     * BasicParser方法。
     */
    public BasicParser() {
    }

    @Override
    /**
     * flatten方法。
     *      * @param options final类型参数
     * @param arguments final类型参数
     * @param stopAtNonOption final类型参数
     * @return String[]类型返回值
     */
    protected String[] flatten(final Options options, final String[] arguments, final boolean stopAtNonOption) {
        return arguments;
    }
}
