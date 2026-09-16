package com.zifang.util.expr.groovy;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;

import java.util.HashMap;
import java.util.Map;

/**
 * Groovy script executor that evaluates Groovy expressions and scripts.
 */
public class GroovyExecutor {

    private Binding binding;
    private GroovyShell shell;

    public GroovyExecutor() {
        this.binding = new Binding();
        this.shell = new GroovyShell(binding);
    }

    /**
     * Evaluate a Groovy script and return the result.
     *
     * @param script the Groovy script to execute
     * @return the result of the script evaluation
     * @throws GroovyException if the script execution fails
     */
    public Object eval(String script) {
        try {
            return shell.evaluate(script);
        } catch (Exception e) {
            throw new GroovyException("Failed to evaluate Groovy script: " + e.getMessage(), e);
        }
    }

    /**
     * Set a variable in the binding scope.
     *
     * @param name  the variable name
     * @param value the variable value
     */
    public void setVariable(String name, Object value) {
        binding.setVariable(name, value);
    }

    /**
     * Get a variable from the binding scope.
     *
     * @param name the variable name
     * @return the variable value
     */
    public Object getVariable(String name) {
        return binding.getVariable(name);
    }

    /**
     * Get all variables from the binding scope.
     *
     * @return a map of all bound variables
     */
    public Map<String, Object> getVariables() {
        Map<String, Object> result = new HashMap<>();
        for (Object nameObj : binding.getVariables().keySet()) {
            String name = nameObj.toString();
            result.put(name, binding.getVariable(name));
        }
        return result;
    }

    /**
     * Set multiple variables in the binding scope.
     *
     * @param variables a map of variable names to values
     */
    public void setVariables(Map<String, Object> variables) {
        if (variables != null) {
            for (Map.Entry<String, Object> entry : variables.entrySet()) {
                binding.setVariable(entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * Parse a script and return the compiled Script object without executing it.
     *
     * @param script the Groovy script to parse
     * @return the parsed Script object
     * @throws GroovyException if parsing fails
     */
    public Script parse(String script) {
        try {
            return shell.parse(script);
        } catch (Exception e) {
            throw new GroovyException("Failed to parse Groovy script: " + e.getMessage(), e);
        }
    }

    /**
     * Evaluate a script with a specific set of binding variables.
     *
     * @param script    the Groovy script to execute
     * @param variables the binding variables to set before execution
     * @return the result of the script evaluation
     * @throws GroovyException if the script execution fails
     */
    public Object eval(String script, Map<String, Object> variables) {
        Map<String, Object> originalVariables = new HashMap<>(getVariables());
        try {
            if (variables != null) {
                setVariables(variables);
            }
            return eval(script);
        } finally {
            clearVariables();
            setVariables(originalVariables);
        }
    }

    /**
     * Clear all variables from the binding scope.
     */
    public void clearVariables() {
        for (Object nameObj : binding.getVariables().keySet()) {
            binding.setVariable(nameObj.toString(), null);
        }
    }
}
