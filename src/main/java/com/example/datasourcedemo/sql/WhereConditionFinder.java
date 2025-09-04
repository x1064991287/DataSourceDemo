package com.example.datasourcedemo.sql;

import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.util.TablesNamesFinder;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 白秀远
 * @date 2025/6/26 11:16:55
 */
public class WhereConditionFinder extends TablesNamesFinder {

    List<String> whereCondition = new ArrayList<>();

    @Override
    public void visitBinaryExpression(BinaryExpression binaryExpression) {
        Expression leftExpression = binaryExpression.getLeftExpression();
        Expression rightExpression = binaryExpression.getRightExpression();
        leftExpression.accept(this);
        rightExpression.accept(this);
    }

    public List<String> getWhereCondition(Statement statement) {
        this.init(false);
        statement.accept(this);
        return whereCondition;
    }

}
