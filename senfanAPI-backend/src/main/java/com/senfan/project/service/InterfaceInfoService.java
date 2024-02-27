package com.senfan.project.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.senfan.project.model.dto.interfaceInfo.InterfaceInfoInvokeRequest;
import com.senfan.senfanapicommon.model.entity.InterfaceInfo;

import javax.servlet.http.HttpServletRequest;

/**
 *
 */
public interface InterfaceInfoService extends IService<InterfaceInfo> {
    void validInterfaceInfo(InterfaceInfo interfaceInfo, boolean add);

    String getInvokeResult(InterfaceInfoInvokeRequest interfaceInfoInvokeRequest, HttpServletRequest request, InterfaceInfo oldInterfaceInfo);

    /**
     * 根据用户id查看用户开通的接口
     *
     * @param current
     * @param pageSize
     * @param userId
     * @return
     */
    Page<InterfaceInfo> listInterfaceInfoByUserId(long current, long pageSize, long userId);
}
