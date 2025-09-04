package com.example.datasourcedemo.sql.entity;

/**
 * 查询语句列信息
 *
 * @author bxy
 * @date 2024/3/13 16:43:36
 */
public class FieldsInfo {

	private boolean isFunction;
	private String name;
	private String alias;
	private String tableName;
	private String tableAlias;
	private String item;
	private Integer index;

	public FieldsInfo(FieldsInfo col) {
		 this.isFunction = col.isFunction;
		 this.name = col.name;
		 this.alias = col.alias;
		 this.tableName = col.tableName;
		 this.tableAlias = col.tableAlias;
		 this.item = col.item;
		 this.index = col.index;
	}

	public FieldsInfo() {
	}

	public Integer getIndex() {
		return index;
	}

	public void setIndex(Integer index) {
		this.index = index;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public boolean isFunction() {
		return isFunction;
	}

	public void setFunction(boolean function) {
		this.isFunction = function;
	}

	public String getTableAlias() {
		return tableAlias;
	}

	public void setTableAlias(String tableAlias) {
		this.tableAlias = tableAlias;
	}

	public String getItem() {
		return item;
	}

	public void setItem(String item) {
		this.item = item;
	}

	@Override
	public String toString() {
		return "FieldsInfo{" +
		       "isFunction=" + isFunction +
		       ", name='" + name + '\'' +
		       ", alias='" + alias + '\'' +
		       ", tableName='" + tableName + '\'' +
		       ", tableAlias='" + tableAlias + '\'' +
		       ", item='" + item + '\'' +
		       ", index=" + index +
		       '}';
	}
}
