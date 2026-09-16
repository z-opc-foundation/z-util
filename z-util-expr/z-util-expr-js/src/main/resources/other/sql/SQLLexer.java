package sql;


import com.zifang.util.expression.scrept.Token;

import java.io.CharArrayReader;
import java.util.ArrayList;
import java.util.List;

/**
 * sql的词法解析器
 */
public class SQLLexer {

    private StringBuffer tokenText = null;

    //保存解析出来的Token
    private List<Token> tokens = null;


    //当前正在解析的Token
    private SQLToken token = null;

    private CharArrayReader reader;

    /**
     * 对代码文本进行token流化
     */
    public List<ISQLToken> tokenize(String code) {

        tokens = new ArrayList<>();
        reader = new CharArrayReader(code.toCharArray());
        StringBuffer tokenText = new StringBuffer(); //临时保存token的文本
        SQLToken sqlToken = new SQLToken(); // 当前正在解析的

        int ich = 0;
        char ch = 0;
        SQLDfaState state = SQLDfaState.Initial;

        return null;
    }
}
