package com.gym.mall.dao;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "commodity_tag")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class CommodityTag extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "commodity_id",nullable = false)
    private Long commodityId;

    @Column(name = "tag_id",nullable = false)
    private Long tagId;

    @Column(name = "tag_group_id",nullable = false)
    private Long tagGroupId;
}
