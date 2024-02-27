package com.senfan.senfanapicommon.service;

/**
 * 内部用户接口信息服务
 */
public interface InnerUserInterfaceInfoService {

    /**
     * 调用接口统计
     * @param interfaceInfoId
     * @param userId
     * @return
     */
    boolean invokeCount(long interfaceInfoId, long userId);

    /**
     * 是否有权限调用接口
     * @param userId
     * @param interfaceInfoId
     * @return
     */
    boolean isHaveInvokeCount(long userId, long interfaceInfoId);

    boolean test();
}
