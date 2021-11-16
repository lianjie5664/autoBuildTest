package cn.cecii.service.impl;

import cn.cecii.entity.DeviceInfoEntity;
import cn.cecii.mapper.DeviceInfoMapper;
import cn.cecii.service.IDeviceInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * 设备信息
 */
@Service
public class DeviceInfoServiceImpl extends ServiceImpl<DeviceInfoMapper, DeviceInfoEntity> implements IDeviceInfoService {

}
