package com.github.ireadmail.api.core.controller;

import com.github.ireadmail.api.core.conf.BlackListConf;
import com.github.ireadmail.api.core.conf.IredConf;
import com.github.ireadmail.api.core.helper.ApiHelper;
import com.github.ireadmail.api.core.model.ResultContent;
import com.github.ireadmail.api.core.util.IPUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.CookieManager;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@RequestMapping("api")
@RestController
@EnableScheduling
public class ApiController {

    @Autowired
    private ApiHelper apiHelper;

    @Autowired
    private IredConf iredConf;

    @Autowired
    private BlackListConf blackListConf;


    private ScheduledExecutorService executors = Executors.newScheduledThreadPool(10);


    final private Map<String, Integer> accessAddUserIpCache = new ConcurrentHashMap<>();


    //表示每天8时30分0秒执行
    @Scheduled(cron = "0 0 4 ? * *")
    public void cleanAddUserIpCache() {
        accessAddUserIpCache.clear();
        log.info("clean add cache");
    }

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
    public ResultContent create(HttpServletRequest request, final String username, final String masterPassword, final Long autoDelTime) {
        final String ip = IPUtil.getRemoteIp(request);

        boolean isMasterPassword = this.iredConf.getMasterPassword().equals(masterPassword);

        //黑名单
        if (blackListConf.getIp().contains(ip)) {
            return ResultContent.build(false);
        }

        //取出当前ip注册次数
        final Integer counter = this.accessAddUserIpCache.getOrDefault(ip, 1);
        if (counter > this.blackListConf.getMaxAddCountFromDay() && !isMasterPassword) {
            log.error("超过最大次数 : {} , {} ", ip, counter);
            return ResultContent.build(false);
        }
        this.accessAddUserIpCache.put(ip, counter + 1);


        CookieManager cookieManager = apiHelper.login();
        boolean success = this.apiHelper.addUser(cookieManager, username);
        if (success) {
            executors.schedule(() -> {
                ApiController.this.del(username);
            }, autoDelTime != null ? autoDelTime : this.iredConf.getAutoDelTime(), TimeUnit.MILLISECONDS);
        }
        return ResultContent.build(success);
    }

    @RequestMapping("del")
    public ResultContent del(String username) {
        log.info("remove {}", username);
        CookieManager cookieManager = apiHelper.login();
        return ResultContent.build(this.apiHelper.delUser(cookieManager, username));
    }


    @RequestMapping("receive")
    public ResultContent receive(HttpServletRequest request, String username) {

        //取出当前ip注册次数
        final String ip = IPUtil.getRemoteIp(request);
        final Integer counter = this.accessAddUserIpCache.getOrDefault(ip, 1);
        if (counter > this.blackListConf.getMaxAddCountFromDay()) {
            log.error("超过最大次数 : {} , {} ", ip, counter);
            return ResultContent.build(false);
        }

        Object ret = this.apiHelper.receive(username);
        return ResultContent.build(ret == null ? false : true, ret);
    }


}
