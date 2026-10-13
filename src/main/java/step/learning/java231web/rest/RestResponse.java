package step.learning.java231web.rest;

public class RestResponse {
    private RestStatus status;
    private RestMeta meta;
    private Object data;

    public RestResponse() {
        this.status = RestStatus.Ok;
        this.meta = new RestMeta();
    }

    public RestResponse(RestStatus status) {
        this.status = status;
        this.meta = new RestMeta();
    }

    public RestResponse(RestStatus status, Object data) {
        this.status = status;
        this.data = data;
        this.meta = new RestMeta();
    }

    public RestStatus getStatus() {
        return status;
    }

    public RestResponse setStatus(RestStatus status) {
        this.status = status;
        return this;
    }

    public RestMeta getMeta() {
        return meta;
    }

    public RestResponse setMeta(RestMeta meta) {
        this.meta = meta;
        return this;
    }

    public RestPagination getPagination() {
        return meta != null ? meta.getPagination() : null;
    }

    public RestResponse setPagination(RestPagination pagination) {
        if (this.meta == null) {
            this.meta = new RestMeta();
        }
        this.meta.setPagination(pagination);
        return this;
    }

    public Object getData() {
        return data;
    }

    public RestResponse setData(Object data) {
        this.data = data;
        return this;
    }
}
