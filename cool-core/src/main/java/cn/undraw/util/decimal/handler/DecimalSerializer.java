package cn.undraw.util.decimal.handler;

import cn.undraw.util.DecimalUtils;
import cn.undraw.util.decimal.annotation.DecimalFormat;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import org.springframework.boot.jackson.JsonComponent;

import java.io.IOException;
import java.util.Objects;

/**
 * @author readpage
 * @date 2022-11-15 11:00
 */
@JsonComponent
public class DecimalSerializer extends JsonSerializer<Double> implements ContextualSerializer {
    // 注解对象
    private DecimalFormat decimalFormat;

    @Override
    public void serialize(Double aDouble, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        if (aDouble != null && decimalFormat != null) {
            DecimalFormat.Access access = decimalFormat.access();
            if (access == DecimalFormat.Access.TenThousand) {
                aDouble = aDouble / 10000;
            }
            jsonGenerator.writeNumber(DecimalUtils.round(aDouble, decimalFormat.value()));
        } else {
            jsonGenerator.writeNumber(DecimalUtils.toString(aDouble));
        }
    }

    @Override
    public JsonSerializer<?> createContextual(SerializerProvider serializerProvider, BeanProperty beanProperty) throws JsonMappingException {
        if (beanProperty != null) {
            if (Objects.equals(beanProperty.getType().getRawClass(), Double.class)) {
                //获取对象属性上的自定义注解
                decimalFormat = beanProperty.getAnnotation(DecimalFormat.class);
                if (decimalFormat == null) {
                    decimalFormat = beanProperty.getContextAnnotation(DecimalFormat.class);
                }
                DecimalSerializer decimalSerializer = new DecimalSerializer();
                if (decimalFormat != null) {
                    decimalSerializer.decimalFormat = decimalFormat;
                }
                return decimalSerializer;
            }
            return new DecimalSerializer();
        }
        return this;
    }
}