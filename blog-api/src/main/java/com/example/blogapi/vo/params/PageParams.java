package com.example.blogapi.vo.params;

import lombok.Data;

@Data
public class PageParams {
    private int page=1;
    private  int pageSize=10;
    private Long categoryId;
    private Long tagId;
    private String year;

    private String month;

    private SearchParams search;

    public String getMonth(){
        if (this.month != null && this.month.length() == 1){
            return "0"+this.month;
        }
        return this.month;
    }

    @Data
    public static class SearchParams {
        public Long beginTime;

        public Long endTime;

        public Byte owerType;

        public String publisher;

        public String all;
    }
}
