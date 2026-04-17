package com.ruoyi.web.controller.common;

import com.aliyun.dingtalkoauth2_1_0.models.GetTokenResponse;
import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.DingTalkClient;
import com.dingtalk.api.request.OapiV2UserGetuserinfoRequest;
import com.dingtalk.api.response.OapiV2UserGetuserinfoResponse;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.constant.Constants;
import com.ruoyi.framework.web.service.SysLoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/dingtalk")

public class DingTalkLoginController {
    @Autowired
    private SysLoginService loginService;

    @Value("${dingtalk.appKey}")
    private String clientId;

    @Value("${dingtalk.appSecret}")
    private String clientSecret;

    @PostMapping("/login")
    public AjaxResult login(@RequestBody Map<String, String> params) {
        String code = params.get("code");

        try {
            // 1. 获取 AccessToken (复用 Demo 逻辑)
            String accessToken = getAccessToken(clientId, clientSecret);

            // 2. 获取钉钉 userId
            DingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/topapi/v2/user/getuserinfo");
            OapiV2UserGetuserinfoRequest req = new OapiV2UserGetuserinfoRequest();
            req.setCode(code);
            OapiV2UserGetuserinfoResponse rsp = client.execute(req, accessToken);

            if (rsp.isSuccess()) {
                String dingUserId = rsp.getResult().getUserid();

                // 3. 调用若依登录逻辑（这一步我们下一步去实现）
                String token = loginService.loginByDingUserId(dingUserId);

                AjaxResult ajax = AjaxResult.success();
                ajax.put(Constants.TOKEN, token);
                return ajax;
            }
            return AjaxResult.error("钉钉校验失败: " + rsp.getErrmsg());
        } catch (Exception e) {
            return AjaxResult.error("免登异常: " + e.getMessage());
        }
    }

    // 提取 Demo 中的 Token 获取逻辑
    private String getAccessToken(String clientId, String clientSecret) throws Exception {
        com.aliyun.teaopenapi.models.Config config = new com.aliyun.teaopenapi.models.Config();
        config.protocol = "https";
        config.regionId = "central";
        com.aliyun.dingtalkoauth2_1_0.Client client = new com.aliyun.dingtalkoauth2_1_0.Client(config);
        com.aliyun.dingtalkoauth2_1_0.models.GetTokenRequest getTokenRequest = new com.aliyun.dingtalkoauth2_1_0.models.GetTokenRequest()
                .setClientId(clientId)
                .setClientSecret(clientSecret)
                .setGrantType("client_credentials");
        GetTokenResponse response = client.getToken(getTokenRequest); // 简化版，通常内部应用不需要 corpId
        return response.getBody().accessToken;
    }
}
