package com.zifang.util.core.lang.dynamic;

import java.util.ArrayList;
import java.util.List;

/**
 * 动态类单元，包含主类和子类列表。
 * <p>
 * 用于组织具有层级关系的动态类结构。
 *
 * @author zifang
 * @see DynamicClass
 */
public class DynamicClassUnit {

    private DynamicClass main;

    private List<DynamicClassUnit> sub = new ArrayList<>();

    /**
     * getMain方法。
     *
     * @return DynamicClass类型返回值
     */
    public DynamicClass getMain() {
        return main;
    }

    /**
     * setMain方法。
     * * @param main DynamicClass类型参数
     */
    public void setMain(DynamicClass main) {
        this.main = main;
    }

    /**
     * getSub方法。
     *
     * @return List<DynamicClassUnit>类型返回值
     */
    public List<DynamicClassUnit> getSub() {
        return sub;
    }

    /**
     * setSub方法。
     * * @param sub ListDynamicClassUnit类型参数
     */
    public void setSub(List<DynamicClassUnit> sub) {
        this.sub = sub;
    }

    /**
     * collect方法。
     *
     * @return List<DynamicClass>类型返回值
     */
    public List<DynamicClass> collect() {
        List<DynamicClass> dynamicClasses = new ArrayList<>();
        dynamicClasses.add(main);
        for (DynamicClassUnit dynamicClassUnit : sub) {
            dynamicClasses.addAll(dynamicClassUnit.collect());
        }
        return dynamicClasses;
    }
}
