package moe.caa.multilogin.core.util;

import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * 简单的分组工具
 */
@NoArgsConstructor
public class GroupBurstArrayList<T> {
    private final LinkedList<ArrayList<T>> contents = new LinkedList<>();

    /**
     * 添加一组数据
     *
     * @param list 一组数据
     */
    public void offer(ArrayList<T> list) {
        contents.offer(list);
    }

    /**
     * 判断是否含有下一分组
     *
     * @return 是否拥有下一分组
     */
    public boolean hasNext() {
        return !contents.isEmpty();
    }

    /**
     * 获得下一组的数据量
     *
     * @return 下一组的数据量
     */
    public int nextQuantity() {
        return contents.get(0).size();
    }

    /**
     * 得到并且移除下一组
     *
     * @return 下一组
     */
    public ArrayList<T> next() {
        return contents.poll();
    }

    /**
     * 获得聚合体大小
     *
     * @return 聚合体大小
     */
    public int size() {
        int num = 0;
        for (ArrayList<T> list : contents) {
            num += list.size();
        }
        return num;
    }
}
