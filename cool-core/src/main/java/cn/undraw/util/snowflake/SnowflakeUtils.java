package cn.undraw.util.snowflake;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


/**
 * @author readpage
 * @date 2023-07-05 13:56
 */
@Component
public class SnowflakeUtils {
    @Value("${cool-core.snowflake.workerId:0}")
    private long workerId;

    @Value("${cool-core.snowflake.datacenterId:0}")
    private long datacenterId;

    private static Snowflake idWorker = new Snowflake(0, 0);

    @PostConstruct
    public void init(){
        idWorker = new Snowflake(workerId, datacenterId);
    }

    public static long nextId() {
        return idWorker.nextId();
    }
}
