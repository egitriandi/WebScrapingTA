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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class GarudaPortalService {
    private String[] userAgents = Constants.USER_AGENT;
    private Pattern yearPattern = Constants.YEAR_PATTERN;

    protected final List getUrl(List paramList, List<Future<Map<String, List<String>>>> futures, ExecutorService executor){
        List returnList = new ArrayList();
        List<String> titleList = new ArrayList<>();
        List<String> yearList = new ArrayList<>();
        List<String> linkList = new ArrayList<>();
        Journal j = new Journal();

        for (int i = 1; i <= (Integer) paramList.get(0); i++) {
            final int pageIndex = i;
            futures.add(executor.submit(() -> scrapeData(Constants.STRING_ONE, pageIndex, (String) paramList.get(1), (String) paramList.get(2),
                    (String) paramList.get(3), (Boolean) paramList.get(4))));
        }

        for (Future<Map<String, List<String>>> future : futures) {
            Map<String, List<String>> pageData = null;
            try {
                pageData = future.get(60, TimeUnit.SECONDS);
                if(pageData != null){
                    titleList.addAll(pageData.get(Constants.TITLES));
                    yearList.addAll(pageData.get(Constants.YEARS));
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
        returnList.add(yearList);
        returnList.add(linkList);

        return returnList;
    }

    protected Map<String, List<String>> scrapeData(String portal, int pageIndex, String finalInputTitle, String fromDate, String toDate, boolean isSingleResult) {
        Map<String, List<String>> pageData = new HashMap<>();
        List<String> pageTitleList = new ArrayList<>();
        List<String> pageYearList = new ArrayList<>();
        List<String> pageLinkList = new ArrayList<>();

        try {
            String url = Constants.EMPTY_STRING;

            if(isSingleResult){
                url = portal.equals(Constants.STRING_ZERO)
                        ? Constants.scholarSingleURLBuilder[0] + finalInputTitle
                        : Constants.garudaSingleURLBuilder[0] + finalInputTitle;
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
                    .timeout(5000);
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
                pageLinkList.add(portal.equals(Constants.STRING_ZERO) ? link : Constants.garudaURL + link);
            }

            pageData.put(Constants.TITLES, pageTitleList);
            pageData.put(Constants.YEARS, pageYearList);
            pageData.put(Constants.LINKS, pageLinkList);

        } catch (Exception e) {
            pageData.put("error", Collections.singletonList(e.getMessage()));
        }

        return pageData;
    }
}
