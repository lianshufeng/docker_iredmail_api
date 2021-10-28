package com.github.ireadmail.api.core.helper;

import com.github.ireadmail.api.core.conf.IredConf;
import com.github.ireadmail.api.core.util.TextUtil;
import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.mail.*;
import javax.mail.internet.MimeMessage;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;
import java.util.Properties;

@Slf4j
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
    public static void getMailTextContent(Part part, StringBuffer content) {
        //如果是文本类型的附件，通过getContent方法可以取到文本内容，但这不是我们需要的结果，所以在这里要做判断
        boolean isContainTextAttach = part.getContentType().indexOf("name") > 0;
        if (part.isMimeType("text/*") && !isContainTextAttach) {
            content.append(part.getContent().toString());
        } else if (part.isMimeType("message/rfc822")) {
            getMailTextContent((Part) part.getContent(), content);
        } else if (part.isMimeType("multipart/*")) {
            Multipart multipart = (Multipart) part.getContent();
            int partCount = multipart.getCount();
            for (int i = 0; i < partCount; i++) {
                BodyPart bodyPart = multipart.getBodyPart(i);
                getMailTextContent(bodyPart, content);
            }
        }
    }

    /**
     * 解析邮件
     *
     * @param messages 要解析的邮件列表
     */
    @SneakyThrows
    public static void parseMessage(Message... messages) {
        if (messages == null || messages.length < 1)
            throw new MessagingException("未找到要解析的邮件!");

        // 解析所有邮件
        for (int i = 0, count = messages.length; i < count; i++) {
            MimeMessage msg = (MimeMessage) messages[i];
            log.info("------------------解析第" + msg.getMessageNumber() + "封邮件-------------------- ");
            StringBuffer content = new StringBuffer(30);
            getMailTextContent(msg, content);
            log.info("邮件正文：" + (content.length() > 100 ? content.substring(0, 100) + "..." : content));
            System.out.println("------------------第" + msg.getMessageNumber() + "封邮件解析结束-------------------- ");
        }
    }

    @SneakyThrows
    public Object receive(String username) {
        String account = username + "@" + this.iredConf.getDomain();


        Properties props = new Properties();
        props.setProperty("mail.store.protocol", "pop3");        // 协议
        props.setProperty("mail.pop3.port", "110");                // 端口
        props.setProperty("mail.pop3.host", this.iredConf.getPop3Host());    // pop3服务器

        // 创建Session实例对象
        Session session = Session.getInstance(props);
        @Cleanup Store store = session.getStore("pop3");
        store.connect(account, this.iredConf.getDefaultPassword());

        // 获得收件箱
        Folder folder = store.getFolder("INBOX");
        folder.open(Folder.READ_WRITE);    //打开收件箱

        // 由于POP3协议无法获知邮件的状态,所以getUnreadMessageCount得到的是收件箱的邮件总数
        log.info("未读邮件数: {}", folder.getUnreadMessageCount());
        // 由于POP3协议无法获知邮件的状态,所以下面得到的结果始终都是为0
        log.info("删除邮件数: {}", folder.getDeletedMessageCount());
        log.info("新邮件: {}", folder.getNewMessageCount());
        // 获得收件箱中的邮件总数
        log.info("邮件总数: {}", folder.getMessageCount());
        // 得到收件箱中的所有邮件,并解析
        Message[] messages = folder.getMessages();
        parseMessage(messages);

        //释放资源
        folder.close(true);

        return null;
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
