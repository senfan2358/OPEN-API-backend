package com.senfan.project.model.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.senfan.senfanapicommon.common.PageRequest;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 接口调用VO
 *
 * @TableName product
 */
@Data
public class UserInterfaceInfoInvokeVO implements Serializable {
    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 调用用户 id
     */
    private Long userId;

    /**
     * 接口 id
     */
    private Long interfaceInfoId;

    /**
     * 总调用次数
     */
    private Integer totalNum;

    /**
     * 剩余调用次数
     */
    private Integer leftNum;

    /**
     * 0-正常，1-禁用
     */
    private Integer status;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 接口名称
     */
    private String name;

    /**
     * 接口描述
     */
    private String description;

}