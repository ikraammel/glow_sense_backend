package com.smart_skin.smart_skin_app_backend.dto;

import lombok.Data;

@Data
public class ProductScanRequestDto {
    private String productName;
    private String brand;
    private String barcode;
    private String ingredients;

}
