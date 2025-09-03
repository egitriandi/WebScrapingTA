package com.egieTA.main.constants;

import java.util.Map;
import java.util.regex.Pattern;

public class Constants {

    public static String LITTLE_A = "a";
    public static String HREF = "href";

    public static final String ERROR_MESSAGE = "errorMessage";
    public static final String ERROR = "error";
    public static final String INDEX = "index";


    public static String EMPTY_STRING = "";
    public static String SPACE = " ";
    public static String STRING_ZERO = "0";
    public static String STRING_ONE = "1";
    public static String STRING_TWO = "2";
    public static String STRING_VERTICAL_BAR = "|";
    public static String YEAR_PICKER_REGEX = "\\b(19\\d{2}|20\\d{2})\\b";
    public static final Pattern YEAR_PATTERN = Pattern.compile(YEAR_PICKER_REGEX);
    public static String WHITE_SPACE_REGEX = "\\s";
    public static String PLUS_OPERATOR = ("+");
    public static String TITLES = "titles";
    public static String TITLE_LIST = "titleList";
    public static String YEARS = "years";
    public static String YEAR_LIST = "yearList";
    public static String LINKS = "links";
    public static String LINK_LIST = "linkList";
    public static String COLLEGES = "colleges";
    public static String COLLEGE_LIST = "collegeList";
    public static String AKREDITASI = "akreditasis";
    public static String AKREDITASI_LIST = "akreditasiList";
    public static String SCHOLAR_TAG = ".gs_ri";
    public static String GARUDA_TAG = ".article-item";

    public static final String SCHOLAR_TITLE_TAG = ".gs_rt";
    public static final String GARUDA_TITLE_TAG = "a";
    public static final String SCHOLAR_YEAR_TAG = ".gs_a";
    public static final String GARUDA_YEAR_TAG = ".subtitle-article";

    public static final String SINTA_TAG = ".list-item";
    public static final String SINTA_TITLE_TAG = "div.affil-name.mb-3 a";
    public static final String SINTA_COLLEGE_TAG = "div.affil-loc.mt-2 a";
    public static final String SINTA_AKREDITASI_TAG = "div.stat-prev.mt-2 a";

    public static final String VIEW_JOURNAL_TAG = ".ar-list-item.mb-5";
    public static final String VIEW_JOURNAL_TITLE_TAG = ".ar-title a";
    public static final String VIEW_JOURNAL_YEAR_TAG = ".ar-year";
    public static final String VIEW_JOURNAL_LINK_TAG = ".ar-title a";

    public static final Map portalMap = Map.of(
            0, "Google Scholar",
            1, "Garuda",
            2, "Sinta"
    );



    public static String[] USER_AGENT = {"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/90.0.4430.85 Safari/537.36",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36",
                    "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:89.0) Gecko/20100101 Firefox/89.0"};

    public static String[] garudaSingleURLBuilder = {"https://garuda.kemdikbud.go.id/documents?select=title&&pdf=1&q="};
    public static String[] garudaMultiURLBuilder = {"https://garuda.kemdikbud.go.id/documents?page=", "&select=title&&pdf=1&q=", "&from=", "&to="};
    public static String[] scholarSingleURLBuilder = {"https://scholar.google.com/scholar?hl=id&as_sdt=0%2C5&q="};
    public static String[] scholarMultiURLBuilder = {"https://scholar.google.com/scholar?start=", "&q=", "&as_ylo=", "&as_yhi"};
    public static String[] sintaURLBuilder = {"https://sinta.kemdikbud.go.id/journals/index/?page=", "&q="};


    public static int ATTEMPT = 10;
    public static int MAX_RECORD_SIZE = 50;




}
