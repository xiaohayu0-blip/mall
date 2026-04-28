package com.gym.mall.service;

import com.gym.mall.converter.CommodityConverter;
import com.gym.mall.dao.Commodity;
import com.gym.mall.dao.CommodityRepository;
import com.gym.mall.dto.commodityDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;

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
        //从前端接收commodityDTO(数据传输对象),需要将DTO转换为(领域模型)Commodity才能保存到数据库
        //输入操作：DTO → Entity
        return commodity.getId();
    }

    @Override
    public commodityDTO getCommodityById(Long commodityId) {
        Commodity commodity=commodityRepository.findById(commodityId).orElseThrow(RuntimeException::new);
        return CommodityConverter.converterCommodity(commodity);
    }

    @Override
    public commodityDTO updateCommodityById(Long id, String name, String price) {
            Commodity commodityInDB=commodityRepository.findById(id).orElseThrow(RuntimeException::new);
        if(StringUtils.hasLength(name) && !commodityInDB.getName().equals(name)){
            commodityInDB.setName(name);
        }
        if(StringUtils.hasLength(price) && !commodityInDB.getPrice().equals(price)){
            commodityInDB.setPrice(price);
        }
        Commodity commodity = commodityRepository.save(commodityInDB);
        return  CommodityConverter.converterCommodity(commodity);
    }
    //get和update从数据库获取的是 Commodity实体
    //需要将Entity转换为DTO才能返回给前端
    //这是一个输出操作：Entity → DTO

    @Override
    public void deleteCommodityById(Long id) {
        commodityRepository.findById(id).orElseThrow(() ->new IllegalStateException("id:" + id + "is not exist"));
        commodityRepository.deleteById(id);
    }
}
