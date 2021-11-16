package cn.cecii.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 设备信息
 */
@Data
@TableName("device_info")
public class DeviceInfoEntity {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Integer sourceSystem;
    private String deviceCode;
    private String deviceName;
    private Integer deviceCategory;
    private Integer deviceType;
    private Integer deviceStatus;
    private Integer currentStatus;
    private Integer country;
    private String merchantCode;
    private String merchantName;
    private String positionName;
    private Integer positionType;
    private Float longitude;
    private Float latitude;
    private String province;
    private String city;
    private String region;
    private String areaName;
    private Integer isPoverty;
    private String deviceUniqueId;
    private String syncTime;
    private String updateBy;
    private String updateTime;
    private String remark;
}
