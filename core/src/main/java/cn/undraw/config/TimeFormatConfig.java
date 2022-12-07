package cn.undraw.config;


import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.format.DateTimeFormatter;


/**
 * @author readpage
 * LocalDateTime全局格式
 */

@Configuration
public class TimeFormatConfig {
    private static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    private static final String DATE_PATTERN = "yyyy-MM-dd";
    private static final String TIME_PATTERN = "HH:mm:ss";



    /**
     * 配置LocalDateTime类型序列化与反序列化
     */
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jsonCustomizer() {
        return builder -> builder
                .simpleDateFormat(DATE_TIME_PATTERN)
                .serializers(new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(DATE_TIME_PATTERN)))
                .deserializers(new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern(DATE_TIME_PATTERN)))
                .serializers(new LocalDateSerializer(DateTimeFormatter.ofPattern(DATE_PATTERN)))
                .deserializers(new LocalDateDeserializer(DateTimeFormatter.ofPattern(DATE_PATTERN)))
                .serializers(new LocalTimeSerializer(DateTimeFormatter.ofPattern(TIME_PATTERN)))
                .deserializers(new LocalTimeDeserializer(DateTimeFormatter.ofPattern(TIME_PATTERN)))
                ;
    }
}


