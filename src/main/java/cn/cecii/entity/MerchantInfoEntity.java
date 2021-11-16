package cn.cecii.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 商户信息实体类
 */
@Data
@TableName("merchant_info")
public class MerchantInfoEntity {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Integer sourceSystem;
    private String merchantCode;
    private String merchantName;
    private String unifiedSocialCode;
    private String contactName;
    private String contactNumber;
    private String regTime;
    private String parentCode;
    private Integer level;
    private String syncTime;
    private String update_by;
    private String update_time;
    private String remark;
}
