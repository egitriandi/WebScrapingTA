package com.egieTA.main.services;

import com.egieTA.main.constants.Constants;
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
        inputTitle = inputTitle.replaceAll(Constants.WHITE_SPACE_REGEX, Constants.PLUS_OPERATOR);
        ExecutorService executor = Executors.newFixedThreadPool(10); // Atur thread pool
        List<Future<Map<String, List<String>>>> futures = new ArrayList<>();

        int attempt = isSingleResult ? 1 : Constants.ATTEMPT;

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
                titleList.addAll(pageData.get(Constants.TITLES));
                yearList.addAll(pageData.get(Constants.YEARS));
                linkList.addAll(pageData.get(Constants.LINKS));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.HOURS);

        resultMap.put(Constants.TITLE_LIST, titleList);
        resultMap.put(Constants.YEAR_LIST, yearList);
        resultMap.put(Constants.LINK_LIST, linkList);

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

                String url = Constants.sintaURLBuilder[0] + pageIndex + Constants.sintaURLBuilder[1] + inputTitle;
                Connection con = Jsoup.connect(url)
                        .userAgent(String.valueOf(userAgents[(int) (Math.random() * userAgents.length)])).timeout(5000);
                Document doc = con.get();
                Thread.sleep(3000);

                Elements results = doc.select(Constants.SINTA_TAG);
                for (Element result : results) {
                    titles.add(result.select(Constants.SINTA_TITLE_TAG).text());
                    colleges.add(result.select(Constants.SINTA_COLLEGE_TAG).text());

                    String a = result.select(Constants.SINTA_AKREDITASI_TAG).text();

                    if(a.length() == 24){
                        String b = a.substring(0, 11);
                        String c = a.substring(13, a.length());
                        akreditasis.add(b.concat(" ||").concat(c));
                    }else{
                        akreditasis.add(a);
                    }

                    links.add(result.select(Constants.LITTLE_A).attr(Constants.HREF));
                }

                pageData.put(Constants.TITLES, titles);
                pageData.put(Constants.COLLEGES, colleges);
                pageData.put(Constants.AKREDITASI, akreditasis);
                pageData.put(Constants.LINKS, links);
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
                titleList.addAll(pageData.get(Constants.TITLES));
                collegeList.addAll(pageData.get(Constants.COLLEGES));
                akreditasiList.addAll(pageData.get(Constants.AKREDITASI));
                linkList.addAll(pageData.get(Constants.LINKS));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.HOURS);

        resultMap.put(Constants.TITLE_LIST, titleList);
        resultMap.put(Constants.COLLEGE_LIST, collegeList);
        resultMap.put(Constants.AKREDITASI_LIST, akreditasiList);
        resultMap.put(Constants.LINK_LIST, linkList);

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
                url = portal.equals(Constants.STRING_ZERO)
                        ? Constants.scholarSingleURLBuilder[0] + finalInputTitle
                        : Constants.garudaMultiURLBuilder[0] + finalInputTitle;
            }else{
                url = portal.equals(Constants.STRING_ZERO)
                        ? Constants.scholarMultiURLBuilder[0] + (pageIndex * 10)
                        + Constants.scholarMultiURLBuilder[1] + finalInputTitle
                        + Constants.scholarMultiURLBuilder[2] + fromDate
                        + Constants.scholarMultiURLBuilder[3] + toDate
                        : Constants.garudaMultiURLBuilder[0] + pageIndex
                        + Constants.garudaMultiURLBuilder[1] + finalInputTitle
                        + Constants.garudaMultiURLBuilder[2] + fromDate
                        + Constants.garudaMultiURLBuilder[3] + toDate;
            }

            Connection con = Jsoup.connect(url)
                    .userAgent(String.valueOf(userAgents[(int) (Math.random() * userAgents.length)]))
                    /*.timeout(5000)*/;
            Document doc = con.get();
            Thread.sleep(3000);

            Elements results = portal.equals(Constants.STRING_ZERO) ? doc.select(Constants.SCHOLAR_TAG) : doc.select(Constants.GARUDA_TAG);
            for (Element result : results) {
                String title = Constants.EMPTY_STRING;
                String tahun = Constants.EMPTY_STRING;
                if (portal.equals(Constants.STRING_ZERO)) {
                    title = result.select(Constants.SCHOLAR_TITLE_TAG).text();
                } else {
                    Elements e = result.select(Constants.LITTLE_A);
                    for (Element tempE : e) {
                        title = tempE.text();
                        break;
                    }
                }

                tahun = portal.equals(Constants.STRING_ZERO)
                        ? result.select(Constants.SCHOLAR_YEAR_TAG).text()
                        : result.select(Constants.GARUDA_YEAR_TAG).text();

                String link = portal.equals(Constants.STRING_ZERO)
                            ? result.select(Constants.SCHOLAR_TITLE_TAG).select(Constants.LITTLE_A).attr(Constants.HREF)
                            : result.select(Constants.LITTLE_A).attr(Constants.HREF);

                pageTitleList.add(title);
                Matcher matcher = yearPattern.matcher(tahun);
                if (matcher.find()) {
                    pageYearList.add(matcher.group());  // Ambil tahun yang ditemukan
                }
                pageLinkList.add(portal.equals(Constants.STRING_ZERO) ? link : "https://garuda.kemdikbud.go.id" + link);
            }

            pageData.put(Constants.TITLES, pageTitleList);
            pageData.put(Constants.YEARS, pageYearList);
            pageData.put(Constants.LINKS, pageLinkList);

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

    public Map<String, Map> scrapeJournal(String url) {
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

        Elements results = doc.select(Constants.VIEW_JOURNAL_TAG);
        for (Element result : results) {
            String title = result.select(Constants.VIEW_JOURNAL_TITLE_TAG).text();
            String tahun = result.select(Constants.VIEW_JOURNAL_YEAR_TAG).text().replaceAll("[^0-9]", Constants.EMPTY_STRING).trim();
            String link = result.select(Constants.VIEW_JOURNAL_LINK_TAG).attr(Constants.HREF);

            pageTitleList.add(title);
            pageYearList.add(tahun);
            pageLinkList.add(link);
        }

        returnMap.put(Constants.TITLES, pageTitleList);
        returnMap.put(Constants.YEARS, pageYearList);
        returnMap.put(Constants.LINKS, pageLinkList);

        return returnMap;
    }

}
