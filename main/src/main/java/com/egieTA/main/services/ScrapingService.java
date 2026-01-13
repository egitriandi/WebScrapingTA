package com.egieTA.main.services;

import com.egieTA.main.constants.Constants;
import com.egieTA.main.entity.Journal;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private ScholarPortalService scholar;

    @Autowired
    private GarudaPortalService garuda;

    @Autowired
    private SintaPortalService sinta;

    public Map scrapePortal(String[] portal, String inputTitle, String fromDate, String toDate, boolean isSingleResult) throws  InterruptedException {
        Map resultMap = new HashMap();
        inputTitle = inputTitle.replaceAll(Constants.WHITE_SPACE_REGEX, Constants.PLUS_OPERATOR);
        Journal j = new Journal();

        List scholarList = new ArrayList();
        List garudaList = new ArrayList();
        List sintaList = new ArrayList();

        if (portal.length > 0) {
            ExecutorService executor = Executors.newFixedThreadPool(Constants.ATTEMPT); // Atur thread pool
            List<Future<Map<String, List<String>>>> futures = new ArrayList<>();
            int attempt = isSingleResult ? 1 : Constants.ATTEMPT;

            List paramList = setParamList(attempt, inputTitle, fromDate, toDate, isSingleResult);

            //SCHOLAR
            if (Arrays.asList(portal).contains(Constants.STRING_ZERO)) {
                scholarList = scholar.getUrl(paramList, futures, executor);
                String status = ((Journal)scholarList.get(0)).getIsError();
                if(status.equals(Constants.STRING_YES)){
                    Journal temp = (Journal) scholarList.get(0);
                    j.setIsError(temp.getIsError());
                    j.setDetailsError(temp.getDetailsError());
                    resultMap.put("SCHOLAR_ERROR", new String[]{j.getIsError(), j.getDetailsError()});
                }
            }

            //GARUDA
            if (Arrays.asList(portal).contains(Constants.STRING_ONE)) {
                garudaList = garuda.getUrl(paramList, futures, executor);
                String status = ((Journal)garudaList.get(0)).getIsError();
                if(status.equals(Constants.STRING_YES)){
                    Journal temp = (Journal) garudaList.get(0);
                    j.setIsError(temp.getIsError());
                    j.setDetailsError(temp.getDetailsError());
                    resultMap.put("GARUDA_ERROR", new String[]{j.getIsError(), j.getDetailsError()});
                }
            }

            //SINTA
            if (Arrays.asList(portal).contains(Constants.STRING_TWO)) {
                sintaList = sinta.getUrl(paramList, futures, executor);
                String status = ((Journal)sintaList.get(0)).getIsError();
                if(status.equals(Constants.STRING_YES)){
                    Journal temp = (Journal) sintaList.get(0);
                    j.setIsError(temp.getIsError());
                    j.setDetailsError(temp.getDetailsError());
                    resultMap.put("SINTA_ERROR", new String[]{j.getIsError(), j.getDetailsError()});
                }
            }

            executor.shutdownNow();
            executor.awaitTermination(1, TimeUnit.SECONDS);
        }

        resultMap.put("SCHOLAR", scholarList);
        resultMap.put("GARUDA", garudaList);
        resultMap.put("SINTA", sintaList);

        return resultMap;
    }

    private List setParamList(int attempt, String inputTitle, String fromDate, String toDate, Boolean isSingleResult){
        List paramList = new ArrayList();
        paramList.add(attempt);
        paramList.add(inputTitle);
        paramList.add(fromDate);
        paramList.add(toDate);
        paramList.add(isSingleResult);
        return paramList;
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

    public boolean isSingleResult(String query) {
        // 1. Jika ada tanda kutip, kemungkinan besar itu judul spesifik
        if (query.contains("\"")) {
            return true;
        }

        // 2. Hitung jumlah kata dalam input
        String[] words = query.trim().split("\\s+"); // Pisahkan berdasarkan spasi
        int wordCount = words.length;

        // Jika jumlah kata >= 6, kemungkinan besar itu judul lengkap
        if (wordCount >= 6) {
            return true;
        }

        // 3. Jika input mengandung angka (tahun), bisa jadi itu jurnal spesifik
        if (query.matches(".*\\b(19|20)\\d{2}\\b.*")) { // Cocokkan dengan tahun (1900-2099)
            return true;
        }

        // 4. Jika input terlihat seperti DOI atau ISBN
        if (query.matches("^10\\.\\d{4,9}/[-._;()/:A-Za-z0-9]+$") || query.matches("\\d{3}-\\d{1,5}-\\d{1,7}-\\d{1,7}-\\d{1}$")) {
            return true;
        }

        // Jika tidak memenuhi kriteria, anggap sebagai pencarian banyak hasil
        return false;
    }

    public List scholarGarHandler(List result, String inputTitle, int resultAsk){
        List returnList = new ArrayList();
        List judulList = new ArrayList();
        List yearList = new ArrayList();
        List linkList = new ArrayList();

        Set<String> uniqueSet = new HashSet<>(); // Set untuk memastikan keunikan data
        judulList.addAll((Collection) result.get(1));
        yearList.addAll((Collection) result.get(2));
        linkList.addAll((Collection) result.get(3));

        int attempt = 0;
        if(isSingleResult(inputTitle)){
            attempt = 1;
        }else{
            attempt = resultAsk;
        }

        for(int i = 0; i < judulList.size(); i++){
            String a = (String) judulList.get(i);
            String b = (String) yearList.get(i);
            String c = (String) linkList.get(i);

            if(a.equals(Constants.EMPTY_STRING) || b.equals(Constants.EMPTY_STRING) || c.equals(Constants.EMPTY_STRING)){
                continue;
            }

            String uniqueKey = a + "|" + b + "|" + c;

            if (!uniqueSet.contains(uniqueKey)) { // Periksa apakah sudah ada di set
                uniqueSet.add(uniqueKey); // Tambahkan ke set jika belum ada
            }
        }

        for(String tempText : uniqueSet){
            String[] parts = tempText.split("\\|");
            if(returnList.size() == attempt){
                break;
            }else{
                returnList.add(parts);
            }
        }

        return returnList;
    }

    public List SintaHandler(List result, String inputTitle, int resultAsk){
        List resultList = new ArrayList();

        List titleList = new ArrayList();
        List collegeList = new ArrayList();
        List akreditasiList = new ArrayList();
        List linkList = new ArrayList();

        Set<String> uniqueSet = new HashSet<>(); // Set untuk memastikan keunikan data

        titleList.addAll((Collection) result.get(1));
        collegeList.addAll((Collection) result.get(2));
        akreditasiList.addAll((Collection) result.get(3));
        linkList.addAll((Collection) result.get(4));

        for(int i = 0; i < titleList.size(); i++){
            String a = (String) titleList.get(i);
            String b = (String) collegeList.get(i);
            String c = (String) akreditasiList.get(i);
            String d = (String) linkList.get(i);

            if(a.equals(Constants.EMPTY_STRING) || b.equals(Constants.EMPTY_STRING) || c.equals(Constants.EMPTY_STRING) || d.equals(Constants.EMPTY_STRING)){
                continue;
            }

            // Gabungkan semua atribut menjadi satu string unik
            String uniqueKey = a + "|" + b + "|" + c + "|" + d;

            if (!uniqueSet.contains(uniqueKey)) { // Periksa apakah sudah ada di set
                uniqueSet.add(uniqueKey); // Tambahkan ke set jika belum ada
            }
        }

        for(String tempText : uniqueSet){
            String[] parts = tempText.split("\\|");
            if (parts[2] != null) {
                String[] temp = parts[2].split(Constants.SPACE, 3);
                if (temp.length == 3) {
                    String akreditasiSpliter = temp[0] + Constants.SPACE + temp[1] + " || " + temp[2];
                    parts[2] = akreditasiSpliter;
                }
            }
            if(resultList.size() == resultAsk){
                break;
            }else{
                resultList.add(parts);
            }
        }
        return resultList;
    }
}
