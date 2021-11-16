package cn.cecii.util;

import cn.cecii.model.Login;
import cn.hutool.core.date.DateUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @Description: 访问二级节点登录接口获取token
 * @Author: hupeng
 * @Date: 9:15 2020/11/16
 */
@Component
@Slf4j
public class TokenConf {
    @Value("${snms.login.url}")
    private String url;

    @Value("${snms.login.username}")
    private String username;

    @Value("${snms.login.password}")
    private String password;

    private static String token;

    /**
     * 调用登录接口
     */
    private void execute() {
        String today= DateUtil.today();
        log.info(today);
        String pass = SecureUtil.sha256(password + today);
        Login login = new Login();
        login.setUsername(username);
        login.setPassword(pass);
        String bodyJson = JSONUtil.toJsonStr(login);
        String result = HttpRequest.post(url)
                .body(bodyJson)
                .execute().body();
        JSONObject jsonObject = JSONUtil.parseObj(result);
        if ("success".equals(jsonObject.getStr("message"))) {
            token = jsonObject.getJSONObject("data").getStr("token");
            log.info(token);
        }
    }

    public String getToken() {
        if (token == null) {
            execute();
        }
        return token;
    }

    public String getNewToken() {
        execute();
        return token;
    }




    public static void main(String[] args) {
//        String url = "http://bsjx.cecii.cn/snmsapi/identity/token/v1";
//        String username = "xywl";
//        String password = "f925a3973927b95ea1fe6c0b473d59cbe460902216f5d20eb46272d8a47836d1";
////        String sha256 = SecureUtil.sha256("xywl@123");
////        System.out.println(sha256);
////        System.out.println(password.equals(sha256));
//        String today= DateUtil.today();
//        System.out.println(today);
//        String pass = SecureUtil.sha256(password + today);
//        Login login = new Login();
//        login.setUsername(username);
//        login.setPassword(pass);
//        String bodyJson = JSONUtil.toJsonStr(login);
//        String result = HttpRequest.post(url)
//                .body(bodyJson)
//                .execute().body();
//        JSONObject jsonObject = JSONUtil.parseObj(result);
////        System.out.println(jsonObject);
//        if ("success".equals(jsonObject.getStr("message"))) {
//            String token = jsonObject.getJSONObject("data").getStr("token");
//            System.out.println(token);
//        }
    }

}
