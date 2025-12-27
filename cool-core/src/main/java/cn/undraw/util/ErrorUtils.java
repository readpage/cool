package cn.undraw.util;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * @author readpage
 * @date 2022-12-02 18:11
 */
public class ErrorUtils {

    /**
     * 获取堆栈信息
     */
    public static String getStackTrace(Throwable e){
        StringWriter sw = new StringWriter();
        try (PrintWriter pw = new PrintWriter(sw)) {
            e.printStackTrace(pw);
            return sw.toString();
        }
    }

    /**
     * 获取异常方法
     * @param e
     * @return java.lang.String
     */
    public static String getErrorMethod(Throwable e) {
        StackTraceElement[] stackTraceElements = e.getStackTrace();
        StackTraceElement se = stackTraceElements[0];
        return se.getClassName() + "." + se.getMethodName() + "(?)";
    }

}
