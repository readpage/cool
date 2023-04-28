package cn.undraw.handler.exception.controller;

import cn.undraw.util.result.R;
import cn.undraw.util.result.ResultEnum;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

/**
 * @author readpage
 * @date 2022-11-11 17:25
 */
@RestController
public class NotFoundController implements ErrorController {

    @Override
    public String getErrorPath() {
        return "/error";
    }

    /**
     * 由于404异常无法被全局异常处理，可通过此种方式进行处理404异常
     * @param response
     * @return cn.undraw.util.result.R
     */
    @RequestMapping("/error")
    public R error(HttpServletResponse response) {
        // 返回异常信息
        return R.fail(ResultEnum.NOT_FOUND);
    }


}
