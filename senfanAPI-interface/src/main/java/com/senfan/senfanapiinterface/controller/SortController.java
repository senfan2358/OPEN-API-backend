package com.senfan.senfanapiinterface.controller;

import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.senfan.senfanapicommon.common.BaseResponse;
import com.senfan.senfanapicommon.common.ErrorCode;
import com.senfan.senfanapicommon.common.ResultUtils;
import com.senfan.senfanapiinterface.model.dto.SortRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 排序API
 */
@RestController
@Slf4j
@RequestMapping("/sort")
public class SortController {
    @PostMapping("/quickSort")
    public BaseResponse<String> sort(@RequestBody SortRequest sortRequest){
        String numStr = sortRequest.getNumStr();
        Integer order = sortRequest.getOrder();
        if (numStr == null || order == null){
            return ResultUtils.error(ErrorCode.PARAMS_ERROR);
        }
        String[] nums = new String[0];
        try {
            String substring = numStr.substring(1, numStr.length() - 1);
            nums = substring.split(",");
        } catch (Exception e) {
            log.info("请求参数错误",e);
            return ResultUtils.error(ErrorCode.PARAMS_ERROR);
        }
        List<Integer> numList = Arrays.stream(nums).map(Integer::parseInt).collect(Collectors.toList());
        numList.sort((a,b)->{
            if (order == 0){
                return a-b;
            }else {
                return b-a;
            }
        });
        String res = numList.stream()
                .map(Object::toString)
                .collect(Collectors.joining(",", "[", "]"));
        return ResultUtils.success(res);
    }

    @PostMapping("/rand.music")
    public BaseResponse<String> randMusic(HttpServletRequest request) {
        String url = "https://api.uomg.com/api/rand.music";
        String body = URLUtil.decode(request.getHeader("body"), CharsetUtil.CHARSET_UTF_8);
        HttpResponse httpResponse = HttpRequest.get(url + "?" + body)
                .execute();
        return ResultUtils.success(httpResponse.body());
    }
}
