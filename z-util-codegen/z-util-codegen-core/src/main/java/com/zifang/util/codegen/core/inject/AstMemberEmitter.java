package com.zifang.util.codegen.core.inject;

import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import com.zifang.util.codegen.core.diag.Diagnostics;
import com.zifang.util.codegen.core.expr.Expr;
import com.zifang.util.codegen.core.expr.Stmt;
import com.zifang.util.codegen.core.expr.TypeRef;
import com.zifang.util.codegen.core.model.Modifier;
import com.zifang.util.codegen.core.spec.ConstructorSpec;
import com.zifang.util.codegen.core.spec.FieldSpec;
import com.zifang.util.codegen.core.spec.MethodSpec;
import com.zifang.util.codegen.core.spec.ParamSpec;
import com.zifang.util.codegen.core.spi.MemberEmitter;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.TypeTag;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCBlock;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCModifiers;
import com.sun.tools.javac.tree.JCTree.JCStatement;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.tree.TreeCopier;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Name;
import com.sun.tools.javac.util.Names;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.Set;

/**
 * 把 handler 描述的成员直接注入目标类的 javac AST——与 Lombok 同一条技术路线。
 *
 * <p>注入发生在注解处理轮次里，javac 之后照常做归属与代码生成，所以注入出来的方法
 * 就是目标类自己的方法：没有父类、没有委托、调用点无感知。</p>
 *
 * <p>本类依赖 javac 内部 API。编译期需要 {@code --add-exports}，运行期需要
 * {@code -XDaccessInternalAPI}（见 README）。</p>
 *
 * @author zifang
 */
public final class AstMemberEmitter implements MemberEmitter {

    private final JCClassDecl decl;
    private final TreeMaker maker;
    private final Names names;
    private final TreeCopier<Void> copier;
    private final Element owner;
    private final Diagnostics diagnostics;

    private AstMemberEmitter(JCClassDecl decl, TreeMaker maker, Names names,
                             TreeCopier<Void> copier, Element owner, Diagnostics diagnostics) {
        this.decl = decl;
        this.maker = maker;
        this.names = names;
        this.copier = copier;
        this.owner = owner;
        this.diagnostics = diagnostics;
    }

    /**
     * 绑定到目标类型。
     *
     * @param environment 编译环境
     * @param typeElement 目标类型
     * @param diagnostics  诊断出口
     * @return 注入器；目标类型不在本次编译内时返回 null
     */
    public static AstMemberEmitter create(ProcessingEnvironment environment, TypeElement typeElement,
                                           Diagnostics diagnostics) {
        if (!(environment instanceof JavacProcessingEnvironment)) {
            diagnostics.error(typeElement, "z-util-codegen 只支持 javac 编译器（当前不是 JavacProcessingEnvironment）");
            return null;
        }
        Context context = ((JavacProcessingEnvironment) environment).getContext();
        TreeMaker maker = TreeMaker.instance(context);
        Names names = Names.instance(context);

        Trees trees = Trees.instance(environment);
        TreePath path = trees.getPath(typeElement);
        if (path == null || !(path.getLeaf() instanceof JCClassDecl)) {
            diagnostics.error(typeElement, "无法定位目标类型的源码 AST：" + typeElement.getQualifiedName());
            return null;
        }
        JCClassDecl decl = (JCClassDecl) path.getLeaf();
        return new AstMemberEmitter(decl, maker, names, new TreeCopier<>(maker), typeElement, diagnostics);
    }

    @Override
    public boolean addMethod(MethodSpec spec) {
        if (hasMethod(spec.name(), spec.parameters().size())) {
            return false;
        }
        JCMethodDecl method = maker.MethodDef(
                modifiers(spec.modifiers()),
                names.fromString(spec.name()),
                typeTree(spec.returnType()),
                List.nil(),
                parameters(spec.parameters()),
                List.nil(),
                maker.Block(0, statements(spec.body())),
                null);
        decl.defs = decl.defs.append(method);
        return true;
    }

    @Override
    public boolean addConstructor(ConstructorSpec spec) {
        if (hasConstructor(spec.parameters().size())) {
            return false;
        }
        JCMethodDecl constructor = maker.MethodDef(
                modifiers(spec.modifiers()),
                names.fromString("<init>"),
                maker.TypeIdent(TypeTag.VOID),
                List.nil(),
                parameters(spec.parameters()),
                List.nil(),
                maker.Block(0, statements(spec.body())),
                null);
        decl.defs = decl.defs.append(constructor);
        return true;
    }

    @Override
    public boolean addField(FieldSpec spec) {
        if (hasField(spec.name())) {
            return false;
        }
        JCVariableDecl field = maker.VarDef(
                modifiers(spec.modifiers()),
                names.fromString(spec.name()),
                typeTree(spec.type()),
                spec.initializer() == null ? null : expression(spec.initializer()));
        decl.defs = decl.defs.append(field);
        return true;
    }

