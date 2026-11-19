package com.zifang.util.codegen.it.mapper;

import com.zifang.util.codegen.annotation.ZMapper;
import com.zifang.util.codegen.annotation.ZMapping;
import com.zifang.util.codegen.it.model.UserDTO;
import com.zifang.util.codegen.it.model.UserEntity;

/**
 * 带定制映射规则的转换接口测试夹具。
 */
@ZMapper
public interface ProfileMapper {

    @ZMapping(source = "name", target = "name", expression = "java(src.getName().toUpperCase())")
    @ZMapping(target = "id", ignore = true)
    UserDTO toDTO(UserEntity entity);
}
