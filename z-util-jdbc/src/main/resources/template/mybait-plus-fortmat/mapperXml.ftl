<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="${basePackage}.mapper.${entityName}Mapper">

    <!-- 基础结果集映射 -->
    <resultMap id="BaseResultMap" type="${basePackage}.entity.${entityName}">
        <#list columns as column>
            <#if column.primaryKey>
                <id column="${column.columnName}" property="${column.javaFieldName}"/>
            <#else>
                <result column="${column.columnName}" property="${column.javaFieldName}"/>
            </#if>
        </#list>
    </resultMap>

    <!-- 基础查询字段 -->
    <sql id="Base_Column_List">
        <#list columns as column>
            ${column.columnName}<#if column_has_next>,</#if>
        </#list>
    </sql>

    <!-- 新增语句 -->
    <insert id="insert" parameterType="${basePackage}.entity.${entityName}">
        INSERT INTO ${tableName} (
        <#list columns as column>
            <#if !column.primaryKey>
                ${column.columnName}<#if column_has_next>,</#if>
            </#if>
        </#list>
        ) VALUES (
        <#list columns as column>
            <#if !column.primaryKey>
                ${column.javaFieldName}<#if column_has_next>,</#if>
            </#if>
        </#list>
        )
    </insert>

    <!-- 根据ID查询 -->
    <select id="selectById" resultMap="BaseResultMap">
        SELECT
        <include refid="Base_Column_List"/>
        FROM ${tableName}
        WHERE
        <#list columns as column>
            <#if column.primaryKey>
                ${column.columnName} = ${column.javaFieldName}
            </#if>
        </#list>
    </select>

    <!-- 更新语句 -->
    <update id="updateById" parameterType="${basePackage}.entity.${entityName}">
        UPDATE ${tableName}
        SET
        <#list columns as column>
            <#if !column.primaryKey>
                ${column.columnName} = ${column.javaFieldName}<#if column_has_next>,</#if>
            </#if>
        </#list>
        WHERE
        <#list columns as column>
            <#if column.primaryKey>
                ${column.columnName} = ${column.javaFieldName}
            </#if>
        </#list>
    </update>

    <!-- 删除语句 -->
    <delete id="deleteById">
        DELETE FROM ${tableName}
        WHERE
        <#list columns as column>
            <#if column.primaryKey>
                ${column.columnName} = ${column.javaFieldName}
            </#if>
        </#list>
    </delete>

</mapper>