    /** 目标类里是否已有同名字段。 */
    public boolean hasField(String name) {
        for (JCTree member : decl.defs) {
            if (member instanceof JCVariableDecl variable && variable.name.contentEquals(name)) {
                return true;
            }
        }
        return false;
    }

    /** 目标类里是否已有同名同参的方法。 */
    public boolean hasMethod(String name, int arity) {
        for (JCTree member : decl.defs) {
            if (member instanceof JCMethodDecl method
                    && method.name.contentEquals(name)
                    && method.params != null
                    && method.params.size() == arity) {
                return true;
            }
        }
        return false;
    }

    /** 目标类里是否已有同参数的构造器。 */
    public boolean hasConstructor(int arity) {
        for (JCTree member : decl.defs) {
            if (member instanceof JCMethodDecl method
                    && method.name == names.init
                    && method.params != null
                    && method.params.size() == arity) {
                return true;
            }
        }
        return false;
    }

    /** 目标类的编译期诊断出口。 */
    public Diagnostics diagnostics() {
        return diagnostics;
    }

    /** 目标类型所属元素。 */
    public Element owner() {
        return owner;
    }

    // ==================== 修饰符 ====================

    private JCModifiers modifiers(Set<Modifier> mods) {
        long flags = 0;
        for (Modifier m : mods) {
            switch (m) {
                case PUBLIC:
                    flags |= Flags.PUBLIC;
                    break;
                case PROTECTED:
                    flags |= Flags.PROTECTED;
                    break;
                case PRIVATE:
                    flags |= Flags.PRIVATE;
                    break;
                case STATIC:
                    flags |= Flags.STATIC;
                    break;
                case FINAL:
                    flags |= Flags.FINAL;
                    break;
                case ABSTRACT:
                    flags |= Flags.ABSTRACT;
                    break;
                case DEFAULT:
                    flags |= Flags.DEFAULT;
                    break;
                case SYNCHRONIZED:
                    flags |= Flags.SYNCHRONIZED;
                    break;
                case VOLATILE:
                    flags |= Flags.VOLATILE;
                    break;
                case TRANSIENT:
                    flags |= Flags.TRANSIENT;
                    break;
                default:
                    break;
            }
        }
        return maker.Modifiers(flags);
    }

    // ==================== 类型 ====================

    private JCExpression typeTree(TypeRef ref) {
        if (ref instanceof TypeRef.OfField ofField) {
            JCVariableDecl field = findField(ofField.fieldName());
            if (field == null) {
                diagnostics.error(owner, "找不到字段：" + ofField.fieldName());
                return maker.TypeIdent(TypeTag.ERROR);
            }
            JCExpression copied = copier.copy(field.vartype);
            return copied == null ? field.vartype : copied;
        }
        if (ref instanceof TypeRef.Qualified qualified) {
            return qualifiedName(qualified.qualifiedName());
        }
        if (ref instanceof TypeRef.Primitive primitive) {
            return maker.TypeIdent(primitiveTag(primitive.keyword()));
        }
        if (ref instanceof TypeRef.Variable variable) {
            return maker.Ident(names.fromString(variable.name()));
        }
        return maker.TypeIdent(TypeTag.ERROR);
    }

    /** 目标类中同名的字段声明。 */
    public JCVariableDecl findField(String fieldName) {
        for (JCTree member : decl.defs) {
            if (member instanceof JCVariableDecl variable && variable.name.contentEquals(fieldName)) {
                return variable;
            }
        }
        return null;
    }

    private JCExpression qualifiedName(String qualifiedName) {
        String[] parts = qualifiedName.split("\\.");
        JCExpression expr = maker.Ident(names.fromString(parts[0]));
        for (int i = 1; i < parts.length; i++) {
            expr = maker.Select(expr, names.fromString(parts[i]));
        }
        return expr;
    }

    private TypeTag primitiveTag(String keyword) {
        switch (keyword) {
            case "void":
                return TypeTag.VOID;
            case "boolean":
                return TypeTag.BOOLEAN;
            case "byte":
                return TypeTag.BYTE;
            case "char":
                return TypeTag.CHAR;
            case "short":
                return TypeTag.SHORT;
            case "int":
                return TypeTag.INT;
            case "long":
                return TypeTag.LONG;
            case "float":
                return TypeTag.FLOAT;
            case "double":
                return TypeTag.DOUBLE;
            default:
                return TypeTag.ERROR;
        }
    }

    // ==================== 形参 ====================

    private List<JCVariableDecl> parameters(java.util.List<ParamSpec> params) {
        List<JCVariableDecl> result = List.nil();
        for (ParamSpec p : params) {
            result = result.append(maker.VarDef(
                    maker.Modifiers(Flags.PARAMETER),
                    names.fromString(p.name()),
                    typeTree(p.type()),
                    null));
        }
        return result;
    }

    // ==================== 语句 ====================

    private List<JCStatement> statements(java.util.List<Stmt> body) {
        List<JCStatement> result = List.nil();
        for (Stmt s : body) {
            JCStatement statement = statement(s);
            if (statement != null) {
                result = result.append(statement);
            }
        }
        return result;
    }

