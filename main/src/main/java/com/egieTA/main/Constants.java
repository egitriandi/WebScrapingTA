package com.egieTA.main;

import java.util.regex.Pattern;

public class Constants {

    public static String EMPTY_STRING = "";
    public static String SPACE = " ";
    public static String STRING_ZERO = "0";
    public static String STRING_ONE = "1";
    public static String STRING_TWO = "2";
    public static String STRING_VERTICAL_BAR = "|";
    public static String YEAR_PICKER_REGEX = "\\b(19\\d{2}|20\\d{2})\\b";
    public static final Pattern YEAR_PATTERN = Pattern.compile(YEAR_PICKER_REGEX);

    public static String[] USER_AGENT = {"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/90.0.4430.85 Safari/537.36",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36",
                    "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:89.0) Gecko/20100101 Firefox/89.0"};

    public static int ATTEMPT = 2;
    public static int MAX_RECORD_SIZE = 10;




}
