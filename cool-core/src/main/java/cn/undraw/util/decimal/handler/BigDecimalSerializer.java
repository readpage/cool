package cn.undraw.util.decimal.handler;

import cn.undraw.util.decimal.annotation.BigDecimalFormat;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import org.springframework.boot.jackson.JsonComponent;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * @author readpage
 * @date 2022-11-15 11:00
 */
@JsonComponent
public class BigDecimalSerializer extends JsonSerializer<BigDecimal> implements ContextualSerializer {
    // 注解对象
    private BigDecimalFormat bigDecimalFormat;

    @Override
    public void serialize(BigDecimal bigDecimal, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        if (bigDecimal != null && bigDecimalFormat != null) {
            BigDecimalFormat.Access access = bigDecimalFormat.access();
            if (access == BigDecimalFormat.Access.TenThousand) {
                bigDecimal = bigDecimal.divide(new BigDecimal(10000));
            }
            // 保留4位小数，四舍五入
            bigDecimal = bigDecimal.setScale(bigDecimalFormat.value(), RoundingMode.HALF_UP);
        }
        jsonGenerator.writeNumber(bigDecimal);
    }

    @Override
    public JsonSerializer<?> createContextual(SerializerProvider serializerProvider, BeanProperty beanProperty) throws JsonMappingException {
        if (beanProperty != null) {
            if (Objects.equals(beanProperty.getType().getRawClass(),BigDecimal.class)) {
                //获取对象属性上的自定义注解
                bigDecimalFormat = beanProperty.getAnnotation(BigDecimalFormat.class);
                if (bigDecimalFormat == null) {
                    // 获取类上的自定义注解
                    bigDecimalFormat = beanProperty.getContextAnnotation(BigDecimalFormat.class);
                }
                BigDecimalSerializer bigDecimalSerializer = new BigDecimalSerializer();
                if (bigDecimalFormat != null) {
                    bigDecimalSerializer.bigDecimalFormat = bigDecimalFormat;
                }
                return bigDecimalSerializer;
            }
            return serializerProvider.findValueSerializer(beanProperty.getType(), beanProperty);
        }
        return serializerProvider.findNullValueSerializer(null);
    }

    @Override
    public Class<BigDecimal> handledType() {
        return super.handledType();
    }
}