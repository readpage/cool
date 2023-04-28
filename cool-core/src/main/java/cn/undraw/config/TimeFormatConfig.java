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
import org.springframework.core.convert.converter.Converter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;


/**
 * 日期格式化
 * @author readpage
 * @date 2022-12-08 9:01
 */
@Configuration
public class TimeFormatConfig {
    private static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    private static final String DATE_PATTERN = "yyyy-MM-dd";
    private static final String TIME_PATTERN = "HH:mm:ss";



    /** 
     * LocalDateTime转换器，用于转换RequestParam和PathVariable参数
     * @return org.springframework.core.convert.converter.Converter<java.lang.String,java.time.LocalDateTime>
     */
    @Bean
    public Converter<String, LocalDateTime> localDateTimeConverter() {
        return new Converter<String, LocalDateTime>() {
            @Override
            public LocalDateTime convert(String source) {
                return LocalDateTime.parse(source, DateTimeFormatter.ofPattern(DATE_TIME_PATTERN));
            }
        };
    }

    /** 
     * LocalDate转换器，用于转换RequestParam和PathVariable参数
     * @return org.springframework.core.convert.converter.Converter<java.lang.String,java.time.LocalDate>
     */
    @Bean
    public Converter<String, LocalDate> localDateConverter() {
        return new Converter<String, LocalDate>() {
            @Override
            public LocalDate convert(String source) {
                return  LocalDate.parse(source, DateTimeFormatter.ofPattern(DATE_PATTERN));

            }
        };
    }

    /** 
     * LocalTime转换器，用于转换RequestParam和PathVariable参数
     * @return org.springframework.core.convert.converter.Converter<java.lang.String,java.time.LocalTime>
     */
    @Bean
    public Converter<String, LocalTime> localTimeConverter() {
        return new Converter<String, LocalTime>() {
            @Override
            public LocalTime convert(String source) {
                return LocalTime.parse(source, DateTimeFormatter.ofPattern(TIME_PATTERN));
            }
        };
    }


    /** 
     * 配置LocalDateTime类型序列化与反序列化
     * @return org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer
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


