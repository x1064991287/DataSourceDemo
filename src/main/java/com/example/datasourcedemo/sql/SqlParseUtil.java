package com.example.datasourcedemo.sql;

import com.example.datasourcedemo.sql.entity.ConditionColumn;
import com.example.datasourcedemo.sql.entity.FieldsInfo;
import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.*;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.statement.update.UpdateSet;
import net.sf.jsqlparser.util.TablesNamesFinder;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * SQL解析工具类 - 兼容JSQLParser 4.9版本
 *
 * @author 白秀远
 * @date 2025/6/20 17:37:33
 */
public class SqlParseUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(SqlParseUtil.class);
    private final List<ConditionColumn> conditionColumns = new ArrayList<>();
    private final Map<String, String> aliasMap = new HashMap<>();
    private List<FieldsInfo> queryColumns = new ArrayList<>();
    private List<ConditionColumn> updateSetColumns = new ArrayList<>();
    private List<String> tables = new ArrayList<>();
    private String sqlType;
    private String sql;

    public SqlParseUtil(String sql) {
        this.sql = sql;
        if (StringUtils.isNotBlank(sql)) {
            parseSql(sql);
        }
    }

    /**
     * 处理CASE表达式
     */
    private static FieldsInfo handleCaseExpression(CaseExpression caseExpression, Alias alias, int index) {
        FieldsInfo queryField = new FieldsInfo();
        queryField.setName(caseExpression.toString());
        queryField.setAlias(alias != null ? alias.getName() : null);
        queryField.setIndex(index);
        queryField.setFunction(true);
        return queryField;
    }

    /**
     * 递归解析 WHERE 子句的表达式
     */
    private void parseWhereExpression(Expression expression, FromItem fromItem) {
        if (expression == null) {
            return;
        }

        if (expression instanceof BinaryExpression) {
            BinaryExpression binaryExpression = (BinaryExpression) expression;

            if (binaryExpression instanceof AndExpression || binaryExpression instanceof OrExpression) {
                // 递归解析左右两边
                parseWhereExpression(binaryExpression.getLeftExpression(), fromItem);
                parseWhereExpression(binaryExpression.getRightExpression(), fromItem);
            } else if (binaryExpression instanceof ComparisonOperator) {
                handleComparisonOperator(binaryExpression, fromItem);
            } else if (expression instanceof LikeExpression) {
                handleLikeExpression((LikeExpression) expression, fromItem);
            }
        } else if (expression instanceof Parenthesis) {
            // 处理括号内的表达式
            Parenthesis parenthesis = (Parenthesis) expression;
            parseWhereExpression(parenthesis.getExpression(), fromItem);
        } else if (expression instanceof InExpression) {
            handleInExpression((InExpression) expression, fromItem);
        } else if (expression instanceof IsNullExpression) {
            // 处理 IS NULL 表达式
            IsNullExpression isNullExpression = (IsNullExpression) expression;
            Column column = (Column) isNullExpression.getLeftExpression();
            String operator = isNullExpression.isNot() ? "IS NOT NULL" : "IS NULL";
            addConditionColumn(column, null, operator, fromItem);
        }
    }

    /**
     * 处理比较运算符表达式
     */
    private void handleComparisonOperator(BinaryExpression binaryExpression, FromItem fromItem) {
        ComparisonOperator condition = (ComparisonOperator) binaryExpression;
        Expression leftExpression = binaryExpression.getLeftExpression();
        Expression rightExpression = binaryExpression.getRightExpression();

        Column column = null;
        Expression valueExpression = null;

        // 确定哪边是列，哪边是值
        if (leftExpression instanceof Column) {
            column = (Column) leftExpression;
            valueExpression = rightExpression;
        } else if (rightExpression instanceof Column) {
            column = (Column) rightExpression;
            valueExpression = leftExpression;
        }

        if (column != null && isValueExpression(valueExpression)) {
            addConditionColumn(column, valueExpression, condition.getStringExpression(), fromItem);
        }
    }

    /**
     * 处理IN表达式
     */
    private void handleInExpression(InExpression inExpression, FromItem fromItem) {
        Expression leftExpression = inExpression.getLeftExpression();
        if (leftExpression instanceof Column) {
            Column column = (Column) leftExpression;
            // JSQLParser 4.9中，getRightItemsList()返回的类型可能不同
            Expression rightExpression = inExpression.getRightExpression();
            if (rightExpression != null) {
                addConditionColumn(column, rightExpression.toString(), "IN", fromItem);
            }
        }
    }

    /**
     * 处理LIKE表达式
     */
    private void handleLikeExpression(LikeExpression likeExpression, FromItem fromItem) {
        Expression leftExpression = likeExpression.getLeftExpression();
        if (leftExpression instanceof Column) {
            Column column = (Column) leftExpression;
            Expression rightExpression = likeExpression.getRightExpression();
            if (rightExpression != null) {
                addConditionColumn(column, rightExpression.toString(), "LIKE", fromItem);
            }
        }
    }

    /**
     * 判断是否为值表达式
     */
    private boolean isValueExpression(Expression expression) {
        return expression instanceof StringValue
               || expression instanceof LongValue
               || expression instanceof DoubleValue
               || expression instanceof DateValue
               || expression instanceof JdbcParameter
               || expression instanceof NullValue
               || expression instanceof TimeValue
               || expression instanceof TimestampValue;
    }

    /**
     * 添加条件列
     */
    private void addConditionColumn(Column column, Object value, String operator, FromItem fromItem) {
        String columnName = column.getColumnName().toLowerCase();
        Table table = column.getTable();

        ConditionColumn conditionColumn = new ConditionColumn();
        conditionColumn.setColumnName(columnName);
        if (value != null) {
            conditionColumn.setValue(value.toString());
        }
        conditionColumn.setOperate(operator);

        // 确定表名
        String tableName = determineTableName(table, fromItem);
        conditionColumn.setTableName(tableName);

        conditionColumns.add(conditionColumn);
    }

    /**
     * 确定表名
     */
    private String determineTableName(Table table, FromItem fromItem) {
        if (table != null) {
            String tableName = table.getName();
            // 检查是否为别名
            if (aliasMap.containsKey(tableName)) {
                return aliasMap.get(tableName).toLowerCase();
            }
            return tableName.toLowerCase();
        }

        // 如果列没有指定表，尝试从FromItem获取
        if (fromItem instanceof Table) {
            Table fromTable = (Table) fromItem;
            return fromTable.getName().toLowerCase();
        }

        return "unknown_table";
    }

    /**
     * 遍历SELECT语句
     *
     * @return 返回该SELECT语句解析出的查询列
     */
    private List<FieldsInfo> traverseSelect(Select select) {
        List<FieldsInfo> currentQueryColumns = new ArrayList<>();
        if (select instanceof PlainSelect) {
            PlainSelect plainSelect = (PlainSelect) select;
            FromItem fromItem = plainSelect.getFromItem();

            // 1) 预扫描 FROM / JOIN，收集子查询别名 -> 列清单 的映射
            Map<String, List<FieldsInfo>> subAliasToCols = new HashMap<>();
            collectSubSelectAliasCols(fromItem, subAliasToCols);
            if (plainSelect.getJoins() != null) {
                for (Join j : plainSelect.getJoins()) {
                    collectSubSelectAliasCols(j.getRightItem(), subAliasToCols);
                }
            }

            // 3. **处理 SELECT 列**：传入子查询的列信息，以便展开 `*`
            List<SelectItem<?>> selectItems = plainSelect.getSelectItems();
            if (selectItems != null) {
                List<FieldsInfo> fieldsInfoList = traverse(selectItems, subAliasToCols, plainSelect);
                currentQueryColumns.addAll(fieldsInfoList);
            }

            // 4. **处理 WHERE 条件**
            Expression whereExpression = plainSelect.getWhere();
            if (whereExpression != null) {
                parseWhereExpression(whereExpression, fromItem);
            }

        } else if (select instanceof SetOperationList) {
            // 处理 UNION 等集合操作
            SetOperationList setOperationList = (SetOperationList) select;
            List<Select> selects = setOperationList.getSelects();
            if (selects != null && !selects.isEmpty()) {
                currentQueryColumns = traverseSelect(selects.get(0));
            }

        }
        return currentQueryColumns;
    }


    /**
     * 遍历处理SELECT语句中的选择项列表
     *
     * @param selectItems     SELECT语句中的选择项列表
     * @param subAliasToCols  子查询别名到列信息列表的映射关系
     * @param plainSelect     当前处理的PlainSelect对象
     * @return 解析后的字段信息列表
     */
    private List<FieldsInfo> traverse(List<SelectItem<?>> selectItems, Map<String, List<FieldsInfo>> subAliasToCols, PlainSelect plainSelect) {
        List<FieldsInfo> currentFields = new ArrayList<>();
        int index = 1;
        for (SelectItem<?> selectItem : selectItems) {
            Expression expression = selectItem.getExpression();
            Alias alias = selectItem.getAlias();

            if (expression instanceof Column) {
                currentFields.add(handleColumnExpression((Column) expression, alias, index));
                index++;
            } else if (expression instanceof Function) {
                currentFields.add(handleFunctionExpression((Function) expression, alias, index));
                index++;
            } else if (expression instanceof ParenthesedSelect) {
                ParenthesedSelect subSelect = (ParenthesedSelect) expression;
                currentFields.addAll(traverseSelect(subSelect.getSelect())); // 递归
            } else if (expression instanceof CaseExpression) {
                currentFields.add(handleCaseExpression((CaseExpression) expression, alias, index));
                index++;
            } else if (expression instanceof AllColumns && !(expression instanceof AllTableColumns)) {

                // 处理 SELECT * ：展开所有"子查询源"的列；基础表以 alias.* 占位
                boolean expandedAny = false;

                // from 源
                expandedAny |= expandStarFromItem(plainSelect.getFromItem(), subAliasToCols, currentFields);
                // join 源
                if (plainSelect.getJoins() != null) {
                    for (Join j : plainSelect.getJoins()) {
                        expandedAny |= expandStarFromItem(j.getRightItem(), subAliasToCols, currentFields);
                    }
                }
                // 如果没有找到可展开的子查询源，保留通配符 *
                if (!expandedAny) {
                    FieldsInfo queryField = new FieldsInfo();
                    queryField.setName("*");
                    queryField.setAlias(alias != null ? alias.getName() : null);
                    queryField.setIndex(index);
                    currentFields.add(queryField);
                    index++;
                }
            } else if (expression instanceof AllTableColumns) {
                // 处理 t.* 展开
                AllTableColumns atc = (AllTableColumns) expression;
                String nameOrAlias = normalize(atc.getTable().getFullyQualifiedName());
                //如果是子查询的
                List<FieldsInfo> fieldsInfos = subAliasToCols.get(nameOrAlias);
                if (fieldsInfos != null) {
                    currentFields.addAll(fieldsInfos);
                } else if (aliasMap.containsKey(nameOrAlias)) {
                    //基础表的列
                    FieldsInfo queryField = new FieldsInfo();
                    queryField.setName("*");
                    queryField.setTableAlias(nameOrAlias);
                    queryField.setTableName(aliasMap.get(nameOrAlias));
                    queryField.setAlias(alias != null ? alias.getName() : null);
                    queryField.setIndex(index);
                    currentFields.add(queryField);
                } else {
                    // 列展开失败，返回 t.*
                    FieldsInfo queryField = new FieldsInfo();
                    queryField.setName(nameOrAlias + ".*");
                    queryField.setAlias(alias != null ? alias.getName() : null);
                    queryField.setIndex(index);
                    currentFields.add(queryField);
                    index++;
                }
            } else {
                // 其他表达式（如字面量、参数等）
                FieldsInfo queryField = new FieldsInfo();
                queryField.setName(expression.toString());
                queryField.setAlias(alias != null ? alias.getName() : null);
                queryField.setIndex(index);
                currentFields.add(queryField);
                index++;
            }
        }
        return currentFields;
    }

    /**
     * 展开 SELECT * 来自一个源（子查询 → 具体列；基础表 → alias.*）
     */
    private boolean expandStarFromItem(FromItem from, Map<String, List<FieldsInfo>> aliasMap, List<FieldsInfo> out) {
        if (from == null) {
            return false;
        }

        if (from instanceof ParenthesedSelect) {
            ParenthesedSelect ss = (ParenthesedSelect) from;
            String alias = ss.getAlias() != null ? normalize(ss.getAlias().getName()) : null;
            if (alias != null) {
                List<FieldsInfo> cols = aliasMap.get(alias);
                if (cols == null && ss.getSelect() != null) {
                    cols = new ArrayList<>(traverseSelect(ss.getSelect()));
                }
                if (cols != null) {
                    out.addAll(cols);
                    return true;
                }
            } else if (ss.getSelect() != null) {
                out.addAll(traverseSelect(ss.getSelect()));
                return true;
            }
            return false;
        }

        // 基础表情况：若有别名则 alias.*，否则表名.*（无 schema 无法枚举具体列）
        String alias = from.getAlias() != null ? normalize(from.getAlias().getName()) : null;
        if (alias != null && !alias.isEmpty()) {
            FieldsInfo queryField = new FieldsInfo();
            queryField.setName(alias + ".*");
            queryField.setAlias(alias);
            queryField.setIndex(out.size() + 1);
            out.add(queryField);
        } else {
            FieldsInfo queryField = new FieldsInfo();
            queryField.setName(from + ".*");
            queryField.setAlias(null);
            queryField.setIndex(out.size() + 1);
            out.add(queryField);
        }
        return true;
    }

    /**
     * 收集 SubSelect 的别名及其列
     */
    private void collectSubSelectAliasCols(FromItem from, Map<String, List<FieldsInfo>> aliasMap) {
        if (from instanceof ParenthesedSelect) {
            ParenthesedSelect ss = (ParenthesedSelect) from;
            String alias = ss.getAlias() != null ? normalize(ss.getAlias().getName()) : null;
            if (alias != null && ss.getSelect() != null) {
                List<FieldsInfo> innerCols = traverseSelect(ss.getSelect());
                aliasMap.put(alias, new ArrayList<>(innerCols));
            }
        }
    }

    private String normalize(String s) {
        if (s == null) {
            return null;
        }
        // 去掉引用并统一大小写，避免 "T" / t 混淆；根据需要可改成保留大小写
        return s.replace("\"", "").replace("`", "").toLowerCase(Locale.ROOT);
    }

    /**
     * 处理列表达式
     */
    private FieldsInfo handleColumnExpression(Column column, Alias alias, int index) {
        FieldsInfo queryField = new FieldsInfo();
        queryField.setName(column.getColumnName());
        queryField.setAlias(alias != null ? alias.getName() : null);
        queryField.setIndex(index);

        Table table = column.getTable();
        if (table != null) {
            String tableName = table.getName();
            // 使用别名映射来查找真实的表名
            if (aliasMap.containsKey(tableName)) {
                queryField.setTableName(aliasMap.get(tableName).toLowerCase());
                queryField.setTableAlias(tableName);
            } else {
                queryField.setTableName(tableName.toLowerCase());
            }
        }
        return queryField;
    }

    /**
     * 处理函数表达式
     */
    private FieldsInfo handleFunctionExpression(Function function, Alias alias, int index) {
        FieldsInfo queryField = new FieldsInfo();
        queryField.setName(function.toString());
        queryField.setAlias(alias != null ? alias.getName() : null);
        queryField.setIndex(index);
        queryField.setFunction(true);
        return queryField;
    }

    // Getter和Setter方法
    public String getSqlType() {
        return sqlType;
    }

    public void setSqlType(String sqlType) {
        this.sqlType = sqlType;
    }

    public List<FieldsInfo> getQueryColumns() {
        return queryColumns;
    }

    public List<ConditionColumn> getConditionColumns() {
        return conditionColumns;
    }

    public List<String> getTables() {
        return tables;
    }

    public void setTables(List<String> tables) {
        this.tables = tables;
    }

    public List<ConditionColumn> getUpdateSetColumns() {
        return updateSetColumns;
    }

    public void setUpdateSetColumns(List<ConditionColumn> updateSetColumns) {
        this.updateSetColumns = updateSetColumns;
    }

    /**
     * 查找表信息
     */
    private void findTables(Statement statement) {
        try {
            TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
            Set<String> tableList = tablesNamesFinder.getTables(statement);
            this.tables.addAll(new HashSet<>(tableList));
        } catch (Exception e) {
            LOGGER.warn("查找表名失败: {}", e.getMessage());
        }
    }

    /**
     * 解析SQL语句
     */
    private void parseSql(String sql) {
        try {
            Statement statement = CCJSqlParserUtil.parse(sql);
            findTables(statement);
            findTableAlias(statement);

            if (statement instanceof Select) {
                handleSelectStatement((Select) statement);
            } else if (statement instanceof Update) {
                handleUpdateStatement((Update) statement);
            } else if (statement instanceof Delete) {
                handleDeleteStatement((Delete) statement);
            } else if (statement instanceof Insert) {
                handleInsertStatement((Insert) statement);
            }
        } catch (Exception e) {
            LOGGER.error("解析SQL失败: {}", sql, e);
        }
    }

    /**
     * 处理SELECT语句
     */
    private void handleSelectStatement(Select select) {
        this.queryColumns = traverseSelect(select);

        // 如果只有一个表，为所有字段设置表名
        if (tables.size() == 1) {
            String tableName = tables.get(0).toLowerCase();
            queryColumns.forEach(fieldsInfo -> {
                if (StringUtils.isBlank(fieldsInfo.getTableName())) {
                    fieldsInfo.setTableName(tableName);
                }
            });
            conditionColumns.forEach(conditionColumn -> {
                if (StringUtils.isBlank(conditionColumn.getTableName())) {
                    conditionColumn.setTableName(tableName);
                }
            });
        }

        // 处理别名映射
        processAliasMapping();
        this.sqlType = "select";
    }

    /**
     * 处理UPDATE语句
     */
    private void handleUpdateStatement(Update update) {
        Table table = update.getTable();

        // 处理UPDATE SET子句 - JSQLParser 4.9中使用getUpdateSets()
        List<UpdateSet> updateSets = update.getUpdateSets();
        if (updateSets != null) {
            traverseUpdateSet(updateSets, table);
        }

        // 处理WHERE条件
        Expression whereExpression = update.getWhere();
        if (whereExpression != null) {
            parseWhereExpression(whereExpression, table);
        }

        // 设置表名
        if (table != null) {
            String tableName = table.getName().toLowerCase();
            conditionColumns.forEach(conditionColumn -> {
                if (StringUtils.isBlank(conditionColumn.getTableName())) {
                    conditionColumn.setTableName(tableName);
                }
            });
            updateSetColumns.forEach(updateSetColumn -> {
                if (StringUtils.isBlank(updateSetColumn.getTableName())) {
                    updateSetColumn.setTableName(tableName);
                }
            });
        }

        this.sqlType = "update";
    }

    /**
     * 处理DELETE语句
     */
    private void handleDeleteStatement(Delete delete) {
        Table table = delete.getTable();

        // 处理WHERE条件
        Expression whereExpression = delete.getWhere();
        if (whereExpression != null) {
            parseWhereExpression(whereExpression, table);
        }

        // 设置表名
        if (table != null) {
            String tableName = table.getName().toLowerCase();
            conditionColumns.forEach(conditionColumn -> {
                if (StringUtils.isBlank(conditionColumn.getTableName())) {
                    conditionColumn.setTableName(tableName);
                }
            });
        }

        this.sqlType = "delete";
    }

    /**
     * 处理INSERT语句
     */
    private void handleInsertStatement(Insert insert) {
        List<Column> columns = insert.getColumns();
        Table table = insert.getTable();
        Select select = insert.getSelect();

        if (select != null) {
            // 判断是 INSERT...VALUES 还是 INSERT...SELECT
            if (select instanceof Values) {
                // 处理 INSERT...VALUES 语句
                handleValuesClause((Values) select, columns, table);
            } else {
                // 处理 INSERT...SELECT 语句
                handleInsertSelect(select, columns, table);
            }
        }
        this.sqlType = "insert";
    }

    /**
     * 处理VALUES子句
     */
    private void handleValuesClause(Values values, List<Column> columns, Table table) {
        ExpressionList<?> expressions = values.getExpressions();
        if (expressions != null && columns != null && !columns.isEmpty()) {
            for (int i = 0; i < expressions.size(); i++) {
                Expression expression = expressions.get(i);
                if (expression instanceof ParenthesedExpressionList) {
                    ParenthesedExpressionList<?> rowValues = (ParenthesedExpressionList<?>) expression;
                    List<Expression> valueList = (List<Expression>) rowValues;

                    for (int j = 0; j < columns.size() && j < valueList.size(); j++) {
                        Column column = columns.get(j);
                        Expression valueExpr = valueList.get(j);

                        ConditionColumn conditionColumn = new ConditionColumn();
                        conditionColumn.setColumnName(column.getColumnName().toLowerCase());
                        conditionColumn.setValue(valueExpr.toString());
                        conditionColumn.setOperate("INSERT");

                        if (table != null) {
                            conditionColumn.setTableName(table.getName().toLowerCase());
                        }

                        conditionColumns.add(conditionColumn);
                    }
                }
            }
        }
    }

    /**
     * 处理INSERT...SELECT语句
     */
    private void handleInsertSelect(Select select, List<Column> columns, Table table) {
        // 递归解析子查询，获取查询列
        List<FieldsInfo> selectFields = traverseSelect(select);

        // 将查询列转换为插入条件
        for (int i = 0; i < selectFields.size() && i < columns.size(); i++) {
            FieldsInfo queryField = selectFields.get(i);
            Column column = columns.get(i);

            ConditionColumn conditionColumn = new ConditionColumn();
            conditionColumn.setColumnName(column.getColumnName().toLowerCase());
            conditionColumn.setValue(queryField.getName());
            conditionColumn.setOperate("INSERT");

            if (table != null) {
                conditionColumn.setTableName(table.getName().toLowerCase());
            }

            conditionColumns.add(conditionColumn);
        }
    }

    /**
     * 查找表别名
     */
    private void findTableAlias(Statement statement) {
        try {
            // 假设你有一个 TableAliasFinder 类
            TableAliasFinder tableAliasFinder = new TableAliasFinder();
            Map<String, String> tableAliasMap = tableAliasFinder.getTableAliasMap(statement);
            if (tableAliasMap != null) {
                aliasMap.putAll(tableAliasMap);
            }
        } catch (Exception e) {
            LOGGER.warn("查找表别名失败: {}", e.getMessage());
        }
    }

    /**
     * 处理别名映射
     */
    private void processAliasMapping() {
        if (MapUtils.isNotEmpty(aliasMap)) {
            // 处理查询列的别名
            queryColumns.forEach(queryColumn -> {
                String tableName = queryColumn.getTableName();
                if (StringUtils.isNotBlank(tableName) && aliasMap.containsKey(tableName)) {
                    queryColumn.setTableName(aliasMap.get(tableName));
                }
            });

            // 处理条件列的别名
            conditionColumns.forEach(conditionColumn -> {
                String tableName = conditionColumn.getTableName();
                if (StringUtils.isNotBlank(tableName) && aliasMap.containsKey(tableName)) {
                    conditionColumn.setTableName(aliasMap.get(tableName));
                }
            });
        }
    }

    /**
     * 处理UPDATE SET子句
     */
    private void traverseUpdateSet(List<UpdateSet> updateSets, Table table) {
        for (UpdateSet updateSet : updateSets) {
            ExpressionList<?> columns = updateSet.getColumns();
            ExpressionList<?> values = updateSet.getValues();

            for (int i = 0; i < columns.size() && i < values.size(); i++) {
                Expression columnExpr = columns.get(i);
                Expression valueExpr = values.get(i);

                if (columnExpr instanceof Column) {
                    Column column = (Column) columnExpr;

                    ConditionColumn conditionColumn = new ConditionColumn();
                    conditionColumn.setColumnName(column.getColumnName().toLowerCase());
                    conditionColumn.setValue(valueExpr.toString());
                    conditionColumn.setOperate("=");

                    if (table != null) {
                        conditionColumn.setTableName(table.getName().toLowerCase());
                    }

                    updateSetColumns.add(conditionColumn);
                }
            }
        }
    }

}
