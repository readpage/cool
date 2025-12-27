package com.undraw.util.page;

import cn.undraw.util.bean.AnnoUtils;
import cn.undraw.util.ConvertUtils;
import cn.undraw.util.ReflectUtils;
import cn.undraw.util.StrUtils;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;

@Data
@Schema(title = "分页参数")
public class PageParam {

    @Schema(title = "页数")
    private Integer current;

    @Schema(title = "页大小")
    private Integer size;

    @Schema(title = "排序参数")
    private String sort;

    @Setter(AccessLevel.NONE)
    @Getter(AccessLevel.NONE)
    private boolean flag = false;

    public String sort(Class<?> clazz) {
        this.sort = this.toSort(this.sort, clazz);
        this.flag = true;
        return sort;
    }

    public String sort(Class<?> clazz, String suffix) {
        this.sort = this.toSort(this.sort, clazz, suffix);
        this.flag = true;
        return sort;
    }

    public String getSort() {
        if (this.sort != null && this.sort.startsWith("ORDER BY") && this.flag) {
            return sort;
        }
        return "";
    }

    /**
     * sql排序
     * @param json
     * @param clazz
     * @param suffix
     * @return
     */
    private String toSort(String json, Class<?> clazz, String suffix) {
        StringBuffer sb = new StringBuffer();
        sb.append("ORDER BY");
        StringJoiner sj = new StringJoiner(",");
        Map<String, String> map = null;
        try {
            map = ConvertUtils.cloneDeep(json, Map.class);
        } catch (Exception e) {
            return "";
        }
        if (StrUtils.isEmpty(map)) {
            return "";
        }

        List<Field> fields = ReflectUtils.getFields(clazz);
        for (Map.Entry<String, String> entry : map.entrySet()) {
            for (Field field : fields) {
                Object exist = StrUtils.isNull(AnnoUtils.getValueByField(field, TableField.class, "exist"), true);
                if (Objects.equals(entry.getKey(), field.getName()) && ConvertUtils.toBoolean(exist)) {
                    String key = StrUtils.toUnderScoreCase(field.getName());
                    if ("asc".equals(entry.getValue())) {
                        sj.add(String.format(" %s ASC", key));
                    } else if ("desc".equals(entry.getValue())) {
                        sj.add(String.format(" %s DESC", key));
                    }
                }
            }
        }
        if (StrUtils.isEmpty(sj.toString())) {
            return "";
        }

        if (StrUtils.isNotEmpty(suffix)) {
            String[] strArr = suffix.split(" ");
            if (StrUtils.isNotEmpty(strArr)) {
                for (Field field : fields) {
                    if (Objects.equals(field.getName(), strArr[0])) {
                        String key = StrUtils.toUnderScoreCase(field.getName());
                        if (strArr.length > 1 && strArr[1] != null) {
                            if ("DESC".equals(strArr[1].toUpperCase())) {
                                key += " DESC";
                            }
                        }
                        sj.add(String.format(" %s", key));
                    }
                }
            }

        }
        sb.append(sj);
        return sb.toString();
    }

    private String toSort(String json, Class<?> clazz) {
        return toSort(json, clazz, null);
    }

    /**
     * 填充日期范围参数
     */
    public LocalDate[] fillDateRange(LocalDate[] localDate) {
        if (localDate == null || localDate.length < 2) {
            return new LocalDate[]{LocalDate.now(), LocalDate.now()};
        }
        return localDate;
    }
}