package com.zifang.util.doc;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/** 文档元数据。字段都可空；各格式读取器按能力尽力填充。 */
public final class DocMeta {

    private String title;
    private String author;
    private String subject;
    private String keywords;
    private String creator;
    private String producer;
    private OffsetDateTime creationDate;
    private OffsetDateTime modDate;
    private Integer pageCount;
    private final Map<String, String> extra = new LinkedHashMap<>();

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getProducer() {
        return producer;
    }

    public void setProducer(String producer) {
        this.producer = producer;
    }

    public OffsetDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(OffsetDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public OffsetDateTime getModDate() {
        return modDate;
    }

    public void setModDate(OffsetDateTime modDate) {
        this.modDate = modDate;
    }

    public Integer getPageCount() {
        return pageCount;
    }

    public void setPageCount(Integer pageCount) {
        this.pageCount = pageCount;
    }

    public Map<String, String> getExtra() {
        return extra;
    }

    public void putExtra(String key, String value) {
        if (key != null && value != null) {
            extra.put(key, value);
        }
    }
}
