package com.zifang.util.validation.core;

import java.util.*;

/**
 * 校验结果收集器
 */
public class ValidateResult {

    private final Map<String, List<String>> errors = new HashMap<>();

    /**
     * 添加错误信息
     */
    public void addError(String fieldName, String message) {
        errors.computeIfAbsent(fieldName, k -> new ArrayList<>()).add(message);
    }

    /**
     * 判断是否有错误
     */
    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    /**
     * 获取所有错误信息
     */
    public Map<String, List<String>> getErrors() {
        return Collections.unmodifiableMap(errors);
    }

    /**
     * 获取第一个错误信息
     */
    public Optional<String> getFirstError() {
        if (errors.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(errors.values().iterator().next().get(0));
    }

    @Override
    /**
     * toString方法。
     * @return String类型返回值
     */
    public String toString() {
        if (errors.isEmpty()) {
            return "ValidateResult{valid=true}";
        }
        StringBuilder sb = new StringBuilder("ValidateResult{valid=false, errors=");
        sb.append(errors);
        sb.append("}");
        return sb.toString();
    }
}