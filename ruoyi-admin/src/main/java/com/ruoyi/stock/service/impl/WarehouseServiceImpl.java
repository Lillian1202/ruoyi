package com.ruoyi.stock.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.stock.domain.Warehouse;
import com.ruoyi.stock.mapper.WarehouseMapper;
import com.ruoyi.stock.service.IWarehouseService;
import org.springframework.stereotype.Service;

@Service
public class WarehouseServiceImpl extends ServiceImpl<WarehouseMapper, Warehouse> implements IWarehouseService {
}