package com.example.datasourcedemo.utils;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

/**
 * @author bxy
 * @date 2024/8/7 15:12:24
 */
public class RestTemplateUseCase {

    //restTemplate 发送get请求
    public void get(String url) {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getForEntity(url, String.class);
    }
    //restTemplate 发送post请求
    public void post(String url,Object body) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        headers.add("Accept", "application/json");
        HttpEntity<Object> requestEntity = new HttpEntity<>(body, headers);
        ResponseEntity<String> responseEntity = restTemplate.postForEntity(url, requestEntity, String.class);
        System.out.println(responseEntity.getBody());
    }
}
