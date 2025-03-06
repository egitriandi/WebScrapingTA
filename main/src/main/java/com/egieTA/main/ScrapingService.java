package com.egieTA.main;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ScrapingService {
    String[] userAgents = Constants.USER_AGENT;
    Pattern yearPattern = Constants.YEAR_PATTERN;

    public Map getScholarURL(String portal, String inputTitle, String fromDate, String toDate, boolean isSingleResult) throws  InterruptedException{
        Map resultMap = new HashMap();
        inputTitle = inputTitle.replaceAll("\\s+", "+");
        ExecutorService executor = Executors.newFixedThreadPool(10); // Atur thread pool
        List<Future<Map<String, List<String>>>> futures = new ArrayList<>();

        int attempt = 0;
        if(isSingleResult){
            attempt = 1;
        }else{
            attempt = Constants.ATTEMPT;
        }

        for (int i = 1; i <= attempt; i++) {
            final int pageIndex = i;
            String finalInputTitle = inputTitle;
            futures.add(executor.submit(() -> scrapeData(portal, pageIndex, finalInputTitle, fromDate, toDate, isSingleResult)));
        }

        // Gabungkan hasil dari semua thread
        List<String> titleList = new ArrayList<>();
        List<String> yearList = new ArrayList<>();
        List<String> linkList = new ArrayList<>();
        for (Future<Map<String, List<String>>> future : futures) {
            try {
                Map<String, List<String>> pageData = future.get();
                titleList.addAll(pageData.get("titles"));
                yearList.addAll(pageData.get("years"));
                linkList.addAll(pageData.get("links"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.HOURS);

        resultMap.put("titleList", titleList);
        resultMap.put("yearList", yearList);
        resultMap.put("linkList", linkList);

        return resultMap;

    }

    public Map getSintaURL(String inputTitle) throws InterruptedException {
        Map resultMap = new HashMap();
        ExecutorService executor = Executors.newFixedThreadPool(10);
        List<Future<Map<String, List<String>>>> futures = new ArrayList<>();

        for (int i = 0; i < Constants.ATTEMPT; i++) {
            final int pageIndex = i + 1;
            futures.add(executor.submit(() -> {
                Map<String, List<String>> pageData = new HashMap<>();
                List<String> titles = new ArrayList<>();
                List<String> colleges = new ArrayList<>();
                List<String> akreditasis = new ArrayList<>();
                List<String> links = new ArrayList<>();

                String url = "https://sinta.kemdikbud.go.id/journals/index/?page=" + pageIndex + "&q=" + inputTitle;
                Connection con = Jsoup.connect(url)
                        .userAgent(String.valueOf(userAgents[(int) (Math.random() * userAgents.length)])).timeout(5000);
                Document doc = con.get();
                Thread.sleep(3000);

                Elements results = doc.select(".list-item");
                for (Element result : results) {
                    titles.add(result.select("div.affil-name.mb-3 a").text());
                    colleges.add(result.select("div.affil-loc.mt-2 a").text());

                    String a = result.select("div.stat-prev.mt-2 a").text();

                    if(a.length() == 24){
                        String b = a.substring(0, 11);
                        String c = a.substring(13, a.length());
                        akreditasis.add(b.concat(" ||").concat(c));
                    }else{
                        akreditasis.add(a);
                    }

                    links.add(result.select("a").attr("href"));
                }

                pageData.put("titles", titles);
                pageData.put("colleges", colleges);
                pageData.put("akreditasis", akreditasis);
                pageData.put("links", links);
                return pageData;
            }));
        }

// Gabungkan hasil
        List<String> titleList = new ArrayList<>();
        List<String> collegeList = new ArrayList<>();
        List<String> akreditasiList = new ArrayList<>();
        List<String> linkList = new ArrayList<>();

        for (Future<Map<String, List<String>>> future : futures) {
            try {
                Map<String, List<String>> pageData = future.get();
                titleList.addAll(pageData.get("titles"));
                collegeList.addAll(pageData.get("colleges"));
                akreditasiList.addAll(pageData.get("akreditasis"));
                linkList.addAll(pageData.get("links"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.HOURS);

        resultMap.put("titleList", titleList);
        resultMap.put("collegeList", collegeList);
        resultMap.put("akreditasiList", akreditasiList);
        resultMap.put("linkList", linkList);

        return resultMap;
    }

    private Map<String, List<String>> scrapeData(String portal, int pageIndex, String finalInputTitle, String fromDate, String toDate, boolean isSingleResult) {
        Map<String, List<String>> pageData = new HashMap<>();
        List<String> pageTitleList = new ArrayList<>();
        List<String> pageYearList = new ArrayList<>();
        List<String> pageLinkList = new ArrayList<>();

        try {
            String url = Constants.EMPTY_STRING;

            if(isSingleResult){
                url = portal.equals("0")
                        ? "https://scholar.google.com/scholar?hl=id&as_sdt=0%2C5&q=" + finalInputTitle
                        : "https://garuda.kemdikbud.go.id/documents?select=title&q=" + finalInputTitle;
            }else{
                url = portal.equals("0")
                        ? "https://scholar.google.com/scholar?start=" + (pageIndex * 10) + "&q=" + finalInputTitle + "&as_ylo=" + fromDate + "&as_yhi=" + toDate
                        : "https://garuda.kemdikbud.go.id/documents?page=" + pageIndex + "&q=" + finalInputTitle + "&from=" + fromDate + "&to=" + toDate;
            }

            Connection con = Jsoup.connect(url)
                    .userAgent(String.valueOf(userAgents[(int) (Math.random() * userAgents.length)]))
                    .timeout(5000);
            Document doc = con.get();
            Thread.sleep(3000);

            Elements results = portal.equals("0") ? doc.select(".gs_ri") : doc.select(".article-item");
            for (Element result : results) {
                String title = Constants.EMPTY_STRING;
                String tahun = Constants.EMPTY_STRING;
                if (portal.equals("0")) {
                    title = result.select(".gs_rt").text();
                } else {
                    Elements e = result.select("a");
                    for (Element tempE : e) {
                        title = tempE.text();
                        break;
                    }
                }

                tahun = portal.equals("0")
                        ? result.select(".gs_a").text()
                        : result.select(".subtitle-article").text();

                String link = portal.equals("0")
                            ? result.select(".gs_rt").select("a").attr("href")
                            : result.select("a").attr("href");

                pageTitleList.add(title);
                Matcher matcher = yearPattern.matcher(tahun);
                if (matcher.find()) {
                    pageYearList.add(matcher.group());  // Ambil tahun yang ditemukan
                }
                pageLinkList.add(portal.equals("0") ? link : "https://garuda.kemdikbud.go.id" + link);
            }

            pageData.put("titles", pageTitleList);
            pageData.put("years", pageYearList);
            pageData.put("links", pageLinkList);

        } catch (Exception e) {
            e.printStackTrace(); // Cetak error jika terjadi masalah
        }

        return pageData;
    }

    private static String modifyURL(String url) {
        String[] parts = url.split("/");
        if (parts.length > 4 && "profile".equals(parts[4])) {
            parts[4] = "google"; // Ubah 'profile' menjadi 'google'
        }
        return String.join("/", parts);
    }

    protected Map<String, Map> scrapeJournal(String url) {
        Map<String, Map> pageData = new HashMap<>();

        try {
            String urlDefault = url;
            String urlModified = modifyURL(url);

            Map schoPicker = valuePicker(urlModified);
            Map garPicker = valuePicker(urlDefault);

            pageData.put("scholarData", schoPicker);
            pageData.put("garudaData", garPicker);

        } catch (Exception e) {
            e.printStackTrace(); // Cetak error jika terjadi masalah
        }

        return pageData;
    }

    private Map valuePicker(String url) throws InterruptedException, IOException {
        Map returnMap = new HashMap();

        List<String> pageTitleList = new ArrayList<>();
        List<String> pageYearList = new ArrayList<>();
        List<String> pageLinkList = new ArrayList<>();

        Connection con = Jsoup.connect(url)
                .userAgent(String.valueOf(userAgents[(int) (Math.random() * userAgents.length)]))
                .timeout(5000);

        Document doc = con.get();
        Thread.sleep(3000);

        Elements results = doc.select(".ar-list-item.mb-5");
        for (Element result : results) {
            String title = result.select(".ar-title a").text();
            String tahun = result.select(".ar-year").text().replaceAll("[^0-9]", "").trim();
            String link = result.select(".ar-title a").attr("href");

            pageTitleList.add(title);
            pageYearList.add(tahun);
            pageLinkList.add(link);
        }

        returnMap.put("titles", pageTitleList);
        returnMap.put("years", pageYearList);
        returnMap.put("links", pageLinkList);

        return returnMap;
    }

}
