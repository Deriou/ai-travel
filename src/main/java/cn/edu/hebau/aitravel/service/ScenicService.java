package cn.edu.hebau.aitravel.service;

import cn.edu.hebau.aitravel.entity.Scenic;
import cn.edu.hebau.aitravel.mapper.ScenicMapper;
import cn.edu.hebau.aitravel.util.BusinessException;
import cn.edu.hebau.aitravel.util.PageResult;
import org.springframework.stereotype.Service;

import java.util.List;

/** 景点业务：校验分页参数，再查询当前页数据和总条数。 */
@Service
public class ScenicService {
    private final ScenicMapper scenicMapper;

    public ScenicService(ScenicMapper scenicMapper) {
        this.scenicMapper = scenicMapper;
    }

    public PageResult<Scenic> page(Long cityId, Integer pageNum, Integer pageSize) {
        if (pageNum < 1) {
            throw new BusinessException("页码不能小于1");
        }
        if (pageSize < 1 || pageSize > 100) {
            throw new BusinessException("每页条数应在1到100之间");
        }

        // 第 1 页从第 0 条开始，第 2 页跳过 pageSize 条，以此类推。
        int offset = (pageNum - 1) * pageSize;
        List<Scenic> list = scenicMapper.findPage(cityId, offset, pageSize);
        long total = scenicMapper.count(cityId);
        return new PageResult<>(list, total, pageNum, pageSize);
    }
}
