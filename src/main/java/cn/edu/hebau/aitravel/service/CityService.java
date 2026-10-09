package cn.edu.hebau.aitravel.service;

import cn.edu.hebau.aitravel.entity.City;
import cn.edu.hebau.aitravel.mapper.CityMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/** 城市业务；当前直接查询全部城市。 */
@Service
public class CityService {
    private final CityMapper cityMapper;

    public CityService(CityMapper cityMapper) {
        this.cityMapper = cityMapper;
    }

    public List<City> list() {
        return cityMapper.findAll();
    }
}
