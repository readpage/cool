package com.example.template.util;

import java.util.Collections;
import java.util.List;

/**
 * 分页查询结果 — 包含总记录数 + 当前页数据。
 *
 * @param <T> 实体类型
 */
public class PageResult<T> {

    /** 总记录数 */
    private long total;

    /** 当前页码（从 1 开始） */
    private int current;

    /** 每页条数 */
    private int size;

    /** 总页数 */
    private int pages;

    /** 当前页数据 */
    private List<T> list;

    public PageResult() {
    }

    public PageResult(long total, int current, int size, List<T> list) {
        this.total = total;
        this.current = current;
        this.size = size;
        this.pages = size > 0 ? (int) Math.ceil((double) total / size) : 0;
        this.list = list != null ? list : Collections.emptyList();
    }

    // ==================== getter / setter ====================

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public int getCurrent() {
        return current;
    }

    public void setCurrent(int current) {
        this.current = current;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getPages() {
        return pages;
    }

    public void setPages(int pages) {
        this.pages = pages;
    }

    public List<T> getList() {
        return list;
    }

    public void setList(List<T> list) {
        this.list = list;
    }
}
