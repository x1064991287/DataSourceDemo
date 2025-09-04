package com.example.datasourcedemo.sql;

import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.util.TablesNamesFinder;

import java.util.HashMap;
import java.util.Map;

/**
 * @author 白秀远
 * @date 2025/6/26 10:49:13
 */
public class TableAliasFinder extends TablesNamesFinder {

    private final Map<String, String> tableAliasMap=new HashMap<>();

    public TableAliasFinder() {
    }

    @Override
    public void visit(Table tableName) {
        String tableWholeName = this.extractTableName(tableName);
        Alias alias = tableName.getAlias();
        if (alias != null) {
            tableAliasMap.put(alias.getName(), tableWholeName);
        }
    }

    public Map<String, String> getTableAliasMap(Statement statement) {
        this.init(false);
        statement.accept(this);
        return tableAliasMap;
    }

}
