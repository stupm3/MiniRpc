package com.stupm.core.utils;

import cn.hutool.core.util.StrUtil;
import cn.hutool.setting.dialect.Props;
import io.netty.util.internal.StringUtil;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.Map;

public class ConfigUtils {
    public static <T> T loadConfig(String prefix, Class<T> clazz) {
        return loadConfig(prefix, clazz, "");
    }

    public static <T> T loadConfig(String prefix, Class<T> clazz, String environment) {
        StringBuilder stringBuilder = new StringBuilder("application");
        if (StrUtil.isNotBlank(environment)) {
            stringBuilder.append("-").append(environment);
        }

        // 尝试读取 properties 文件
        try {
            stringBuilder.append(".properties");
            Props props = new Props(stringBuilder.toString(), "utf-8");
            return props.toBean(clazz, prefix);
        } catch (Exception e) {
            // 如果 properties 文件读取失败，尝试读取 yaml 文件
            stringBuilder.setLength(0); // 清空 StringBuilder
            stringBuilder.append("application");
            if (StrUtil.isNotBlank(environment)) {
                stringBuilder.append("-").append(environment);
            }
            stringBuilder.append(".yaml");
            return loadYamlConfig(stringBuilder.toString(), clazz, prefix);
        }
    }

    private static <T> T loadYamlConfig(String yamlFilePath, Class<T> clazz, String prefix) {
        try (InputStream inputStream = ConfigUtils.class.getClassLoader().getResourceAsStream(yamlFilePath)) {
            if (inputStream == null) {
                throw new IllegalArgumentException("YAML 文件未找到: " + yamlFilePath);
            }
            Yaml yaml = new Yaml();
            return yaml.loadAs(inputStream, clazz);
        } catch (Exception e) {
            throw new RuntimeException("读取 YAML 文件失败: " + yamlFilePath, e);
        }
    }

}