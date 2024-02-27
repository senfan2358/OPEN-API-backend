package com.senfan.project.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.senfan.senfanapicommon.model.entity.UserInterfaceInfo;

/**
 *
 */
public interface UserInterfaceInfoService extends IService<UserInterfaceInfo> {

    void validUserInterfaceInfo(UserInterfaceInfo userInterfaceInfo, boolean add);

    /**
     * 接口调用统计
     *
     * @param interfaceInfoId
     * @param userId
     * @return
     */
    boolean invokeCount(long interfaceInfoId, long userId);

    /**
     * 开通接口
     * @param userId
     * @param interfaceInfoId
     * @return
     */
    boolean openInterfaceInfo(Long userId, Long interfaceInfoId);

    /**
     * 校验是否有接口调用权限
     * @param userId
     * @param interfaceInfoId
     * @return
     */
    boolean isHaveInvokeCount(long userId,long interfaceInfoId);
}
