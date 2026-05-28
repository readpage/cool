package com.example.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.dao.OptionDao;
import com.example.domain.entity.Option;
import com.example.mapper.OptionMapper;
import com.example.service.OptionService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 选项服务实现
 */
@Service
public class OptionServiceImpl extends ServiceImpl<OptionMapper, Option> implements OptionService {

    @Resource
    private OptionDao optionDao;

    @Override
    public List<Option> search(String type, String keyword, Integer limit) {
        return optionDao.search(type, keyword, limit);
    }

    @Override
    public List<Option> listByType(String type, Integer limit) {
        return optionDao.listByType(type, limit);
    }
}