    private JCStatement statement(Stmt stmt) {
        if (stmt instanceof Stmt.Return r) {
            return maker.Return(expression(r.value()));
        }
        if (stmt instanceof Stmt.ExprStmt e) {
            return maker.Exec(expression(e.expression()));
        }
        if (stmt instanceof Stmt.VarDecl v) {
            return maker.VarDef(
                    maker.Modifiers(0),
                    names.fromString(v.name()),
                    typeTree(v.type()),
                    v.initializer() == null ? null : expression(v.initializer()));
        }
        if (stmt instanceof Stmt.Assign a) {
            return maker.Exec(maker.Assign(expression(a.target()), expression(a.value())));
        }
        if (stmt instanceof Stmt.Throw t) {
            return maker.Throw(expression(t.value()));
        }
        if (stmt instanceof Stmt.If i) {
            return maker.If(expression(i.condition()), statement(i.then()), statement(i.otherwise()));
        }
        if (stmt instanceof Stmt.Block b) {
            return maker.Block(0, statements(b.body()));
        }
        return null;
    }

    // ==================== 表达式 ====================

    private JCExpression expression(Expr expr) {
        if (expr instanceof Expr.ThisRef) {
            return maker.Ident(names.fromString("this"));
        }
        if (expr instanceof Expr.SuperRef) {
            return maker.Ident(names.fromString("super"));
        }
        if (expr instanceof Expr.Field f) {
            return maker.Select(expression(f.target()), names.fromString(f.name()));
        }
        if (expr instanceof Expr.Var v) {
            return maker.Ident(names.fromString(v.name()));
        }
        if (expr instanceof Expr.Literal l) {
            return literal(l.value());
        }
        if (expr instanceof Expr.NullLiteral) {
            return maker.Literal(TypeTag.BOT, null);
        }
        if (expr instanceof Expr.Call c) {
            List<JCExpression> args = List.nil();
            for (Expr a : c.arguments()) {
                args = args.append(expression(a));
            }
            JCExpression method = maker.Select(expression(c.target()), names.fromString(c.method()));
            return maker.Apply(List.nil(), method, args);
        }
        if (expr instanceof Expr.TypeName t) {
            return qualifiedName(t.qualifiedName());
        }
        if (expr instanceof Expr.Binary b) {
            return maker.Binary(operatorTag(b.operator()), expression(b.left()), expression(b.right()));
        }
        if (expr instanceof Expr.Unary u) {
            return maker.Unary(operatorTag(u.operator()), expression(u.operand()));
        }
        if (expr instanceof Expr.Cast c) {
            return maker.TypeCast(typeTree(c.type()), expression(c.value()));
        }
        if (expr instanceof Expr.InstanceOf i) {
            return maker.TypeTest(expression(i.value()), typeTree(i.type()));
        }
        if (expr instanceof Expr.New n) {
            List<JCExpression> args = List.nil();
            for (Expr a : n.arguments()) {
                args = args.append(expression(a));
            }
            return maker.NewClass(null, List.nil(), typeTree(n.type()), args, null);
        }
        if (expr instanceof Expr.ClassLiteral c) {
            return classLiteral(c.type());
        }
        if (expr instanceof Expr.ArrayOf a) {
            List<JCExpression> elements = List.nil();
            for (Expr e : a.elements()) {
                elements = elements.append(expression(e));
            }
            return maker.NewArray(typeTree(a.componentType()), List.nil(), elements);
        }
        if (expr instanceof Expr.Conditional c) {
            return maker.Conditional(expression(c.condition()),
                    expression(c.whenTrue()), expression(c.whenFalse()));
        }
        return maker.Literal(TypeTag.ERROR, null);
    }

    private JCExpression literal(Object value) {
        return maker.Literal(value);
    }

    private JCExpression classLiteral(TypeRef type) {
        if (type instanceof TypeRef.OfField ofField) {
            JCVariableDecl field = findField(ofField.fieldName());
            if (field != null && field.sym != null) {
                return maker.ClassLiteral(field.sym.type);
            }
        }
        return maker.ClassLiteral(decl.sym);
    }

    private JCTree.Tag operatorTag(String operator) {
        switch (operator) {
            case "==":
                return JCTree.Tag.EQ;
            case "!=":
                return JCTree.Tag.NE;
            case "<":
                return JCTree.Tag.LT;
            case ">":
                return JCTree.Tag.GT;
            case "<=":
                return JCTree.Tag.LE;
            case ">=":
                return JCTree.Tag.GE;
            case "&&":
                return JCTree.Tag.AND;
            case "||":
                return JCTree.Tag.OR;
            case "+":
                return JCTree.Tag.PLUS;
            case "-":
                return JCTree.Tag.MINUS;
            case "*":
                return JCTree.Tag.MUL;
            case "/":
                return JCTree.Tag.DIV;
            case "%":
                return JCTree.Tag.MOD;
            case "!":
                return JCTree.Tag.NOT;
            default:
                return JCTree.Tag.NO_TAG;
        }
    }
}
