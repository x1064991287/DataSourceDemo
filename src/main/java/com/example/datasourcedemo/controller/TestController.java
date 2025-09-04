package com.example.datasourcedemo.controller;

import com.bosssoft.nontax3.saas.certificate.thridapi.ThirdApiFactory;
import com.bosssoft.nontax3.saas.certificate.thridapi.dto.TencentGeocoderResp;
import com.bosssoft.nontax3.saas.certificate.thridapi.sdk.IThridSupplementApiService;
import com.example.datasourcedemo.domain.TUser;
import com.example.datasourcedemo.mapper.TUserMapper;
import com.example.datasourcedemo.service.AsyncService;
import com.example.datasourcedemo.utils.IpUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 测试的
 *
 * @author tychen
 * @date 2024/5/11 14:22:50
 */
@RestController
@Slf4j
public class TestController {

    @Autowired
    private DataSource dataSource;

    private final ExecutorService executor = Executors.newCachedThreadPool();
    @Autowired
    private AsyncService asyncService;

    @GetMapping("/test")
    public String test() {
        return "test";
    }
    @Resource
    private TUserMapper tUserMapper;

    @GetMapping("/select")
    public String test2() {
        for (int i = 0; i < 10; i++) {
            try {
                String sql = "select * from t_user";
                Connection connection = dataSource.getConnection();
                PreparedStatement ps = connection.prepareStatement(sql);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    log.info(rs.getString("username"));
                }
                rs.close();
                ps.close();
                connection.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            System.out.println(i);
        }
        return null;
    }

    @GetMapping("/insert")
    public void insert() {
        TUser tUser = new TUser();
        tUser.setUsername("tychen");
        tUser.setPassword("123456");
        tUser.setSex("男");
        tUser.setAge(18.0);
        int insert = tUserMapper.insert(tUser);
        System.out.println("aa");
    }

    @GetMapping("/insert1")
    public void insert1() {
        try {
            Connection connection = dataSource.getConnection();
            PreparedStatement ps = connection.prepareStatement("insert into t_user(id,username,password,sex) values(?,?,?,?)");
            ps.setInt(1, 1);
            ps.setString(2, "tychen");
            ps.setString(3, "123456");
            ps.setString(4, "男");
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/testMysql")
    public String testMysql() throws SQLException {
        Connection connect = dataSource.getConnection();
        String tableName = "t_user";
        List<String> columnNames = new ArrayList<>();
        String sql = "SELECT * FROM " + tableName + " WHERE 1=0";
        try (Statement stmt = connect.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            for (int i = 1; i <= columnCount; i++) {
                columnNames.add(metaData.getColumnName(i)); // 或 getColumnLabel(i)
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @GetMapping("/testReadConfigByCommons")
    public String testReadConfigByCommons1() throws IOException, ConfigurationException {
        return testReadConfigByCommons();
    }

    public String testReadConfigByCommons() throws IOException, ConfigurationException {
        PropertiesConfiguration config = new PropertiesConfiguration();
        String path = "src/main/resources/application.yml";
        Reader reader = new FileReader(path);
        config.read(reader);
        String string = config.getString("server.port");
        log.info(string);

        return "testReadConfigByCommons";
    }

    @PostMapping(path = "/v1/chat/completions", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamSse() throws JsonProcessingException {
        SseEmitter emitter = new SseEmitter();

        Map<String, Object> data = new HashMap<>();
        data.put("model", "asd");
        data.put("message", "asdasd");
        data.put("cost_time", "17.12");
        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(data);
        executor.execute(() -> {
            try {
                for (int i = 0; i < 10; i++) {

                    if (i == 9) {
                        emitter.send(SseEmitter.event()
                                               .data("[DONE]", MediaType.APPLICATION_JSON)
                                               .id(String.valueOf(i))
                                               .name("message"));
                    }
                    SseEmitter.SseEventBuilder event = SseEmitter.event()
                                                                 .data(json, MediaType.APPLICATION_JSON)
                                                                 .id(String.valueOf(i))
                                                                 .name("message");

                    emitter.send(event);
                    Thread.sleep(100);
                }
                emitter.complete();
            } catch (Exception ex) {
                emitter.completeWithError(ex);
            }
        });

        return emitter;
    }

    @GetMapping("/testAsync")
    public String testAsync() {
        System.out.println("主线程开始：" + Thread.currentThread().getName());
        asyncService.asyncMethod();
        System.out.println("主线程结束：" + Thread.currentThread().getName());
        return "testAsync";
    }

    @GetMapping("/testGetIp")
    public String testGetIp(HttpServletRequest request) {
        String clientIp = IpUtils.getClientIp(request);
        System.out.println("请求的IP:" + clientIp);
        return "testGetIp";
    }

    @GetMapping("/test111")
    public String test111() throws IOException {
        //        IThirdapiAiSDKService thirdapiAiSDKService = ThirdApiFactory.getThirdApiAiSDKServiceByConfig(
        //                "http://172.18.150.59:8080/saas/certificate/thirdapi/ai/",
        //                "SCDXHXYY",
        //                "Qb6bNWrxGWJDG9Xa1eeA3K5K05q8CLwt9AVuKPIqTlQZ",
        //                4000L,
        //                3000L
        //        );
        IThridSupplementApiService thridSupplementApiService = ThirdApiFactory.getThirdSupplementApiSDKServiceByConfig("http://172.18.150.59:8080/saas/certificate/thirdapi/location/",
                                                                                                                       "SCDXHXYY",
                                                                                                                       "Qb6bNWrxGWJDG9Xa1eeA3K5K05q8CLwt9AVuKPIqTlQZ",
                                                                                                                       4000L,
                                                                                                                       3000L);
        TencentGeocoderResp.AddressComponent locationByAddress = thridSupplementApiService.getLocationByAddress("上海");
        System.out.println(locationByAddress);
        return "test111";
    }

}
