package com.example.todoapp;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
@Mapper
public interface ApiKeyMapper {
    int insert(@Param("username") String username, @Param("keyHash") String keyHash, @Param("name") String name);
    ApiKey findActiveByHash(@Param("keyHash") String keyHash);
    int updateLastUsedAt(@Param("id") Long id);
    int revoke(@Param("id") Long id, @Param("username") String username);
    List<ApiKey> findByUsername(@Param("username") String username);
}
