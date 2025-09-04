//package com.example.datasourcedemo.sql;
//
//import com.example.datasourcedemo.sql.entity.FieldsInfo;
//import net.sf.jsqlparser.schema.Column;
//import net.sf.jsqlparser.statement.Statement;
//import net.sf.jsqlparser.statement.select.AllTableColumns;
//import net.sf.jsqlparser.statement.select.SelectItem;
//import net.sf.jsqlparser.util.TablesNamesFinder;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Objects;
//
///**
// * @author 白秀远
// * @date 2025/6/26 11:03:28
// */
//public class SelectColumnFonder extends TablesNamesFinder {
//
//    List<FieldsInfo> columns = new ArrayList<>();
//
//    @Override
//    public void visit(SelectItem item) {
//        //        item.getExpression().accept(this);
//        if (item.getExpression() instanceof Column) {
//            Column column = (Column) item.getExpression();
//            FieldsInfo fieldsInfo = new FieldsInfo();
//            fieldsInfo.setName(column.getColumnName());
//            fieldsInfo.setAlias(Objects.toString(item.getAlias(), null));
//            fieldsInfo.setIndex(columns.size() + 1);
//            fieldsInfo.setTableName(column.getTable().getName());
//            columns.add(fieldsInfo);
//        } else if (item.getExpression() instanceof AllTableColumns) {
//            AllTableColumns allTableColumns = (AllTableColumns) item.getExpression();
//            FieldsInfo fieldsInfo = new FieldsInfo();
//            fieldsInfo.setName("*");
//            fieldsInfo.setAlias(Objects.toString(item.getAlias(), null));
//            fieldsInfo.setIndex(columns.size() + 1);
//            fieldsInfo.setTableName(allTableColumns.getTable().getName());
//            columns.add(fieldsInfo);
//        } else {
//            item.getExpression().accept(this);
//        }
//    }
//
//    public List<FieldsInfo> getColumns(Statement statement) {
//        this.init(false);
//        statement.accept(this);
//        return columns;
//    }
//}
