package com.github.ireadmail.api.core.conf;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Vector;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Component
@ConfigurationProperties(prefix = "blacklist")
public class BlackListConf {

    private Vector<String> ip;

    //每日ip注册最大数量
    private int maxAddCountFromDay = 5;

}
