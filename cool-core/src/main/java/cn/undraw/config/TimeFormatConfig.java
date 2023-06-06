package cn.undraw.config;


import cn.undraw.util.DateUtils;
import cn.undraw.util.StrUtils;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;


/**
 * 时间格式化
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
                if (StrUtils.isNumber(source)) {
                    return DateUtils.toDateTime(Long.parseLong(source));
                }
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
                if (StrUtils.isNumber(source)) {
                    return DateUtils.toDateTime(Long.parseLong(source)).toLocalDate();
                }
                return LocalDate.parse(source, DateTimeFormatter.ofPattern(DATE_PATTERN));
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

//    public static class LocalDateTimeSerializer extends JsonSerializer<LocalDateTime> {
//        @Override
//        public void serialize(LocalDateTime value, JsonGenerator gen, SerializerProvider serializers)
//                throws IOException {
//            if (value != null) {
//                gen.writeNumber(value.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
//            }
//        }
//    }

    public static class LocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {
        @Override
        public LocalDateTime deserialize(JsonParser parser, DeserializationContext deserializationContext) throws IOException {
            String str = parser.getText();
            if (StrUtils.isNumber(str)) {
                return DateUtils.toDateTime(Long.parseLong(str));
            }
            return LocalDateTime.parse(parser.getText(), DateTimeFormatter.ofPattern(DATE_TIME_PATTERN));
        }
    }

    public static class LocalDateDeserializer extends JsonDeserializer<LocalDate> {
        @Override
        public LocalDate deserialize(JsonParser parser, DeserializationContext deserializationContext) throws IOException {
            String str = parser.getText();
            if (StrUtils.isNumber(str)) {
                return DateUtils.toDateTime(Long.parseLong(str)).toLocalDate();
            }
            return LocalDate.parse(str, DateTimeFormatter.ofPattern(DATE_PATTERN));
        }
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
                .deserializerByType(LocalDateTime.class, new LocalDateTimeDeserializer())
                .serializers(new LocalDateSerializer(DateTimeFormatter.ofPattern(DATE_PATTERN)))
                .deserializerByType(LocalDate.class, new LocalDateDeserializer())
                .serializers(new LocalTimeSerializer(DateTimeFormatter.ofPattern(TIME_PATTERN)))
                .deserializers(new LocalTimeDeserializer(DateTimeFormatter.ofPattern(TIME_PATTERN)))
                ;
    }
}


