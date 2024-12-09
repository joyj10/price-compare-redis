package com.redis.pricecompare.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.redis.pricecompare.common.exception.NotFoundException;
import com.redis.pricecompare.vo.Keyword;
import com.redis.pricecompare.vo.Product;
import com.redis.pricecompare.vo.ProductGroup;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class LowestPriceServiceImpl implements LowestPriceService {
    private final RedisTemplate myProductPriceRedis;

    public Set GetZsetValue(String key)  {
        Set myTempSet = new HashSet();
        myTempSet = myProductPriceRedis.opsForZSet().rangeWithScores(key, 0, 9);
        return myTempSet;
    };

    public Set GetZsetValueWithStatus(String key) throws Exception {
        Set myTempSet = new HashSet();
        myTempSet = myProductPriceRedis.opsForZSet().rangeWithScores(key, 0, 9);
        if (myTempSet.size() < 1 ) {
            throw new Exception("The Key doesn't have any member");
        }
        return myTempSet;
    };

    public Set GetZsetValueWithSpecificException(String key) throws Exception {
        Set myTempSet = new HashSet();
        myTempSet = myProductPriceRedis.opsForZSet().rangeWithScores(key, 0, 9);
        if (myTempSet.size() < 1 ) {
            throw new NotFoundException("The Key doesn't exist in redis", HttpStatus.NOT_FOUND);
        }
        return myTempSet;
    };

    public int SetNewProduct(Product newProduct) {
        int rank = 0;
        myProductPriceRedis.opsForZSet().add(newProduct.getProductGroupId(), newProduct.getProductId(), newProduct.getPrice());
        rank = myProductPriceRedis.opsForZSet().rank(newProduct.getProductGroupId(), newProduct.getProductId()).intValue();
        return rank;
    }

    public int SetNewProductGroup(ProductGroup newProductGroup) {
        List<Product> product = newProductGroup.getProductList();
        String productId = product.get(0).getProductId();
        double price = product.get(0).getPrice();
        myProductPriceRedis.opsForZSet().add(newProductGroup.getProductGroupId(), productId, price);
        int productCnt = myProductPriceRedis.opsForZSet().zCard(newProductGroup.getProductGroupId()).intValue();
        return productCnt;
    }

    public void DeleteKey (String key) {
        myProductPriceRedis.delete(key);
    }
    public int SetNewProductGroupToKeyword (String keyword, String productGroupId, double score){
        myProductPriceRedis.opsForZSet().add(keyword, productGroupId, score);
        return myProductPriceRedis.opsForZSet().rank(keyword, productGroupId).intValue();
    }

    public Keyword GetLowestPriceProductByKeyword(String keyword) {
        Keyword returnInfo = new Keyword();
        List<ProductGroup> tempProductGroupList = new ArrayList<>();
        // keyword 를 통해 ProductGroup 가져오기 (10개)
        tempProductGroupList = GetProductGroupUsingKeyword(keyword);

        // 가져온 정보들을 Return 할 Object 에 넣기
        returnInfo.setKeyword(keyword);
        returnInfo.setProductGroupList(tempProductGroupList);
        // 해당 Object return
        return returnInfo;
    }

    public List<ProductGroup> GetProductGroupUsingKeyword(String keyword) {
        List<ProductGroup> returnInfo = new ArrayList<>();

        // input 받은 keyword 로 productGroupId를 조회
        List<String> productGroupIdList = new ArrayList<>();
        productGroupIdList = List.copyOf(myProductPriceRedis.opsForZSet().reverseRange(keyword, 0, 9));
        List<Product> tempProdList = new ArrayList<>();

        //10개 productGroupId로 loop
        for (final String productGroupId : productGroupIdList) {
            // Loop 타면서 ProductGroupID 로 Product:price 가져오기 (10개)
            ProductGroup tempProductGroup = new ProductGroup();

            Set prodAndPriceList = new HashSet();
            prodAndPriceList = myProductPriceRedis.opsForZSet().rangeWithScores(productGroupId, 0, 9);
            Iterator<Object> prodPricObj = prodAndPriceList.iterator();

            // loop 타면서 product obj에 bind (10개)
            while (prodPricObj.hasNext()) {
                ObjectMapper objMapper = new ObjectMapper();
                // {"value":00-10111-}, {"score":11000}
                Map<String, Object> productPriceMap = objMapper.convertValue(prodPricObj.next(), Map.class);
                Product tempProduct = new Product();
                // Product Obj bind
                tempProduct.setProductId(productPriceMap.get("value").toString());
                tempProduct.setPrice(Double.valueOf(productPriceMap.get("score").toString()).intValue()); //es 검색된 score
                tempProduct.setProductGroupId(productGroupId);

                tempProdList.add(tempProduct);
            }
            // 10개 product price 입력완료
            tempProductGroup.setProductGroupId(productGroupId);
            tempProductGroup.setProductList(tempProdList);
            returnInfo.add(tempProductGroup);
        }

        return returnInfo;
    }
}
