package com.LinYi.backend.model.dto.pay;

import lombok.Data;

import java.io.Serializable;

/**
 * @author: LinYi
 * @Date: 2023/08/25 05:00:08
 * @Version: 1.0
 * @Description: 付款创建请求
 */
@Data
public class PayCreateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 接口id
     */
    private String productId;

    /**
     * 支付类型
     */
    private String payType;

}