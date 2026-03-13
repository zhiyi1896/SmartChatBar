package com.niu.community.sensitive.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.niu.community.sensitive.ac.AcSensitiveFilter;
import com.niu.community.sensitive.entity.SensitiveWordEntity;
import com.niu.community.sensitive.mapper.SensitiveWordMapper;
import jakarta.annotation.PostConstruct;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class SensitiveWordService {

    private final SensitiveWordMapper sensitiveWordMapper;
    private final AcSensitiveFilter acSensitiveFilter = new AcSensitiveFilter();

    public SensitiveWordService(SensitiveWordMapper sensitiveWordMapper) {
        this.sensitiveWordMapper = sensitiveWordMapper;
    }

    @PostConstruct
    public void init() {
        refresh();
    }

    public boolean containsSensitiveWord(String text) {
        return acSensitiveFilter.contains(text);
    }

    public void refresh() {
        Set<String> words = sensitiveWordMapper.selectList(Wrappers.emptyWrapper())
            .stream()
            .map(SensitiveWordEntity::getWord)
            .collect(Collectors.toSet());
        acSensitiveFilter.init(words);
    }
}
