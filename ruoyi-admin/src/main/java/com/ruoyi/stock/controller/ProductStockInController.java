package com.ruoyi.stock.controller;

import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.stock.dto.StockInDTO;
import com.ruoyi.stock.service.IProductStockInService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/stock/in")
public class ProductStockInController {

    @Autowired
    private IProductStockInService stockInService;


    @PostMapping("/scanSave")
    public AjaxResult scanSave(@Valid @RequestBody StockInDTO dto) {
        stockInService.scanSave(dto);
        return AjaxResult.success("入库成功");
    }
}