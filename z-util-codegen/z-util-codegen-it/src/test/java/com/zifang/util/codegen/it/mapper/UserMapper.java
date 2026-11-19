package com.zifang.util.codegen.it.mapper;

import com.zifang.util.codegen.annotation.ZMapper;
import com.zifang.util.codegen.it.model.UserDTO;
import com.zifang.util.codegen.it.model.UserEntity;

/**
 * 转换接口测试夹具。
 */
@ZMapper
public interface UserMapper {

    UserDTO toDTO(UserEntity entity);
}
