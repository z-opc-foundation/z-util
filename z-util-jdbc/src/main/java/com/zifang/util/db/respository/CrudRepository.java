package com.zifang.util.db.respository;

import java.util.Optional;

/**
 * 基本的增删改查接口，定义常用的数据访问操作
 *
 * @param <T>  实体类型
 * @param <ID> 主键类型
 */
public interface CrudRepository<T, ID> extends Repository<T, ID> {

    /**
     * 保存或更新实体
     *
     * @param entity 要保存的实体
     * @param <S>    实体类型
     * @return 保存后的实体
     */
    <S extends T> S save(S entity);

    /**
     * 批量保存或更新实体
     *
     * @param entities 要保存的实体集合
     * @param <S>      实体类型
     * @return 保存后的实体集合
     */
    <S extends T> Iterable<S> saveAll(Iterable<S> entities);

    /**
     * 根据主键查找实体
     *
     * @param id 主键
     * @return 实体Optional对象
     */
    Optional<T> findById(ID id);

    /**
     * 判断指定主键的实体是否存在
     *
     * @param id 主键
     * @return 是否存在
     */
    boolean existsById(ID id);

    /**
     * 查询所有实体
     *
     * @return 所有实体
     */
    Iterable<T> findAll();

    /**
     * 根据主键集合查询所有实体
     *
     * @param ids 主键集合
     * @return 实体集合
     */
    Iterable<T> findAllById(Iterable<ID> ids);

    /**
     * 统计实体总数
     *
     * @return 实体数量
     */
    long count();

    /**
     * 根据主键删除实体
     *
     * @param id 主键
     */
    void deleteById(ID id);

    /**
     * 删除指定实体
     *
     * @param entity 要删除的实体
     */
    void delete(T entity);

    /**
     * 批量删除实体
     *
     * @param entities 要删除的实体集合
     */
    void deleteAll(Iterable<? extends T> entities);

    /**
     * 删除所有实体
     */
    void deleteAll();

}
