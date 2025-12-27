package com.undraw.gen;

import cn.undraw.util.FileUtils;
import cn.undraw.util.StrUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;

public class ClassGenerator {

    private String url = "jdbc:mysql://localhost:3306/cool?serverTimezone=GMT%2B8";
    private String user = "root";
    private String password = "root";

    private String projectPath = System.getProperty("user.dir");

    private String packageName = "com.example.domain.entity";

    private List<String> tables = new ArrayList<>();

    public ClassGenerator() {
        super();
    }

    public ClassGenerator(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }

    public ClassGenerator projectPath(String v) {
        this.projectPath = v;
        return this;
    }

    public ClassGenerator packageName(String v) {
        this.packageName = v;
        return this;
    }

    public ClassGenerator tables(List<String> tables) {
        this.tables = tables;
        return this;
    }

    public void build() {
        for (String table : tables) {
            codeGenerator(table);
        }
    }

    public static ClassGenerator create(String url, String user, String password) {
        return new ClassGenerator(url, user, password);
    }

    public ClassGenerator config(Consumer<ClassGenerator> con) {
        con.accept(this);
        return this;
    }

    public List<Map<String, Object>> queryList(String sql) {
        // 创建数据源
        DataSource dataSource = new DriverManagerDataSource(url, user, password);

        // 创建JdbcTemplate对象
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        // 执行查询并获取结果
        List<Map<String, Object>> results = jdbcTemplate.queryForList(sql);
        return results;
    }

    private List<Map<String, Object>> listColumn(String tableName) {
        String sql = String.format("SELECT * FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = (SELECT DATABASE()) AND TABLE_NAME = '%s' ORDER BY ORDINAL_POSITION", tableName);
        return queryList(sql);
    }

    Map<String, Object> getTable(String tableName) {
        String sql = String.format("SELECT * FROM information_schema.TABLES WHERE TABLE_SCHEMA = (SELECT DATABASE()) AND TABLE_NAME = '%s' LIMIT 1", tableName);
        return queryList(sql).get(0);
    }

