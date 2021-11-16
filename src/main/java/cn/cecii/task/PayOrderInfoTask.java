package cn.cecii.task;

import cn.cecii.model.*;
import cn.cecii.util.SnmsOperateUtil;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * @Description: 支付成功订单数据，包含商品详情数据
 * @Author: hupeng
 * @Date: 14:09 2020/11/18
 */
@Component
@Slf4j
public class PayOrderInfoTask {

    @Autowired
    private SnmsOperateUtil snmsOperateUtil;

    @Value("${snms.prefix.device}")
    private String devicePrefix;

    @KafkaListener(topics = "oda_pay_order_info")
    public void handle(ConsumerRecord<?, ?> record, Acknowledgment ack, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        Optional message = Optional.ofNullable(record.value());
        if (message.isPresent()) {
            Object msg = message.get();
            log.info("oda_pay_order_info 消费了： Topic:" + topic + ",Message:" + msg);
            PaySuccessOrderInfoModel paySuccessOrderInfoModel = JSONUtil.toBean(msg.toString(), PaySuccessOrderInfoModel.class);
            String id = devicePrefix + "/" + paySuccessOrderInfoModel.getSourceSystem() + "-" + paySuccessOrderInfoModel.getDeviceCode();
            try {
                //根据ID查询
                SnmsTemplateModel snmsTemplateModel = snmsOperateUtil.resolveIdentification(id);
                Double orderTotalAmount = paySuccessOrderInfoModel.getOrderTotalAmount();
                List<SnmsTemplateValueModel> valueModelList = snmsTemplateModel.getValue();
                for (SnmsTemplateValueModel valueModel : valueModelList) {
                    if (valueModel.getIndex() == SnmsDeviceTemplateConst.TOTAL_ORDER) {
                        //设备订单总数
                        Integer totalOrder = Integer.valueOf(valueModel.getData().getValue());
                        valueModel.setData(new SnmsTemplateDataModel(String.valueOf(totalOrder + 1)));
                    }else if(valueModel.getIndex() == SnmsDeviceTemplateConst.TOTAL_ORDER_AMOUNT) {
                        //设备订单总金额
                        Double totalOrderAmount = Double.valueOf(valueModel.getData().getValue());
                        valueModel.setData(new SnmsTemplateDataModel(String.valueOf(totalOrderAmount + orderTotalAmount/1000)));
                    }
                }
                //更新标识
                snmsOperateUtil.updateIdentification(snmsTemplateModel);
            } catch (Exception e) {
                log.error("支付成功订单更新标识失败："+paySuccessOrderInfoModel);
                log.error("错误信息：",e);
            }
            ack.acknowledge();
        }
    }
}
