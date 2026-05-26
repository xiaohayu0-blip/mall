package com.gym.mall.domain.entity;

import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.Data;

import java.time.Instant;

@Data
@MappedSuperclass
//实体类的继承映射,这个父类本身不是实体类，不会被映射到数据库表，但它的属性会被子类继承并映射到子类对应的表中。
public class BaseEntity {
    private long createTime;
    private long updateTime;

    @PrePersist
    //在保存[新]记录到数据库前，自动执行一个初始化方法。
    //自动填充字段：比如在[插入]数据库前，设置创建时间（createdAt）、生成默认值、计算字段等
    public void onCreate(){
        if(this.createTime==0L){
            this.createTime = Instant.now().toEpochMilli();
            this.updateTime = Instant.now().toEpochMilli();
        }
    }

    @PreUpdate
    //实体对象[更新]到数据库之前自动触发标记的方法。
    public void  onUpdate(){
        this.updateTime = Instant.now().toEpochMilli();
    }
}
