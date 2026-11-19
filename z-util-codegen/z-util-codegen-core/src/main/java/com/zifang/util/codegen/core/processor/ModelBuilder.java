package com.zifang.util.codegen.core.processor;

import com.zifang.util.codegen.core.model.AnnotationSpec;
import com.zifang.util.codegen.core.model.FieldModel;
import com.zifang.util.codegen.core.model.MethodModel;
import com.zifang.util.codegen.core.model.Modifier;
import com.zifang.util.codegen.core.model.ParamModel;
import com.zifang.util.codegen.core.model.TypeKind;
import com.zifang.util.codegen.core.model.TypeModel;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 把 {@code javax.lang.model} 侧的 {@link TypeElement} 摊平成 {@link TypeModel}。
 *
 * @author zifang
 */
final class ModelBuilder {

    private final Elements elements;
    private final Types types;

    ModelBuilder(Elements elements, Types types) {
        this.elements = elements;
        this.types = types;
    }

    TypeModel build(TypeElement type) {
        String qualifiedName = type.getQualifiedName().toString();
        String simpleName = type.getSimpleName().toString();
        String packageName = elements.getPackageOf(type).getQualifiedName().toString();

        List<FieldModel> fields = new ArrayList<>();
        List<MethodModel> methods = new ArrayList<>();
        for (Element enclosed : type.getEnclosedElements()) {
            if (enclosed.getKind() == ElementKind.FIELD) {
                fields.add(buildField((VariableElement) enclosed, qualifiedName));
            } else if (enclosed.getKind() == ElementKind.METHOD) {
                methods.add(buildMethod((ExecutableElement) enclosed));
            }
        }

        List<String> interfaces = new ArrayList<>();
        for (javax.lang.model.type.TypeMirror i : type.getInterfaces()) {
            interfaces.add(i.toString());
        }

        List<String> typeParameters = new ArrayList<>();
        for (TypeParameterElement tp : type.getTypeParameters()) {
            typeParameters.add(tp.getSimpleName().toString());
        }

        Element enclosing = type.getEnclosingElement();
        String enclosingTypeName = enclosing instanceof TypeElement
                ? ((TypeElement) enclosing).getQualifiedName().toString()
                : null;

        return new TypeModel(
                qualifiedName,
                simpleName,
                packageName,
                kindOf(type),
                type.getSuperclass().toString(),
                interfaces,
                typeParameters,
                fields,
                methods,
                modifiersOf(type.getModifiers()),
                annotationsOf(type),
                enclosingTypeName);
    }

    private TypeKind kindOf(TypeElement type) {
        switch (type.getKind()) {
            case INTERFACE:
                return TypeKind.INTERFACE;
            case ENUM:
                return TypeKind.ENUM;
            case ANNOTATION_TYPE:
                return TypeKind.ANNOTATION;
            case RECORD:
                return TypeKind.RECORD;
            default:
                return TypeKind.CLASS;
        }
    }

    private FieldModel buildField(VariableElement field, String declaringTypeName) {
        return new FieldModel(
                field.getSimpleName().toString(),
                field.asType().toString(),
                modifiersOf(field.getModifiers()),
                annotationsOf(field),
                declaringTypeName);
    }

    private MethodModel buildMethod(ExecutableElement method) {
        List<ParamModel> parameters = new ArrayList<>();
        for (VariableElement p : method.getParameters()) {
            parameters.add(new ParamModel(
                    p.getSimpleName().toString(),
                    p.asType().toString(),
                    annotationsOf(p)));
        }
        List<String> typeParameters = new ArrayList<>();
        for (TypeParameterElement tp : method.getTypeParameters()) {
            typeParameters.add(tp.getSimpleName().toString());
        }
        List<String> thrown = new ArrayList<>();
        for (javax.lang.model.type.TypeMirror t : method.getThrownTypes()) {
            thrown.add(t.toString());
        }
        return new MethodModel(
                method.getSimpleName().toString(),
                method.getReturnType().toString(),
                parameters,
                modifiersOf(method.getModifiers()),
                typeParameters,
                thrown,
                annotationsOf(method));
    }

    private Set<Modifier> modifiersOf(Set<? extends javax.lang.model.element.Modifier> jdkModifiers) {
        Set<Modifier> result = EnumSet.noneOf(Modifier.class);
        for (javax.lang.model.element.Modifier m : jdkModifiers) {
            switch (m) {
                case PUBLIC:
                    result.add(Modifier.PUBLIC);
                    break;
                case PROTECTED:
                    result.add(Modifier.PROTECTED);
                    break;
                case PRIVATE:
                    result.add(Modifier.PRIVATE);
                    break;
                case STATIC:
                    result.add(Modifier.STATIC);
                    break;
                case FINAL:
                    result.add(Modifier.FINAL);
                    break;
                case ABSTRACT:
                    result.add(Modifier.ABSTRACT);
                    break;
                case DEFAULT:
                    result.add(Modifier.DEFAULT);
                    break;
                case SYNCHRONIZED:
                    result.add(Modifier.SYNCHRONIZED);
                    break;
                case VOLATILE:
                    result.add(Modifier.VOLATILE);
                    break;
                case TRANSIENT:
                    result.add(Modifier.TRANSIENT);
                    break;
                default:
                    break;
            }
        }
        return result;
    }

    List<AnnotationSpec> annotationsOf(Element element) {
        List<AnnotationSpec> specs = new ArrayList<>();
        for (AnnotationMirror mirror : element.getAnnotationMirrors()) {
            specs.add(annotationOf(mirror));
        }
        return specs;
    }

    private AnnotationSpec annotationOf(AnnotationMirror mirror) {
        String name = ((TypeElement) mirror.getAnnotationType().asElement()).getQualifiedName().toString();
        Map<String, Object> values = new LinkedHashMap<>();
        for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry
                : elements.getElementValuesWithDefaults(mirror).entrySet()) {
            values.put(entry.getKey().getSimpleName().toString(), unwrap(entry.getValue()));
        }
        return new AnnotationSpec(name, values);
    }

    private Object unwrap(AnnotationValue value) {
        Object raw = value.getValue();
        if (raw instanceof List<?> list) {
            List<Object> result = new ArrayList<>(list.size());
            for (Object item : list) {
                result.add(item instanceof AnnotationValue av ? unwrap(av) : item);
            }
            return result;
        }
        if (raw instanceof AnnotationMirror nested) {
            // javac 的 Attribute.Compound 自身就实现 AnnotationMirror，嵌套注解走这里。
            return annotationOf(nested);
        }
        if (raw instanceof VariableElement enumConstant) {
            return enumConstant.getSimpleName().toString();
        }
        if (raw instanceof javax.lang.model.type.TypeMirror type) {
            return type.toString();
        }
        return raw;
    }
}
