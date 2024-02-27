package com.senfan.senfanapiclientsdk.client;

import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.http.HttpException;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import com.senfan.senfanapicommon.common.ErrorCode;
import com.senfan.senfanapicommon.exception.BusinessException;
import com.senfan.senfanapicommon.utils.SM2Utils;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class SenfanAPIClient {
    private String accessKey;
    private String secretKey;
    private String gatewayHost;

    public SenfanAPIClient(String accessKey, String secretKey) {
        this.secretKey = secretKey;
        this.accessKey = accessKey;
    }

    public SenfanAPIClient(String accessKey, String secretKey, String gatewayHost) {
        this.accessKey = accessKey;
        this.secretKey = secretKey;
        this.gatewayHost = gatewayHost;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    /**
     * 转发请求调用
     * @param id
     * @param params
     * @param url
     * @param method
     * @param path
     * @return
     */
    public String invokeInterface(long id, String params, String url, String method, String path) {

        log.info("SDK正在转发至GATEWAY_HOST:{}", gatewayHost);
        HttpResponse response = null;
        if ("POST".equals(method)) {
            response = postRequest(id, params, url, method, path);
        } else if ("GET".equals(method)) {
            response = getRequest(id, params, url, method, path);
        }
        // 错误响应码处理
        int status = response.getStatus();
        if (status != 200){
            if (status == 404){
                throw new BusinessException(ErrorCode.SYSTEM_ERROR,"接口不存在");
            }
            if (status == 500){
                throw new BusinessException(ErrorCode.SYSTEM_ERROR);
            }
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String body = response.body();
        String result = JSONUtil.formatJsonStr(body);
        log.info("SDK调用接口完成，响应数据：{}", result);
        return result;
    }

    private Map<String, String> getHeaderMap(long id, String body, String url, String path, String method) {
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("accessKey", accessKey);
        // 一定不能直接发送
        // hashMap.put("secretKey",secretKey);
        hashMap.put("nonce", RandomUtil.randomNumbers(5));
        hashMap.put("id", String.valueOf(id));
        hashMap.put("url", url);
        hashMap.put("path", path);
        hashMap.put("method", method);
        // 签名
        // hashMap.put("sign",getSign(body, secretKey));
        hashMap.put("sign",SM2Utils.encrypt(accessKey, body));
        // 处理参数中文问题
        body = URLUtil.encode(body, CharsetUtil.CHARSET_UTF_8);
        hashMap.put("body", body);
        // 一定时间内有效
        hashMap.put("timestamp", String.valueOf(System.currentTimeMillis() / 1000));
        return hashMap;
    }


    public HttpResponse postRequest(long id, String params, String url, String method, String path) {
        HttpResponse response = null;
        try {
            response = HttpRequest.post(gatewayHost + path)
                    // 处理中文编码
                    .header("Accept-Charset", CharsetUtil.UTF_8)
                    .addHeaders(getHeaderMap(id, params, method, path, url))
                    .body(params)
                    .execute();
            /*
            // 可以在SDK处理接口404的情况
            if(httpResponse.getStatus()==404){
                body = String.format("{\"code\": %d,\"msg\":\"%s\",\"data\":\"%s\"}",
                        httpResponse.getStatus(), "接口请求路径不存在", "null");
                log.info("响应结果：" + body);
            }
            */
            // 将返回的JSON结果格式化，其实就是加换行符
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        return response;
    }


    public HttpResponse getRequest(long id, String params, String url, String method, String path) {
        HttpResponse response = null;
        try {
            Map<String, Object> bean = JSONUtil.toBean(params, Map.class);
            // 发送 GET 请求并带有 JSON 请求参数
            response = HttpRequest.get(gatewayHost + path)
                    .header("Accept-Charset", CharsetUtil.UTF_8)
                    .addHeaders(getHeaderMap(id, params, method, path, url))
                    .form(bean)
                    .execute();
        } catch (HttpException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        return response;
    }

    public static void main(String[] args) {
        String path = "/api/name/toUpperCase1";
        // JSON 请求参数
        String jsonParams = "{\"str\":\"john\"}";
        Map bean = JSONUtil.toBean(jsonParams, Map.class);
        Map<String, Object> map = new HashMap();
        map.put("str", "senfan");
        // 发送 GET 请求并带有 JSON 请求参数
        HttpResponse response = HttpRequest.get("http://localhost:8123" + path)
                .header("Accept-Charset", CharsetUtil.UTF_8)
                .form(bean)
                .execute();
        System.out.println(response.getStatus());
        // 输出响应内容
        System.out.println(response.body());
    }
}