    /**
     * 生成类
     * @param tableName 表名
     */
    public void codeGenerator(String tableName){
        String path = projectPath;
        List<Map<String, Object>> columns = listColumn(tableName); //查询所有的表字段信息
        Map<String, Object> table = getTable(tableName);
        String className = getClassName(tableName);
        StringBuffer sb = new StringBuffer();
        sb.append("package "+packageName+";\n\n");
        sb.append("import com.baomidou.mybatisplus.annotation.*;\n");
        sb.append("import com.fasterxml.jackson.annotation.JsonProperty;\n");
        sb.append("import io.swagger.v3.oas.annotations.media.Schema;\n");
        sb.append("import jakarta.validation.constraints.*;\n");
        sb.append("import lombok.Data;\n");
        sb.append("import lombok.NoArgsConstructor;\n");
        sb.append("import lombok.ToString;\n");
        sb.append("import java.io.Serializable;\n");
        sb.append("import java.time.LocalDate;\n");
        sb.append("import java.time.LocalDateTime;\n");
        sb.append("import java.math.BigDecimal;\n");
        sb.append("import cn.idev.excel.annotation.ExcelIgnore;\n");
        sb.append("import cn.idev.excel.annotation.ExcelProperty;\n\n");
        sb.append("@Data\n");
        sb.append("@NoArgsConstructor\n");
        sb.append(String.format("@TableName(\"%s\")\n", tableName));
        sb.append("@ToString\n");
        sb.append(String.format("@Schema(title = \"%s\")\n", StrUtils.isNull(table.get("TABLE_COMMENT"), tableName)));
        sb.append("public class "+className+" implements Serializable {\n\n");
        sb.append("\tprivate static final long serialVersionUID = 1L;\n\n");
        //获取字段信息
        for (Map<String, Object> column : columns) {
            String dataType = String.valueOf(column.get("DATA_TYPE"));//获取数据类型
            String type = classCast(dataType);//数据类型
            String property = getProperty(column.get("COLUMN_NAME"));//属性名称
            String remark = StrUtils.isNull(String.valueOf(column.get("COLUMN_COMMENT")), property);//注解
            remark = remark.split(",")[0];
            String extra = (String) column.get("EXTRA");
            String nullable = (String) column.get("IS_NULLABLE");

            sb.append(String.format("\t@Schema(title = \"%s\")\n", remark));
            // 不能为空
            if (!Objects.equals(extra, "auto_increment") && Objects.equals(nullable, "NO")) {
                List<String> numList = List.of("Integer", "Long", "Double");
                if (numList.contains(type)) {
                    sb.append(String.format("\t@NotNull(message = \"%s为必填项，不能为空\")\n", remark));
                } else if (Objects.equals(type, "String")) {
                    sb.append(String.format("\t@NotBlank(message = \"%s为必填项，不能为空\")\n", remark));
                }
            }
            if (Arrays.asList("createTime", "updateTime", "id").contains(property)) {
                sb.append("\t@ExcelIgnore\n");
            } else {
                sb.append(String.format("\t@ExcelProperty(value = \"%s\")\n", remark));
            }
            // COLUMN_KEY
            if ("createTime".equals(property)) {
                sb.append("\t@JsonProperty(access = JsonProperty.Access.READ_ONLY)\n");
                sb.append("\t@TableField(fill = FieldFill.INSERT)\n");
            } else if ("updateTime".equals(property)) {
                sb.append("\t@JsonProperty(access = JsonProperty.Access.READ_ONLY)\n");
                sb.append("\t@TableField(fill = FieldFill.INSERT_UPDATE)\n");
            }
            if ("id".equals(property)) {
                sb.append("\t@TableId(value = \"id\", type = IdType.AUTO)\n");
            }
            sb.append("\tprivate "+type+" "+property+";\n\n");
        }
        sb.append("\n}");
        //拼接结束

        //生成文件
        path = String.format("%s/src/main/java/%s/%s.java", path, packageName.replace(".", "/"), className);
        try {
            FileUtils.writeStringToFile(new File(path), String.valueOf(sb), "UTF-8");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 生成类名
     * @param tableName 表名
     * @return
     */
    private String getClassName(String tableName){
        String newClassName="";
        int i = tableName.indexOf("_");
        if (i<0){//没有下划线
            newClassName = tableName.substring(0, 1).toUpperCase() + tableName.substring(1);
        }else{//有下划线
            String[] strArr = tableName.split("_");
            StringBuffer sb = new StringBuffer();
            for (int m = 0; m<strArr.length; m++){
                sb.append(strArr[m].substring(0, 1).toUpperCase() + strArr[m].substring(1));
            }
            newClassName=sb.toString();
        }
        return newClassName;
    }

    //生成属性java类型
    private String classCast(Object obj){
        String type="";
        String str=(String)obj;
        if (str.equals("varchar")||str.equals("char")||str.equals("text")){
            type="String";
        } else if (str.equals("int")){
            type="Integer";
        } else if (str.equals("bigint")){
            type="Long";
        } else if (str.equals("double")||str.equals("float")){
            type="Double";
        } else if (str.equals("date")){
            type="LocalDate";
        } else if (str.equals("datetime")) {
            type="LocalDateTime";
        } else if (str.equals("decimal")) {
            type="BigDecimal";
        } else {
            type="String";
        }
        return type;
    }

    //数据库字段名字转java属性名字
    private String getProperty(Object obj){
        String pro="";
        String colum=(String)obj;
        int index = colum.indexOf("_");//判断是否存在下划线
        if (index<0){//没有下划线
            pro=colum.substring(0,1).toLowerCase()+colum.substring(1);//首字母小写
        }else {//有下划线
            StringBuilder sb = new StringBuilder();
            String[] colums = colum.split("_");
            for (int i = 0; i<colums.length; i++){
                if (i==0){
                    sb.append(colums[i].substring(0,1).toLowerCase()+colums[i].substring(1));//拼接第一个,并将首字母小写
                }else{
                    sb.append(colums[i].substring(0,1).toUpperCase()+colums[i].substring(1));//除了第一个都将首字母大写
                }
            }
            pro=sb.toString();
        }

        return pro;
    }

}
