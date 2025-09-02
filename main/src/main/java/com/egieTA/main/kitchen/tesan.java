package com.egieTA.main.kitchen;

import com.egieTA.main.constants.Constants;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class tesan {

    static String[] userAgents = Constants.USER_AGENT;
    static Pattern yearPattern = Constants.YEAR_PATTERN;

    public static void main(String [] args){
        Map tempMap = new HashMap();

        tempMap = scrapeDataSingleTerm("1", 10, "PEMBELAJARAN ILMU KOMPUTER TANPA KOMPUTER UNPLUGGED ACTIVITIES UNTUK MELATIH KETERAMPILAN LOGIKA ANAK", "2021", "2024");
        System.out.println("a");
    }

    private static Map<String, List<String>> scrapeDataSingleTerm(String portal, int pageIndex, String finalInputTitle, String fromDate, String toDate) {
        Map<String, List<String>> pageData = new HashMap<>();
        List<String> pageTitleList = new ArrayList<>();
        List<String> pageYearList = new ArrayList<>();
        List<String> pageLinkList = new ArrayList<>();
        String titlele = finalInputTitle.replaceAll("\\s+", "+");
        try {
            String url = portal.equals("0")
                    ? "https://scholar.google.com/scholar?hl=id&as_sdt=0%2C5&q=" + titlele
                    : "https://garuda.kemdikbud.go.id/documents?select=title&q=" + titlele;

            Connection con = Jsoup.connect(url)
                    .userAgent(String.valueOf(userAgents[(int) (Math.random() * userAgents.length)]))
                    /*.timeout(5000)*/;
            Document doc = con.get();
            Thread.sleep(3000);

            Elements results = portal.equals("0")
                    ? doc.select(".gs_ri")
                    : doc.select(".article-item");

            for (Element result : results) {
                String title = Constants.EMPTY_STRING;
                String tahun = Constants.EMPTY_STRING;

                if (portal.equals("0")) {
                    title = result.select(".gs_rt").text();
                } else {
                    Elements e = result.select(".gs_rt").select("a");
                    for (Element tempE : e) {
                        title = tempE.text();
                        break;
                    }
                }
                tahun = portal.equals("0")
                        ? result.select(".gs_a").text()
                        : result.select(".subtitle-article").text();
                Matcher matcher = yearPattern.matcher(tahun);
                if (matcher.find()) {
                    pageYearList.add(matcher.group());  // Ambil tahun yang ditemukan
                }


//                pageYearList.add(tahun);
                String link = result.select(".gs_rt").select("a").attr("href");
                pageTitleList.add(title);

                pageLinkList.add(portal.equals("0") ? link : "https://garuda.kemdikbud.go.id" + link);
            }

            pageData.put("titles", pageTitleList);
            pageData.put("links", pageLinkList);

        } catch (Exception e) {
            e.printStackTrace(); // Cetak error jika terjadi masalah
        }

        return pageData;
    }

    private static Map<String, List<String>> scrapeData(String portal, int pageIndex, String finalInputTitle, String fromDate, String toDate) {
        Map<String, List<String>> pageData = new HashMap<>();
        List<String> pageTitleList = new ArrayList<>();
        List<String> pageYearList = new ArrayList<>();
        List<String> pageLinkList = new ArrayList<>();

        try {
            String url = portal.equals("0")
                    ? "https://scholar.google.com/scholar?start=" + (pageIndex * 10) + "&q=" + finalInputTitle + "&as_ylo=" + fromDate + "&as_yhi=" + toDate
                    : "https://garuda.kemdikbud.go.id/documents?page=" + pageIndex + "&q=" + finalInputTitle + "&from=" + fromDate + "&to=" + toDate;

            Connection con = Jsoup.connect(url)
                    .userAgent(String.valueOf(userAgents[(int) (Math.random() * userAgents.length)]))
                    .timeout(5000);
            Document doc = con.get();
            Thread.sleep(3000);

            Elements results = portal.equals("0")
                                ? doc.select(".gs_ri")
                                : doc.select(".article-item");

            for (Element result : results) {
                String title = Constants.EMPTY_STRING;
                String tahun = Constants.EMPTY_STRING;

                if (portal.equals("0")) {
                    title = result.select(".gs_rt").text();
                } else {
                    Elements e = result.select(".gs_rt").select("a");
                    for (Element tempE : e) {
                        title = tempE.text();
                        break;
                    }
                }
                tahun = portal.equals("0")
                        ? result.select(".gs_a").text()
                        : result.select(".subtitle-article").text();
                Matcher matcher = yearPattern.matcher(tahun);
                if (matcher.find()) {
                    pageYearList.add(matcher.group());  // Ambil tahun yang ditemukan
                }


//                pageYearList.add(tahun);
                String link = result.select(".gs_rt").select("a").attr("href");
                pageTitleList.add(title);

                pageLinkList.add(portal.equals("0") ? link : "https://garuda.kemdikbud.go.id" + link);
            }

//            pageData.put("titles", pageTitleList);
////            pageData.put("links", pageLinkList);

        } catch (Exception e) {
            e.printStackTrace(); // Cetak error jika terjadi masalah
        }

        return pageData;
    }
}
