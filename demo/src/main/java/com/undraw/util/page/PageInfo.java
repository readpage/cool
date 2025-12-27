package com.undraw.util.page;

import cn.undraw.util.ConvertUtils;
import com.github.pagehelper.Page;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

@Data
@ToString
public class PageInfo<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long total;
    private List<T> list;

    public PageInfo() {
        super();
    }

    public PageInfo(List<T> list) {
        if (list instanceof Page) {
            Page page = (Page) list;
            this.total = page.getTotal();
            this.list = page;
        }
    }

    public PageInfo(List list, Class<T> clazz) {
        this.list = ConvertUtils.cloneDeep(list, clazz);
        Page page = (Page) list;
        this.total = page.getTotal();
    }

}
