package com.ruoyi.stock.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.ruoyi.common.core.domain.BaseEntity;
import lombok.Data;
import java.math.BigDecimal;

@Data
@TableName("warehouse")
public class Warehouse extends BaseEntity {
    @TableId
    private Long warehouseId;      // 仓库ID
    private String warehouseName;  // 仓库名称
    private String warehouseCode;  // 仓库标识（wh1/wh2/wh3）
    private BigDecimal longitude;  // 经度
    private BigDecimal latitude;   // 纬度
    private String status;         // 状态（0正常 1停用）
}