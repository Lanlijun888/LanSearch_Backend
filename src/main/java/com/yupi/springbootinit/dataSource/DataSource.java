package com.yupi.springbootinit.dataSource;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * @author Editor
 */
public interface DataSource<T> {
    /**
     * 搜索:实现接口
     * @param searchText
     * @param pageNum
     * @param pageSize
     * @return
     */
    Page<T> doSearch(String searchText, long pageNum, long pageSize);
}
