package com.ruoyi.stock.controller;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.common.core.domain.AjaxResult;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
@RestController
@RequestMapping("/spare")
public class SpareCredentialController {

    private static final String OTHER_SYSTEM_URL = "http://192.168.188.1:15052/prod-api";

    @GetMapping("/credential")
    public AjaxResult getCredential() {
        AjaxResult result = AjaxResult.success();
        result.put("username", "xiaochunjing");
        result.put("password", "Wxzy@2025");
        return result;
    }

    /**
     * 入库代理接口
     */
    @PostMapping("/part/saveByOtherSystem")
    public AjaxResult saveSparePart(@RequestBody JSONObject requestData) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders loginHeaders = new HttpHeaders();
            loginHeaders.setContentType(MediaType.APPLICATION_JSON);
            JSONObject loginBody = new JSONObject();
            loginBody.put("username", "xiaochunjing");
            loginBody.put("password", "Wxzy@2025");
            HttpEntity<String> loginRequest = new HttpEntity<>(loginBody.toJSONString(), loginHeaders);
            ResponseEntity<String> loginResponse = restTemplate.postForEntity(
                    OTHER_SYSTEM_URL + "/loginByOtherSystem",
                    loginRequest,
                    String.class);
            JSONObject loginResult = JSON.parseObject(loginResponse.getBody());
            if (loginResult == null || loginResult.getInteger("code") != 200) {
                return AjaxResult.error("获取token失败");
            }
            String token = loginResult.getString("token");
            HttpHeaders saveHeaders = new HttpHeaders();
            saveHeaders.setContentType(MediaType.APPLICATION_JSON);
            saveHeaders.set("Authorization", token);
            System.out.println("saveRequest = " + requestData);
            HttpEntity<String> saveRequest = new HttpEntity<>(requestData.toJSONString(), saveHeaders);
            ResponseEntity<String> saveResponse = restTemplate.postForEntity(
                    OTHER_SYSTEM_URL + "/spare/part/saveByOtherSystem",
                    saveRequest,
                    String.class);
            System.out.println("saveResponse = " + saveResponse.getBody());
            JSONObject saveResult = JSON.parseObject(saveResponse.getBody());
            if (saveResult != null && saveResult.getInteger("code") == 200) {
                Object data = saveResult.get("data");
                AjaxResult result = AjaxResult.success("入库成功");
                result.put("token", token);
                if (data != null) {
                    result.put("data", data);
                }
                return result;
            } else {
                String errorMsg = saveResult != null ? saveResult.getString("msg") : "外部系统返回异常";
                return AjaxResult.error(errorMsg);
            }
        } catch (Exception e) {
            return AjaxResult.error("调用外部系统失败：" + e.getMessage());
        }
    }

    /**
     * 出库代理接口
     */
    @PostMapping("/part/outboundByOtherSystem")
    public AjaxResult outboundByOtherSystem(@RequestBody JSONObject requestData) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders loginHeaders = new HttpHeaders();
            loginHeaders.setContentType(MediaType.APPLICATION_JSON);
            JSONObject loginBody = new JSONObject();
            loginBody.put("username", "xiaochunjing");
            loginBody.put("password", "Wxzy@2025");
            HttpEntity<String> loginRequest = new HttpEntity<>(loginBody.toJSONString(), loginHeaders);
            System.out.println("outbound loginRequest = " + loginRequest);
            ResponseEntity<String> loginResponse = restTemplate.postForEntity(
                    OTHER_SYSTEM_URL + "/loginByOtherSystem",
                    loginRequest,
                    String.class);
            System.out.println("outbound loginResponse = " + loginResponse.getBody());
            JSONObject loginResult = JSON.parseObject(loginResponse.getBody());
            if (loginResult == null || loginResult.getInteger("code") != 200) {
                return AjaxResult.error("获取token失败");
            }
            String token = loginResult.getString("token");
            HttpHeaders outboundHeaders = new HttpHeaders();
            outboundHeaders.setContentType(MediaType.APPLICATION_JSON);
            outboundHeaders.set("Authorization", token);
            System.out.println("outboundRequest = " + requestData);
            HttpEntity<String> outboundRequest = new HttpEntity<>(requestData.toJSONString(), outboundHeaders);
            ResponseEntity<String> outboundResponse = restTemplate.postForEntity(
                    OTHER_SYSTEM_URL + "/spare/part/outboundByOtherSystem",
                    outboundRequest,
                    String.class);
            System.out.println("outboundResponse = " + outboundResponse.getBody());
            JSONObject outboundResult = JSON.parseObject(outboundResponse.getBody());
            if (outboundResult != null && outboundResult.getInteger("code") == 200) {
                return AjaxResult.success("出库成功");
            } else {
                String errorMsg = outboundResult != null ? outboundResult.getString("msg") : "外部系统返回异常";
                return AjaxResult.error(errorMsg);
            }
        } catch (Exception e) {
            System.out.println("outbound error = " + e.getMessage());
            return AjaxResult.error("调用外部系统失败：" + e.getMessage());
        }
    }

    /**
     * 根据SN码查询信息代理接口
     */
    @GetMapping("/part/getInfoBySnCode")
    public AjaxResult getInfoBySnCode(@RequestParam("esnCode") String esnCode) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders loginHeaders = new HttpHeaders();
            loginHeaders.setContentType(MediaType.APPLICATION_JSON);
            JSONObject loginBody = new JSONObject();
            loginBody.put("username", "xiaochunjing");
            loginBody.put("password", "Wxzy@2025");
            HttpEntity<String> loginRequest = new HttpEntity<>(loginBody.toJSONString(), loginHeaders);
            ResponseEntity<String> loginResponse = restTemplate.postForEntity(
                    OTHER_SYSTEM_URL + "/loginByOtherSystem",
                    loginRequest,
                    String.class);
            JSONObject loginResult = JSON.parseObject(loginResponse.getBody());
            if (loginResult == null || loginResult.getInteger("code") != 200) {
                return AjaxResult.error("获取token失败");
            }
            String token = loginResult.getString("token");
            HttpHeaders queryHeaders = new HttpHeaders();
            queryHeaders.setContentType(MediaType.APPLICATION_JSON);
            queryHeaders.set("Authorization", token);
            System.out.println("Authorization header token = " + token);
            String url = OTHER_SYSTEM_URL + "/spare/part/getInfoBySnCode?esnCode=" + esnCode;
            HttpEntity<String> queryRequest = new HttpEntity<>(null, queryHeaders);
            ResponseEntity<String> queryResponse = restTemplate.exchange(url, HttpMethod.GET, queryRequest, String.class);
            JSONObject result = JSON.parseObject(queryResponse.getBody());
            if (result != null && result.getInteger("code") == 200) {
                Object data = result.get("data");
                if (data != null) {
                    return AjaxResult.success(data);
                } else {
                    return AjaxResult.error("外部系统返回数据为空");
                }
            } else {
                String errorMsg = result != null ? result.getString("msg") : "外部系统返回异常";
                return AjaxResult.error(errorMsg);
            }
        } catch (Exception e) {
            return AjaxResult.error("调用外部系统失败：" + e.getMessage());
        }
    }

    /**
     * 测试入库接口（先获取token再调用入库）
     */
    @GetMapping("/part/testSave")
    public AjaxResult testSaveSparePart() {
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            JSONObject loginBody = new JSONObject();
            loginBody.put("username", "xiaochunjing");
            loginBody.put("password", "Wxzy@2025");
            HttpEntity<String> loginRequest = new HttpEntity<>(loginBody.toJSONString(), headers);
            ResponseEntity<String> loginResponse = restTemplate.postForEntity(
                    OTHER_SYSTEM_URL + "/loginByOtherSystem", 
                    loginRequest, 
                    String.class);
            JSONObject loginResult = JSON.parseObject(loginResponse.getBody());
            if (loginResult == null || loginResult.getInteger("code") != 200) {
                return AjaxResult.error("获取token失败");
            }
            String token = loginResult.getString("token");
            JSONObject saveData = new JSONObject();
            saveData.put("city", "测试城市");
            saveData.put("county", "测试区县");
            saveData.put("esnCode", "xinghaipangddu2");
            saveData.put("resourceType", "1");
            saveData.put("manufacturer", "华为");
            saveData.put("model", "TEST-MODEL");
            saveData.put("warehouseId", "WH001");
            saveData.put("partStatus", "0");
            saveData.put("shelfId", "SHELF001");
            HttpHeaders saveHeaders = new HttpHeaders();
            saveHeaders.setContentType(MediaType.APPLICATION_JSON);
            saveHeaders.set("Authorization", token);
            HttpEntity<String> saveRequest = new HttpEntity<>(saveData.toJSONString(), saveHeaders);
            System.out.println("saveRequest = " + saveRequest);
            ResponseEntity<String> saveResponse = restTemplate.postForEntity(
                    OTHER_SYSTEM_URL + "/spare/part/saveByOtherSystem", 
                    saveRequest, 
                    String.class);
            System.out.println("saveResponse = " + saveResponse);
            JSONObject saveResult = JSON.parseObject(saveResponse.getBody());
            AjaxResult result = AjaxResult.success("测试完成");
            result.put("token", token);
            result.put("saveResult", saveResult);
            return result;
        } catch (Exception e) {
            return AjaxResult.error("测试失败：" + e.getMessage());
        }
    }

    /**
     * 测试查询接口（先获取token再根据SN码查询）
     */
    @GetMapping("/part/testQuery")
    public AjaxResult testQueryBySnCode(@RequestParam(value = "esnCode", defaultValue = "TEST") String esnCode) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            JSONObject loginBody = new JSONObject();
            loginBody.put("username", "xiaochunjing");
            loginBody.put("password", "Wxzy@2025");
            HttpEntity<String> loginRequest = new HttpEntity<>(loginBody.toJSONString(), headers);
            ResponseEntity<String> loginResponse = restTemplate.postForEntity(
                    OTHER_SYSTEM_URL + "/loginByOtherSystem", 
                    loginRequest, 
                    String.class);
            JSONObject loginResult = JSON.parseObject(loginResponse.getBody());
            if (loginResult == null || loginResult.getInteger("code") != 200) {
                return AjaxResult.error("获取token失败");
            }
            String token = loginResult.getString("token");
            HttpHeaders queryHeaders = new HttpHeaders();
            queryHeaders.setContentType(MediaType.APPLICATION_JSON);
            queryHeaders.set("Authorization", token);
            String url = OTHER_SYSTEM_URL + "/spare/part/getInfoBySnCode?esnCode=" + esnCode;
            HttpEntity<String> queryRequest = new HttpEntity<>(null, queryHeaders);
            ResponseEntity<String> queryResponse = restTemplate.exchange(url, HttpMethod.GET, queryRequest, String.class);
            JSONObject queryResult = JSON.parseObject(queryResponse.getBody());
            AjaxResult result = AjaxResult.success("查询完成");
            result.put("token", token);
            result.put("queryResult", queryResult);
            return result;
        } catch (Exception e) {
            return AjaxResult.error("测试失败：" + e.getMessage());
        }
    }
}
