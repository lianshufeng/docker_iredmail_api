package com.github.ireadmail.api.core.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResultContent {

    //结果
    private boolean success;

    //内容
    private Object content;


    private long time = System.currentTimeMillis();

    public static ResultContent build(boolean success) {
        return ResultContent.builder().success(success).build();
    }


    public static ResultContent build(boolean success, Object content) {
        return ResultContent.builder().success(success).content(content).build();
    }

}
