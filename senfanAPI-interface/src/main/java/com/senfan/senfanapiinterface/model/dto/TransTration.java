package com.senfan.senfanapiinterface.model.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class TransTration implements Serializable {

    private static final long serialVersionUID = 4340440724958351587L;
    private String code;
    private String operate;
}
