package com.redis.pricecompare.controller;

import com.redis.pricecompare.common.exception.NotFoundException;
import com.redis.pricecompare.service.LowestPriceService;
import com.redis.pricecompare.vo.Keyword;
import com.redis.pricecompare.vo.Product;
import com.redis.pricecompare.vo.ProductGroup;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.Set;

@RestController
@RequestMapping("/")
@RequiredArgsConstructor
public class LowestPriceController {
    private final LowestPriceService lowestPriceService;

    @GetMapping("/product")
    public Set GetZsetValue (String key){
        return lowestPriceService.GetZsetValue(key);
    }

    @GetMapping("/product1")
    public Set GetZsetValueWithStatus (String key){
        try {
            return lowestPriceService.GetZsetValueWithStatus(key);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex);
        }
    }

    @GetMapping("/product2")
    public Set GetZsetValueUsingExController (String key) throws Exception {
        try {
            return lowestPriceService.GetZsetValueWithStatus(key);
        } catch (Exception ex) {
            throw new Exception(ex);
        }
    }

    @GetMapping("/product3")
    public ResponseEntity<Set> GetZsetValueUsingExControllerWithSpecificException (String key) throws Exception {
        Set<String> mySet = new HashSet<>();
        try {
            mySet =  lowestPriceService.GetZsetValueWithSpecificException(key);
        } catch (NotFoundException ex) {
            throw new Exception(ex);
        }
        HttpHeaders responseHeaders = new HttpHeaders();

        return new ResponseEntity<>(mySet, responseHeaders, HttpStatus.OK);
    }

    @PutMapping("/product")
    public int SetNewProduct(@RequestBody Product newProduct) {
        return lowestPriceService.SetNewProduct(newProduct);
    }

    @PutMapping("/productGroup")
    public int SetNewProductGroup(@RequestBody ProductGroup newProductGrp) {
        return lowestPriceService.SetNewProductGroup(newProductGrp);
    }

    @PutMapping("/productGroupToKeyword")
    public int SetNewProductGrpToKeyword (String keyword, String prodGrpId, double score) {
        return lowestPriceService.SetNewProductGroupToKeyword(keyword, prodGrpId, score);
    }

    @GetMapping("/productPrice/lowest")
    public Keyword GetLowestPriceProductByKeyword (String keyword) {
        return lowestPriceService.GetLowestPriceProductByKeyword(keyword);
    }
}
