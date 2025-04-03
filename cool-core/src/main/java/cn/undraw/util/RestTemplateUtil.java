package cn.undraw.util;

import cn.undraw.util.result.R;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Map;


/**
 * @author readpage
 * @date 2022-09-16 18:05
 */
@Component
public class RestTemplateUtil {
    @Resource
    private RestTemplate restTemplate;


    /**
     * 参数拼接到url上
     * @param url 访问地址
     * @param param 参数
     * @return java.lang.String
     */
    public String join(String url, java.util.Map<String, ?> param) {
        if (StrUtils.isEmpty(param)) {
            return url;
        }
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url);
        for (Map.Entry<String, ?> e : param.entrySet()) {
            builder.queryParam(e.getKey(), e.getValue());
        }
        return builder.build().toString(); // 在此处拼接真实请求地址  "?pageNum=1&pageSize=5"
    }

    /**
     *
     * @param url
     * @param headers
     * @param param
     * @param type
     * @return
     * @param <T>
     */
    //其中<T>是为了定义当前我有一个 范型变量类型，类型名使用T来表示，
    //而第二部分T，表示method这个函数的返回值类型为T
    public <T> T get(String url, HttpHeaders headers, Map<String, ?> param, Class<T> type) {
        if (headers.getAccept() == null) {
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        }
        HttpEntity<Map<String, ?>> httpEntity = new HttpEntity<>(null, headers);
        return restTemplate.exchange(join(url, param), HttpMethod.GET, httpEntity, type).getBody();
    }
    public String get(String url, HttpHeaders headers, Map<String, ?> param) {
        return get(url, headers, param, String.class);
    }
    public <T> T get(String url, Map<String, ?> param, Class<T> type) {
        HttpHeaders headers = new HttpHeaders();
        return get(url, headers, param, type);
    }

    public String get(String url, Map<String, ?> param) {
        return get(url, param, String.class);
    }

    public <T> T get(String url, Class<T> type) {
        return restTemplate.getForObject(url, type);
    }

    public String get(String url) {
        return get(url, String.class);
    }

    public <T> R<T> getR(String url, HttpHeaders headers, Map<String, ?> param, Class<T> type) {
        return ConvertUtils.toObject(get(url, headers, param), new TypeReference<R<T>>() {});
    }

    public <T> R<T> getR(String url, Map<String, ?> param, Class<T> type) {
        return ConvertUtils.toObject(get(url, param), new TypeReference<R<T>>() {});
    }

    public <T> R<T> getR(String url, Class<T> type) {
        return ConvertUtils.toObject(get(url), new TypeReference<R<T>>() {});
    }

    public R<Object> getR(String url, HttpHeaders headers, Map<String, ?> param) {
        return getR(url, headers, param, Object.class);
    }

    public R<Object> getR(String url, Map<String, ?> param) {
        return getR(url, param, Object.class);
    }

    public R<Object> getR(String url) {
        return getR(url, Object.class);
    }

    public <T, K> T exchange(String url, HttpHeaders headers, HttpMethod method, K body, Class<T> type) {
        if (headers == null) {
            headers = new HttpHeaders();
        }
        if (headers.getContentType() == null) {
            headers.setContentType(MediaType.APPLICATION_JSON);
        }
        if (headers.getContentType() == null) {
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        }
        HttpEntity<K> entity = new HttpEntity<>(body, headers);
        return restTemplate.exchange(url, HttpMethod.DELETE, entity, type).getBody();
    }

    public <T, K> T delete(String url, HttpHeaders headers, K body, Class<T> type) {
        return exchange(url, headers, HttpMethod.DELETE, body, type);
    }

    public <K> String delete(String url, HttpHeaders headers, K body) {
        return delete(url, headers, body, String.class);
    }

    public <K> String delete(String url, K body) {
        HttpHeaders headers = new HttpHeaders();
        return delete(url, headers, body);
    }

    public String delete(String url) {
        return delete(url, new Object());
    }


    public <T, K> T put(String url, HttpHeaders headers, K body, Class<T> type) {
        return exchange(url, headers, HttpMethod.PUT, body, type);
    }

    public <K> String put(String url, HttpHeaders headers, K body) {
        return put(url, headers, body, String.class);
    }

    public <K> String put(String url, K body) {
        HttpHeaders headers = new HttpHeaders();
        return put(url, headers, new Object());
    }

    public String put(String url) {
        return put(url, new Object());
    }



    //Accept代表发送端（客户端）希望接受的数据类型。
    //Content-Type代表发送端（客户端|服务器）发送的实体数据的数据类型。

    /**
     * post
     * @param url 请求地址
     * @param body 实体
     * @param responseType 返回类型
     * @param headers 自定义的头信息
     * @return T
     */
    public <T, K> T post(String url, HttpHeaders headers, K body, Class<T> responseType) {
        if (headers == null) {
            headers = new HttpHeaders();
        }
        if (headers.getContentType() == null) {
            headers.setContentType(MediaType.APPLICATION_JSON);
        }
        if (headers.getContentType() == null) {
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        }
        HttpEntity<K> httpEntity = new HttpEntity<>(body, headers);
        return restTemplate.postForObject(url, httpEntity, responseType);
    }

    /**
     * post
     * @param url 请求地址
     * @param body 实体
     * @param responseType 返回类型
     * @return T
     */
    //Accept：用于在http请求报头，指明客户端接受那些类型的数据。
    //Content-Type: 用户指明本次（客户端或服务器）发送的数据类型
    public <T, K> T post(String url, K body, Class<T> responseType) {
        return post(url, null, body, responseType);
    }

    public <K> String post(String url, HttpHeaders headers, K body) {
        return post(url, headers, body, String.class);
    }

    public <K> String post(String url, K body) {
        return post(url, body, String.class);
    }

    public <T, K> R<T> postR(String url, HttpHeaders headers, K body,  Class<T> type) {
        return ConvertUtils.toObject(post(url, headers, body), new TypeReference<R<T>>() {});
    }

    public <T, K> R<T> postR(String url, K body, Class<T> type) {
        return ConvertUtils.toObject(post(url, body), new TypeReference<R<T>>() {});
    }

    public <K> R<Object> postR(String url, HttpHeaders headers, K body) {
        return postR(url, headers, body, Object.class);
    }

    public <K> R<Object> postR(String url, K body) {
        return postR(url, body, Object.class);
    }


    public void transferFile(String url, HttpServletResponse response, Map<String, ?> param) {
        ResponseEntity<byte[]> responseEntity = restTemplate.exchange(join(url, param), HttpMethod.GET, null, byte[].class);
        toHttpServletResponse(responseEntity, response);
    }

    public void transferFile(String url, HttpServletResponse response, Object obj) {
        transferFile(url, response, ConvertUtils.cloneDeep(obj, Map.class));
    }

    public void transferFile(String url, HttpServletResponse response) {
        transferFile(url, response, null);
    }

    private void toHttpServletResponse(ResponseEntity<byte[]> responseEntity, HttpServletResponse response) {
        OutputStream out = null;
        try {
            HttpHeaders headers = responseEntity.getHeaders();
            byte[] bytes = responseEntity.getBody();

            // 设置响应头
            for (String key : headers.keySet()) {
                response.setHeader(key, headers.get(key).get(0));
            }

            out = response.getOutputStream(); // 获取OutputStream对象

            out.write(bytes);
            out.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

}
