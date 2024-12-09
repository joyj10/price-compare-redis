package com.redis.pricecompare.service;

import com.redis.pricecompare.vo.Keyword;
import com.redis.pricecompare.vo.Product;
import com.redis.pricecompare.vo.ProductGroup;

import java.util.Set;

public interface LowestPriceService {
    Set GetZsetValue(String key);
    Set GetZsetValueWithStatus(String key) throws Exception;

    Set GetZsetValueWithSpecificException(String key) throws Exception;
    int SetNewProduct(Product newProduct);

    int SetNewProductGroup(ProductGroup newProductGrp);

    int SetNewProductGroupToKeyword (String keyword, String prodGrpId, double score);

    Keyword GetLowestPriceProductByKeyword(String keyword);
}
