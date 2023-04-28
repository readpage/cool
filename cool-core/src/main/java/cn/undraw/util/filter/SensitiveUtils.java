package cn.undraw.util.filter;

import cn.undraw.handler.exception.customer.CustomerException;
import cn.undraw.util.StrUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.TextNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

/**
 * 敏感词过滤工具类（需要在resource下放置敏感词文件）
 * @author readpage
 * @date 2023-03-02 19:09
 **/
public class SensitiveUtils {

    private static final Logger log = LoggerFactory.getLogger(SensitiveUtils.class);

    /**
     * 敏感词文件
     */
    private static final String SENSITIVE_WORD = "assets/sensitive-words.txt";

    /**
     * 替换符
     */
    private static final String REPLACEMENT = "*";

    /**
     * 根节点
     */
    private static final TrieNode ROOT_NODE = new TrieNode();

    private static List<String> dynamicWords = new ArrayList<>();

    static {
        try (
                InputStream is = new ClassPathResource(SENSITIVE_WORD).getInputStream();
                // 提前判断对象是否为空，如果对象为空的话，提前抛出异常。而不是走到调用对象的具体方法的时候抛异常
                BufferedReader reader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(is)))
        ) {
            String keyword;
            while ((keyword = reader.readLine()) != null) {
                // 添加到前缀树
                add(xor(keyword));
            }
        } catch (Exception e) {
            log.warn("加载敏感词文件失败: " + e.getMessage());
        }
    }

    /**
     * 异或加密
     * @param s
     * @return java.lang.String
     */
    public static String xor(String s) {
        char[] chars = s.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            chars[i] = (char) (chars[i] ^ 2);
        }
        return new String(chars);
    }

    public static boolean isBlank(String str) {
        int strLen;
        if (str != null && (strLen = str.length()) != 0) {
            for(int i = 0; i < strLen; ++i) {
                // 判断字符是否为空格、制表符、tab
                if (!Character.isWhitespace(str.charAt(i))) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 将一个敏感词添加到前缀树中
     *
     * @param keyword 敏感词
     */
    public static void add(String keyword) {
        TrieNode tempNode = ROOT_NODE;
        for (int i = 0; i < keyword.length(); i++) {
            char c = keyword.charAt(i);
            TrieNode subNode = tempNode.getSubNode(c);

            if (subNode == null) {
                // 初始化子节点
                subNode = new TrieNode();
                tempNode.addSubNode(c, subNode);
            }

            // 指向子节点,进入下一轮循环
            tempNode = subNode;

            // 设置结束标识
            if (i == keyword.length() - 1) {
                tempNode.setKeywordEnd(true);
            }
        }
    }

    /***
     * 删除词
     * @param keyword
     * @return void
     */
    public static void remove(String keyword) {
        TrieNode tempNode = ROOT_NODE;
        for (int i = 0; i < keyword.length(); i++) {
            char c = keyword.charAt(i);
            TrieNode subNode = tempNode.getSubNode(c);
            if (i == keyword.length() - 1) {
                tempNode.remove(c);
            }
            tempNode = subNode;
        }
    }

    /**
     * 过滤敏感词
     * @param text 待过滤的文本
     * @return java.lang.String 过滤后的文本
     */
    public static String filter(String text) {
        if (isBlank(text)) {
            return null;
        }
        // 指针1
        TrieNode tempNode = ROOT_NODE;
        // 指针2
        int begin = 0;
        // 指针3
        int position = 0;

        // 结果
        StringBuilder sb = new StringBuilder();

        while (position < text.length()) {
            char c = text.charAt(position);

            // 跳过符号
            if (isSymbol(c)) {
                // 若指针1处于根节点,将此符号计入结果,让指针2向下走一步
                if (tempNode == ROOT_NODE) {
                    sb.append(c);
                    begin++;
                }
                // 无论符号在开头或中间,指针3都向下走一步
                position++;
                continue;
            }

            // 检查下级节点
            tempNode = tempNode.getSubNode(c);
            if (tempNode == null) {
                // 以begin开头的字符串不是敏感词
                sb.append(text.charAt(begin));
                // 进入下一个位置
                position = ++begin;
                // 重新指向根节点
                tempNode = ROOT_NODE;
            } else if (tempNode.isKeywordEnd()) {
                // 发现敏感词,将begin~position字符串替换掉
                sb.append(StrUtils.join(Collections.nCopies(position-begin+1, REPLACEMENT), ""));
                // 进入下一个位置
                begin = ++position;
                // 重新指向根节点
                tempNode = ROOT_NODE;
            } else {
                // 检查下一个字符
                position++;
            }
        }

        // 将最后一批字符计入结果
        sb.append(text.substring(begin));

        return sb.toString();
    }

    // json格式: 敏感词过滤
    private static ObjectMapper jackson = new ObjectMapper();
    public static String jsonFilter(String str) {
        if (!isValidJSON(str)) {
            return filter(str);
        }
        try {
            JsonNode node = jackson.readTree(str);
            jsonLeaf(node);
            return node.toString();
        } catch (JsonProcessingException e) {
            throw new CustomerException("xss和敏感词过滤异常!", e);
        }
    }

    /**
     * 是否是有效的json
     * @param json
     * @return boolean
     */
    public static boolean isValidJSON(final String json) {
        boolean valid = true;
        try{
            jackson.readTree(json);
        } catch(JsonProcessingException e){
            valid = false;
        }
        return valid;
    }

    private static void jsonLeaf(JsonNode node) {
        if (node.isObject())
        {
            Iterator<Map.Entry<String, JsonNode>> it = node.fields();
            while (it.hasNext())
            {
                Map.Entry<String, JsonNode> entry = it.next();
                if(entry.getValue() instanceof TextNode
                        && entry.getValue().isValueNode()){
                    TextNode t = (TextNode)entry.getValue();
                    String filter = filter(t.asText());
                    entry.setValue(new TextNode(filter));
                }

                jsonLeaf(entry.getValue());
            }
        }

        if (node.isArray())
        {
            ArrayNode arr = (ArrayNode) node;
            for (int i = 0; i < node.size(); i++) {
                JsonNode value = arr.get(i);
                if (value instanceof TextNode && value.isValueNode()) {
                    String filter = filter(value.asText());
                    arr.set(i, new TextNode(filter));
                } else {
                    jsonLeaf(node.get(i));
                }
            }
        }
    }

    /**
     * 判断是否为符号
     *
     * @param c 字符
     * @return 判断
     */
    private static boolean isSymbol(Character c) {
        // 0x2E80~0x9FFF 是东亚文字范围
        return !isAsciiAlphanumeric(c) && (c < 0x2E80 || c > 0x9FFF);
    }

    public static boolean isAsciiAlpha(char ch) {
        return isAsciiAlphaUpper(ch) || isAsciiAlphaLower(ch);
    }

    public static boolean isAsciiAlphaUpper(char ch) {
        return ch >= 'A' && ch <= 'Z';
    }

    public static boolean isAsciiAlphaLower(char ch) {
        return ch >= 'a' && ch <= 'z';
    }

    public static boolean isAsciiNumeric(char ch) {
        return ch >= '0' && ch <= '9';
    }

    public static boolean isAsciiAlphanumeric(char ch) {
        return isAsciiAlpha(ch) || isAsciiNumeric(ch);
    }

    /**
     * 前缀树
     */
    private static class TrieNode {

        // 关键词结束标识
        private boolean isKeywordEnd = false;

        // 子节点(key是下级字符,value是下级节点)
        private final Map<Character, TrieNode> subNodes = new HashMap<>();

        public boolean isKeywordEnd() {
            return isKeywordEnd;
        }

        public void setKeywordEnd(boolean keywordEnd) {
            isKeywordEnd = keywordEnd;
        }

        // 添加子节点
        public void addSubNode(Character c, TrieNode node) {
            subNodes.put(c, node);
        }

        // 删除节点
        public void remove(Character c) {
            subNodes.remove(c);
        }

        // 获取子节点
        public TrieNode getSubNode(Character c) {
            return subNodes.get(c);
        }

    }
}



