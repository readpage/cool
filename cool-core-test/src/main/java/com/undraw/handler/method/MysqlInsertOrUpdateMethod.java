package com.undraw.handler.method;

import cn.undraw.util.StrUtils;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.mapping.SqlSource;

@Slf4j
public class MysqlInsertOrUpdateMethod extends InsertOrUpdateBathMethod {
    @Override
    protected SqlSource prepareSqlSource(TableInfo tableInfo, Class<?> modelClass) {
        final String sql = "<script>insert into %s %s values %s ON DUPLICATE KEY UPDATE %s</script>";
        final String tableName = tableInfo.getTableName();
        final String filedSql = prepareFieldSql(tableInfo);
        final String modelValuesSql = prepareModelValuesSql(tableInfo);
        final String duplicateKeySql = prepareDuplicateKeySql(tableInfo);
        final String sqlResult = String.format(sql, tableName, filedSql, modelValuesSql,duplicateKeySql);
        log.debug("sqlResult----->{}", sqlResult);
        return languageDriver.createSqlSource(configuration, sqlResult, modelClass);
    }

    @Override
    protected String prepareInsertOrUpdateBathName() {
        return "insertOrUpdateBath";
    }

    /**
     * 准备ON DUPLICATE KEY UPDATE sql
     * @param tableInfo
     * @return
     */
    private String prepareDuplicateKeySql(TableInfo tableInfo) {
        final StringBuffer duplicateKeySql = new StringBuffer();
        if(StrUtils.isNotEmpty(tableInfo.getKeyColumn())) {
            duplicateKeySql.append(tableInfo.getKeyColumn()).append("=values(").append(tableInfo.getKeyColumn()).append("),");
        }

        tableInfo.getFieldList().forEach(x -> {
            duplicateKeySql.append(x.getColumn())
                    .append("=values(")
                    .append(x.getColumn())
                    .append("),");
        });
        duplicateKeySql.delete(duplicateKeySql.length() - 1, duplicateKeySql.length());
        return duplicateKeySql.toString();
    }

    /**
     * 准备属性名
     * @param tableInfo
     * @return
     */
    private String prepareFieldSql(TableInfo tableInfo) {
        StringBuffer fieldSql = new StringBuffer();
        fieldSql.append(tableInfo.getKeyColumn()).append(",");
        tableInfo.getFieldList().forEach(x -> {
            fieldSql.append(x.getColumn()).append(",");
        });
        fieldSql.delete(fieldSql.length() - 1, fieldSql.length());
        fieldSql.insert(0, "(");
        fieldSql.append(")");
        return fieldSql.toString();
    }

    private String prepareModelValuesSql(TableInfo tableInfo){
        final StringBuffer valueSql = new StringBuffer();
        valueSql.append("<foreach collection=\"list\" item=\"item\" index=\"index\" open=\"(\" separator=\"),(\" close=\")\">");
        if(StrUtils.isNotEmpty(tableInfo.getKeyProperty())) {
            valueSql.append("#{item.").append(tableInfo.getKeyProperty()).append("},");
        }
        tableInfo.getFieldList().forEach(x -> valueSql.append("#{item.").append(x.getProperty()).append("},"));
        valueSql.delete(valueSql.length() - 1, valueSql.length());
        valueSql.append("</foreach>");
        return valueSql.toString();
    }
}
