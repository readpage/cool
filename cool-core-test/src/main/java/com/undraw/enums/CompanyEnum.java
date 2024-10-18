package com.undraw.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author readpage
 * @date 2023-03-13 9:13
 */
@Getter
@AllArgsConstructor
public enum CompanyEnum {
    AUTO("","",  "", "", ""),
    SP("S01","S0101",  "SP", "昆山速品食品有限公司", "昆山速品"),
    DYD("S02","S0201",  "DYD",  "苏州大有得网络科技有限公司", "苏州大有得"),
    CGC("S03", "S0301", "CGC", "苏州茶功禅餐饮管理有限公司", "苏州茶功禅"),
    SZC("S04","S0401",  "SZC", "昆山市数智茶智能科技有限公司", "昆山数智茶"),
    SH("S05","S0501",  "SH", "速品食品（上海）有限公司", "速品上海"),
    WDF("S06","S0601",  "WDF", "福州市唯德甫食品有限公司", "唯德甫"),
    ZFHH("S07","S0701",  "ZFHH", "中福合和（昆山）茶业有限公司", "中福合和"),
    TZY("S08","S0601",  "QZ", "泉州同周圆食品有限公司", "泉州同周圆"),
   ;

    private String code;
    private String code1;
    private String code2;
    private String name;
    private String shortName;

    public static String getName(String code) {
        CompanyEnum company = getCompanyType(code);
        if (company != null) {
            return company.getName();
        }
        return null;
    }

    public static String getCode1(String name) {
        if (name == null) {
            return null;
        }
        switch (name) {
            case "速品":
            case "SP":
                return SP.getCode1();
            case "大有得":
            case "DYD":
                return DYD.getCode1();
            case "茶功禅":
            case "CGC":
                return CGC.getCode1();
            case "上海分公司":
            case "SH":
                return SH.getCode1();
            case "唯德甫":
            case "WDF":
                return WDF.getCode1();
            case "数智茶":
            case "SZC":
                return SZC.getCode1();
            default:
                return null;
        }
    }



    public static CompanyEnum getCompanyType(String code) {
        for (CompanyEnum value : CompanyEnum.values()) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        return CompanyEnum.AUTO;
    }


}
