package com.zifang.util.codegen.it.mapper;

import com.zifang.util.codegen.annotation.ZMapper;
import com.zifang.util.codegen.it.model.AddressDTO;
import com.zifang.util.codegen.it.model.AddressEntity;

/**
 * 嵌套转换接口测试夹具。
 */
@ZMapper
public interface AddressMapper {

    AddressDTO toDTO(AddressEntity entity);
}
