package com.gym.mall.dao;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Table(name="likes_statistic")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LikesStatistic extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long businessId;

    private Long itemId;

    private Long likeCount;

}
