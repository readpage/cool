package cn.undraw.config;


import cn.undraw.handler.exception.customer.CustomerException;
import cn.undraw.util.DateUtils;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jackson.JsonComponent;
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

    @Value("${cool-core.date-format:global}")
    private String dateFormat;


    /**
     * 反序列化
     * LocalDateTime转换器，用于转换RequestParam和PathVariable参数
     * @return org.springframework.core.convert.converter.Converter<java.lang.String,java.time.LocalDateTime>
     */
    @Bean
    public Converter<String, LocalDateTime> localDateTimeConverter() {
        return new Converter<String, LocalDateTime>() {
            @Override
            public LocalDateTime convert(String source) {
                try {
                    return DateUtils.toDateTime(source);
                } catch (Exception e) {
                    throw new CustomerException("时间参数[" +source + "]类型转换异常");
                }
            }
        };
    }

    /**
     * 反序列化
     * LocalDate转换器，用于转换RequestParam和PathVariable参数
     * @return org.springframework.core.convert.converter.Converter<java.lang.String,java.time.LocalDate>
     */
    @Bean
    public Converter<String, LocalDate> localDateConverter() {
        return new Converter<String, LocalDate>() {
            @Override
            public LocalDate convert(String source) {
                return DateUtils.toLocalDate(source);
            }
        };
    }

    /**
     * 反序列化
     * LocalTime转换器，用于转换RequestParam和PathVariable参数
     * @return org.springframework.core.convert.converter.Converter<java.lang.String,java.time.LocalTime>
     */
    @Bean
    public Converter<String, LocalTime> localTimeConverter() {
        return new Converter<String, LocalTime>() {
            @Override
            public LocalTime convert(String source) {
                try {
                    return LocalTime.parse(source, DateTimeFormatter.ofPattern(TIME_PATTERN));
                } catch (Exception e) {
                    throw new CustomerException("时间参数[" +source + "]类型转换异常");
                }
            }
        };
    }

    // 反序列化
    @JsonComponent
    public class LocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {
        @Override
        public LocalDateTime deserialize(JsonParser parser, DeserializationContext deserializationContext) throws IOException {
            String str = parser.getText();
            return DateUtils.toDateTime(str);
        }
    }

    // 反序列化
    @JsonComponent
    public class LocalDateDeserializer extends JsonDeserializer<LocalDate> {
        @Override
        public LocalDate deserialize(JsonParser parser, DeserializationContext deserializationContext) throws IOException {
            String str = parser.getText();
            return DateUtils.toLocalDate(str);
        }
    }

    //序列化
    @JsonComponent
    public class LocalDateTimeSerializer extends JsonSerializer<LocalDateTime> {
        @Override
        public void serialize(LocalDateTime localDateTime, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
            if (dateFormat.equals("timestamp")) {
                jsonGenerator.writeNumber(DateUtils.toMilli(localDateTime));
            } else {
                jsonGenerator.writeString(DateUtils.toString(localDateTime));
            }
        }
    }

    // 序列化
    @JsonComponent
    public class LocalDateSerializer extends JsonSerializer<LocalDate> {
        @Override
        public void serialize(LocalDate localDate, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
            if (dateFormat.equals("timestamp")) {
                jsonGenerator.writeNumber(DateUtils.toMilli(localDate));
            } else {
                jsonGenerator.writeString(localDate.toString());
            }
        }
    }


}


