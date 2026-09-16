package sql;

public enum SQLTokenType {
    SELECT, // select
    STAR, // *
    FROM, //from
    ALIAS, // 表别名
    WHERE, // 条件语句
    // 接下来是出现在where下条件的部分
    IN,
    OR,
    GROUP_BY, // 需要配合函数相关
    HAVING,
    ORDER_BY,
    LIMIT,
    SemiColon, // ;
    LeftParen, // (
    RightParen,// )
}
