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

    /** 用户角色：ADMIN（管理员）/ USER（普通用户） */
    @Column(name = "role", nullable = false)
    private String role = "USER";
}
