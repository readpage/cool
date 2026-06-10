package cn.undraw.util.filter;


import cn.undraw.util.StrUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Safelist;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author readpage
 * @date 2022-11-15 11:00
 */
@Component
public class JsoupUtils {

    /**
     * 配置过滤化参数,不对代码进行格式化
     */
    static Document.OutputSettings OUTPUT_SETTINGS = new Document.OutputSettings().prettyPrint(false);

    @Value("${cool-core.filter.sensitive}")
    private static boolean sensitive = false;

    @Value("${cool-core.filter.xss}")
    private static boolean xss = true;

    private static Safelist safelist = Safelist.relaxed();


    static {
        safelist.addTags("svg").addAttributes("svg","width", "height", "viewBox", "fill");
        safelist.addTags("path").addAttributes("path","d");
        safelist.addTags("header", "nav", "main", "aside", "article", "footer", "think");
        safelist.addAttributes(":all", "style", "class", "id"); // 允许所有标签的style, class, id属性
    }


    public static String clean(String originStr) {
        return Jsoup.clean(originStr, "", safelist, OUTPUT_SETTINGS);
    }


    public static String filter(String str) {
        // 去除 XSS 防御：直接返回原字符串，不对请求体做 HTML 实体编码
        return str;
    }

}
