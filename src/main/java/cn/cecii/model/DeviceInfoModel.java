package cn.cecii.model;

import lombok.Data;

/**
 * 设备信息
 */
@Data
public class DeviceInfoModel {
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
    private String syncTime;
}
