package com.redis.pricecompare.vo;

import lombok.Data;

import java.util.List;

@Data
public class Keyword {
    private String keyword; // 유아용품 - 딸랑이, 턱받이
    private List<ProductGroup> productGroupList;    // {"FPG001", "FPG002"}
}
