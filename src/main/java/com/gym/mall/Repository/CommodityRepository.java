package com.gym.mall.Repository;

import com.gym.mall.domain.entity.Commodity;
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

    // Status-filtered queries for regular users
    Page<Commodity> findByStatus(Integer status, Pageable pageable);

    Page<Commodity> findByNameContainingAndStatus(String name, Integer status, Pageable pageable);

    Page<Commodity> findByCategoryIdAndStatus(Long categoryId, Integer status, Pageable pageable);

    Page<Commodity> findByNameContainingAndCategoryIdAndStatus(String name, Long categoryId, Integer status, Pageable pageable);

    @Query("""
        SELECT c.id          AS commodityId,
               c.name        AS commodityName,
               c.categoryId  AS categoryId,
               cat.name      AS categoryName,
               t.id          AS tagId,
               t.tagName        AS tagName
        FROM Commodity c
        LEFT JOIN Category cat ON c.categoryId = cat.id
        LEFT JOIN CommodityTag ct ON c.id = ct.commodityId
        LEFT JOIN Tag t ON ct.tagId = t.id
        WHERE ct.tagId = :tagId
    """)
    List<CommodityTagView> findCommoditiesWithTags(@Param("tagId") Long tagId);

    @Query(value = "SELECT * FROM commodity WHERE id IN :ids",
            countQuery = "SELECT COUNT(*) FROM commodity WHERE id IN :ids",
            nativeQuery = true)
    Page<Commodity> findAllByIdIn(List<Long> ids, Pageable pageable);

    @Query(value = "SELECT * FROM commodity WHERE id IN :ids AND status = 1",
            countQuery = "SELECT COUNT(*) FROM commodity WHERE id IN :ids AND status = 1",
            nativeQuery = true)
    Page<Commodity> findAllByIdInAndStatus(List<Long> ids, Pageable pageable);

    // 搜索上架商品：关键词(FULLTEXT)、分类、价格区间均可选
    @Query(value = """
            SELECT * FROM commodity
            WHERE status = 1
              AND (:keyword IS NULL OR MATCH(name, description) AGAINST(:keyword IN BOOLEAN MODE))
              AND (:categoryId IS NULL OR category_id = :categoryId)
              AND (:minPrice IS NULL OR price >= :minPrice)
              AND (:maxPrice IS NULL OR price <= :maxPrice)
            """,
            countQuery = """
            SELECT COUNT(*) FROM commodity
            WHERE status = 1
              AND (:keyword IS NULL OR MATCH(name, description) AGAINST(:keyword IN BOOLEAN MODE))
              AND (:categoryId IS NULL OR category_id = :categoryId)
              AND (:minPrice IS NULL OR price >= :minPrice)
              AND (:maxPrice IS NULL OR price <= :maxPrice)
            """,
            nativeQuery = true)
    Page<Commodity> searchOnSale(
            @Param("keyword") String keyword,
            @Param("categoryId") Long categoryId,
            @Param("minPrice") Long minPrice,
            @Param("maxPrice") Long maxPrice,
            Pageable pageable);

}
