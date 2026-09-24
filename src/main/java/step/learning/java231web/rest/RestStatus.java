package step.learning.java231web.rest;

import java.util.Objects;

public class RestStatus {
    private boolean isOk;
    private int code;
    private String message;

    public RestStatus() {
    }

    public RestStatus(boolean isOk, int code, String message) {
        this.isOk = isOk;
        this.code = code;
        this.message = message;
    }

    public static final RestStatus Ok = new RestStatus(true, 200, "OK");
    public static final RestStatus Created = new RestStatus(true, 201, "Created");
    public static final RestStatus Accepted = new RestStatus(true, 202, "Accepted");
    public static final RestStatus NoContent = new RestStatus(true, 204, "No Content");

    public static final RestStatus BadRequest = new RestStatus(false, 400, "Bad Request");
    public static final RestStatus Unauthorized = new RestStatus(false, 401, "Unauthorized");
    public static final RestStatus PaymentRequired = new RestStatus(false, 402, "Payment Required");
    public static final RestStatus Forbidden = new RestStatus(false, 403, "Forbidden");
    public static final RestStatus NotFound = new RestStatus(false, 404, "Not Found");
    public static final RestStatus MethodNotAllowed = new RestStatus(false, 405, "Method Not Allowed");
    public static final RestStatus NotAcceptable = new RestStatus(false, 406, "Not Acceptable");
    public static final RestStatus RequestTimeout = new RestStatus(false, 408, "Request Timeout");
    public static final RestStatus Conflict = new RestStatus(false, 409, "Conflict");
    public static final RestStatus Gone = new RestStatus(false, 410, "Gone");
    public static final RestStatus PayloadTooLarge = new RestStatus(false, 413, "Payload Too Large");
    public static final RestStatus UnsupportedMediaType = new RestStatus(false, 415, "Unsupported Media Type");
    public static final RestStatus UnprocessableEntity = new RestStatus(false, 422, "Unprocessable Entity");
    public static final RestStatus TooManyRequests = new RestStatus(false, 429, "Too Many Requests");

    public static final RestStatus InternalServerError = new RestStatus(false, 500, "Internal Server Error");
    public static final RestStatus NotImplemented = new RestStatus(false, 501, "Not Implemented");
    public static final RestStatus BadGateway = new RestStatus(false, 502, "Bad Gateway");
    public static final RestStatus ServiceUnavailable = new RestStatus(false, 503, "Service Unavailable");
    public static final RestStatus GatewayTimeout = new RestStatus(false, 504, "Gateway Timeout");

    // ----- NON STANDARD --------
    public static final RestStatus HeaderRequired = new RestStatus(false, 440, "Header Required");
    public static final RestStatus HeaderMalformed = new RestStatus(false, 441, "Header Malformed");
    public static final RestStatus QueryParameterRequired = new RestStatus(false, 442, "Query Parameter Required");
    public static final RestStatus QueryParameterMalformed = new RestStatus(false, 443, "Query Parameter Malformed");
    public static final RestStatus TokenRequired = new RestStatus(false, 444, "Token Required");
    public static final RestStatus TokenExpired = new RestStatus(false, 445, "Token Expired");
    public static final RestStatus TokenMalformed = new RestStatus(false, 446, "Token Malformed");
    public static final RestStatus ValidationFailed = new RestStatus(false, 447, "Validation Failed");
    public static final RestStatus LoginOccupied = new RestStatus(false, 448, "Login Occupied");
    public static final RestStatus AccountLocked = new RestStatus(false, 449, "Account Locked");

    public static final RestStatus DatabaseError = new RestStatus(false, 550, "Database Error");
    public static final RestStatus ExternalServiceUnavailable = new RestStatus(false, 551, "External Service Unavailable");

    public boolean isOk() {
        return isOk;
    }

    public RestStatus setOk(boolean isOk) {
        this.isOk = isOk;
        return this;
    }

    public int getCode() {
        return code;
    }

    public RestStatus setCode(int code) {
        this.code = code;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public RestStatus setMessage(String message) {
        this.message = message;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RestStatus that = (RestStatus) o;
        return isOk == that.isOk && code == that.code && Objects.equals(message, that.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(isOk, code, message);
    }

    @Override
    public String toString() {
        return "RestStatus{" +
                "isOk=" + isOk +
                ", code=" + code +
                ", message='" + message + '\'' +
                '}';
    }
}
