package com.gym.mall.service;

import com.gym.mall.converter.CommodityConverter;
import com.gym.mall.dao.Commodity;
import com.gym.mall.dao.CommodityRepository;
import com.gym.mall.dto.commodityDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Service
//标记一个类作为服务层（Service Layer）
public class CommodityServiceImpl implements CommodityService {

    @Autowired
    private CommodityRepository commodityRepository;

    @Override
    //向编译器声明当前的方法意图覆盖（重写）父类或接口中的方法
    public Long addCommodity(commodityDTO commodityDTO) {
        List<Commodity> commodityList = commodityRepository.findByName(commodityDTO.getName());
        if(!CollectionUtils.isEmpty(commodityList)){
            throw new IllegalStateException("id"+commodityDTO.getId()+"has been taken");
        }
        Commodity commodity = commodityRepository.save(CommodityConverter.converterCommodity(commodityDTO));
        return commodity.getId();
    }
}
