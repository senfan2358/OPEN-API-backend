package com.senfan.senfanapiinterface.controller;

import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import com.senfan.senfanapicommon.common.BaseResponse;
import com.senfan.senfanapicommon.common.ResultUtils;
import com.senfan.senfanapiinterface.model.dto.TransTration;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/random")
public class TransController {
    @GetMapping("/randomImg")
    public BaseResponse<String> randomImg() {
        String url = "https://api.thecatapi.com/v1/images/search?limit=1";
        HttpResponse response = HttpRequest.get(url)
                .execute();
        String body = response.body();
        body = body.replaceAll("\n", "").replaceAll("\\\\", "");
        return ResultUtils.success(body);
    }

}
