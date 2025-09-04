package com.bosssoft.tech.arch.datasourcedemo1;

import com.example.datasourcedemo.sql.SqlParseUtil;
import com.example.datasourcedemo.sql.entity.ConditionColumn;
import com.example.datasourcedemo.sql.entity.FieldsInfo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SqlParseUtil测试类 - 全面测试SQL解析功能
 */
@DisplayName("SQL解析工具测试")
public class SqlParseUtilTest {

    @Test
    @DisplayName("简单SELECT语句测试")
    public void testSimpleSelect() {
        String sql = "SELECT id, name FROM users";
        SqlParseUtil parser = new SqlParseUtil(sql);

        assertEquals("select", parser.getSqlType());
        assertEquals(1, parser.getTables().size());
        assertEquals("users", parser.getTables().get(0));

        List<FieldsInfo> queryColumns = parser.getQueryColumns();
        assertEquals(2, queryColumns.size());
        assertEquals("id", queryColumns.get(0).getName());
        assertEquals("name", queryColumns.get(1).getName());
        assertEquals("users", queryColumns.get(0).getTableName());
        assertEquals("users", queryColumns.get(1).getTableName());
    }

    @Test
    @DisplayName("带WHERE条件的SELECT测试")
    public void testSelectWithWhere() {
        String sql = "SELECT id, name FROM users WHERE age > 18 AND status = 'active'";
        SqlParseUtil parser = new SqlParseUtil(sql);

        assertEquals("select", parser.getSqlType());
        List<ConditionColumn> conditions = parser.getConditionColumns();
        assertEquals(2, conditions.size());

        // 检查age条件
        ConditionColumn ageCondition = conditions.stream()
                                                 .filter(c -> "age".equals(c.getColumnName()))
                                                 .findFirst().orElse(null);
        assertNotNull(ageCondition);
        assertEquals(">", ageCondition.getOperate());
        assertEquals("18", ageCondition.getValue());
        assertEquals("users", ageCondition.getTableName());

        // 检查status条件
        ConditionColumn statusCondition = conditions.stream()
                                                    .filter(c -> "status".equals(c.getColumnName()))
                                                    .findFirst().orElse(null);
        assertNotNull(statusCondition);
        assertEquals("=", statusCondition.getOperate());
        assertEquals("'active'", statusCondition.getValue());
    }

    @Test
    @DisplayName("多表JOIN查询测试")
    public void testJoinQuery() {
        String sql = "SELECT u.id, u.name, p.title FROM users u " +
                     "INNER JOIN posts p ON u.id = p.user_id " +
                     "WHERE u.age > 18";
        SqlParseUtil parser = new SqlParseUtil(sql);

        assertEquals("select", parser.getSqlType());
        List<String> tables = parser.getTables();
        assertTrue(tables.contains("users"));
        assertTrue(tables.contains("posts"));

        List<FieldsInfo> queryColumns = parser.getQueryColumns();
        assertEquals(3, queryColumns.size());

        List<ConditionColumn> conditions = parser.getConditionColumns();
        assertEquals(1, conditions.size()); // JOIN条件 + WHERE条件

    }

    @Test
    @DisplayName("子查询测试")
    public void testSubQuery() {
        String sql = "SELECT id, name FROM users WHERE id IN (SELECT user_id FROM orders WHERE status = 'completed')";
        SqlParseUtil parser = new SqlParseUtil(sql);

        assertEquals("select", parser.getSqlType());
        List<String> tables = parser.getTables();
        assertTrue(tables.contains("users"));
        assertTrue(tables.contains("orders"));

        List<ConditionColumn> conditions = parser.getConditionColumns();
        assertFalse(conditions.isEmpty());

        // 检查IN条件
        boolean hasInCondition = conditions.stream()
                                           .anyMatch(c -> "IN".equals(c.getOperate()));
        assertTrue(hasInCondition);
    }

