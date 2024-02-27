package com.senfan.senfanapiinterface.model.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class SortRequest implements Serializable {

    private static final long serialVersionUID = 1688229147876510026L;
    /**
     * 排序的数组
     */
    private String numStr;
    /**
     * 排序规则， 0 升序，1 降序
     */
    private Integer order;
}
