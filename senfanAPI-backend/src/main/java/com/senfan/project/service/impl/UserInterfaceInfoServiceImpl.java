package com.senfan.project.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.senfan.project.mapper.InterfaceInfoMapper;
import com.senfan.project.mapper.UserInterfaceInfoMapper;
import com.senfan.project.mapper.UserMapper;
import com.senfan.project.model.dto.userInterfaceInfo.UserInterfaceInfoInvokeRequest;
import com.senfan.project.model.vo.UserInterfaceInfoInvokeVO;
import com.senfan.project.service.UserInterfaceInfoService;
import com.senfan.project.service.UserService;
import com.senfan.senfanapicommon.common.ErrorCode;
import com.senfan.senfanapicommon.constant.CommonConstant;
import com.senfan.senfanapicommon.constant.UserConstant;
import com.senfan.senfanapicommon.exception.BusinessException;
import com.senfan.senfanapicommon.model.entity.InterfaceInfo;
import com.senfan.senfanapicommon.model.entity.User;
import com.senfan.senfanapicommon.model.entity.UserInterfaceInfo;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 *
 */
@Service
public class UserInterfaceInfoServiceImpl extends ServiceImpl<UserInterfaceInfoMapper, UserInterfaceInfo>
        implements UserInterfaceInfoService {
    private static final int INVOKE_COUNT = 200;
    @Resource
    private RedissonClient redissonClient;
    @Resource
    private UserInterfaceInfoMapper userInterfaceInfoMapper;
    @Resource
    private InterfaceInfoMapper interfaceInfoMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private UserService userService;
    @Override
    public void validUserInterfaceInfo(UserInterfaceInfo userInterfaceInfo, boolean add) {
        if (userInterfaceInfo == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long id = userInterfaceInfo.getId();
        Long userId = userInterfaceInfo.getUserId();
        Long interfaceInfoId = userInterfaceInfo.getInterfaceInfoId();
        Integer totalNum = userInterfaceInfo.getTotalNum();
        Integer leftNum = userInterfaceInfo.getLeftNum();
        Integer status = userInterfaceInfo.getStatus();
        // 创建时，所有参数必须非空
        if (add) {
            // if (StringUtils.isAnyBlank(name,description,url,requestHeader,responseHeader,method)) {
            //     throw new BusinessException(ErrorCode.PARAMS_ERROR);
            // }
        } else {
            // 更新时


        }
    }

    @Override
    public boolean invokeCount(long interfaceInfoId, long userId) {
        if (interfaceInfoId <= 0 || userId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 只有一个线程能获取到锁
        RLock lock = redissonClient.getLock("senfan:invokeCount:" + interfaceInfoId);
        UpdateWrapper<UserInterfaceInfo> updateQuery = new UpdateWrapper<>();
        try {
            // 抢到锁并执行
            while (true) {
                if (lock.tryLock(0, -1, TimeUnit.MILLISECONDS)) {
                    UserInterfaceInfo userInterfaceInfo = new UserInterfaceInfo();
                    userInterfaceInfo.setUserId(userId);
                    userInterfaceInfo.setInterfaceInfoId(interfaceInfoId);
                    UserInterfaceInfo interfaceInfo = this.getOne(new QueryWrapper<>(userInterfaceInfo));
                    if (interfaceInfo == null){
                        userInterfaceInfo.setTotalNum(0);
                        userInterfaceInfo.setLeftNum(100);
                        userInterfaceInfo.setStatus(0);
                        this.save(userInterfaceInfo);
                    }
                    updateQuery = new UpdateWrapper<>();
                    updateQuery.eq("interfaceInfoId", interfaceInfoId);
                    updateQuery.eq("userId", userId);
                    updateQuery.gt("leftNum", 0);
                    updateQuery.setSql("leftNum = leftNum -1,totalNum = totalNum +1");
                    boolean result = this.update(updateQuery);
                    if (!result){
                        throw new BusinessException(ErrorCode.SYSTEM_ERROR,"接口调用次数跟新失败");
                    }
                    return result;
                }
            }
        } catch (InterruptedException e) {
            log.error("do invokeCount error", e);
            return false;
        } finally {
            // 只能释放自己的锁
            if (lock.isHeldByCurrentThread()) {
                System.out.println("unLock: " + Thread.currentThread().getId());
                lock.unlock();
            }
        }
    }

    @Override
    public boolean openInterfaceInfo(Long userId, Long interfaceInfoId) {
        // 1. 查询是否存在该接口
        InterfaceInfo interfaceInfo = interfaceInfoMapper.selectById(interfaceInfoId);
        if (interfaceInfo == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "该接口不存在");
        }
        // 2. 查询数据库是否已存在 对应用户接口数据
        QueryWrapper<UserInterfaceInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userId).eq("interfaceInfoId", interfaceInfoId);
        List<UserInterfaceInfo> userInterfaceInfos = userInterfaceInfoMapper.selectList(queryWrapper);
        if (userInterfaceInfos != null && userInterfaceInfos.size() > 0) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "用户已开通该接口");
        }
        // 3. 不存在，添加数据
        UserInterfaceInfo userInterfaceInfo = new UserInterfaceInfo();
        userInterfaceInfo.setUserId(userId);
        userInterfaceInfo.setInterfaceInfoId(interfaceInfoId);
        userInterfaceInfo.setTotalNum(0);
        userInterfaceInfo.setLeftNum(INVOKE_COUNT);
        userInterfaceInfo.setStatus(0);
        boolean save = this.save(userInterfaceInfo);
        return save;
    }

    @Override
    public boolean isHaveInvokeCount(long userId, long interfaceInfoId) {
        // 1. 如果是管理员 直接调用
        User user = userMapper.selectById(userId);
        if (UserConstant.ADMIN_ROLE.equals(user.getUserRole())) {
            return true;
        }
        // 2. 非管理员，查询数据库，判断是否开通接口并是否还有调用次数
        QueryWrapper<UserInterfaceInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userId).eq("interfaceInfoId", interfaceInfoId);
        UserInterfaceInfo userInterfaceInfo = userInterfaceInfoMapper.selectOne(queryWrapper);
        if (userInterfaceInfo == null || userInterfaceInfo.getLeftNum() < 0) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public List<UserInterfaceInfoInvokeVO> listInterfaceInvokeByPage(UserInterfaceInfoInvokeRequest userInterfaceInfoInvokeRequest, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        userInterfaceInfoInvokeRequest.setUserId(loginUser.getId());

        List<UserInterfaceInfoInvokeVO> list = userInterfaceInfoMapper.listInterfaceInvokeByPage(userInterfaceInfoInvokeRequest);
        return list;
    }

}




