package com.egieTA.main.repo;

import com.egieTA.main.entity.Journal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface JournalRepository extends JpaRepository<Journal, Integer> {
    // Bisa tambahkan custom method jika butuh
    Journal findByTitle(String title);

    @Query(
            value = "SELECT * FROM journals WHERE keyword = :keyword AND source IN (:source) ORDER BY year DESC",
            nativeQuery = true
    )
    List<Journal> findByKeywordAndSourceNative(
            @Param("keyword") String keyword,
            @Param("source") List source);
}