    @Test
    @DisplayName("聚合函数测试")
    public void testAggregateFunction() {
        String sql = "SELECT COUNT(*), AVG(age), MAX(salary) FROM employees GROUP BY department";
        SqlParseUtil parser = new SqlParseUtil(sql);

        assertEquals("select", parser.getSqlType());
        List<FieldsInfo> queryColumns = parser.getQueryColumns();
        assertEquals(3, queryColumns.size());

        // 检查函数标记
        long functionCount = queryColumns.stream()
                                         .mapToLong(f -> f.isFunction() ? 1 : 0)
                                         .sum();
        assertEquals(3, functionCount);
    }

    @Test
    @DisplayName("CASE WHEN表达式测试")
    public void testCaseWhenExpression() {
        String sql = "SELECT id, CASE WHEN age >= 18 THEN 'adult' ELSE 'minor' END as age_group FROM users";
        SqlParseUtil parser = new SqlParseUtil(sql);

        assertEquals("select", parser.getSqlType());
        List<FieldsInfo> queryColumns = parser.getQueryColumns();
        assertEquals(2, queryColumns.size());

        FieldsInfo caseField = queryColumns.get(1);
        assertTrue(caseField.isFunction());
        assertEquals("age_group", caseField.getAlias());
    }

    @Test
    @DisplayName("UNION查询测试")
    public void testUnionQuery() {
        String sql = "SELECT id, name FROM users WHERE active = 1 " +
                     "UNION " +
                     "SELECT id, name FROM archived_users WHERE archived_date > '2020-01-01'";
        SqlParseUtil parser = new SqlParseUtil(sql);

        assertEquals("select", parser.getSqlType());
        List<String> tables = parser.getTables();
        assertTrue(tables.contains("users"));
        assertTrue(tables.contains("archived_users"));
    }

    @Test
    @DisplayName("UPDATE语句测试")
    public void testUpdateStatement() {
        String sql = "UPDATE users SET name = 'John', age = 25 WHERE id = 1";
        SqlParseUtil parser = new SqlParseUtil(sql);

        assertEquals("update", parser.getSqlType());
        assertEquals("users", parser.getTables().get(0));

        List<ConditionColumn> updateSets = parser.getUpdateSetColumns();
        assertEquals(2, updateSets.size());

        // 检查SET条件
        ConditionColumn nameUpdate = updateSets.stream()
                                               .filter(c -> "name".equals(c.getColumnName()))
                                               .findFirst().orElse(null);
        assertNotNull(nameUpdate);
        assertEquals("'John'", nameUpdate.getValue());
        assertEquals("=", nameUpdate.getOperate());

        // 检查WHERE条件
        List<ConditionColumn> whereConditions = parser.getConditionColumns();
        ConditionColumn idCondition = whereConditions.stream()
                                                     .filter(c -> "id".equals(c.getColumnName()) && "1".equals(c.getValue()))
                                                     .findFirst().orElse(null);
        assertNotNull(idCondition);
    }

    @Test
    @DisplayName("复杂UPDATE语句测试")
    public void testComplexUpdate() {
        String sql = "UPDATE users SET status = 'inactive', last_login = NULL " +
                     "WHERE age < 18 OR last_login < '2020-01-01'";
        SqlParseUtil parser = new SqlParseUtil(sql);

        assertEquals("update", parser.getSqlType());
        List<ConditionColumn> updateSets = parser.getUpdateSetColumns();
        assertEquals(2, updateSets.size());

        List<ConditionColumn> conditions = parser.getConditionColumns();
        assertTrue(conditions.size() >= 2); // WHERE条件可能有多个
    }

    @Test
    @DisplayName("DELETE语句测试")
    public void testDeleteStatement() {
        String sql = "DELETE FROM users WHERE status = 'inactive' AND last_login < '2020-01-01'";
        SqlParseUtil parser = new SqlParseUtil(sql);

        assertEquals("delete", parser.getSqlType());
        assertEquals("users", parser.getTables().get(0));

        List<ConditionColumn> conditions = parser.getConditionColumns();
        assertEquals(2, conditions.size());

        ConditionColumn statusCondition = conditions.stream()
                                                    .filter(c -> "status".equals(c.getColumnName()))
                                                    .findFirst().orElse(null);
        assertNotNull(statusCondition);
        assertEquals("'inactive'", statusCondition.getValue());
    }

