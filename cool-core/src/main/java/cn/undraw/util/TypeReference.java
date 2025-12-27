package cn.undraw.util;


import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class TypeReference<T> extends com.fasterxml.jackson.core.type.TypeReference<T>
{
    protected final Type type;
 
    // 带参数的方法，支持泛型类传递
    protected <E> TypeReference(Class<E> clazz) {
        Type type = ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        ParameterizedTypeImpl clsInfo = ParameterizedTypeImpl.make(((ParameterizedTypeImpl) type).getRawType(), new Type[]{clazz}, null);
        this.type = clsInfo;
    }
 
    protected TypeReference()
    {
        Type superClass = getClass().getGenericSuperclass();
        if (superClass instanceof Class<?>) { // sanity check, should never happen
            throw new IllegalArgumentException("Internal error: TypeReference constructed without actual type information");
        }
        this.type = ((ParameterizedType) superClass).getActualTypeArguments()[0];
    }
 
    @Override
    public Type getType() { return type; }
}