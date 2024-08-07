package com.example.datasourcedemo.utils;

import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Arrays;

/**
 * @author bxy
 * @date 2024/8/7 15:12:24
 */
public class RestTemplateUseCase {

    public static void main(String[] args) {
        RestTemplateUseCase restTemplateUseCase = new RestTemplateUseCase();
        String url = "http://localhost:8080" + "/test";
//        restTemplateUseCase.getWithHeaderAndParams(url);
        restTemplateUseCase.get(url);
//        restTemplateUseCase.post(url, Arrays.asList("1", "2", "3"));
    }

    //restTemplate 发送get请求  不带请求头和请求参数
    public void get(String url) {
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> forEntity = restTemplate.getForEntity(url, String.class);
        System.out.println(forEntity.getBody());
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

    //restTemplate 发送get请求  带请求头和请求参数
    public void getWithHeaderAndParams(String url) {
        RestTemplate restTemplate = new RestTemplate();

        // 设置请求头
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer your-token-here");
        headers.set("Custom-Header", "value");

        // 设置请求参数
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
                                                           .queryParam("param1", "value1")
                                                           .queryParam("param2", "value2");

        // 创建HttpEntity对象
        HttpEntity<?> entity = new HttpEntity<>(headers);

        // 发送GET请求
        ResponseEntity<String> response = restTemplate.exchange(
                builder.toUriString(),
                HttpMethod.GET,
                entity,
                String.class);

        // 处理响应
        HttpStatus statusCode = response.getStatusCode();
        String responseBody = response.getBody();

        System.out.println("Status code: " + statusCode);
        System.out.println("Response body: " + responseBody);
    }

    //    post请求接收端示例
    //    @PostMapping("/test4")
    //    public String test4(@RequestBody List<String> o)  {
    //        System.out.println(o);
    //        return "success";
    //    }
    //
    //    get请求 带请求参数和请求头  接收端示例
    //    @GetMapping("/test5")
    //    public String test5(@RequestParam("param1") String param1)  {
    //        System.out.println(o);
    //        return "success";
    //    }

    //    get请求 不带请求参数和请求头
    //    @GetMapping("/test6")
    //    public String test6()  {
    //        return "success";
    //    }
}