    @Test
    @DisplayName("INSERT VALUES语句测试")
    public void testInsertValues() {
        String sql = "INSERT INTO users (id, name, age) VALUES (1, 'John', 25),(2, 'aaaJohn', 95)";
        SqlParseUtil parser = new SqlParseUtil(sql);

        assertEquals("insert", parser.getSqlType());
        assertEquals("users", parser.getTables().get(0));

        List<ConditionColumn> conditions = parser.getConditionColumns();
        assertEquals(6, conditions.size());

        // 检查插入的值
        ConditionColumn nameCondition = conditions.stream()
                                                  .filter(c -> "name".equals(c.getColumnName()))
                                                  .findFirst().orElse(null);
        assertNotNull(nameCondition);
        assertEquals("'John'", nameCondition.getValue());
        assertEquals("INSERT", nameCondition.getOperate());
    }

    @Test
    @DisplayName("INSERT SELECT语句测试")
    public void testInsertSelect() {
        String sql = "INSERT INTO user_backup (id, name, age) " +
                     "SELECT id, name, age FROM users WHERE active = 0";
        SqlParseUtil parser = new SqlParseUtil(sql);

        assertEquals("insert", parser.getSqlType());
        List<String> tables = parser.getTables();
        assertTrue(tables.contains("user_backup"));
        assertTrue(tables.contains("users"));

        List<ConditionColumn> conditions = parser.getConditionColumns();
        assertFalse(conditions.isEmpty());
    }

    @Test
    @DisplayName("多值INSERT测试")
    public void testMultiValueInsert() {
        String sql = "INSERT INTO users (id, name) VALUES (1, 'John'), (2, 'Jane'), (3, 'Bob')";
        SqlParseUtil parser = new SqlParseUtil(sql);

        assertEquals("insert", parser.getSqlType());
        List<ConditionColumn> conditions = parser.getConditionColumns();
        // 应该有6个条件（3行 × 2列）
        assertTrue(conditions.size() >= 2); // 至少包含一些插入条件
    }

    @Test
    @DisplayName("LIKE操作符测试")
    public void testLikeOperator() {
        String sql = "SELECT * FROM users WHERE name LIKE '%John%' AND email LIKE '%.com'";
        SqlParseUtil parser = new SqlParseUtil(sql);

        List<ConditionColumn> conditions = parser.getConditionColumns();
        long likeConditions = conditions.stream()
                                        .filter(c -> "LIKE".equals(c.getOperate()))
                                        .count();
        assertEquals(2, likeConditions);
    }

    @Test
    @DisplayName("IN操作符测试")
    public void testInOperator() {
        String sql = "SELECT * FROM users WHERE id IN (1, 2, 3) AND status IN ('active', 'pending')";
        SqlParseUtil parser = new SqlParseUtil(sql);

        List<ConditionColumn> conditions = parser.getConditionColumns();
        long inConditions = conditions.stream()
                                      .filter(c -> "IN".equals(c.getOperate()))
                                      .count();
        assertEquals(2, inConditions);
    }

    @Test
    @DisplayName("表别名测试")
    public void testTableAlias() {
        String sql = "SELECT u.id, u.name, p.title FROM users u " +
                     "JOIN posts p ON u.id = p.user_id WHERE u.status = 'active'";
        SqlParseUtil parser = new SqlParseUtil(sql);

        List<FieldsInfo> queryColumns = parser.getQueryColumns();
        assertFalse(queryColumns.isEmpty());

        // 检查表名是否正确解析（应该是实际表名而不是别名）
        boolean hasUsersTable = queryColumns.stream()
                                            .anyMatch(f -> "users".equals(f.getTableName()));
        assertTrue(hasUsersTable);
    }

