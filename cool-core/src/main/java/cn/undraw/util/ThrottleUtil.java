package cn.undraw.util;

import org.springframework.stereotype.Component;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class ThrottleUtil {
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private volatile boolean taskPending = false;

    public void action(Runnable action) {
        if (!taskPending) {
            action.run();
            taskPending = true;
            scheduler.schedule(() -> {
                taskPending = false;
            }, 1, TimeUnit.SECONDS); // 修改为所需的节流时间，例如1秒
        }
    }

}
