lexer grammar SQLLexer;

// ==================== 关键字（长词优先，避免前缀冲突） ====================

DISTINCT  : 'DISTINCT' | 'distinct' ;
BETWEEN   : 'BETWEEN' | 'between' ;
IS        : 'IS' | 'is' ;
NOT       : 'NOT' | 'not' ;
NULL_     : 'NULL' | 'null' ;
TRUE_     : 'TRUE' | 'true' ;
FALSE_    : 'FALSE' | 'false' ;
SELECT    : 'SELECT' | 'select' ;
INSERT    : 'INSERT' | 'insert' ;
UPDATE    : 'UPDATE' | 'update' ;
DELETE    : 'DELETE' | 'delete' ;
FROM      : 'FROM' | 'from' ;
WHERE     : 'WHERE' | 'where' ;
ORDER     : 'ORDER' | 'order' ;
GROUP     : 'GROUP' | 'group' ;
HAVING    : 'HAVING' | 'having' ;
LIMIT     : 'LIMIT' | 'limit' ;
OFFSET    : 'OFFSET' | 'offset' ;
JOIN      : 'JOIN' | 'join' ;
INNER     : 'INNER' | 'inner' ;
LEFT      : 'LEFT' | 'left' ;
RIGHT     : 'RIGHT' | 'right' ;
FULL      : 'FULL' | 'full' ;
CROSS     : 'CROSS' | 'cross' ;
ON        : 'ON' | 'on' ;
AND       : 'AND' | 'and' ;
OR        : 'OR' | 'or' ;
AS        : 'AS' | 'as' ;
BY        : 'BY' | 'by' ;
ASC       : 'ASC' | 'asc' ;
DESC      : 'DESC' | 'desc' ;
IN        : 'IN' | 'in' ;
LIKE      : 'LIKE' | 'like' ;
EXISTS    : 'EXISTS' | 'exists' ;
CAST      : 'CAST' | 'cast' ;
CASE      : 'CASE' | 'case' ;
WHEN      : 'WHEN' | 'when' ;
THEN      : 'THEN' | 'then' ;
ELSE      : 'ELSE' | 'else' ;
END       : 'END' | 'end' ;
COUNT     : 'COUNT' | 'count' ;
SUM       : 'SUM' | 'sum' ;
AVG       : 'AVG' | 'avg' ;
MAX       : 'MAX' | 'max' ;
MIN       : 'MIN' | 'min' ;

// ==================== 标识符、数字、字符串 ====================

ID     : [a-zA-Z_] [a-zA-Z0-9_]* ;
NUM    : [0-9]+ ('.' [0-9]+)? ([eE] [+-]? [0-9]+)? ;
STRING : '\'' (~['\\] | '\\' .)* '\'' ;

// ==================== 运算符 ====================

EQ    : '=' | '==' ;
NEQ   : '<>' | '!=' ;
LE    : '<=' ;
GE    : '>=' ;
LT    : '<' ;
GT    : '>' ;
PLUS  : '+' ;
MINUS : '-' ;
STAR  : '*' ;
SLASH : '/' ;
PERCENT : '%' ;

// ==================== 标点 ====================

DOT    : '.' ;
COMMA  : ',' ;
LPAREN : '(' ;
RPAREN : ')' ;

// ==================== 空白与注释 ====================

LINE_COMMENT  : '--' ~[\r\n]* -> skip ;
BLOCK_COMMENT : '/*' .*? '*/' -> skip ;
WS            : [ \t\r\n]+ -> skip ;
