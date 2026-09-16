package com.zifang.util.db.test0;


import com.zifang.util.db.define.Param;
import com.zifang.util.db.define.Select;
import com.zifang.util.db.respository.CrudRepository;

import java.util.List;
import java.util.Map;

/**
 * ResourceItemRepository接口。
 */
public interface ResourceItemRepository extends CrudRepository<ResourceItem, Long> {

    @Select("select * from resource_item where cms_id = :cmsId and shared = :shared")
    List<ResourceItem> findByNameList(@Param("cmsId") String cmsId, @Param("shared") Integer shared);

    @Select("select * from resource_item where cms_id = :cmsId and shared = :shared")
    List<Map<String, Object>> findByNameListMap(@Param("cmsId") String cmsId, @Param("shared") Integer shared);

    @Select("select * from resource_item where cms_id = :cmsId and shared = :shared")
    ResourceItem findByNameBean(@Param("cmsId") String cmsId, @Param("shared") Integer shared);

    @Select("select * from resource_item where cms_id = :cmsId and shared = :shared")
    Map<String, Object> findByNameMap(@Param("cmsId") String cmsId, @Param("shared") Integer shared);
}
