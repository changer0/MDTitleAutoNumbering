package com.lulu.autonumbering;

import net.sf.json.JSONObject;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 本地文件 kv 存储
 */
public class KVStorage {

    public static void put(String key, String value) {

        try {
            File file = getAndCreateConfigFile();

            String s = FileUtils.readFileToString(file, StandardCharsets.UTF_8);

            JSONObject jsonObj;
            if (s.isEmpty()) {
                jsonObj = new JSONObject();
            } else {
                jsonObj = JSONObject.fromObject(s);
            }
            jsonObj.put(key, value);
            FileUtils.writeStringToFile(file, jsonObj.toString(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String get(String key, String defaultValue) {
        String s = "";
        try {
            s = FileUtils.readFileToString(getAndCreateConfigFile(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
        JSONObject jsonObj;
        if (s.isEmpty()) {
            return defaultValue;
        } else {
            jsonObj = JSONObject.fromObject(s);
        }
        return jsonObj.optString(key, defaultValue);
    }

    private static File getAndCreateConfigFile() {
        String fileName = "config.json";
        File file = new File(fileName);
        if (file.exists()) {
            return file;
        }
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }

}
