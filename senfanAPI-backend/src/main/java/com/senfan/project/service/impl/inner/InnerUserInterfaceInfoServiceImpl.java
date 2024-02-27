package com.senfan.project.service.impl.inner;

import com.senfan.project.service.UserInterfaceInfoService;
import com.senfan.senfanapicommon.service.InnerUserInterfaceInfoService;
import org.apache.dubbo.config.annotation.DubboService;

import javax.annotation.Resource;

/**
 * 内部用户接口信息服务实现类
 *
 */
@DubboService
public class InnerUserInterfaceInfoServiceImpl implements InnerUserInterfaceInfoService {

    @Resource
    private UserInterfaceInfoService userInterfaceInfoService;

    @Override
    public boolean invokeCount(long interfaceInfoId, long userId) {
        return userInterfaceInfoService.invokeCount(interfaceInfoId, userId);
    }

    @Override
    public boolean isHaveInvokeCount(long userId, long interfaceInfoId) {
        return userInterfaceInfoService.isHaveInvokeCount(userId,interfaceInfoId);
    }

    @Override
    public boolean test() {
        return false;
    }
}