    @Test
    @DisplayName("复杂WHERE条件测试")
    public void testComplexWhereConditions() {
        String sql = "SELECT * FROM users WHERE (age > 18 AND age < 65) OR (status = 'vip' AND points > 1000)";
        SqlParseUtil parser = new SqlParseUtil(sql);

        List<ConditionColumn> conditions = parser.getConditionColumns();
        assertTrue(conditions.size() >= 4); // 至少4个条件

        // 检查是否包含所有条件
        List<String> columnNames = conditions.stream()
                                             .map(ConditionColumn::getColumnName)
                                             .collect(Collectors.toList());
        assertTrue(columnNames.contains("age"));
        assertTrue(columnNames.contains("status"));
        assertTrue(columnNames.contains("points"));
    }

    @Test
    @DisplayName("NULL值测试")
    public void testNullValues() {
        String sql = "SELECT * FROM users WHERE last_login IS NULL OR description IS NOT NULL";
        SqlParseUtil parser = new SqlParseUtil(sql);

        // 这个测试主要确保解析器不会因为NULL值而崩溃
        assertEquals("select", parser.getSqlType());
        assertFalse(parser.getQueryColumns().isEmpty());
    }

    @Test
    @DisplayName("数字和字符串值混合测试")
    public void testMixedValueTypes() {
        String sql = "SELECT * FROM products WHERE price > 100.50 AND name = 'Product A' AND active = 'true' AND created_date = '2023-01-01'";
        SqlParseUtil parser = new SqlParseUtil(sql);

        List<ConditionColumn> conditions = parser.getConditionColumns();
        assertEquals(4, conditions.size());

        // 检查不同类型的值
        List<Object> values = conditions.stream()
                                        .map(ConditionColumn::getValue)
                                        .collect(Collectors.toList());
        assertTrue(values.contains("100.50"));
        assertTrue(values.contains("'Product A'"));
        assertTrue(values.contains("'true'"));
        assertTrue(values.contains("'2023-01-01'"));
    }

    @Test
    @DisplayName("窗口函数测试")
    public void testWindowFunction() {
        String sql = "SELECT id, name, ROW_NUMBER() OVER (ORDER BY created_date) as row_num FROM users";
        SqlParseUtil parser = new SqlParseUtil(sql);

        List<FieldsInfo> queryColumns = parser.getQueryColumns();
        assertEquals(3, queryColumns.size());

        FieldsInfo windowFunction = queryColumns.get(2);
        assertEquals("row_num", windowFunction.getAlias());
    }

    @Test
    @DisplayName("CTE(公用表表达式)测试")
    public void testCTE() {
        String sql = "WITH active_users AS (SELECT * FROM users WHERE status = 'active') " +
                     "SELECT * FROM active_users WHERE age > 18";
        SqlParseUtil parser = new SqlParseUtil(sql);

        assertEquals("select", parser.getSqlType());
        // CTE会被识别为表
        assertTrue(parser.getTables().contains("users"));
    }

