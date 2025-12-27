package cn.undraw.handler.ser;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

@Slf4j
public class ToURLSerializer extends JsonSerializer<String> {

    @Deprecated
    @Override
    public void serialize(String s, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        try {
            jsonGenerator.writeObject(new URL(s));
        } catch (MalformedURLException e) {
            log.warn("图片地址'" + s + "'不是有效的!", e);
        }
    }
}
