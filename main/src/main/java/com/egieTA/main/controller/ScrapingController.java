package com.egieTA.main.controller;

import com.egieTA.main.entity.Journal;
import com.egieTA.main.services.JournalService;
import com.egieTA.main.services.ScrapingService;
import com.egieTA.main.constants.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@Controller
public class ScrapingController {
    private static final Logger logger = LoggerFactory.getLogger(ScrapingController.class);

    @Autowired
    private ScrapingService scrapingService;

    @Autowired
    private JournalService journalService;

    @GetMapping("/")
    public String home() {
        return Constants.INDEX;  // menampilkan halaman input
    }

    @GetMapping("/viewJournal")
    public String viewJournal(@RequestParam String url, Model model) {
        logger.info("Menerima request scraping untuk URL: {}", url);

        // Validasi URL
        if (url == null || url.isEmpty()) {
            logger.error("Error: URL tidak valid atau kosong.");
            model.addAttribute("errorMessage", "URL tidak valid atau kosong.");
            return Constants.ERROR; // Redirect ke halaman error
        }

        // Lakukan scraping berdasarkan URL yang diterima
        Map<String, Map> result;

        try {
            result = scrapingService.scrapeJournal(url);
        }catch (Exception e) {
            logger.error("Gagal melakukan scraping: {}", e.getMessage());
            model.addAttribute("errorMessage", "Terjadi kesalahan saat mengambil data.");
            return Constants.ERROR; // Redirect ke halaman error
        }

        List<Map<String, String>> scholarData = evoker((Map<String, Object>) result.get("scholarData"));
        List<Map<String, String>> garudaData = evoker((Map<String, Object>) result.get("garudaData"));

        model.addAttribute("forScholar", "Google Scholar");
        model.addAttribute("forScholarData", scholarData);
        model.addAttribute("forGaruda", "Garuda");
        model.addAttribute("forGarudaData", garudaData);

        return "viewJournal"; // Tampilan hasil scraping (HTML baru)
    }

    @PostMapping("/scrapeJournal")
    public ResponseEntity<String> scrapeJournal(@RequestBody Map<String, String> request) {
        String url = request.get("url");
        if (url == null || url.isEmpty()) {
            return ResponseEntity.badRequest().body("URL tidak boleh kosong");
        }

        // Kirim URL tujuan sebagai respons
        String redirectUrl = "/viewJournal?url=" + url;
        return ResponseEntity.ok(redirectUrl);
    }

    private List evoker(Map portal){
        List returnList = new ArrayList();
        List judulList = new ArrayList();
        List yearList = new ArrayList();
        List linkList = new ArrayList();

        judulList.addAll((Collection) portal.get("titles"));
        yearList.addAll((Collection) portal.get("years"));
        linkList.addAll((Collection) portal.get("links"));

        for(int i = 0; i < judulList.size(); i++){
            String a = (String) judulList.get(i);
            String b = (String) yearList.get(i);
            String c = (String) linkList.get(i);
            String[] d = {a,b,c};

            returnList.add(d);
        }

        return returnList;
    }

