package cn.cecii.model;

import lombok.Data;

/**
 * @Description:
 * @Author: hupeng
 * @Date: 15:00 2020/11/16
 */
@Data
public class SnmsTemplateDataModel {
    String format = "string";
    String value;

    public SnmsTemplateDataModel(String value) {
        this.value = value;
    }
}
