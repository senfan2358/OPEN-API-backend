package com.senfan.project.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.senfan.project.mapper.InterfaceInfoMapper;
import com.senfan.project.mapper.UserInterfaceInfoMapper;
import com.senfan.project.model.dto.interfaceInfo.InterfaceInfoInvokeRequest;
import com.senfan.project.model.enums.InterfaceInfoStatusEnum;
import com.senfan.project.service.InterfaceInfoService;
import com.senfan.project.service.UserService;
import com.senfan.senfanapiclientsdk.client.SenfanAPIClient;
import com.senfan.senfanapicommon.common.ErrorCode;
import com.senfan.senfanapicommon.exception.BusinessException;
import com.senfan.senfanapicommon.model.entity.InterfaceInfo;
import com.senfan.senfanapicommon.model.entity.UserInterfaceInfo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 */
@Service
public class InterfaceInfoServiceImpl extends ServiceImpl<InterfaceInfoMapper, InterfaceInfo>
        implements InterfaceInfoService {
    @Resource
    UserService userService;
    @Resource
    InterfaceInfoMapper interfaceInfoMapper;
    @Resource
    UserInterfaceInfoMapper userInterfaceInfoMapper;
    /**
     * @param interfaceInfo
     * @param add           用户判断此次校验为添加 还是 修改等操作
     */
    @Override
    public void validInterfaceInfo(InterfaceInfo interfaceInfo, boolean add) {
        if (interfaceInfo == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String name = interfaceInfo.getName();
        String description = interfaceInfo.getDescription();
        String url = interfaceInfo.getUrl();
        String requestHeader = interfaceInfo.getRequestHeader();
        String responseHeader = interfaceInfo.getResponseHeader();
        String method = interfaceInfo.getMethod();
        // 创建时，所有参数必须非空
        if (add) {
            if (StringUtils.isAnyBlank(name, description, url, method)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR);
            }
        }
    }

    @Override
    public String getInvokeResult(InterfaceInfoInvokeRequest interfaceInfoInvokeRequest, HttpServletRequest request, InterfaceInfo oldInterfaceInfo) {
        Long id = oldInterfaceInfo.getId();
        String url = oldInterfaceInfo.getUrl();
        String method = oldInterfaceInfo.getMethod();
        String path = oldInterfaceInfo.getPath();
        String requestParams = interfaceInfoInvokeRequest.getUserRequestParams();

        SenfanAPIClient senfanAPIClient = userService.getSenfanAPIClient(request);
        String invokeResult = null;
        try {
            // 执行方法
            invokeResult = senfanAPIClient.invokeInterface(id, requestParams, url, method, path);

        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "接口调用失败");
        }
        // 走到下面，接口肯定调用成功了
        // 如果调用出现了接口内部异常或者路径错误，需要下线接口（网关已经将异常结果统一处理了）
        if (StrUtil.isBlank(invokeResult)) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "接口返回值为空");
        } else {
            // 下线接口
            // InterfaceInfo interfaceInfo = new InterfaceInfo();
            // interfaceInfo.setId(id);
            // interfaceInfo.setStatus(InterfaceInfoStatusEnum.OFFLINE.getValue());
            // interfaceInfoMapper.updateById(interfaceInfo);
        }
        return invokeResult;
    }
    @Override
    public Page<InterfaceInfo> listInterfaceInfoByUserId(long current, long pageSize, long userId) {
        // 1. 查询用户接口信息表，获取用户开通的接口
        QueryWrapper<UserInterfaceInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userId);
        List<UserInterfaceInfo> list = userInterfaceInfoMapper.selectList(queryWrapper);
        List<Long> interfaceInfoIds = list.stream().map(item -> item.getInterfaceInfoId()).collect(Collectors.toList());
        // 2. 根据获取到的接口id，查询接口信息表获取接口
        QueryWrapper<InterfaceInfo> interfaceInfoQueryWrapper = new QueryWrapper<>();
        interfaceInfoQueryWrapper.in("id",interfaceInfoIds);
        Page<InterfaceInfo> result = interfaceInfoMapper.selectPage(new Page<>(current, pageSize), interfaceInfoQueryWrapper);
        return result;
    }
}




