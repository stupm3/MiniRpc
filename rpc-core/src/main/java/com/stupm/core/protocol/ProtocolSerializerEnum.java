package com.stupm.core.protocol;

import cn.hutool.core.util.ObjectUtil;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public enum ProtocolSerializerEnum {

    JDK(0,"jdk"),
    HESSIAN(1,"hessian"),
    KRYO(2,"kryo"),
    JSON(3,"json");

    private final int key;

    private final String value;

    ProtocolSerializerEnum(int key, String value) {
        this.key = key;
        this.value = value;
    }

    public static List<String> getValues(){
        return Arrays.stream(values()).map(item -> item.value).collect(Collectors.toList());
    }

    public static ProtocolSerializerEnum getEnumByKey(int key) {
        for (ProtocolSerializerEnum e : ProtocolSerializerEnum.values()) {
            if (e.key == key) {
                return e;
            }
        }
        return null;
    }

    public static ProtocolSerializerEnum getEnumByValue(String value) {
        if(ObjectUtil.isEmpty(value)){
            return null;
        }
        for (ProtocolSerializerEnum e : ProtocolSerializerEnum.values()) {
            if (e.value.equals(value)) {
                return e;
            }
        }
        return null;
    }
}
