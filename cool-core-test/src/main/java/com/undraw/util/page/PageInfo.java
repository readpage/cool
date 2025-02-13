package com.undraw.util.page;


import com.github.pagehelper.Page;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

@Setter
@Getter
@ToString
public class PageInfo<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long total;
    private List<T> records;

    public PageInfo() {
        super();
    }

    public PageInfo(List<T> list) {
        if (list instanceof Page) {
            Page page = (Page) list;
            this.total = page.getTotal();
            this.records = page;
        }
    }

}