    @PostMapping("/")
    public String scrape(@RequestParam(value = "portal", required = false) String portal,
                         @RequestParam("inputTitle") String inputTitle,
                         @RequestParam(value = "fromDate", required = false) String fromDate,
                         @RequestParam(value = "toDate", required = false) String toDate,
                         @RequestParam(value = "resultAsk", required = false) int resultAsk,
                         Model model) {
        // Validasi portal harus dipilih
        if (portal == null || portal.trim().isEmpty()) {
            model.addAttribute("errorMessage", "Silakan pilih minimal satu portal untuk melakukan pencarian.");
            return Constants.INDEX; // Kembali ke halaman dengan pesan error
        }

        // Validasi tahun wajib diisi jika portal adalah Google Scholar atau GARUDA
        if ((portal.contains("0") || portal.contains("1")) && (fromDate == null || toDate == null || fromDate.isEmpty() || toDate.isEmpty())) {
            model.addAttribute("errorMessage", "Silakan isi jarak tahun untuk Google Scholar dan GARUDA.");
            return Constants.INDEX;
        }

        if(resultAsk == 0 || resultAsk > 50){
            model.addAttribute("errorMessage", "Jumlah Result yang dibutuhkan harus > 0 atau <= 50");
            return Constants.INDEX;
        }

        String[] tempPortal = portal.split("\\,");
        LocalDateTime ldt = LocalDateTime.now();
        int fromDateParse = tempPortal.length == 1 && tempPortal[0].equals(Constants.STRING_TWO) ? ldt.getYear() -4 : Integer.parseInt(fromDate);
        int toDateParse = tempPortal.length == 1 && tempPortal[0].equals(Constants.STRING_TWO) ?  ldt.getYear() : Integer.parseInt(toDate);

        Map sourceMap = needScrape(portal,inputTitle, fromDateParse, toDateParse, resultAsk);
            try {
                Map result = new HashMap();
                List handlerList = new ArrayList();
                List temp;
                String[] isError;

                //SCOLAR
                if(Arrays.asList(tempPortal).contains(Constants.STRING_ZERO)){
                    Boolean needScrape = (boolean)((Map)sourceMap.get("needScrape")).get("Google Scholar");
                    if(needScrape){
                        result = scrapingResult(tempPortal, inputTitle, fromDate, toDate);

                        isError = result.containsKey("SCHOLAR_ERROR") ? (String[]) result.get("SCHOLAR_ERROR") : new String[]{Constants.STRING_NO, Constants.EMPTY_STRING};
                        if(isError[0].equals(Constants.STRING_NO)){
                            temp = (List) result.get("SCHOLAR");
                            handlerList = scrapingService.scholarGarHandler(temp, inputTitle, resultAsk);
                        }else{
                            model.addAttribute("isErrorScholar", isError[1]);
                        }
                        saveJurnals(handlerList, isError, Constants.STRING_ZERO, inputTitle, resultAsk);
                        model.addAttribute("pickedPortalScholar", "Google Scholar");
                        model.addAttribute("resultListScholar", handlerList);
                    }else{
                        List tempList = (List) ((Map)sourceMap.get("dataList")).get("Google Scholar");
                        List dataList = new ArrayList();
                        for(int i = 0; i < tempList.size(); i++){
                            Journal j = (Journal) tempList.get(i);
                            if(j.getSource().equals("Google Scholar")){
                                String[] parts = {j.getTitle(), String.valueOf(j.getYear()), j.getUrl()};
                                if(dataList.size() == resultAsk){
                                    break;
                                }else{
                                    dataList.add(parts);
                                }
                            }
                        }
                        model.addAttribute("pickedPortalScholar", "Google Scholar");
                        model.addAttribute("resultListScholar", dataList);
                    }
                }

                //GARUDA
                if(Arrays.asList(tempPortal).contains(Constants.STRING_ONE)){
                    Boolean needScrape = (boolean)((Map)sourceMap.get("needScrape")).get("Garuda");
                    if(needScrape){
                        result = scrapingResult(tempPortal, inputTitle, fromDate, toDate);

                        isError = result.containsKey("GARUDA_ERROR") ? (String[]) result.get("GARUDA_ERROR") : new String[]{Constants.STRING_NO, Constants.EMPTY_STRING};
                        if(isError[0].equals(Constants.STRING_NO)){
                            temp = (List) result.get("GARUDA");
                            handlerList = scrapingService.scholarGarHandler(temp, inputTitle, resultAsk);
                        }else{
                            model.addAttribute("isErrorGaruda", isError[1]);
                        }
                        saveJurnals(handlerList, isError, Constants.STRING_ONE, inputTitle, resultAsk);
                        model.addAttribute("pickedPortalGaruda", "GARUDA");
                        model.addAttribute("resultListGaruda", handlerList);
                    }else{
                        List tempList = (List) ((Map)sourceMap.get("dataList")).get("Garuda");
                        List dataList = new ArrayList();
                        for(int i = 0; i < tempList.size(); i++){
                            Journal j = (Journal) tempList.get(i);
                            if(j.getSource().equals("Garuda")){
                                String[] parts = {j.getTitle(), String.valueOf(j.getYear()), j.getUrl()};
                                if(dataList.size() == resultAsk){
                                    break;
                                }else{
                                    dataList.add(parts);
                                }
                            }
                        }
                        model.addAttribute("pickedPortalGaruda", "GARUDA");
                        model.addAttribute("resultListGaruda", dataList);
                    }
                }

                //SINTA
                if(Arrays.asList(tempPortal).contains(Constants.STRING_TWO)){
                    Boolean needScrape = (boolean)((Map)sourceMap.get("needScrape")).get("Sinta");
                    if(needScrape){
                        isError = result.containsKey("SINTA_ERROR") ? (String[]) result.get("SINTA_ERROR") : new String[]{Constants.STRING_NO, Constants.EMPTY_STRING};
                        if(isError[0].equals(Constants.STRING_NO)){
                            temp = (List) result.get("SINTA");
                            handlerList = scrapingService.SintaHandler(temp, inputTitle, resultAsk);
                        }else{
                            model.addAttribute("isErrorSinta", isError[1]);
                        }
                        saveJurnals(handlerList, isError, Constants.STRING_TWO, inputTitle, resultAsk);
                        model.addAttribute("pickedPortalSinta", "SINTA");
                        model.addAttribute("resultListSinta", handlerList);
                    }else{
                        List tempList = (List) ((Map)sourceMap.get("dataList")).get("Sinta");
                        List dataList = new ArrayList();
                        for(int i = 0; i < tempList.size(); i++){
                            Journal j = (Journal) tempList.get(i);
                            if(j.getSource().equals("Sinta")){
                                String[] parts = {j.getTitle(), j.getInstansi(), j.getAkreditasi(), j.getUrl()};
                                if(dataList.size() == resultAsk){
                                    break;
                                }else{
                                    dataList.add(parts);
                                }
                            }
                        }
                        model.addAttribute("pickedPortalSinta", "SINTA");
                        model.addAttribute("resultListSinta", dataList);
                    }

                }
                model.addAttribute("keyword", inputTitle);
            } catch (Exception e) {
                model.addAttribute("result", "Error: " + e.getMessage());
            }
        model.addAttribute("keyword", inputTitle);
        return Constants.INDEX;
    }

