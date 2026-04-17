package com.ruoyi.stock.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.common.core.exception.ServiceException;
import com.ruoyi.stock.domain.ProductCategory;
import com.ruoyi.stock.domain.ProductStockIn;
import com.ruoyi.stock.domain.Warehouse;
import com.ruoyi.stock.dto.StockInDTO;
import com.ruoyi.stock.mapper.ProductStockInMapper;
import com.ruoyi.stock.service.IProductCategoryService;
import com.ruoyi.stock.service.IProductStockInService;
import com.ruoyi.stock.service.IWarehouseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class ProductStockInServiceImpl extends ServiceImpl<ProductStockInMapper, ProductStockIn> implements IProductStockInService {

    @Autowired
    private IProductCategoryService categoryService;

    @Autowired
    private IWarehouseService warehouseService;

    @Override
    public void scanSave(StockInDTO dto) {
        // 1. 校验分类是否存在且正常
        ProductCategory category = categoryService.getOne(new LambdaQueryWrapper<ProductCategory>()
                .eq(ProductCategory::getCategoryCode, dto.getCategory())
                .eq(ProductCategory::getStatus, "0"));
        if (category == null) {
            throw new ServiceException("商品分类不存在或已停用：" + dto.getCategory());
        }

        // 2. 解析商品编码（英伟达逻辑）
        String part1 = null, part2 = null;
        if ("NVIDIA".equals(dto.getCategory())) {
            String snContent = dto.getProductCode().substring(3).trim(); // 去除“SN:”
            if (!snContent.contains("Y")) {
                throw new ServiceException("英伟达扫码格式错误（缺少Y分隔符）");
            }
            String[] parts = snContent.split("Y", 2);
            part1 = parts[0];
            part2 = parts[1];
        }

        // 3. 转换仓库标识为ID（wh1→1，wh2→2，wh3→3）
        Long warehouseId;
        if ("wh1".equals(dto.getWarehouse())) {
            warehouseId = 1L;
        } else if ("wh2".equals(dto.getWarehouse())) {
            warehouseId = 2L;
        } else if ("wh3".equals(dto.getWarehouse())) {
            warehouseId = 3L;
        } else {
            throw new ServiceException("仓库标识无效：" + dto.getWarehouse());
        }
        // 校验仓库是否存在且正常
        Warehouse warehouse = warehouseService.getById(warehouseId);
        if (warehouse == null || !"0".equals(warehouse.getStatus())) {
            throw new ServiceException("仓库不存在或已停用");
        }

        // 4. 构建入库记录
        ProductStockIn stockIn = new ProductStockIn();
        stockIn.setProductCode(dto.getProductCode());
        stockIn.setProductName(dto.getProductName());
        stockIn.setCategoryId(category.getCategoryId());
        stockIn.setWarehouseId(warehouseId);
        stockIn.setScanPart1(part1);
        stockIn.setScanPart2(part2);
        stockIn.setRemark(dto.getRemark());
        stockIn.setCreateBy("admin");
        stockIn.setCreateTime(new Date());

        // 5. 入库
        if (!save(stockIn)) {
            throw new ServiceException("数据库入库失败，请重试");
        }
    }
}