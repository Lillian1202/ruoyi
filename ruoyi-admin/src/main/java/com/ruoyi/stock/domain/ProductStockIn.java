package com.ruoyi.stock.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ruoyi.common.core.domain.BaseEntity;
import lombok.Data;

@Data
@TableName("product_stock_in")
public class ProductStockIn extends BaseEntity {
    @TableId
    private Long stockInId;        // 入库记录ID
    private String productCode;    // 扫码原始结果
    private String productName;    // 商品名称
    private Long categoryId;       // 分类ID
    private Long warehouseId;      // 仓库ID
    private String scanPart1;      // 英伟达Y前部分
    private String scanPart2;      // 英伟达Y后部分
    private String remark;         // 入库备注

    /** 新增：OCR识别出的文字 */
    private String ocrText;
}