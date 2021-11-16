package cn.cecii.model;

import lombok.Data;

import java.util.List;

/**
 * 商品上架
 */
@Data
public class GoodsUpModel {
    private Integer sourceSystem;
    private String deviceCode;
    private String shoppeCode;
    private String merchantCode;
    private String merchantName;
    private Integer operationType;
    private String syncTime;
    private List<GoodsUpDetailModel> goodsList;
}
