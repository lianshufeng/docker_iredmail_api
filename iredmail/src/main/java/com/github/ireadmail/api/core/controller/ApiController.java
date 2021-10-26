package com.github.ireadmail.api.core.controller;

import com.github.ireadmail.api.core.helper.ApiHelper;
import com.github.ireadmail.api.core.model.ResultContent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    @RequestMapping("createMail")
    public ResultContent create(String userName) {

        return ResultContent.build(true);
    }


}
