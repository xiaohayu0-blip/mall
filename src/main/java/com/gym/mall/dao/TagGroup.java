package com.gym.mall.dao;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tag_group")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TagGroup {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String tagGroupName;
}
