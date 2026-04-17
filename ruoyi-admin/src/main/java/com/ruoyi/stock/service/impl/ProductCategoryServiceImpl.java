package com.ruoyi.stock.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ruoyi.stock.domain.ProductCategory;
import com.ruoyi.stock.mapper.ProductCategoryMapper;
import com.ruoyi.stock.service.IProductCategoryService;
import org.springframework.stereotype.Service;

@Service
public class ProductCategoryServiceImpl extends ServiceImpl<ProductCategoryMapper, ProductCategory> implements IProductCategoryService {
}