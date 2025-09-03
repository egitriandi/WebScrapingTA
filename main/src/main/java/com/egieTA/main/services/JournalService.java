package com.egieTA.main.services;

import com.egieTA.main.constants.Constants;
import com.egieTA.main.entity.Journal;
import com.egieTA.main.repo.JournalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class JournalService {

    @Autowired
    private JournalRepository journalRepository;

    public void saveJournals(List result, String portal, String keyword){
        List returnList = new ArrayList();

        for(int i = 0; i < result.size(); i++){
            Journal j = new Journal();
            String[] tempArray = (String[]) result.get(i);
            j.setTitle(tempArray[0]);

            if(!portal.equals("2")){
                try {
                    short tempYear = Short.parseShort(tempArray[1]);
                    j.setYear(tempYear);
                }catch (NumberFormatException ne){
                    ne.getMessage();
                }
            }else{
                j.setInstansi(tempArray[1]);
                j.setAkreditasi(tempArray[2]);
            }

            j.setUrl(tempArray[2]);

            String tempSource = portal.equals(Constants.STRING_ZERO) ? "Google Scholar"
                    : portal.equals(Constants.STRING_ONE) ? "Garuda"
                    : portal.equals(Constants.STRING_TWO) ? "Sinta"
                    : "Unknown";
            j.setSource(tempSource);

            j.setKeyword(keyword);

            returnList.add(j);
        }

        for(int i = 0; i < returnList.size(); i++){
            Journal j = (Journal) returnList.get(i);
            journalRepository.save(j);
        }
    }

    public List<Journal> findByKeywordAndSourceNative(String keyword, List source){
        List<Journal> returnList = new ArrayList();

        returnList = journalRepository.findByKeywordAndSourceNative(keyword, source);

        return returnList;
    }

}
