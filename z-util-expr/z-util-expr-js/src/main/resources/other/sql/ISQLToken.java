package sql;

public interface ISQLToken {

    /**
     * 返回SQLToken的类型
     */
    SQLTokenType getType();

    /**
     * Token的文本值
     *
     * @return
     */
    SQLTokenType getText();

}
