package com.egieTA.main.repo;

import com.egieTA.main.entity.Journal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface JournalRepository extends JpaRepository<Journal, Integer> {
    // Bisa tambahkan custom method jika butuh
    Journal findByTitle(String title);

    @Query(
            value = "SELECT * FROM journals WHERE keyword = :keyword AND source IN (:source) ORDER BY scraped_at DESC",
            nativeQuery = true
    )
    List<Journal> findByKeywordAndSourceNative(
            @Param("keyword") String keyword,
            @Param("source") List source);

    @Modifying
    @Transactional
    @Query(value = """
        INSERT INTO journals 
        (title, url, source, keyword, year, instansi, akreditasi) 
        VALUES (:title, :url, :source, :keyword, :year, :instansi, :akreditasi)
        ON CONFLICT (title) DO NOTHING
    """, nativeQuery = true)
    void insertIgnoreDuplicate(@Param("title") String title,
                               @Param("url") String url,
                               @Param("source") String source,
//                               @Param("scrapedAt") LocalDateTime scrapedAt,
                               @Param("keyword") String keyword,
                               @Param("year") int year,
                               @Param("instansi") String instansi,
                               @Param("akreditasi") String akreditasi);
}


