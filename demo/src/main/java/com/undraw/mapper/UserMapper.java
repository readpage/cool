package com.undraw.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.undraw.domain.entity.User;
import org.apache.ibatis.annotations.Select;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author readpage
 * @since 2023-03-15 18:00
 */
public interface UserMapper extends BaseMapper<User> {
    @Select("SELECT * FROM user1")
    void badSql();

}
