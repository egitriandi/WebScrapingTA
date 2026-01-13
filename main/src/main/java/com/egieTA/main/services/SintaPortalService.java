package com.egieTA.main.services;

import com.egieTA.main.constants.Constants;
import com.egieTA.main.entity.Journal;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

@Service
public class SintaPortalService {
    private String[] userAgents = Constants.USER_AGENT;
    private Pattern yearPattern = Constants.YEAR_PATTERN;

    protected final List getUrl(List paramList, List<Future<Map<String, List<String>>>> futures, ExecutorService executor){
        List returnList = new ArrayList();
        List<String> titleList = new ArrayList<>();
        List<String> collegeList = new ArrayList<>();
        List<String> akreditasiList = new ArrayList<>();
        List<String> linkList = new ArrayList<>();
        Journal j = new Journal();

        for (int i = 1; i <= (Integer) paramList.get(0); i++) {
            final int pageIndex = i;
            futures.add(executor.submit(() -> scrapeData(Constants.STRING_TWO, pageIndex, (String) paramList.get(1), (String) paramList.get(2),
                    (String) paramList.get(3), (Boolean) paramList.get(4))));
        }

        for (Future<Map<String, List<String>>> future : futures) {
            Map<String, List<String>> pageData = null;
            try {
                pageData = future.get(60, TimeUnit.SECONDS);
                if(pageData != null){
                    titleList.addAll(pageData.get(Constants.TITLES));
                    collegeList.addAll(pageData.get(Constants.COLLEGES));
                    akreditasiList.addAll(pageData.get(Constants.AKREDITASI));
                    linkList.addAll(pageData.get(Constants.LINKS));
                }
                j.setIsError(Constants.STRING_NO);
            } catch (Exception e) {
                j.setIsError(Constants.STRING_YES);
                j.setDetailsError(String.valueOf(pageData.get("error")));
                break;
            }
        }

        returnList.add(j);
        returnList.add(titleList);
        returnList.add(collegeList);
        returnList.add(akreditasiList);
        returnList.add(linkList);

        return returnList;
    }

    protected Map<String, List<String>> scrapeData(String portal, int pageIndex, String inputTitle, String fromDate, String toDate, boolean isSingleResult) {
        Map<String, List<String>> pageData = new HashMap<>();
        List<String> titles = new ArrayList<>();
        List<String> colleges = new ArrayList<>();
        List<String> akreditasis = new ArrayList<>();
        List<String> links = new ArrayList<>();

        try {
            String url = Constants.sintaURLBuilder[0] + pageIndex + Constants.sintaURLBuilder[1] + inputTitle;
            Connection con = Jsoup.connect(url)
                    .userAgent(String.valueOf(userAgents[(int) (Math.random() * userAgents.length)]))
                    .timeout(5000);
            Document doc = con.get();
            Thread.sleep(3000);

            Elements results = doc.select(Constants.SINTA_TAG);
            for (Element result : results) {
                titles.add(result.select(Constants.SINTA_TITLE_TAG).text());
                colleges.add(result.select(Constants.SINTA_COLLEGE_TAG).text());

                String a = result.select(Constants.SINTA_AKREDITASI_TAG).text();

                if (a.length() == 24) {
                    String b = a.substring(0, 11);
                    String c = a.substring(13, a.length());
                    akreditasis.add(b.concat(" ||").concat(c));
                } else {
                    akreditasis.add(a);
                }

                links.add(result.select(Constants.LITTLE_A).attr(Constants.HREF));
            }

            pageData.put(Constants.TITLES, titles);
            pageData.put(Constants.COLLEGES, colleges);
            pageData.put(Constants.AKREDITASI, akreditasis);
            pageData.put(Constants.LINKS, links);

        } catch (Exception e) {
            pageData.put("error", Collections.singletonList(e.getMessage()));
        }

        return pageData;
    }

}
