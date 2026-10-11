package cn.edu.hebau.aitravel.util;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/** 分页查询结果：当前页数据、总条数、页码和每页条数。 */
@Data
@AllArgsConstructor
public class PageResult<T> {
    private List<T> list;
    private Long total;
    private Integer pageNum;
    private Integer pageSize;
}
