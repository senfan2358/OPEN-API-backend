package com.senfan.senfanapiinterface.controller;
import com.senfan.senfanapiclientsdk.model.User;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * 名称API
 */
@RestController
@RequestMapping("/name")
public class NameController {
    @GetMapping("/")
    public String getNameByGet(String name) {
        return "GET 你的名字是" + name;
    }

    @PostMapping("/")
    public String getNameByPost(@RequestParam String name) {
        return "POST 你的名字是" + name;
    }
    @GetMapping("/toUpperCase")
    public String toUpperCase(@RequestParam String str) {
        return str.toUpperCase();
    }
    @PostMapping("/user")
    public String getUsernameByPost(@RequestBody User user, HttpServletRequest request) {
        // String accesskey = request.getHeader("accesskey");
        // String nonce = request.getHeader("nonce");
        // String timestamp = request.getHeader("timestamp");
        // String sign = request.getHeader("sign");
        // String body = request.getHeader("body");
        // if (!accesskey.equals("senfan")) {
        //     throw new RuntimeException("无权限1");
        // }
        // if (Long.parseLong(nonce) > 100000) {
        //     throw new RuntimeException("无权限2");
        // }
        // // todo 时间和当前时间不能超过 5 分钟
        // System.out.println(timestamp);
        // System.out.println(System.currentTimeMillis());
        // // todo 实际情况是从数据库中查出 secretKey
        // String serverSign = SignUtils.getSign(body, "senfan");
        // if (!sign.equals(serverSign)) {
        //     throw new RuntimeException("无权限3");
        // }
        System.out.println("a");
        return "POST getUsernameByPost 用户名字是" + user.getUsername();
    }
    @PostMapping("/user2")
    public String getUsernameByPost2(@RequestBody User user, HttpServletRequest request) {
        // String accesskey = request.getHeader("accesskey");
        // String nonce = request.getHeader("nonce");
        // String timestamp = request.getHeader("timestamp");
        // String sign = request.getHeader("sign");
        // String body = request.getHeader("body");
        // if (!accesskey.equals("senfan")) {
        //     throw new RuntimeException("无权限1");
        // }
        // if (Long.parseLong(nonce) > 100000) {
        //     throw new RuntimeException("无权限2");
        // }
        // // todo 时间和当前时间不能超过 5 分钟
        // System.out.println(timestamp);
        // System.out.println(System.currentTimeMillis());
        // // todo 实际情况是从数据库中查出 secretKey
        // String serverSign = SignUtils.getSign(body, "senfan");
        // if (!sign.equals(serverSign)) {
        //     throw new RuntimeException("无权限3");
        // }
        System.out.println("a");
        return "POST getUsernameByPost2 用户名字是" + user.getUsername();
    }
}
