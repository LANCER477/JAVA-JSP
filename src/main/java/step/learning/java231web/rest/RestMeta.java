package step.learning.java231web.rest;

import java.util.Date;

public class RestMeta {
    private String service;
    private String author;
    private long timestamp;
    private Date datetime;

    public RestMeta() {
        this.timestamp = System.currentTimeMillis();
        this.datetime = new Date();
    }

    public RestMeta(String service, String author) {
        this();
        this.service = service;
        this.author = author;
    }

    public String getService() {
        return service;
    }

    public RestMeta setService(String service) {
        this.service = service;
        return this;
    }

    public String getAuthor() {
        return author;
    }

    public RestMeta setAuthor(String author) {
        this.author = author;
        return this;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public RestMeta setTimestamp(long timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public Date getDatetime() {
        return datetime;
    }

    public RestMeta setDatetime(Date datetime) {
        this.datetime = datetime;
        return this;
    }
}
