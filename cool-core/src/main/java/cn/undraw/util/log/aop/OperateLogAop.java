package cn.undraw.util.log.aop;

import cn.undraw.handler.exception.customer.CustomerException;
import cn.undraw.util.ConvertUtils;
import cn.undraw.util.DateUtils;
import cn.undraw.util.StrUtils;
import cn.undraw.util.filter.JsonFilterUtils;
import cn.undraw.util.log.annotation.OperateLog;
import cn.undraw.util.log.enums.OperateTypeEnum;
import cn.undraw.util.log.service.OperateLogWorkService;
import cn.undraw.util.log.vo.OperationLog;
import cn.undraw.util.result.R;
import cn.undraw.util.servlet.IpUtils;
import cn.undraw.util.servlet.ServletUtils;
import cn.undraw.util.servlet.UserAgent;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author readpage
 * @date 2022-11-29 18:54
 */

@Component
@Aspect
@Slf4j
public class OperateLogAop {

    @Resource
    private OperateLogWorkService operateLogWorkService;

    @Around("@annotation(operateLog)")
    public Object around(ProceedingJoinPoint pjp, OperateLog operateLog) throws Throwable {
        Instant start = Instant.now();

        Object result = null;
        try {
            setRequestAttribute(pjp, operateLog);
            result = pjp.proceed();
            record(pjp, operateLog, start, result);
            return result;
        } catch (CustomerException e) {
            R r = e.getR();
            record(pjp, operateLog, start, r);
            R res = ConvertUtils.cloneDeep(r, R.class);
            return res;
        }
    }

    /**
     * 设置 请求的operateLog属性
     * @param pjp
     * @param operateLog
     */
    public void setRequestAttribute(ProceedingJoinPoint pjp, OperateLog operateLog) {
        // 设置日志信息
        HttpServletRequest request = ServletUtils.getRequest();
        OperationLog operationLog = new OperationLog();
        setModuleFields(operationLog, pjp, operateLog);
        request.setAttribute("operateLog", operationLog);
    }

    /**
     * 记录日志
     * @param pjp
     * @param operateLog
     * @param start
     * @param result
     */
    private void record(ProceedingJoinPoint pjp, OperateLog operateLog, Instant start, Object result) {
        Instant end = Instant.now();
        long time = Duration.between(start, end).toMillis();

        OperationLog operationLog = new OperationLog();
        operationLog.setStartTime(LocalDateTime.ofInstant(start, ZoneId.systemDefault()));

        // 设置模块信息
        setModuleFields(operationLog, pjp, operateLog);

        // 设置请求信息
        setRequestFields(operationLog, pjp);

        // 设置持续时间
        operationLog.setDuration(time);

        R r = R.ok();

        if (result instanceof R) {
            r = ConvertUtils.cloneDeep(result, R.class);
        }
        // 设置结果信息
        setResultFields(operationLog, r);

        // 打印日志
        print(operationLog);
        operateLogWorkService.createLog(operationLog, ServletUtils.getRequest());
    }

    public static <T extends Annotation> T getMethodAnnotation(JoinPoint joinPoint, Class<T> annotationClass) {
        return ((MethodSignature) joinPoint.getSignature()).getMethod().getAnnotation(annotationClass);
    }

    public static <T extends Annotation> T getClassAnnotation(JoinPoint joinPoint, Class<T> annotationClass) {
        return ((MethodSignature) joinPoint.getSignature()).getMethod().getDeclaringClass().getAnnotation(annotationClass);
    }

    /**
     * 设置模块信息
     * @param operationLog
     * @param pjp
     * @param operateLog
     * @return void
     */
    private void setModuleFields(OperationLog operationLog, ProceedingJoinPoint pjp, OperateLog operateLog) {
        // 获取操作
        Tag tag = getClassAnnotation(pjp, Tag.class);
        Operation operation = getMethodAnnotation(pjp, Operation.class);
        // 记录模板名
        if (tag != null) {
            operationLog.setModule(tag.name());
        }
        // 记录name属性
        if (operation != null) {
            operationLog.setName(operation.summary());
        }
        if (operateLog != null) {
            operationLog.setType(operateLog.type().getType());
            String module = operateLog.module();
            String name = operateLog.name();
            if (StrUtils.isNotEmpty(module)) {
                operationLog.setModule(module);
            }
            if (StrUtils.isNotEmpty(name)) {
                operationLog.setName(name);
            }
        }
    }

