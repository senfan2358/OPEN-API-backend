package com.senfan.project.model.dto.userInterfaceInfo;

import com.senfan.senfanapicommon.common.PageRequest;
import lombok.Data;

import java.io.Serializable;

/**
 * 创建请求
 *
 * @TableName product
 */
@Data
public class UserInterfaceInfoInvokeRequest extends PageRequest implements Serializable {

    /**
     * 用户id
     */
    private Long userId;
    /**
     * 接口名称
     */
    private String name;

    /**
     * 接口描述
     */
    private String description;

    /**
     * 接口状态
     */
    private Integer status;
}