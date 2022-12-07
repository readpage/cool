package cn.undraw.util.filter;


import cn.undraw.util.StrUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Whitelist;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

public class JsoupUtils {

    /**
     * 配置过滤化参数,不对代码进行格式化
     */
    static Document.OutputSettings OUTPUT_SETTINGS = new Document.OutputSettings().prettyPrint(false);
    private static final ClassPathResource WHITE_LIST = new ClassPathResource("/assets/whiteList.json");

    //添加默认base配置,因为本项目的富文本图片实现使用base64,暂不使用默认的baseImage配置
//    private static Whitelist whitelist = Whitelist.basicWithImages();
    private static Whitelist whitelist = Whitelist.relaxed();

    private static ObjectMapper mapper = new ObjectMapper();

    //再载入json自定义白名单
    static {
        InputStream whiteConfig = null;
        try {
            whiteConfig = WHITE_LIST.getInputStream();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (whiteConfig == null) {
            throw new RuntimeException("读取jsoup xss 白名单文件失败");
        } else {
            try {
                Map<String, Object> map  = mapper.readValue(whiteConfig, new TypeReference< Map<String, Object> >(){});

                //添加标签 addTags
                List<String> addTagArr = (List<String>) map.get("addTags");
                String[] addTags =  addTagArr.toArray(new String[0]);
                whitelist.addTags(addTags);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static String clean2(String originStr) {

        whitelist = (new Whitelist()
                .addTags("b", "div", "img", "p", "strong") // 设置允许的标签
                .addAttributes("href", "title", "...") // 设置标签允许的属性
                .addAttributes(":all", "class", "id", "src") // 通配符，对所有标签配置允许的属性
                .addProtocols("img", "src", "http", "https")); // 设置Protocol，这是代表img的src属性只允许http和https开头

        return Jsoup.clean(originStr, whitelist);
    }

    public static String clean(String originStr) {
        return Jsoup.clean(originStr, "", whitelist, OUTPUT_SETTINGS);
    }


    public static String filter(String str) {
        if (StrUtils.isEmpty(str)) {
            return "";
        }
        String filter = SensitiveUtils.jsonFilter(str);
        return clean(filter);
    }

}
