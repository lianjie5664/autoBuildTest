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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 商品上架 接收kafka数据进行处理
 * 更新标识数据
 */
@Component
@Slf4j
public class GoodsUpTask {

    @Autowired
    private SnmsOperateUtil snmsOperateUtil;

    @Value("${snms.prefix.device}")
    private String devicePrefix;

    @KafkaListener(topics = "oda_goods_up")
    public void handle(ConsumerRecord<?, ?> record, Acknowledgment ack, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        Optional message = Optional.ofNullable(record.value());
        if (message.isPresent()) {
            Object msg = message.get();
            log.info("oda_goods_up 消费了： Topic:" + topic + ",Message:" + msg);
            GoodsUpModel goodsUpModel = JSONUtil.toBean(msg.toString(), GoodsUpModel.class);
            String id = devicePrefix + "/" + goodsUpModel.getSourceSystem()+"-"+goodsUpModel.getDeviceCode();
            List<GoodsUpDetailModel> goodsList = goodsUpModel.getGoodsList();
            if (goodsList == null || goodsList.isEmpty()){
                log.warn("商品上架记录没有goodsList！");
                return;
            }
            try {
                //根据ID查询
                SnmsTemplateModel snmsTemplateModel = snmsOperateUtil.resolveIdentification(id);
                int sum = 0;
                for (GoodsUpDetailModel model : goodsList) {
                    sum += model.getGoodsNum();
                }
                //更新snmsTemplateModel
                List<SnmsTemplateValueModel> valueModelList = snmsTemplateModel.getValue();
                for (SnmsTemplateValueModel valueModel : valueModelList) {
                    if (valueModel.getIndex() == SnmsDeviceTemplateConst.TOTAL_UP_GOODS) {
                        Integer totalUpGoods = Integer.valueOf(valueModel.getData().getValue());
                        valueModel.setData(new SnmsTemplateDataModel(String.valueOf(sum+totalUpGoods)));
                    }
                }
                //更新标识
                snmsOperateUtil.updateIdentification(snmsTemplateModel);
            } catch (Exception e) {
                log.error("商品上架更新标识失败："+goodsUpModel);
                log.error("错误信息：",e);
            }
            ack.acknowledge();
        }
    }
}
