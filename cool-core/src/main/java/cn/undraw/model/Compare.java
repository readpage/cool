package cn.undraw.model;

import java.util.ArrayList;
import java.util.List;

public class Compare<T> {
    // 新增数据
    public List<T> saveList = new ArrayList<>();
    // 修改数据
    public List<T> updateList = new ArrayList<>();
    // 删除数据
    public List<T> removeList = new ArrayList<>();

    @Override
    public String toString() {
        return "Compare{" +
                "saveList=" + saveList +
                ", updateList=" + updateList +
                ", removeList=" + removeList +
                '}';
    }
}
