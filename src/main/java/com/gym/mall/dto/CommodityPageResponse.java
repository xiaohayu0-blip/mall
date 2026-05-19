package com.gym.mall.dto;

public class CommodityPageResponse {
    private java.util.List<commodityDTO> records;
    private Long total;
    private Integer page;
    private Integer pageSize;
    private Integer totalPages;

    public java.util.List<commodityDTO> getRecords() {
        return records;
    }

    public void setRecords(java.util.List<commodityDTO> records) {
        this.records = records;
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public Integer getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(Integer totalPages) {
        this.totalPages = totalPages;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private java.util.List<commodityDTO> records;
        private Long total;
        private Integer page;
        private Integer pageSize;
        private Integer totalPages;

        public Builder records(java.util.List<commodityDTO> records) {
            this.records = records;
            return this;
        }

        public Builder total(Long total) {
            this.total = total;
            return this;
        }

        public Builder page(Integer page) {
            this.page = page;
            return this;
        }

        public Builder pageSize(Integer pageSize) {
            this.pageSize = pageSize;
            return this;
        }

        public Builder totalPages(Integer totalPages) {
            this.totalPages = totalPages;
            return this;
        }

        public CommodityPageResponse build() {
            CommodityPageResponse response = new CommodityPageResponse();
            response.records = this.records;
            response.total = this.total;
            response.page = this.page;
            response.pageSize = this.pageSize;
            response.totalPages = this.totalPages;
            return response;
        }
    }
}