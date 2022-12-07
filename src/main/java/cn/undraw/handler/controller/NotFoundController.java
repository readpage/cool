package cn.undraw.handler.controller;

import cn.undraw.util.result.Result;
import cn.undraw.util.result.ResultEnum;
import cn.undraw.util.result.ResultUtils;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

/**
 * @author readpage
 * @create 2022-11-11 17:25
 */
@RestController
public class NotFoundController implements ErrorController {

    @Override
    public String getErrorPath() {
        return "/error";
    }

    /**
     * 由于404异常无法被全局异常处理，可通过此种方式进行处理404异常
     *
     * @return
     */
    @RequestMapping("/error")
    public Result<?> error(HttpServletResponse response) {
        // 返回异常信息
        return ResultUtils.fail(ResultEnum.NOT_FOUND);
    }
}