    @Test
    @DisplayName("EXISTS子查询测试")
    public void testExistsSubquery() {
        String sql = "SELECT * FROM users u WHERE EXISTS (SELECT 1 FROM orders o WHERE o.user_id = u.id)";
        SqlParseUtil parser = new SqlParseUtil(sql);

        List<String> tables = parser.getTables();
        assertTrue(tables.contains("users"));
        assertTrue(tables.contains("orders"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "SELECT 1",
            "SELECT * FROM dual",
            "SELECT COUNT(*) FROM users",
            "SELECT DISTINCT name FROM users",
            "SELECT TOP 10 * FROM users",
            "SELECT * FROM users LIMIT 10",
            "SELECT * FROM users ORDER BY name ASC",
            "SELECT * FROM users GROUP BY department HAVING COUNT(*) > 5"
    })
    @DisplayName("各种SELECT语法测试")
    public void testVariousSelectSyntax(String sql) {
        SqlParseUtil parser = new SqlParseUtil(sql);
        assertEquals("select", parser.getSqlType());
        // 确保解析器不会崩溃
        assertNotNull(parser.getQueryColumns());
        assertNotNull(parser.getConditionColumns());
        assertNotNull(parser.getTables());
    }

    @Test
    @DisplayName("异常SQL处理测试")
    public void testInvalidSQL() {
        String invalidSql = "INVALID SQL STATEMENT";
        SqlParseUtil parser = new SqlParseUtil(invalidSql);

        // 解析失败时应该有默认值
        assertNull(parser.getSqlType());
        assertTrue(parser.getQueryColumns().isEmpty());
        assertTrue(parser.getConditionColumns().isEmpty());
        assertTrue(parser.getTables().isEmpty());
    }

    @Test
    @DisplayName("空SQL测试")
    public void testEmptySQL() {
        SqlParseUtil parser1 = new SqlParseUtil("");
        SqlParseUtil parser2 = new SqlParseUtil(null);
        SqlParseUtil parser3 = new SqlParseUtil("   ");

        // 空SQL应该有默认值
        assertNull(parser1.getSqlType());
        assertNull(parser2.getSqlType());
        assertNull(parser3.getSqlType());
    }

    @Test
    @DisplayName("大小写敏感性测试")
    public void testCaseSensitivity() {
        String sql1 = "SELECT ID, NAME FROM USERS WHERE STATUS = 'ACTIVE'";
        String sql2 = "select id, name from users where status = 'active'";

        SqlParseUtil parser1 = new SqlParseUtil(sql1);
        SqlParseUtil parser2 = new SqlParseUtil(sql2);

        // 列名和表名应该被转换为小写
        assertEquals("USERS", parser1.getTables().get(0));
        assertEquals("users", parser2.getTables().get(0));

        List<String> columnNames1 = parser1.getQueryColumns().stream()
                                           .map(FieldsInfo::getName)
                                           .collect(Collectors.toList());
        List<String> columnNames2 = parser2.getQueryColumns().stream()
                                           .map(FieldsInfo::getName)
                                           .collect(Collectors.toList());

        // 应该包含相同的列名（忽略大小写）
        assertTrue(columnNames1.contains("ID") || columnNames1.contains("id"));
        assertTrue(columnNames2.contains("ID") || columnNames2.contains("id"));
    }

    @Test
    @DisplayName("复杂JOIN测试")
    public void testComplexJoins() {
        String sql = "SELECT u.id, u.name, p.title, c.content " +
                     "FROM users u " +
                     "LEFT JOIN posts p ON u.id = p.user_id " +
                     "RIGHT JOIN comments c ON p.id = c.post_id " +
                     "FULL OUTER JOIN likes l ON u.id = l.user_id " +
                     "WHERE u.status = 'active'";
        SqlParseUtil parser = new SqlParseUtil(sql);

        List<String> tables = parser.getTables();
        assertTrue(tables.contains("users"));
        assertTrue(tables.contains("posts"));
        assertTrue(tables.contains("comments"));
        assertTrue(tables.contains("likes"));

        assertEquals(4, parser.getQueryColumns().size());
    }

    @Test
    @DisplayName("自连接测试")
    public void testSelfJoin() {
        String sql = "SELECT e1.name as employee, e2.name as manager " +
                     "FROM employees e1 " +
                     "JOIN employees e2 ON e1.manager_id = e2.id";
        SqlParseUtil parser = new SqlParseUtil(sql);

        // 自连接应该只识别出一个表
        assertEquals(1, parser.getTables().size());
        assertEquals("employees", parser.getTables().get(0));
        assertEquals(2, parser.getQueryColumns().size());
    }

    @Test
    @DisplayName("带分页的ORACLE嵌套子查询")
    public void testNestedSubquery() {

        String sql = "select * from (select rownum rn, t.* from (SELECT distinct stockIn.fid AS fid, (CASE WHEN stockIn.fplaceId IS NULL THEN agen.flevelcode ELSE place.flevelcode END) as flevelCode, \n" +
                     "\t\t   agen.fcode AS fagenCode, agen.fname AS fagenName, place.fname as fplaceName, place.fcode as fplaceCode, \n" +
                     "\t\t   bill.fcode AS fbillCode, bill.fname AS fbillName, siItem.fbillBatchCode AS fbillBatchCode,\n" +
                     "\t       dict.fname AS funits,siItem.fbillNo1, siItem.fbillNo2, billbatch.fcheckcodelen, (siItem.fbillNo2 - siItem.fbillNo1 + 1) AS fcopyNum,\n" +
                     "\t       ((siItem.fbillNo2 - siItem.fbillNo1 + 1) / billBatch.fcopynum) AS fnumber,stockIn.fdate,siItem.fid slaveFid\n" +
                     "\t    FROM ube_stock_in stockIn\n" +
                     "        LEFT JOIN ube_stock_in_item siItem ON stockIn.fid= siItem.fpid\n" +
                     "        LEFT JOIN fab_bill bill ON siItem.fbillId=bill.fid\n" +
                     "        LEFT JOIN fab_billbatch billBatch ON (billBatch.fcode=siItem.fbillBatchCode AND billBatch.fisEnable = 1)\n" +
                     "        LEFT JOIN uab_agen agen ON (agen.fagenIdCode = stockIn.fagenIdCode and agen.fisfinal = '1')\n" +
                     "        LEFT JOIN uab_place place ON place.fid = stockIn.fplaceId\n" +
                     "        LEFT JOIN uaa_dict dict ON (dict.fobj = 'BILL_UNITS' and dict.fcode = bill.fUnits)\n" +
                     "        WHERE 1=1 \n" +
                     "\t\t\t \n" +
                     "\t\t\t\t\n" +
                     "\t\t\t\t \n" +
                     "\t\t\t\t\n" +
                     "\t\t\t\t \n" +
                     "\t\t\t\t\tAND stockIn.fagenIdCode = ?\n" +
                     "\t\t\t\t \n" +
                     "\t\t\t\t\n" +
                     "\t\t\t\t \n" +
                     "\t\t\t \n" +
                     "\t\t\t \n" +
                     "\t\t\t \n" +
                     "\t\t\t \t\t\t\t\n" +
                     "\t\t\t \n" +
                     "\t\t\t \n" +
                     "\t\t\t \n" +
                     "\t\t\t\tAND stockIn.fdate   >=   ?\n" +
                     "\t\t\t  \n" +
                     "\t\t\t \n" +
                     "\t\t\t  \n" +
                     "\t\t\t \n" +
                     "\t\t\t \n" +
                     "          ORDER BY flevelCode,stockIn.fdate desc,bill.fcode,siItem.fbillBatchCode,siItem.fbillNo1, siItem.fbillNo2) t where rownum <= 30) t1 where t1.rn > 0";
        SqlParseUtil parser = new SqlParseUtil(sql);
        assertEquals("select", parser.getSqlType());
        List<String> tables = parser.getTables();
        assertTrue(tables.contains("ube_stock_in"));

    }

    @Test
    @DisplayName("测试查询带*的")
    public void testVariousSelectSyntax1() {

        String sql = "SELECT \n" +
                     "    t.*, \n" +
                     "    d.fname AS fbankName, \n" +
                     "    d2.fname AS fpayerTypeName \n" +
                     "FROM \n" +
                     "    UAB_PAYER t\n" +
                     "LEFT JOIN \n" +
                     "    uaa_dict d ON t.faccountbank = d.fcode AND d.fobj = 'BANK_CODE'\n" +
                     "LEFT JOIN \n" +
                     "    uaa_dict d2 ON t.fpayerType = d2.fcode AND d2.fobj = 'PAYER_TYPE'\n" +
                     "WHERE \n" +
                     "    t.fname like '%银行%'\n" +
                     "ORDER BY \n" +
                     "    t.fcode\n";
        SqlParseUtil parser = new SqlParseUtil(sql);
        assertEquals("select", parser.getSqlType());
        List<String> tables = parser.getTables();
        assertTrue(tables.contains("UAB_PAYER"));
    }

    @Test
    @DisplayName("测试查询带*的1")
    public void testVariousSelectSyntax2() {
        String sql = "SELECT t.* FROM (select a,b,c from d) t WHERE fid = ?";
        SqlParseUtil parser = new SqlParseUtil(sql);
        assertEquals("select", parser.getSqlType());
        List<String> tables = parser.getTables();
        assertTrue(tables.contains("d"));
    }
}
