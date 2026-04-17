package com.ruoyi.stock.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;

@Data
public class StockInDTO {
    @NotBlank(message = "商品编码不能为空")
    private String productCode;    // 前端“编码”字段

    @NotBlank(message = "商品名称不能为空")
    private String productName;    // 前端“名称”字段

    @NotBlank(message = "商品分类不能为空")
    private String category;       // 前端“分类”字段（HUAWEI/NVIDIA）

    @NotBlank(message = "入库仓库不能为空")
    private String warehouse;      // 前端“入库仓库”字段（wh1/wh2/wh3）

    private String remark;         // 前端“入库备注”字段

    /** 新增：OCR识别出的文字 */
    private String ocrText;
}