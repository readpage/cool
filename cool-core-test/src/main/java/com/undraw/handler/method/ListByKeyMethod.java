package com.undraw.handler.method;

import com.baomidou.mybatisplus.core.injector.AbstractMethod;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;

/**
 * 批量插入方法实现
 * @author readpage
 * @date 2025-02-13 22:00
 */
@Slf4j
public class ListByKeyMethod extends AbstractMethod {


    /**
     * SELECT * FROM table WHERE (col1, col12) IN (('value1', 'value2'), ('value3', 'value4'))
     * <script>
     *   SELECT * FROM table WHERE (col1, col12) IN (
     *     <foreach collection="list" item="item" index="index" open="(" separator="),(" close=")">
     *       <foreach collection="item" item="v" index="k" separator=",">
     *         #{v}
     *       </foreach>
     *     </foreach>
     * 	 )
     * </script>
     * @param mapperClass
     * @param modelClass
     * @param tableInfo
     * @return
     */
    @Override
    public MappedStatement injectMappedStatement(Class<?> mapperClass, Class<?> modelClass, TableInfo tableInfo) {
//        Map<String, ColumnCache> columnMap = LambdaUtils.getColumnMap(Role.class);
//        ColumnCache columnCache = columnMap.get("CREATETIME");
//        String column = columnCache.getColumn();
        System.out.println(tableInfo);
//        final String sql = "<script>SELECT * FROM %s WHERE (%s) IN (%s)</script>";
        final String sql = "<script>SELECT #{ew.sqlSelect}, %S</script>";
        String selectSql = this.sqlSelectColumns(tableInfo, true);
//        String inMoreSql = this.prepareInMoreSql(tableInfo);

        final String sqlResult = String.format(sql, tableInfo.getTableName(), selectSql);
        SqlSource sqlSource = languageDriver.createSqlSource(configuration, sqlResult, modelClass);

        // 第三个参数必须和RootMapper的自定义方法名一致
        return this.addSelectMappedStatementForTable(mapperClass, SqlMethodChild.LIST_BY_key.getMethod(), sqlSource, tableInfo);
    }

    private String prepareInMoreSql(TableInfo tableInfo) {
        final StringBuffer valueSql = new StringBuffer();
        valueSql.append("<foreach collection=\"list\" item=\"item\" index=\"index\" open=\"(\" separator=\"),(\" close=\")\">");
        valueSql.append("#{item.").append(tableInfo.getKeyProperty()).append("},");
        tableInfo.getFieldList().forEach(x -> valueSql.append("#{item.").append(x.getProperty()).append("},"));
        valueSql.delete(valueSql.length() - 1, valueSql.length());
        valueSql.append("</foreach>");
        return valueSql.toString();
    }
}
