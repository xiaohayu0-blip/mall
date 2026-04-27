package com.gym.mall.dao;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
//标记为持久化实体：告诉 JPA 这个类对应数据库中的一张表
//启用JPA功能：使得这个类可以使用其他 JPA 注解（如 @Id、@Column 等）
@Table(name="mall")
//指定具体的表名
public class Commodity {
    @Id
    //标记主键字段
    @Column(name="id")
    //指定列名和约束
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    //指定主键生成策略
    private Long id;

    @Column(name="name")
    private String name;

    @Column(name="price")
    private String price;

}
