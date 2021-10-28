package com.github.ireadmail.api.core.controller;

import com.github.ireadmail.api.core.helper.ApiHelper;
import com.github.ireadmail.api.core.model.ResultContent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.CookieManager;

@RequestMapping("api")
@RestController
public class ApiController {

    @Autowired
    private ApiHelper apiHelper;

    /**
     * 创建邮箱
     *
     * @return
     */
    @RequestMapping("add")
    public ResultContent create(String username) {
        CookieManager cookieManager = apiHelper.login();
        return ResultContent.build(this.apiHelper.addUser(cookieManager, username));
    }

    @RequestMapping("del")
    public ResultContent del(String username) {
        CookieManager cookieManager = apiHelper.login();
        return ResultContent.build(this.apiHelper.delUser(cookieManager, username));
    }


    @RequestMapping("receive")
    public ResultContent receive(String username) {
        return ResultContent.build(true, this.apiHelper.receive(username));
    }

}
