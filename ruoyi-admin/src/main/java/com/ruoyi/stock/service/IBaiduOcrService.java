package com.ruoyi.stock.service;

import com.ruoyi.stock.dto.OcrResultDTO;

public interface IBaiduOcrService {
    
    OcrResultDTO recognize(String base64Image, String type);
}