    private void saveJurnals(List handlerList, String[] isError, String setPortal, String inputTitle, int resultAsk){
        if(handlerList.size() > 0 || (!isError[0].isEmpty())){
            journalService.saveJournals(handlerList, setPortal, inputTitle, isError[0], isError[1], resultAsk);
        }
    }

    private Map scrapingResult(String[] tempPortal, String inputTitle, String fromDate, String toDate){
        Map returnMap = new HashMap();
        try {
            returnMap = scrapingService.scrapePortal(tempPortal, inputTitle, fromDate, toDate, scrapingService.isSingleResult(inputTitle));
        }catch (Exception e){
            logger.error(e.getMessage());
        }
        return returnMap;
    }

    private Map needScrape(String portal, String inputTitle, int fromYear, int toYear, int resultAsk){
        Map returnMap = new HashMap();

        List sourceList = new ArrayList();
        Boolean portalChecker = portal.length() > 1;

        if(portalChecker){
            sourceList = Arrays.asList(portal.split(","));
        }else{
            sourceList.add(portal);
        }
        for(int i = 0; i < sourceList.size(); i++){
            String tempString = sourceList.get(i).toString();
            sourceList.set(i, tempString.equals("0") ? "Google Scholar"
                    : tempString.equals("1") ? "Garuda"
                    : tempString.equals("2") ? "Sinta"
                    : "Unknown");
        }

        LocalDateTime ldt = LocalDateTime.now();
        List dataList = journalService.findByKeywordAndSourceNative(inputTitle, sourceList);
        Map sourceRecord = new HashMap<>();
        Map resultMap = new HashMap<>();
        Map recordMap = new HashMap<>();
        if(dataList.size() > 0){
            for(int i = 0; i < dataList.size(); i++){
                Journal j = (Journal) dataList.get(i);
                if(!sourceRecord.containsKey(j.getSource())){
                    sourceRecord.put(j.getSource(), j.getSource());
                }
            }

            if(sourceRecord.size() < sourceList.size()){
                for(int i = 0; i < sourceList.size(); i++){
                    resultMap.put(sourceList.get(i), sourceRecord.containsKey(sourceList.get(i)) ? false : true);
                }
            }
            List tempList = new ArrayList();

            for(int i = 0; i < sourceList.size(); i++){
                tempList = new ArrayList();
                if(resultMap.containsKey(sourceList.get(i)) && (boolean) resultMap.get(sourceList.get(i)) == false){
                    for(int z = 0; z < dataList.size(); z++){
                        Journal j = (Journal) dataList.get(z);
                        if((j.getYear() >= fromYear && j.getYear() <= toYear && (j.getSource().equals("Garuda") || j.getSource().equals("Google Scholar")))
                                || (j.getYear() == 0 && j.getSource().equals("Sinta"))){
                            tempList.add(j);
                        }else{
                            if(tempList.size() < resultAsk){
                                resultMap.put(sourceList.get(i), true);
                            }
                        }
                    }
                    recordMap.put(sourceList.get(i), tempList);
                }
            }
            returnMap.put("needScrape", resultMap);
            returnMap.put("dataList", recordMap);
        }else{
            for(int i = 0; i < sourceList.size(); i++){
                sourceRecord.put(sourceList.get(i), true);
            }
            returnMap.put("needScrape", true);
            returnMap.put("dataList", recordMap);
        }

        return returnMap;
    }

//    private Boolean reallyNeedScrape(List dataList, int fromYear, int toYear,  LocalDateTime ldt, int resultAsk){
//        if(dataList.size() < resultAsk){
//            return true;
//        }
//        for(int i = 0; i < dataList.size(); i++){
//            Journal j = (Journal) dataList.get(i);
//            if((j.getSource().equals("Garuda") || j.getSource().equals("Google Scholar") && (j.getYear() == fromYear || j.getYear() == toYear))
//                || (j.getSource().equals("Sinta") && j.getYear() == 0)){
//                return false;
//            }
//            Duration duration = Duration.between(j.getScrapedAt(), ldt);
//            return duration.toHours() >= 6 ? true : false;
//        }
//
//        return true;
//    }

}
