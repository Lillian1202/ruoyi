package com.ruoyi.stock.controller;

import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.stock.dto.OcrResultDTO;
import com.ruoyi.stock.service.IBaiduOcrService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/ocr")
public class BaiduOcrController {

    @Autowired
    private IBaiduOcrService baiduOcrService;

    @PostMapping("/recognize")
    public AjaxResult recognize(@RequestBody Map<String, String> params) {
        String base64Image = params.get("image");
        if (base64Image == null || base64Image.isEmpty()) {
            return AjaxResult.error("图片数据不能为空");
        }
        
        String type = params.get("type");
        if (type == null || type.isEmpty()) {
            type = "all";
        }
        System.out.println("type = " + type);
        OcrResultDTO result = baiduOcrService.recognize(base64Image, type);
        return AjaxResult.success(result);
    }
}
