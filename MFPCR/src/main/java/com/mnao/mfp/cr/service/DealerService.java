package com.mnao.mfp.cr.service;


import com.mnao.mfp.cr.entity.Dealers;
import com.mnao.mfp.cr.repository.DealerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class DealerService {

    @Autowired
    private DealerRepository dealerRepository;

    @Cacheable
    public List<Dealers> findAll() {
        return StreamSupport.stream(dealerRepository.findAll().spliterator(), false).collect(Collectors.toList());
    }


}
