package step.learning.java231web.rest;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RestMeta {
    private String serviceName;
    private String dataType;
    private long serverTime;
    private long cacheTime;
    private List<String> manipulations;
    private Map<String, String> links;
    private Object parameters;
    private RestPagination pagination;

    public RestMeta() {
        this.serviceName = "API 'Product Sections'";
        this.dataType = "array";
        this.serverTime = System.currentTimeMillis();
        this.cacheTime = 1000000L;
        this.manipulations = new ArrayList<>();
        this.manipulations.add("GET");
        this.links = new LinkedHashMap<>();
        this.links.put("list", "/");
        this.links.put("details", "/{slug}");
        this.parameters = null;
    }

    public RestMeta(String serviceName, String dataType) {
        this();
        this.serviceName = serviceName;
        this.dataType = dataType;
    }

    public String getServiceName() {
        return serviceName;
    }

    public RestMeta setServiceName(String serviceName) {
        this.serviceName = serviceName;
        return this;
    }

    public String getService() {
        return serviceName;
    }

    public RestMeta setService(String service) {
        this.serviceName = service;
        return this;
    }

    public String getDataType() {
        return dataType;
    }

    public RestMeta setDataType(String dataType) {
        this.dataType = dataType;
        return this;
    }

    public long getServerTime() {
        return serverTime;
    }

    public RestMeta setServerTime(long serverTime) {
        this.serverTime = serverTime;
        return this;
    }

    public long getCacheTime() {
        return cacheTime;
    }

    public RestMeta setCacheTime(long cacheTime) {
        this.cacheTime = cacheTime;
        return this;
    }

    public List<String> getManipulations() {
        return manipulations;
    }

    public RestMeta setManipulations(List<String> manipulations) {
        this.manipulations = manipulations;
        return this;
    }

    public Map<String, String> getLinks() {
        return links;
    }

    public RestMeta setLinks(Map<String, String> links) {
        this.links = links;
        return this;
    }

    public Object getParameters() {
        return parameters;
    }

    public RestMeta setParameters(Object parameters) {
        this.parameters = parameters;
        return this;
    }

    public RestPagination getPagination() {
        return pagination;
    }

    public RestMeta setPagination(RestPagination pagination) {
        this.pagination = pagination;
        return this;
    }

    public long getTimestamp() {
        return serverTime;
    }

    public RestMeta setTimestamp(long timestamp) {
        this.serverTime = timestamp;
        return this;
    }

    public Date getDatetime() {
        return new Date(serverTime);
    }

    public RestMeta setDatetime(Date datetime) {
        if (datetime != null) {
            this.serverTime = datetime.getTime();
        }
        return this;
    }

    public String getAuthor() {
        return "Lector";
    }

    public RestMeta setAuthor(String author) {
        return this;
    }
}
