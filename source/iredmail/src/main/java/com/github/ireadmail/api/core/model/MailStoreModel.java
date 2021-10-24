package com.github.ireadmail.api.core.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MailStoreModel {
    private String username;
    private String password;
    //过期时间
    private long timeout;
}
