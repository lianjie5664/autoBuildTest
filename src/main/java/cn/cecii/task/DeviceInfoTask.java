package cn.cecii.task;

import cn.cecii.entity.DeviceInfoEntity;
import cn.cecii.entity.MerchantInfoEntity;
import cn.cecii.mapper.DeviceInfoMapper;
import cn.cecii.mapper.MerchantInfoMapper;
import cn.cecii.model.*;
import cn.cecii.util.SnmsOperateUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
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
 * 设备信息相关数据上传标识解析
 */
@Component
@Slf4j
public class DeviceInfoTask {

    @Value("${snms.prefix.device}")
    private String devicePrefix;

    @Value("${snms.template.device}")
    private String deviceTemplate;

    @Autowired
    private DeviceInfoMapper deviceInfoMapper;

    @Autowired
    private MerchantInfoMapper merchantInfoMapper;

    @Autowired
    private SnmsOperateUtil snmsOperateUtil;


    @KafkaListener(topics = "oda_device_info")
    public void handle(ConsumerRecord<?, ?> record, Acknowledgment ack, @Header(KafkaHeaders.RECEIVED_TOPIC) String topic) {
        Optional message = Optional.ofNullable(record.value());
        if (message.isPresent()) {
            Object msg = message.get();
            log.info("oda_device_info 消费了： Topic:" + topic + ",Message:" + msg);
            DeviceInfoModel deviceInfoModel = JSONUtil.toBean(msg.toString(), DeviceInfoModel.class);
            try {
                String id = devicePrefix + "/" + deviceInfoModel.getSourceSystem()+"-"+deviceInfoModel.getDeviceCode();
                boolean exist = snmsOperateUtil.isExist(id);
                if (exist) {
                    //设备已存在，更新
                    log.info("设备已存在，更新标识");
                    SnmsTemplateModel snmsTemplateModel = snmsOperateUtil.resolveIdentification(id);
                    updateDeviceModel2SnmsTemplateModel(snmsTemplateModel,deviceInfoModel);
                    snmsOperateUtil.updateIdentification(snmsTemplateModel);
                } else {
                    //新增设备
                    log.info("新增设备");
                    SnmsTemplateModel snmsTemplateModel = initSnmsModel(deviceInfoModel);
                    snmsOperateUtil.registerIdentification(snmsTemplateModel);
                    //更新device_info表中标识字段
                    QueryWrapper<DeviceInfoEntity> queryWrapper = new QueryWrapper<>();
                    queryWrapper.lambda().eq(DeviceInfoEntity::getSourceSystem,deviceInfoModel.getSourceSystem())
                            .eq(DeviceInfoEntity::getDeviceCode,deviceInfoModel.getDeviceCode());
                    Integer count = deviceInfoMapper.selectCount(queryWrapper);
                    if (count == 0) {
                        //因Process模块还没有将Kafka数据存入mysql
                        ThreadUtil.sleep(5000);
                        log.info("device_info表中无记录");
                    }
                    List<DeviceInfoEntity> deviceInfoEntities = deviceInfoMapper.selectList(queryWrapper);
                    for (DeviceInfoEntity deviceInfoEntity:deviceInfoEntities) {
                        deviceInfoEntity.setDeviceUniqueId(id);
                        deviceInfoMapper.updateById(deviceInfoEntity);
                    }
                }
            } catch (Exception e) {
                log.error("设备标识失败："+deviceInfoModel);
                log.error("错误信息：",e);
            }
            ack.acknowledge();
        }
    }

