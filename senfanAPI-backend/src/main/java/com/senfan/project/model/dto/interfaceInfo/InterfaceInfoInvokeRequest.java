package com.senfan.project.model.dto.interfaceInfo;

import lombok.Data;

import java.io.Serializable;

/**
 * 更新请求
 *
 * @TableName product
 */
@Data
public class InterfaceInfoInvokeRequest implements Serializable {
    /**
     * 主键
     */
    private Long id;
    /**
     * 用户请求参数
     */
    private String userRequestParams;

    //剩下的参数都可以通过接口ID查出来
    /**
     * 接口路径
     */
    private String path;
    /**
     * 接口地址
     */
    private String url;
    /**
     * 请求类型
     */
    private String method;

}