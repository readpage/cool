package com.example.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author readpage
 * @date 2023-03-13 10:23
 */
@Getter
@AllArgsConstructor
public enum DemoStateEnum {
    CANCEL(0, "已取消"),
    FINISH(1, "已完成");

    private int code;
    private String name;

    public static String getName(int code) {
        DemoStateEnum quarterType = getReceiveState(code);
        if (quarterType != null) {
            return quarterType.getName();
        }
        return null;
    }

    public static DemoStateEnum getReceiveState(int code) {
        for (DemoStateEnum value : DemoStateEnum.values()) {
            if (value.getCode() == code) {
                return value;
            }
        }
        return null;
    }

}
