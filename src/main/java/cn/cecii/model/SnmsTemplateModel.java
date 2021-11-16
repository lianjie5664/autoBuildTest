package cn.cecii.model;

import lombok.Data;

import java.util.List;

/**
 * @Description:
 * @Author: hupeng
 * @Date: 15:03 2020/11/16
 */
@Data
public class SnmsTemplateModel {
    String handle;
    String templateVersion;
    List<SnmsTemplateValueModel> value;
}
