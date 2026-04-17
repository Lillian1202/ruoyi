package com.ruoyi.stock.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.stock.dto.OcrResultDTO;
import com.ruoyi.stock.service.IBaiduOcrService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.CompletableFuture;

@Service
public class BaiduOcrServiceImpl implements IBaiduOcrService {

    private static final Logger log = LoggerFactory.getLogger(BaiduOcrServiceImpl.class);

    @Value("${baidu.ocr.api-key}")
    private String apiKey;

    @Value("${baidu.ocr.secret-key}")
    private String secretKey;

    private static final String TOKEN_URL = "https://aip.baidubce.com/oauth/2.0/token?grant_type=client_credentials&client_id={apiKey}&client_secret={secretKey}";
    private static final String OCR_URL = "https://aip.baidubce.com/rest/2.0/ocr/v1/general_basic?access_token={token}";
    private static final String QR_URL = "https://aip.baidubce.com/rest/2.0/ocr/v1/qrcode?access_token={token}";

    private final RestTemplate restTemplate = new RestTemplate();

    private String cachedToken;
    private long tokenExpireTime;

    @Override
    public OcrResultDTO recognize(String base64Image, String type) {
        OcrResultDTO result = new OcrResultDTO();
        
        try {
            String token = getAccessToken();
            if (token == null) {
                result.setOcrSuccess(false);
                result.setOcrError("获取百度Token失败");
                result.setQrSuccess(false);
                result.setQrError("获取百度Token失败");
                return result;
            }

            boolean needOcr = "all".equals(type) || "ocr".equals(type);
            boolean needQr = "all".equals(type) || "qrcode".equals(type);

            if (needOcr && needQr) {
                CompletableFuture<String> ocrFuture = CompletableFuture.supplyAsync(() -> callOcrApi(token, base64Image));
                CompletableFuture<String> qrFuture = CompletableFuture.supplyAsync(() -> callQrApi(token, base64Image));
                
                String ocrText = ocrFuture.join();
                if (ocrText != null) {
                    result.setOcrText(ocrText);
                    result.setOcrSuccess(true);
                } else {
                    result.setOcrSuccess(false);
                    result.setOcrError("文字识别失败");
                }

                String qrCode = qrFuture.join();
                if (qrCode != null) {
                    result.setQrCode(qrCode);
                    result.setQrSuccess(true);
                } else {
                    result.setQrSuccess(false);
                    result.setQrError("未发现条码");
                }
            } else if (needOcr) {
                String ocrText = callOcrApi(token, base64Image);
                if (ocrText != null) {
                    result.setOcrText(ocrText);
                    result.setOcrSuccess(true);
                } else {
                    result.setOcrSuccess(false);
                    result.setOcrError("文字识别失败");
                }
                result.setQrSuccess(false);
                result.setQrError("未请求");
            } else if (needQr) {
                String qrCode = callQrApi(token, base64Image);
                if (qrCode != null) {
                    result.setQrCode(qrCode);
                    result.setQrSuccess(true);
                } else {
                    result.setQrSuccess(false);
                    result.setQrError("未发现条码");
                }
                result.setOcrSuccess(false);
                result.setOcrError("未请求");
            }

        } catch (Exception e) {
            log.error("百度OCR识别异常", e);
            result.setOcrSuccess(false);
            result.setOcrError("服务异常: " + e.getMessage());
            result.setQrSuccess(false);
            result.setQrError("服务异常: " + e.getMessage());
        }
        
        return result;
    }

    private synchronized String getAccessToken() {
        if (cachedToken != null && System.currentTimeMillis() < tokenExpireTime) {
            return cachedToken;
        }

        try {
            String url = TOKEN_URL.replace("{apiKey}", apiKey).replace("{secretKey}", secretKey);
            ResponseEntity<String> response = restTemplate.postForEntity(url, null, String.class);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                JSONObject json = JSON.parseObject(response.getBody());
                cachedToken = json.getString("access_token");
                Integer expiresIn = json.getInteger("expires_in");
                if (expiresIn != null) {
                    tokenExpireTime = System.currentTimeMillis() + (expiresIn - 300) * 1000L;
                }
                return cachedToken;
            }
        } catch (Exception e) {
            log.error("获取百度AccessToken失败", e);
        }
        return null;
    }

    private String callOcrApi(String token, String base64Image) {
        try {
            String url = OCR_URL.replace("{token}", token);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("image", base64Image);
            params.add("language_type", "CHN_ENG");
            
            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                JSONObject json = JSON.parseObject(response.getBody());
                JSONArray wordsResult = json.getJSONArray("words_result");
                if (wordsResult != null && !wordsResult.isEmpty()) {
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < wordsResult.size(); i++) {
                        if (i > 0) sb.append(";");
                        sb.append(wordsResult.getJSONObject(i).getString("words"));
                    }
                    return sb.toString();
                }
            }
        } catch (Exception e) {
            log.error("调用百度OCR API失败", e);
        }
        return null;
    }

    private String callQrApi(String token, String base64Image) {
        try {
            String url = QR_URL.replace("{token}", token);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("image", base64Image);
            
            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
            log.info("调用百度二维码API: {}", url.substring(0, url.length() > 50 ? 50 : url.length()));
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            
            log.info("百度二维码API响应状态: {}", response.getStatusCode());
            log.info("百度二维码API响应内容: {}", response.getBody());
            
            if (response.getStatusCode() == HttpStatus.OK) {
                JSONObject json = JSON.parseObject(response.getBody());
                
                if (json.containsKey("error_code")) {
                    log.error("百度二维码API返回错误: error_code={}, error_msg={}", 
                        json.getString("error_code"), json.getString("error_msg"));
                    return null;
                }
                
                Integer codesResultNum = json.getInteger("codes_result_num");
                log.info("二维码识别数量: {}", codesResultNum);
                
                JSONArray codesResult = json.getJSONArray("codes_result");
                if (codesResult != null && !codesResult.isEmpty()) {
                    JSONObject firstCode = codesResult.getJSONObject(0);
                    String codeType = firstCode.getString("type");
                    log.info("第一个码类型: {}", codeType);
                    
                    JSONArray textArray = firstCode.getJSONArray("text");
                    if (textArray != null && !textArray.isEmpty()) {
                        String qrText = textArray.getString(0);
                        log.info("二维码内容: {}", qrText);
                        return qrText;
                    }
                }
            }
        } catch (Exception e) {
            log.error("调用百度二维码API失败", e);
        }
        return null;
    }
}
