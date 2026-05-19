package com.gym.mall.dto;

public class commodityDTO {
    private Long id;
    private String name;
    private String price;
    private Long categoryId;
    private String categoryName;
    private String description;
    private Integer stock;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private String name;
        private String price;
        private Long categoryId;
        private String categoryName;
        private String description;
        private Integer stock;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder price(String price) {
            this.price = price;
            return this;
        }

        public Builder categoryId(Long categoryId) {
            this.categoryId = categoryId;
            return this;
        }

        public Builder categoryName(String categoryName) {
            this.categoryName = categoryName;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder stock(Integer stock) {
            this.stock = stock;
            return this;
        }

        public commodityDTO build() {
            commodityDTO dto = new commodityDTO();
            dto.id = this.id;
            dto.name = this.name;
            dto.price = this.price;
            dto.categoryId = this.categoryId;
            dto.categoryName = this.categoryName;
            dto.description = this.description;
            dto.stock = this.stock;
            return dto;
        }
    }
}