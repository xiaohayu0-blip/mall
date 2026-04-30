package com.gym.mall.dao;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Table(name="likes_user_record")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LikesUserRecord extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private long userId;

    private long businessId;

    private long itemId;

    private boolean likes;


}
