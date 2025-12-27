package com.undraw.controller;

import cn.idev.excel.annotation.ExcelProperty;
import cn.undraw.util.bean.AnnoUtils;
import cn.undraw.util.ConvertUtils;
import cn.undraw.util.StrUtils;
import cn.undraw.util.log.annotation.OperateLog;
import cn.undraw.util.result.R;
import com.undraw.domain.dto.ImportDemo;
import com.undraw.domain.model.Employee;
import com.undraw.domain.model.Employee1;
import com.undraw.domain.model.Student;
import com.undraw.domain.vo.ExcelDB;
import com.undraw.util.excel.ExcelUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static cn.undraw.util.log.enums.OperateTypeEnum.EXPORT;


/**
 * @author readpage
 * @date 2022-12-08 8:25
 */

@RestController
@Tag(name = "excel管理")
@RequestMapping("/excel")
@Slf4j
public class ExcelController {
//    @Operation(summary = "导入数据")
//    @PostMapping("/import")
//    public R upload(@RequestExcel List<ImportDemo> importDemoList, String arg, BindingResult bindingResult) {
//        System.out.println(arg);
//        // JSR 303 校验通用校验获取失败的数据
//        List<ErrorMessage> errorMessageList = (List<ErrorMessage>) bindingResult.getTarget();
//        return R.ok(importDemoList);
//    }
    /**
     * 去重复
     * @param list
     * @return
     */
    public List<ImportDemo> nodup(List<ImportDemo> list) {
        if (StrUtils.isEmpty(list)) {
            return new ArrayList<>();
        }
        Set<ImportDemo> set = new TreeSet<>(Comparator.comparing(o -> o.getKey()));
        set.addAll(list);
        List<String> keys = set.stream().map(o -> o.getKey()).collect(Collectors.toList());

        // 查询重复的键值
//        List<String> removeKeys = mealMapper.listByKey(keys);
        List<ImportDemo> nodupList = null;
//        List<ImportDemo> nodupList = set.stream()
//                .filter(o -> !removeKeys.contains(o.getKey())
//                .collect(Collectors.toList());
        return nodupList;
    }

    @Operation(summary = "导入数据")
    @PostMapping(value = "/import")
    public R upload(@RequestBody MultipartFile file) {
        List<ImportDemo> list = ExcelUtils.read(file, ImportDemo.class);
        return R.ok(list);
    }

    @GetMapping("/export")
    @Operation(summary = "导出excel")
    @OperateLog(type = EXPORT)
    public void export(HttpServletResponse response) {
        try {
           Object o = AnnoUtils.getValueByField(Employee.class.getDeclaredField("name"), ExcelProperty.class);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
        ExcelUtils.export(response, "导出", "sheet", Employee.employeeList);
    }

    @GetMapping(value = "/export2")
    @Operation(summary = "导出excel2")
    @OperateLog(type = EXPORT)
    public void export2(HttpServletResponse response) {
        List<ExcelDB> excelDBS = new ArrayList<>();
        // 测试数据
        ExcelDB excelDB = new ExcelDB();
        excelDB.setName("数据1");
        excelDB.setIntroduce("数据介绍");
        excelDB.setImg("http://eip.supinfood.com/static/basic/2024/5/14/113991700279459840.jpg,http://eip.supinfood.com/static/basic/2024/5/14/113992591195766784.png");
        excelDB.setImgList(Arrays.asList("http://eip.supinfood.com/static/basic/2024/5/14/113992591195766784.png",
                "http://eip.supinfood.com/static/basic/2024/5/14/113992448358744064.jpg")
        );
        excelDB.setIntroduce("介绍");
        excelDBS.add(excelDB);
        ExcelDB excelDB2 = new ExcelDB();
        excelDB2.setName("数据2");
        excelDB2.setIntroduce("数据介绍2");
        excelDB2.setImg("");
        excelDB2.setImgList(Arrays.asList(""));
        excelDBS.add(excelDB2);
        ExcelUtils.export(response, "数据信息", "sheet", excelDBS);
    }

    @GetMapping("/custom-header-export")
    @Operation(summary = "自定义表头导出excel")
    @OperateLog(type = EXPORT)
    public void customHeaderExport(HttpServletResponse response) {
//        ExcelUtils.export(response, "自定义表头导出", "sheet", Employee.employeeList, t -> {
//            t.(getHeader());
//        });
    }

    private List<List<String>> getHeader() {
        String now = LocalDate.now().toString();
        List<List<String>> head = new ArrayList<>();
        head.add(Arrays.asList(now, "区域", "区域"));
        head.add(Arrays.asList(now, "序号", ""));
        head.add(Arrays.asList(now, "具体", "内容"));
        head.add(Arrays.asList(now, "具体", "其他"));
        return head;
    }

    @GetMapping("/more-export")
    @Operation(summary = "导出多个工作表excel")
    @OperateLog(type = EXPORT)
    public void moveExport(HttpServletResponse response) {
        List<ExcelUtils.ExcelModel> excelModelList = new ArrayList<>(Arrays.asList(
                new ExcelUtils.ExcelModel("employee", ConvertUtils.cloneDeep(Employee.employeeList, Employee1.class)),
                new ExcelUtils.ExcelModel("employee2", Employee.employeeList),
                new ExcelUtils.ExcelModel("student", Student.studentList)
        ));
        ExcelUtils.moreExport(response, "多个工作表导出", excelModelList);
    }
}
