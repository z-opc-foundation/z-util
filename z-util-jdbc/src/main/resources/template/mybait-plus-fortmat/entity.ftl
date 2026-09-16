package ${basePackage}.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.math.BigDecimal;

/**
* ${comment!"无注释"}
*/
@TableName("${tableName}")
public class ${entityName} implements Serializable {

private static final long serialVersionUID = 1L;
<#list columns as column >

    /**
    *${column.comment!"无注释"}
    */
    <#if column.primaryKey?? && column.primaryKey>
        @TableId(type = IdType.AUTO)
    </#if>
    private ${column.javaType} ${column.javaFieldName};
</#list>
}
