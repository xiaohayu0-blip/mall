package com.gym.mall.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name="users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
//自动生成包含所有字段的构造方法。
public class User extends BaseEntity{
    @Id
    @Column(name="id")
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long user_id;

    @Column(name="username")
    private String userName;

    @Column(name="password")
    private String password;

    private String salt;

}
