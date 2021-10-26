package com.github.ireadmail.api.core.conf;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


@ConfigurationProperties("ired")
@Data
@Component
@AllArgsConstructor
@NoArgsConstructor
public class IredConf {

    //主机名
    private String host;

    //域名
    private String domain;

    private String masterUsername = "postmaster";

    private String masterPassword;

    //默认密码
    private String defaultPassword = "Aa0!Aa0!";

}
