package cn.cecii.util;

import cn.cecii.model.SnmsTemplateModel;
import cn.cecii.model.SnmsTemplateValueModel;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @Description:
 * @Author: hupeng
 * @Date: 16:34 2020/11/17
 */
@Component
@Slf4j
public class SnmsOperateUtil {

    @Autowired
    private TokenConf tokenConf;

    @Value("${snms.register.url}")
    private String registerUrl;

    @Value("${snms.resolve.url}")
    private String resolveUrl;

    /**
     * 注册标识（如果标识存在则更新）
     */
    public void registerIdentification(SnmsTemplateModel snmsTemplateModel) {
        String jsonBody = JSONUtil.toJsonStr(snmsTemplateModel);
        String token = tokenConf.getToken();
        int i = 0;
        //重试2次
        while (i < 2) {
            try {
                String result = HttpRequest.post(registerUrl)
                        .header("Authorization", "Bearer " + token)
                        .header("Content-Type", "application/json")
                        .body(jsonBody)
                        .execute().body();
                JSONObject jsonObject = JSONUtil.parseObj(result);
                String status = jsonObject.getStr("status");
                if ("-1".equals(status)) {
                    log.error("登录接口获取到的Token失效");
                    token = tokenConf.getNewToken();
                    i++;
                }else if ("1".equals(status) ){
                    log.info(jsonObject.getStr("message"));
                    break;
                }else if ("-2".equals(status)){
                    log.info(jsonObject.getStr("message"));
                    //标识已存在，更新标识
//                    updateIdentification(snmsTemplateModel);
                    break;
                }
            } catch (Exception e) {
                e.printStackTrace();
                token = tokenConf.getNewToken();
            }
        }
    }

    /**
     * 查询标识
     * @param id
     * @return
     */
    public SnmsTemplateModel resolveIdentification(String id) {
        String url = resolveUrl + "/" + id;
        String body = HttpRequest.get(url)
                .charset(CharsetUtil.CHARSET_UTF_8)
                .execute()
                .body();
        SnmsTemplateModel snmsTemplateModel = JSONUtil.toBean(body, SnmsTemplateModel.class);
        List<SnmsTemplateValueModel> valueModelList = snmsTemplateModel.getValue();
        for (int i=0;i<valueModelList.size();i++) {
            SnmsTemplateValueModel snmsTemplateValueModel = valueModelList.get(i);
            if (snmsTemplateValueModel.getIndex() == 1001) {
                snmsTemplateModel.setTemplateVersion(snmsTemplateValueModel.getData().getValue());
                valueModelList.remove(i);
            }
        }
        return snmsTemplateModel;
    }

    /**
     * 判断标识是否存在
     * @param id
     * @return
     */
    public boolean isExist(String id) {
        String url = resolveUrl + "/" + id;
        String body = HttpRequest.get(url)
                .charset(CharsetUtil.CHARSET_UTF_8)
                .execute()
                .body();
        JSONObject jsonObject = JSONUtil.parseObj(body);
        String responseCode = jsonObject.getStr("responseCode");
        if ("1".equals(responseCode)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 更新标识
     * status: -1 没有登录，需要验证；1 成功；-2 业务失败；-3 没有权限；-4 系统内部错误
     * @param snmsTemplateModel
     */
    public void updateIdentification(SnmsTemplateModel snmsTemplateModel) {
        String jsonBody = JSONUtil.toJsonStr(snmsTemplateModel);
        String token = tokenConf.getToken();
        int i = 0;
        //重试2次
        while (i < 2) {
            try {
                String result = HttpRequest.put(registerUrl)
                        .header("Authorization", "Bearer " + token)
                        .header("Content-Type", "application/json")
                        .body(jsonBody)
                        .execute().body();
                JSONObject jsonObject = JSONUtil.parseObj(result);
                String status = jsonObject.getStr("status");
                if ("-1".equals(status)) {
                    log.error("登录接口获取到的Token失效");
                    token = tokenConf.getNewToken();
                    i++;
                }else if ("1".equals(status) || "-2".equals(status)){
                    log.info(jsonObject.getStr("message"));
                    break;
                }else {
                    log.error("标识解析接口出问题");
                    break;
                }
            } catch (Exception e) {
                e.printStackTrace();
                token = tokenConf.getNewToken();
            }
        }
    }

}
