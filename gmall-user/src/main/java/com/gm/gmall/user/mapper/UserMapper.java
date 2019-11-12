package com.gm.gmall.user.mapper;

import com.gm.gmall.bean.UmsMember;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface UserMapper extends Mapper<UmsMember> {
     List<UmsMember> selectAllUser();

//     UmsMember getUmsMemberById(String id);
//
//     int deleteUmsMemberById(String id);
//
//     int insertUmsMember(UmsMember umsMember);
//
//     int updateUmsMember(UmsMember umsMember);
}