    private SnmsTemplateModel initSnmsModel(DeviceInfoModel deviceInfoModel) {
        String deviceName = deviceInfoModel.getDeviceName();
        String deviceCode = deviceInfoModel.getDeviceCode();
        Integer deviceCategory = deviceInfoModel.getDeviceCategory();
        Integer currentStatus = deviceInfoModel.getCurrentStatus();
        String merchantName = deviceInfoModel.getMerchantName();
        QueryWrapper<MerchantInfoEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(MerchantInfoEntity::getSourceSystem,deviceInfoModel.getSourceSystem())
                .eq(MerchantInfoEntity::getMerchantCode,deviceInfoModel.getMerchantCode());
        List<MerchantInfoEntity> merchantInfoEntities = merchantInfoMapper.selectList(queryWrapper);
        String contactName = merchantInfoEntities.size()==0 || merchantInfoEntities.get(0).getContactName().isEmpty()? "**" : merchantInfoEntities.get(0).getContactName();
        String contactNumber = merchantInfoEntities.size()==0 || merchantInfoEntities.get(0).getContactNumber().isEmpty()? "**" : merchantInfoEntities.get(0).getContactNumber();

        SnmsTemplateModel snmsTemplateModel = new SnmsTemplateModel();
        //注册标识数据模型准备
        String handle = devicePrefix + "/" +deviceInfoModel.getSourceSystem()+"-"+deviceInfoModel.getDeviceCode();
        snmsTemplateModel.setHandle(handle);
        snmsTemplateModel.setTemplateVersion(deviceTemplate);
        List<SnmsTemplateValueModel> valueModels = new ArrayList<>();
        SnmsTemplateValueModel deviceNameModel = new SnmsTemplateValueModel();
        deviceNameModel.setData(new SnmsTemplateDataModel(deviceName));
        deviceNameModel.setIndex(SnmsDeviceTemplateConst.DEVICE_NAME);
        deviceNameModel.setType("deviceName");
        valueModels.add(deviceNameModel);

        SnmsTemplateValueModel deviceCodeModel = new SnmsTemplateValueModel();
        deviceCodeModel.setData(new SnmsTemplateDataModel(deviceCode));
        deviceCodeModel.setIndex(SnmsDeviceTemplateConst.DEVICE_CODE);
        deviceCodeModel.setType("deviceCode");
        valueModels.add(deviceCodeModel);

        SnmsTemplateValueModel deviceCategoryModel = new SnmsTemplateValueModel();
        deviceCategoryModel.setData(new SnmsTemplateDataModel(deviceCategory.toString()));
        deviceCategoryModel.setIndex(SnmsDeviceTemplateConst.DEVICE_CATEGORY);
        deviceCategoryModel.setType("deviceCategory");
        valueModels.add(deviceCategoryModel);

        SnmsTemplateValueModel currentStatusModel = new SnmsTemplateValueModel();
        currentStatusModel.setData(new SnmsTemplateDataModel(currentStatus.toString()));
        currentStatusModel.setIndex(SnmsDeviceTemplateConst.CURRENT_STATUS);
        currentStatusModel.setType("currentStatus");
        valueModels.add(currentStatusModel);

        SnmsTemplateValueModel merchantNameModel = new SnmsTemplateValueModel();
        merchantNameModel.setData(new SnmsTemplateDataModel(merchantName));
        merchantNameModel.setIndex(SnmsDeviceTemplateConst.MERCHANT_NAME);
        merchantNameModel.setType("merchantName");
        valueModels.add(merchantNameModel);

        SnmsTemplateValueModel contactNameModel = new SnmsTemplateValueModel();
        contactNameModel.setData(new SnmsTemplateDataModel(contactName));
        contactNameModel.setIndex(SnmsDeviceTemplateConst.CONTACT_NAME);
        contactNameModel.setType("contactName");
        valueModels.add(contactNameModel);

        SnmsTemplateValueModel contactNumberModel = new SnmsTemplateValueModel();
        contactNumberModel.setData(new SnmsTemplateDataModel(contactNumber));
        contactNumberModel.setIndex(SnmsDeviceTemplateConst.CONTACT_NUMBER);
        contactNumberModel.setType("contactNumber");
        valueModels.add(contactNumberModel);

        SnmsTemplateValueModel totalUpGoodsModel = new SnmsTemplateValueModel();
        totalUpGoodsModel.setData(new SnmsTemplateDataModel("0"));
        totalUpGoodsModel.setIndex(SnmsDeviceTemplateConst.TOTAL_UP_GOODS);
        totalUpGoodsModel.setType("totalUpGoods");
        valueModels.add(totalUpGoodsModel);

        SnmsTemplateValueModel totalOrderModel = new SnmsTemplateValueModel();
        totalOrderModel.setData(new SnmsTemplateDataModel("0"));
        totalOrderModel.setIndex(SnmsDeviceTemplateConst.TOTAL_ORDER);
        totalOrderModel.setType("totalOrder");
        valueModels.add(totalOrderModel);

        SnmsTemplateValueModel totalOrderAmountModel = new SnmsTemplateValueModel();
        totalOrderAmountModel.setData(new SnmsTemplateDataModel("0"));
        totalOrderAmountModel.setIndex(SnmsDeviceTemplateConst.TOTAL_ORDER_AMOUNT);
        totalOrderAmountModel.setType("totalOrderAmount");
        valueModels.add(totalOrderAmountModel);

        snmsTemplateModel.setValue(valueModels);
        return snmsTemplateModel;
    }


    /**
     * 对于设备编辑数据更新标识解析模型
     * @param snmsTemplateModel
     * @param deviceInfoModel
     * @return
     */
    private SnmsTemplateModel updateDeviceModel2SnmsTemplateModel(SnmsTemplateModel snmsTemplateModel,DeviceInfoModel deviceInfoModel) {
        List<SnmsTemplateValueModel> valueList = snmsTemplateModel.getValue();
        for (SnmsTemplateValueModel valueModel : valueList) {
            Integer index = valueModel.getIndex();
            switch (index) {
                case SnmsDeviceTemplateConst.DEVICE_NAME :
                    valueModel.setData(new SnmsTemplateDataModel(deviceInfoModel.getDeviceName()));
                    break;
                case SnmsDeviceTemplateConst.DEVICE_CODE :
                    valueModel.setData(new SnmsTemplateDataModel(deviceInfoModel.getDeviceCode()));
                    break;
                case SnmsDeviceTemplateConst.DEVICE_CATEGORY :
                    valueModel.setData(new SnmsTemplateDataModel(deviceInfoModel.getDeviceCategory().toString()));
                    break;
                case SnmsDeviceTemplateConst.CURRENT_STATUS :
                    valueModel.setData(new SnmsTemplateDataModel(deviceInfoModel.getCurrentStatus().toString()));
                    break;
                case SnmsDeviceTemplateConst.MERCHANT_NAME :
                    valueModel.setData(new SnmsTemplateDataModel(deviceInfoModel.getMerchantName()));
                    break;
            }
        }
        return snmsTemplateModel;
    }
}
