package cn.undraw.util;

import cn.undraw.util.result.R;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Arrays;
import java.util.Map;

/**
 * @author readpage
 * @date 2022-09-16 18:05
 */
@Component
public class RestTemplateUtil {
    @Autowired
    private RestTemplate restTemplate;

    /**
     *
     * @param url
     * @param param
     * @param type
     * @return
     * @param <T>
     */
    //其中<T>是为了定义当前我有一个 范型变量类型，类型名使用T来表示，
    //而第二部分T，表示method这个函数的返回值类型为T
    public <T> T get(String url, HttpHeaders headers, Map<String, ?> param, Class<T> type) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url);
        if (headers.getAccept() == null) {
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        }
        for (Map.Entry<String, ?> e : param.entrySet()) {
            builder.queryParam(e.getKey(), e.getValue());
        }
        String realUrl = builder.build().toString(); // 在此处拼接真实请求地址  "?pageNum=1&pageSize=5"

        HttpEntity<Map<String, ?>> httpEntity = new HttpEntity<>(null, headers);
        return restTemplate.exchange(realUrl, HttpMethod.GET, httpEntity, type).getBody();
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


    public <T> R<T> getResult(String url, Map<String, ?> param, Class<T> type) {
        return ConvertUtils.toObject(get(url, param), new TypeReference<R<T>>() {});
    }

    public <T> R<T> getResult(String url, Class<T> type) {
        return ConvertUtils.toObject(get(url), new TypeReference<R<T>>() {});
    }

    public R<Object> getResult(String url, Map<String, ?> param) {
        return getResult(url, param, Object.class);
    }

    public R<Object> getR(String url) {
        return getResult(url, Object.class);
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
    public <T, K> T post(String url, K body, HttpHeaders headers, Class<T> responseType) {
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
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<K> httpEntity = new HttpEntity<>(body, headers);
        return post(url, body, headers, responseType);
    }

    public <K> String post(String url, K body, HttpHeaders headers) {
        if (headers.getContentType() == null) {
            headers.setContentType(MediaType.APPLICATION_JSON);
        }
        if (headers.getContentType() == null) {
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        }
        return post(url, body, headers, String.class);
    }

    public <K> String post(String url, K body) {
        return post(url, body, String.class);
    }

    public <T, K> R<T> postResult(String url, K body, HttpHeaders headers, Class<T> type) {
        return ConvertUtils.toObject(post(url, body, headers), new TypeReference<R<T>>() {});
    }

    public <T, K> R<T> postResult(String url, K body, Class<T> type) {
        return ConvertUtils.toObject(post(url, body), new TypeReference<R<T>>() {});
    }

    public <K> R<Object> postResult(String url, K body, HttpHeaders headers) {
        return postResult(url, body, headers, Object.class);
    }

    public <K> R<Object> postResult(String url, K body) {
        return postResult(url, body, Object.class);
    }


}
