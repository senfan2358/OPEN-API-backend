package com.senfan.project.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.senfan.senfanapicommon.model.entity.UserInterfaceInfo;

import java.util.List;

/**
 * @Entity com.senfan.project.model.entity.UserInterfaceInfo
 */
public interface UserInterfaceInfoMapper extends BaseMapper<UserInterfaceInfo> {
    List<UserInterfaceInfo> listTopInvokeInterfaceInfo(int limit);

}




