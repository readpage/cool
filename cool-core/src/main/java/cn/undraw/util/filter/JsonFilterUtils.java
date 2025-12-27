package cn.undraw.util.filter;

import cn.undraw.handler.exception.customer.CustomerException;
import cn.undraw.util.StrUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.TextNode;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiFunction;

public class JsonFilterUtils {

    // json格式: 过滤
    private static ObjectMapper jackson = new ObjectMapper();

    public static String filter(String str, BiFunction<String, String, String> fun) {
        if (!isValidJSON(str)) {
            return fun.apply(UUID.randomUUID().toString(), str);
        }
        try {
            JsonNode node = jackson.readTree(str);
            jsonLeaf(node, fun);
            return node.toString();
        } catch (JsonProcessingException e) {
            throw new CustomerException("过滤异常!", e);
        }
    }

    /**
     * 是否是有效的json
     * @param json
     * @return boolean
     */
    public static boolean isValidJSON(final String json) {
        if (StrUtils.isEmpty(json)) {
            return false;
        }
        boolean valid = true;
        try{
            jackson.readTree(json);
        } catch(JsonProcessingException e){
            valid = false;
        }
        return valid;
    }

    private static void jsonLeaf(JsonNode node, BiFunction<String, String, String> fun) {
        if (node.isObject())
        {
            Iterator<Map.Entry<String, JsonNode>> it = node.fields();
            while (it.hasNext())
            {
                Map.Entry<String, JsonNode> entry = it.next();
                JsonNode value = entry.getValue();
                if(value instanceof TextNode || value.isNumber()){
                    String oldVal = value.asText();
                    String key = entry.getKey();
                    String newVal = fun.apply(key, oldVal);
                    if (!oldVal.equals(newVal)) {
                        entry.setValue(new TextNode(newVal));
                    }
                }

                jsonLeaf(entry.getValue(), fun);
            }
        }

        if (node.isArray())
        {
            ArrayNode arr = (ArrayNode) node;
            for (int i = 0; i < node.size(); i++) {
                JsonNode value = arr.get(i);
                if (value instanceof TextNode || value.isNumber()) {
                    String oldVal = value.asText();
                    String newVal = fun.apply(UUID.randomUUID().toString(), oldVal);
                    if (!oldVal.equals(newVal)) {
                        arr.set(i, new TextNode(newVal));
                    }
                } else {
                    jsonLeaf(value, fun);
                }
            }
        }
    }

}
