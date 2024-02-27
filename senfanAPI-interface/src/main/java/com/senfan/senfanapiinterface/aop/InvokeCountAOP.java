package com.senfan.senfanapiinterface.aop;


import com.senfan.senfanapicommon.model.entity.InterfaceInfo;
import com.senfan.senfanapicommon.model.entity.User;
import com.senfan.senfanapicommon.service.InnerInterfaceInfoService;
import com.senfan.senfanapicommon.service.InnerUserInterfaceInfoService;
import com.senfan.senfanapicommon.service.InnerUserService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.UUID;

// todo 尝试使用 AOP 切面实现每调用一次接口增加一次调用次数
@Aspect
@Component
public class InvokeCountAOP {
    @DubboReference
    private InnerUserService innerUserService;
    @DubboReference
    private InnerInterfaceInfoService innerInterfaceInfoService;
    @DubboReference
    private InnerUserInterfaceInfoService innerUserInterfaceInfoService;
    private static final String INTERFACE_HOST = "http://localhost:8123";
    /**
     * 执行拦截
     */
    @Around("execution(* com.senfan.senfanapiinterface.controller.*.*(..))")
    public Object doInterceptor(ProceedingJoinPoint point) throws Throwable {
        // 获取请求路径
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        // 获取 用户
        String accessKey = request.getHeader("accessKey");
        User invokeUser = innerUserService.getInvokeUser(accessKey);
        // 获取接口信息
        String path = request.getRequestURI();
        String method = request.getMethod();
        InterfaceInfo interfaceInfo = innerInterfaceInfoService.getInterfaceInfo(path, method);
        // 执行原方法
        Object result = point.proceed();
        boolean res = innerUserInterfaceInfoService.invokeCount(interfaceInfo.getId(), invokeUser.getId());
        return result;
    }
}
