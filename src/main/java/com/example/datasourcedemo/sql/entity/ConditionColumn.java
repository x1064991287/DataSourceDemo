package com.example.datasourcedemo.sql.entity;



import java.util.Objects;

/**
 * @author bxy
 * @version 1.0
 * @Description       条件列
 * @date 2023/10/12 17:27:04
 */

public class ConditionColumn {

    private String tableName;

    private String columnName;

    private Object value;

    private String operate;

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        ConditionColumn that = (ConditionColumn) object;
        return Objects.equals(tableName, that.tableName) && Objects.equals(columnName, that.columnName) && Objects.equals(value, that.value) && Objects.equals(operate, that.operate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tableName, columnName, value, operate);
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public String getOperate() {
        return operate;
    }

    public void setOperate(String operate) {
        this.operate = operate;
    }

    public ConditionColumn() {
    }

    public ConditionColumn(String tableName, String columnName, Object value, String operate) {
        this.tableName = tableName;
        this.columnName = columnName;
        this.value = value;
        this.operate = operate;
    }

    @Override
    public String toString() {
        return "ConditionColumn{" +
               "tableName='" + tableName + '\'' +
               ", columnName='" + columnName + '\'' +
               ", value=" + value +
               ", operate='" + operate + '\'' +
               '}';
    }
}
