package com.gym.mall.Repository;

import com.gym.mall.dao.Commodity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface CommodityRepository extends JpaRepository<Commodity,Long>, JpaSpecificationExecutor<Commodity> {

    List<Commodity> findByName(String name);

    Page<Commodity> findByNameContaining(String name, Pageable pageable);

    Page<Commodity> findByCategoryId(Long categoryId, Pageable pageable);

    Page<Commodity> findByNameContainingAndCategoryId(String name, Long categoryId, Pageable pageable);

    @Query("""
        SELECT c.id          AS commodityId,
               c.name        AS commodityName,
               c.categoryId  AS categoryId,
               cat.name      AS categoryName,
               t.id          AS tagId,
               t.name        AS tagName
        FROM Commodity c
        LEFT JOIN Category cat ON c.categoryId = cat.id
        LEFT JOIN CommodityTag ct ON c.id = ct.commodityId
        LEFT JOIN Tag t ON ct.tagId = t.id
        WHERE ct.tagId = :tagId
    """)
    List<CommodityTagView> findCommoditiesWithTags(@Param("tagId") Long tagId);

    @Query(value = "SELECT * FROM commodity " +
            "WHERE id IN :ids"
            , nativeQuery = true)
    Page<Commodity> findAllByIdIn(List<Long> ids, Pageable pageable);

}
