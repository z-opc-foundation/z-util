package ${basePackage}.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import ${basePackage}.entity.${table.entityName};
import ${basePackage}.mapper.${table.entityName}Mapper;
import ${basePackage}.service.I${table.entityName}Service;

/**
* ${table.comment!"无注释"} 服务实现类
* @author auto-generated
*/
@Service
public class ${table.entityName}ServiceImpl extends ServiceImpl
<${table.entityName}Mapper, ${table.entityName}> implements I${table.entityName}Service {
}