package com.LinYi.backend.model.dto.productinfo;

import com.LinYi.backend.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @author: LinYi
 * @Date: 2023/09/04 11:33:48
 * @Version: 1.0
 * @Description: 产品信息搜索文本请求
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ProductInfoSearchTextRequest extends PageRequest implements Serializable {
    private static final long serialVersionUID = -6337349622479990038L;

    private String searchText;
}
