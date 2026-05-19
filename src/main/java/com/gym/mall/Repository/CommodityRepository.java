package com.gym.mall.Repository;

import com.gym.mall.dao.Commodity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface CommodityRepository extends JpaRepository<Commodity,Long>, JpaSpecificationExecutor<Commodity> {

    List<Commodity> findByName(String name);

    Page<Commodity> findByNameContaining(String name, Pageable pageable);

    Page<Commodity> findByCategoryId(Long categoryId, Pageable pageable);

    Page<Commodity> findByNameContainingAndCategoryId(String name, Long categoryId, Pageable pageable);

}
