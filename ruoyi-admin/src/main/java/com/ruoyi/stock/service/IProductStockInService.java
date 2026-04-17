package com.ruoyi.stock.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ruoyi.stock.domain.ProductStockIn;
import com.ruoyi.stock.dto.StockInDTO;

public interface IProductStockInService extends IService<ProductStockIn> {
    void scanSave(StockInDTO dto);
}