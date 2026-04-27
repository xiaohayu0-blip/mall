package com.gym.mall.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
//纳入 Spring 容器管理
public interface CommodityRepository extends JpaRepository<Commodity,Long> {

    List<Commodity> findByName(String name);

}
