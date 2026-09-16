package com.zifang.util.cli.exception;

import java.util.Collection;
import java.util.Iterator;

/**
 * Thrown when an option cannot be uniquely identified from a partial name.
 */
public class AmbiguousOptionException extends UnrecognizedOptionException {

    private static final long serialVersionUID = 1L;
    private final Collection<String> matchingOptions;

    /**
     * AmbiguousOptionException方法。
     * * @param option final类型参数
     *
     * @param matchingOptions final类型参数
     */
    public AmbiguousOptionException(final String option, final Collection<String> matchingOptions) {
        super(createMessage(option, matchingOptions), option);
        this.matchingOptions = matchingOptions;
    }

    private static String createMessage(final String option, final Collection<String> matchingOptions) {
        StringBuilder buf = new StringBuilder("Ambiguous option: '");
        buf.append(option);
        buf.append("'  (could be: ");
        Iterator<String> it = matchingOptions.iterator();
        while (it.hasNext()) {
            buf.append('\'');
            buf.append(it.next());
            buf.append('\'');
            if (it.hasNext()) {
                buf.append(", ");
            }
        }
        buf.append(")");
        return buf.toString();
    }

    /**
     * getMatchingOptions方法。
     *
     * @return Collection<String>类型返回值
     */
    public Collection<String> getMatchingOptions() {
        return matchingOptions;
    }
}
