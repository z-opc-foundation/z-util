package spel;

import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

public class SpELTest {

    public static void main(String[] args) {

        //创建ExpressionParser解析表达式
        ExpressionParser parser = new SpelExpressionParser();
        //表达式放置
        Expression exp = parser.parseExpression("表达式");
        //执行表达式，默认容器是spring本身的容器：ApplicationContext
        Object value = exp.getValue();

        /**如果使用其他的容器，则用下面的方法*/
        //创建一个虚拟的容器EvaluationContext
        StandardEvaluationContext ctx = new StandardEvaluationContext();
        //向容器内添加bean
        String beanA = "吃饭";
        ctx.setVariable("bean_id", beanA);

        //setRootObject并非必须；一个EvaluationContext只能有一个RootObject，引用它的属性时，可以不加前缀
        //ctx.setRootObject(XXX);

        //getValue有参数ctx，从新的容器中根据SpEL表达式获取所需的值
        //Object value = exp.getValue(ctx);
    }
}