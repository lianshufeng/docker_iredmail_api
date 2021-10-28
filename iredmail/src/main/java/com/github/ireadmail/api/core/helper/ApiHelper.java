package com.github.ireadmail.api.core.helper;

import com.github.ireadmail.api.core.conf.IredConf;
import com.github.ireadmail.api.core.util.TextUtil;
import com.sun.mail.util.MailSSLSocketFactory;
import lombok.SneakyThrows;
import org.apache.catalina.Store;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.mail.MailReceiver;
import org.springframework.integration.mail.Pop3MailReceiver;
import org.springframework.stereotype.Component;

import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;
import java.util.Properties;

@Component
public class ApiHelper {

    @Autowired
    private IredConf iredConf;

//    @Autowired
//    private void init(ApplicationContext applicationContext) {
//        Caffeine caffeine = Caffeine.newBuilder()
//                .expireAfterWrite(10, TimeUnit.MINUTES)
//                .maximumSize(10);
//        CaffeineCacheManager caffeineCacheManager = new CaffeineCacheManager();
//        caffeineCacheManager.setCaffeine(caffeine);
//        this.cache = (Cache) caffeineCacheManager.getCache(CacheName).getNativeCache();
//        this.cache.cleanUp();
//    }

    @SneakyThrows
    public Object receive(String username) {

//        String host, int port, String username, String password

        Pop3MailReceiver receiver = new Pop3MailReceiver(
                this.iredConf.getHost(),
                110,
                username,
                this.iredConf.getDefaultPassword()
        );

        MailSSLSocketFactory sf = new MailSSLSocketFactory();
        sf.setTrustAllHosts(true);
        Properties mailProperties = new Properties();
        mailProperties.put("mail.pop3.ssl.enable", "true");
        mailProperties.put("mail.pop3.ssl.socketFactory", sf);

        receiver.setJavaMailProperties(mailProperties);

        return receiver.receive();
    }

    /**
     * 登陆
     *
     * @return
     */
    @SneakyThrows
    public CookieManager login() {
        CookieManager cookieManager = new CookieManager();
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);

        String url = String.format("https://%s/iredadmin/login", this.iredConf.getHost());
        String username = this.iredConf.getMasterUsername() + "@" + this.iredConf.getDomain();
        String info = String.format(
                "username=%s&password=%s&form_login=登录&lang=zh_CN", username, this.iredConf.getMasterPassword()
        );
        HttpClient httpClient = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .cookieHandler(cookieManager)
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(info))
                .build();
        HttpResponse<String> httpResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofString(Charset.forName("utf-8")));
        return httpResponse.body().indexOf(username) > -1 ? cookieManager : null;
    }


    @SneakyThrows
    public boolean addUser(CookieManager cookieManager, String username) {
        String token = csrf_token(cookieManager);
        String url = String.format("https://%s/iredadmin/create/user/%s", this.iredConf.getHost(), this.iredConf.getDomain());
        String info = String.format(
                "csrf_token=%s&domainName=%s&username=%s&newpw=%s&confirmpw=%s&cn=%s&preferredLanguage=zh_CN&mailQuota=10",
                token,
                this.iredConf.getDomain(),
                username,
                this.iredConf.getDefaultPassword(),
                this.iredConf.getDefaultPassword(),
                username
        );

        HttpClient httpClient = HttpClient.newBuilder()
                .cookieHandler(cookieManager)
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(info))
                .build();
        HttpResponse<String> httpResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofString(Charset.forName("utf-8")));
        return true;
    }


    @SneakyThrows
    public boolean delUser(CookieManager cookieManager, String username) {
        String token = csrf_token(cookieManager);
        String url = String.format("https://%s/iredadmin/users/%s/page/1", this.iredConf.getHost(), this.iredConf.getDomain());
        String mail = username + "@" + this.iredConf.getDomain();
        String info = String.format(
                "csrf_token=%s&mail=%s&cur_page=1&action=delete&keep_mailbox_days=1",
                token,
                URLEncoder.encode(mail, "UTF-8")
        );

        HttpClient httpClient = HttpClient.newBuilder()
                .cookieHandler(cookieManager)
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(info))
                .build();
        HttpResponse<String> httpResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofString(Charset.forName("utf-8")));
        return true;
    }


    @SneakyThrows
    private String csrf_token(CookieManager cookieManager) {
        final String url = String.format("https://%s/iredadmin/users/%s", this.iredConf.getHost(), this.iredConf.getDomain());
        HttpClient httpClient = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .cookieHandler(cookieManager)
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();
        HttpResponse<String> httpResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofString(Charset.forName("utf-8")));
        String ret = httpResponse.body();

        ret = TextUtil.subText(
                ret,
                "csrf_token\"",
                "/>",
                -1
        );
        return TextUtil.subText(ret, "\"", "\"", 1);
    }
}
