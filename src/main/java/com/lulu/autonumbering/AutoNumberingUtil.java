package com.lulu.autonumbering;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.math.NumberUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Auto Numbering Util
 */
public class AutoNumberingUtil {

    /**
     * titlePrefix 标题顺序
     */
    private static int[] titleNumber;

    /**
     * Title 前缀
     */
    private static final String[] titlePrefix = {
            "##",
            "###",
            "####",
            "#####",
    };

    public static File bakFile(MainFrame mainFrame, String path) {
        File file = checkAndGetFile(mainFrame, path);
        if (file == null) return null;
        String bakPath = "bak" + File.separator + file.getName() + "." + System.currentTimeMillis() + ".bak";
        //备份当前
        File backFile = new File(bakPath);
        mainFrame.log("start bak: " + backFile.getName());
        try {
            FileUtils.copyFile(file, backFile);
            mainFrame.log("bak finish: " + backFile.getName());
            return backFile;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void revertFile(MainFrame mainFrame, File file, String path) {
        if (file == null || !file.exists()) {
            mainFrame.log("error bak file is null !");
            return;
        }
        mainFrame.log("start revert: " + file.getName());
        try {
            FileUtils.copyFile(file, new File(path));
            mainFrame.log("revert finish: " + file.getName());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void autoNumbering(MainFrame mainFrame, String path) {
        File file = checkAndGetFile(mainFrame, path);
        if (file == null) return;

        mainFrame.log("start conversion!");

        titleNumber = new int[titlePrefix.length];
        try {
            List<String> strings = FileUtils.readLines(file, StandardCharsets.UTF_8);
            List<String> newStrings = new ArrayList<>();

            for (String line : strings) {
                newStrings.add(checkLine(mainFrame, line));
            }
            FileUtils.writeLines(file, "UTF-8", newStrings);
            mainFrame.log("conversion completed!");
        } catch (IOException e) {
            mainFrame.log("error: " + e);
            e.printStackTrace();
        }

    }

    private static File checkAndGetFile(MainFrame mainFrame, String path) {
        if (!path.endsWith("md")) {
            mainFrame.log("error! no markdown file");
            return null;
        }
        File file = new File(path);
        if (!file.exists()) {
            mainFrame.log("error! file not exist");
            return null;
        }
        return file;
    }

    /**
     * 检查每一个行
     * @param originLine 原始行
     * @return 处理过的行
     */
    private static String checkLine(MainFrame mainFrame, String originLine) {
        int titleIndex = -1;
        String curPrefix = "";
        int numSize = titleNumber.length;
        //检查是否有标题前缀，从后往前找
        for (int i = numSize - 1; i >= 0 ; i--) {
            curPrefix = titlePrefix[i];
            if (originLine.startsWith(curPrefix)) {
                titleIndex = i;
                break;
            }

        }
        if (titleIndex < 0) {
            return originLine;
        }
        mainFrame.log("converting: " + originLine);
        //如果当前当前标题的值要加1，则后续子标题都需要值 0
        resetChildTitleNumber(titleIndex, numSize);
        titleNumber[titleIndex]++;
        StringBuilder titleNumber = getTitleNumberString();
        String outputString = handleRemoveOldTitleNumber(mainFrame, originLine, curPrefix);
        return curPrefix + " " + titleNumber + " " + outputString;
    }

    private static String handleRemoveOldTitleNumber(MainFrame mainFrame, String originLine, String curPrefix) {
        //去掉 ## 后面的空格,故len+1
        String outputString = originLine.substring(curPrefix.length() + 1);
        int subIndex = 0;
        for (int i = 0; i < outputString.length(); i++) {
            char c = outputString.charAt(i);
            if (NumberUtils.isNumber(String.valueOf(c)) || c == '.' || c == ' ') {
                subIndex++;
            } else {
                break;
            }
        }
        if (subIndex > 0) {
            outputString = outputString.substring(subIndex);
            mainFrame.log("it needs to remove the old title number");
        }
        return outputString;
    }

    private static void resetChildTitleNumber(int titleIndex, int numSize) {
        if (titleIndex + 1 < numSize) {
            for (int i = titleIndex + 1; i < numSize; i++) {
                titleNumber[i] = 0;
            }
        }
    }

    private static StringBuilder getTitleNumberString() {
        StringBuilder title = new StringBuilder();
        for (int number : titleNumber) {
            if (number > 0) {
                title.append(number);
                title.append(".");
            }
        }
        title.deleteCharAt(title.length()-1);
        return title;
    }
}
