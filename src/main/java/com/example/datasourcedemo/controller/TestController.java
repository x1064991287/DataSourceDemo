package com.example.datasourcedemo.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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

    @GetMapping("/test")
    public String test() {
        return "test";
    }

    @GetMapping("/select")
    public String test2() {
        try {
            String sql = "select * from t_user";
            Connection connection = dataSource.getConnection();
            PreparedStatement ps = connection.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                log.info(rs.getString("username"));
            }
            return "success";

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
