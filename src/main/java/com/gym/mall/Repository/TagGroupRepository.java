package com.gym.mall.Repository;

import com.gym.mall.domain.entity.TagGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface TagGroupRepository extends JpaRepository<TagGroup, Long> , JpaSpecificationExecutor<TagGroup> {
}
