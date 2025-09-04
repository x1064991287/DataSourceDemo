package com.example.datasourcedemo.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @TableName t_user
 */
@TableName(value = "t_user")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TUser implements Serializable {

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
    /**
     *
     */
    @TableId
    private Integer id;
    /**
     *
     */
    private String password;
    /**
     *
     */
    private String sex;
    /**
     *
     */
    private String username;

    private Double age;

}
