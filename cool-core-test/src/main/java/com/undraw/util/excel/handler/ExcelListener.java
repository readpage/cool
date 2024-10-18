package com.undraw.util.excel.handler;
 
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
 
import java.util.ArrayList;
import java.util.List;
 
public class ExcelListener extends AnalysisEventListener {
    /**
     * 可以通过实例获取该值
     */
    private List<Object> dataList = new ArrayList<>();
 
    /**
     * 逐行读取excel内容
     * */
    @Override
    public void invoke(Object object, AnalysisContext analysisContext) {
        //数据存储到list，供批量处理，或后续自己业务逻辑处理。
        dataList.add(object);
    }
 
    /**
     * 读取完成之后执行
     * */
    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        //非必要语句，查看导入的数据
//        System.out.println("导入的数据条数为: " + dataList.size());
    }
 

    public List<Object> getDataList() {
        return dataList;
    }
 
    public void setDataList(List<Object> dataList) {
        this.dataList = dataList;
    }
}