    @Value("${cool-core.filter.log-param:}")
    private String logParam;

    /**
     * 设置请求信息
     * @param operationLog
     * @param pjp
     */
    private void setRequestFields(OperationLog operationLog, ProceedingJoinPoint pjp) {
        HttpServletRequest request = ServletUtils.getRequest();

        // 请求方式
        operationLog.setRequestMethod(request.getMethod());
        // 请求URL
        operationLog.setRequestUrl(request.getRequestURI());
        operationLog.setUserIp(IpUtils.getClientIP());
        UserAgent userAgent = new UserAgent();
        operationLog.setUserAgent(userAgent.toString());
        operationLog.setDevice(userAgent.getDevice());
        operationLog.setAddress(IpUtils.getAddress());


        // 请求方法
        MethodSignature methodSignature = (MethodSignature) pjp.getSignature();
        operationLog.setOptMethod(methodSignature.toString());
        if (ServletUtils.isNotMultipart(request)) {
            try {
                // 构造参数组集合
                List<Object> argList = new ArrayList<>();
                for (Object arg : pjp.getArgs()) {
                    if (arg instanceof HttpServletRequest) {
                        argList.add("request");
                    } else if (arg instanceof HttpServletResponse) {
                        argList.add("response");
                    } else if (arg instanceof String) {
                        argList.add(arg);
                    } else {
                        argList.add(arg);
                    }
                }
                String s = null;
                if (argList != null) {
                    if (argList.size() == 1) {
                        s = ConvertUtils.toJson(argList.get(0));
                    } else {
                        s = ConvertUtils.toJson(argList);
                    }
                }
                s = filter(s);
                // 请求参数
                operationLog.setRequestParam(s);
            } catch (Exception e) {
            }
        }
    }

    private String filter(String s) {
        if (StrUtils.isNotEmpty(logParam)) {
            s = JsonFilterUtils.filter(s, (k, v) -> {
                List<String> keys = Arrays.asList(logParam.split(", "));
                if (keys.contains(k)) {
                    v = "******";
                }
                return v;
            });
        }
        return s;
    }

    /**
     * 设置结果信息
     * @param operationLog
     * @param result
     */
    private void setResultFields(OperationLog operationLog, Object result) {
        R r = R.ok();
        if (result instanceof R) {
            r = (R) result;
        } else {
            result = r;
        }

        operationLog.setResultCode(r.getCode());
        operationLog.setResultMsg(r.getMsg());
        Object data = r.getData();
        String res = filter(ConvertUtils.toJson(data));
        operationLog.setResultData(res);
    }

    /**
     * 打印日志
     * @param operationLog
     */
    private static void print(OperationLog operationLog) {
        String str = "操作信息: " + operationLog.getModule() + "|" + operationLog.getName() + " " + OperateTypeEnum.getMsg(operationLog.getType()) +
                "; 开始时间:" + DateUtils.toString(operationLog.getStartTime()) + "|持续" + operationLog.getDuration() + "ms\n" +
                "请求信息:" + operationLog.getRequestMethod() + "|" + operationLog.getRequestUrl() + "\n方法名:" + operationLog.getOptMethod() +
                "\n请求参数:" + operationLog.getRequestParam() + "\n地理位置:" + operationLog.getUserIp()+ "(" + operationLog.getAddress() + ")" +
                "|" + operationLog.getDevice() + "\n操作结果:" +
                operationLog.getResultCode()  + "|" + operationLog.getResultMsg();


        String resultData = operationLog.getResultData();
        if (resultData != null && !resultData.equals("null")) {
            str += "\n----------------------------------------------------------------\n" +
                    operationLog.getResultData() + "\n";
        } else {
            str += "\n";
        }

        if (R.ok().getCode().equals(operationLog.getResultCode())) {
            log.info(str);
        } else if (R.error().getCode().equals(operationLog.getResultCode())) {
            log.error(str);
        } else {
            log.warn(str);
        }
    }
}
