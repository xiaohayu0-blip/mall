package com.gym.mall.Repository;
import com.gym.mall.domain.entity.CommodityTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommodityTagRepository extends JpaRepository<CommodityTag, Long> {

    List<CommodityTag> findByCommodityId(Long commodityId);

    List<CommodityTag> findByTagId(Long tagId);

    List<CommodityTag> findByTagGroupId(Long tagGroupId);

    void deleteByCommodityId(Long commodityId);

    void deleteByCommodityIdAndTagId(Long commodityId, Long tagId);

    @Query(value = "SELECT ct.commodity_id" +
            " FROM commodity_tag ct" +
            " WHERE ct.tag_id IN :tagIds"
            , nativeQuery = true)
    List<Long> findCommodityIdsByTagIds(List<Long> tagIds);

    boolean existsByCommodityIdAndTagId(Long commodityId, Long tagId);
}
