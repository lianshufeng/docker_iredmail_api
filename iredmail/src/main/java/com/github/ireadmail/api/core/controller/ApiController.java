package com.github.ireadmail.api.core.controller;

import com.github.ireadmail.api.core.conf.IredConf;
import com.github.ireadmail.api.core.helper.ApiHelper;
import com.github.ireadmail.api.core.model.ResultContent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.CookieManager;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@RequestMapping("api")
@RestController
public class ApiController {

    @Autowired
    private ApiHelper apiHelper;

    @Autowired
    private IredConf iredConf;

    private ScheduledExecutorService executors = Executors.newScheduledThreadPool(10);

    @Autowired
    private void init(ApplicationContext applicationContext) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            executors.shutdownNow();
        }));
    }

    /**
     * 创建邮箱
     *
     * @return
     */
    @RequestMapping("add")
    public ResultContent create(final String username) {
        CookieManager cookieManager = apiHelper.login();
        boolean success = this.apiHelper.addUser(cookieManager, username);
        if (success) {
            executors.scheduleAtFixedRate(() -> {
                ApiController.this.del(username);
            }, this.iredConf.getAutoDelTime(), this.iredConf.getAutoDelTime(), TimeUnit.MILLISECONDS);
        }
        return ResultContent.build(success);
    }

    @RequestMapping("del")
    public ResultContent del(String username) {
        CookieManager cookieManager = apiHelper.login();
        return ResultContent.build(this.apiHelper.delUser(cookieManager, username));
    }


    @RequestMapping("receive")
    public ResultContent receive(String username) {
        Object ret = this.apiHelper.receive(username);
        return ResultContent.build(ret == null ? false : true, ret);
    }

}
