package cn.cecii.model;

import lombok.Data;

import java.util.List;

/**
 * 支付订单信息
 */
@Data
public class PaySuccessOrderInfoModel {
    private Integer sourceSystem;
    private String orderCode;
    private String deviceCode;
    private String merchantCode;
    private Double orderTotalAmount;
    private Double actualPayment;
    private Double discountAmount;
    private String createTime;
    private Integer orderWay;
    private String payTime;
    private Integer payMode;
    private Double distributionAmount;
    private String remark;
    private String syncTime;
    private List<PayOrderGoodsDetailModel> goodsList;
}
