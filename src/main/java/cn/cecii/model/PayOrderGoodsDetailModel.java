package cn.cecii.model;

import lombok.Data;

/**
 * 支付订单商品详情
 */
@Data
public class PayOrderGoodsDetailModel {
    private String goodsCode;
    private String goodsName;
    private Double paymentAmount;
    private Double discountAmount;
}
