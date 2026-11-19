package com.zifang.util.codegen.it;

import com.zifang.util.codegen.it.mapper.ProfileMapperImpl;
import com.zifang.util.codegen.it.mapper.UserMapperImpl;
import com.zifang.util.codegen.it.model.AddressDTO;
import com.zifang.util.codegen.it.model.AddressEntity;
import com.zifang.util.codegen.it.model.UserDTO;
import com.zifang.util.codegen.it.model.UserEntity;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * MapStruct 性质转换集的集成测试：断言编译期生成的转换实现真的能跑。
 */
class MapperFeatureTest {

    @Test
    void mapsSameNamedProperties() {
        UserEntity entity = new UserEntity();
        entity.setId("U-1");
        entity.setName("zifang");

        UserDTO dto = new UserMapperImpl().toDTO(entity);

        assertNotNull(dto);
        assertEquals("U-1", dto.getId());
        assertEquals("zifang", dto.getName());
    }

    @Test
    void returnsNullForNullSource() {
        assertNull(new UserMapperImpl().toDTO(null));
    }

    @Test
    void mapsNestedObjectThroughNestedMapper() {
        AddressEntity address = new AddressEntity();
        address.setCity("杭州");

        UserEntity entity = new UserEntity();
        entity.setId("U-2");
        entity.setAddress(address);

        UserDTO dto = new UserMapperImpl().toDTO(entity);

        assertNotNull(dto.getAddress());
        assertEquals("杭州", dto.getAddress().getCity());
    }

    @Test
    void honoursExplicitMappingRules() {
        UserEntity entity = new UserEntity();
        entity.setId("U-3");
        entity.setName("zifang");

        UserDTO dto = new ProfileMapperImpl().toDTO(entity);

        assertEquals("ZIFANG", dto.getName());
        assertNull(dto.getId());
    }
}
