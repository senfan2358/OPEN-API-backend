package com.senfan.project.service.impl;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.senfan.project.mapper.UserMapper;
import com.senfan.project.service.UserService;
import com.senfan.senfanapiclientsdk.client.SenfanAPIClient;
import com.senfan.senfanapicommon.common.ErrorCode;
import com.senfan.senfanapicommon.constant.UserConstant;
import com.senfan.senfanapicommon.exception.BusinessException;
import com.senfan.senfanapicommon.model.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;


/**
 * 用户服务实现类
 *
 * @author senfan
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {

    @Resource
    private UserMapper userMapper;

    /**
     * 盐值，混淆密码
     */
    private static final String SALT = "senfan";

    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {
        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }
        // 密码和校验密码相同
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }
        synchronized (userAccount.intern()) {
            // 账户不能重复
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("userAccount", userAccount);
            long count = userMapper.selectCount(queryWrapper);
            if (count > 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");
            }
            // 2. 加密
            String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
            // 3. 分配 accessKey，secretKey
            String accessKey = DigestUtil.md5Hex(SALT+userAccount+ RandomUtil.randomNumbers(5));
            String secretKey = DigestUtil.md5Hex(SALT+userAccount+ RandomUtil.randomNumbers(8));
            // 4. 插入数据
            User user = new User();
            user.setUserAccount(userAccount);
            user.setUserName(userAccount);
            user.setUserPassword(encryptPassword);
            user.setAccessKey(accessKey);
            user.setSecretKey(secretKey);
            boolean saveResult = this.save(user);
            if (!saveResult) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败，数据库错误");
            }
            return user.getId();
        }
    }

    @Override
    public User userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号错误");
        }
        if (userPassword.length() < 6) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
        }
        User user = checkPassword(userAccount, userPassword);
        // 3. 记录用户的登录态
        request.getSession().setAttribute(UserConstant.USER_LOGIN_STATE, user);
        return user;
    }

    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    @Override
    public User getLoginUser(HttpServletRequest request) {
        // 先判断是否已登录
        Object userObj = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null || currentUser.getId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        // 从数据库查询（追求性能的话可以注释，直接走缓存）
        long userId = currentUser.getId();
        currentUser = this.getById(userId);
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return currentUser;
    }

    /**
     * 是否为管理员
     *
     * @param request
     * @return
     */
    @Override
    public boolean isAdmin(HttpServletRequest request) {
        // 仅管理员可查询
        Object userObj = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        User user = (User) userObj;
        return user != null && UserConstant.ADMIN_ROLE.equals(user.getUserRole());
    }

    /**
     * 用户注销
     *
     * @param request
     */
    @Override
    public boolean userLogout(HttpServletRequest request) {
        if (request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE) == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "未登录");
        }
        // 移除登录态
        request.getSession().removeAttribute(UserConstant.USER_LOGIN_STATE);
        return true;
    }

    @Override
    public SenfanAPIClient getSenfanAPIClient(HttpServletRequest request) {
        // 获取当前登录用户
        User loginUser = this.getLoginUser(request);
        // 从数据库查完整信息
        String accessKey = loginUser.getAccessKey();
        String secretKey = loginUser.getSecretKey();
        //log.info("admin生成ReApiClient，secretKey为：{}",secretKey);
        SenfanAPIClient senfanAPIClient = new SenfanAPIClient(accessKey, secretKey);
        // 设置网关地址，使用配置类，直接注入新网关地址，避免魔法值，方便上线
        // SenfanAPIClient.setGateway_Host(gatewayConfig.getHost());
        return senfanAPIClient;
    }


    /**
     * 获取登录用户的密钥
     * @param userAccount
     * @param userPassword
     * @param request
     * @return
     */
    @Override
    public Map<String, String> getUserKey(String userAccount, String userPassword, HttpServletRequest request) {
        User loginUser = getLoginUser(request);
        if (!userAccount.equals(loginUser.getUserAccount())){
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        User user = checkPassword(userAccount,userPassword);
        Map<String,String> map = new HashMap<>();
        map.put("accessKey",user.getAccessKey());
        map.put("secretKey",user.getSecretKey());
        return map;
    }

    /**
     * 更新用户的密钥
     * @param userAccount
     * @param userPassword
     * @param request
     * @return
     */
    @Override
    public Boolean updateUserKey(String userAccount, String userPassword, HttpServletRequest request) {
        User loginUser = getLoginUser(request);
        if (!userAccount.equals(loginUser.getUserAccount())){
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        User user = checkPassword(userAccount,userPassword);
        String accessKey = DigestUtil.md5Hex(SALT+userAccount+ RandomUtil.randomNumbers(5));
        String secretKey = DigestUtil.md5Hex(SALT+userAccount+ RandomUtil.randomNumbers(8));
        user.setAccessKey(accessKey);
        user.setSecretKey(secretKey);
        int res = userMapper.updateById(user);
        if (res <= 0){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        return true;
    }
    private User checkPassword(String userAccount, String userPassword) {
        //  加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        // 查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", encryptPassword);
        User user = userMapper.selectOne(queryWrapper);
        // 用户不存在
        if (user == null) {
            log.info("user login failed, userAccount cannot match userPassword");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
        }
        return user;
    }

}




