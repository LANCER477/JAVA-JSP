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

    public RestResponse(RestStatus status, RestMeta meta, Object data) {
        this.status = status;
        this.meta = meta;
        this.data = data;
    }

    public static RestResponse Ok(RestMeta meta, Object data) {
        return new RestResponse(RestStatus.Ok, meta, data);
    }

    public static RestResponse NotFound(RestMeta meta, Object data) {
        return new RestResponse(RestStatus.NotFound, meta, data);
    }

    public static RestResponse ErrorInternal(RestMeta meta, Object data) {
        return new RestResponse(RestStatus.InternalServerError, meta, data);
    }

    public static RestResponse AlreadyReported(RestMeta meta, Object data) {
        return new RestResponse(RestStatus.AlreadyReported, meta, data);
    }

    public static RestResponse Unprocessable(RestMeta meta, Object data) {
        return new RestResponse(RestStatus.Unprocessable, meta, data);
    }

    public static RestResponse QueryParamMissed(RestMeta meta, Object data) {
        return new RestResponse(RestStatus.QueryParamMissed, meta, data);
    }

    public static RestResponse Forbidden(RestMeta meta, Object data) {
        return new RestResponse(RestStatus.Forbidden, meta, data);
    }

    public static RestResponse Conflict(RestMeta meta, Object data) {
        return new RestResponse(RestStatus.Conflict, meta, data);
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
