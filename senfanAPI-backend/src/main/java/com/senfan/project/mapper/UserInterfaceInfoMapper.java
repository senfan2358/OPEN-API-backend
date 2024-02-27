package com.senfan.project.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.senfan.project.model.dto.userInterfaceInfo.UserInterfaceInfoInvokeRequest;
import com.senfan.project.model.vo.UserInterfaceInfoInvokeVO;
import com.senfan.senfanapicommon.model.entity.UserInterfaceInfo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Entity com.senfan.project.model.entity.UserInterfaceInfo
 */
public interface UserInterfaceInfoMapper extends BaseMapper<UserInterfaceInfo> {
    List<UserInterfaceInfo> listTopInvokeInterfaceInfo(@Param("limit") int limit);

    List<UserInterfaceInfoInvokeVO> listInterfaceInvokeByPage(UserInterfaceInfoInvokeRequest userInterfaceInfoInvokeRequest);
}




