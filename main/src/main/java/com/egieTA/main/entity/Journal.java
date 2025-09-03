package com.egieTA.main.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "journals")
public class Journal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(columnDefinition = "TEXT", nullable = false, unique = true)
    private String title;

    @Column(nullable = false)
    private String url;

    @Column(nullable = false)
    private String source;

    @Column(name = "scraped_at", insertable = false, updatable = false)
    private LocalDateTime scrapedAt;

    @Column(nullable = false)
    private String keyword;

    @Column(nullable = false)
    private short year;

    @Column(nullable = false)
    private String instansi;

    @Column(nullable = false)
    private String akreditasi;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public LocalDateTime getScrapedAt() {
        return scrapedAt;
    }

    public void setScrapedAt(LocalDateTime scrapedAt) {
        this.scrapedAt = scrapedAt;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public short getYear() {
        return year;
    }

    public void setYear(short year) {
        this.year = year;
    }

    public String getInstansi() {
        return instansi;
    }

    public void setInstansi(String instansi) {
        this.instansi = instansi;
    }

    public String getAkreditasi() {
        return akreditasi;
    }

    public void setAkreditasi(String akreditasi) {
        this.akreditasi = akreditasi;
    }
}
