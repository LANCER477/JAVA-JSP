package step.learning.java231web.rest;

public class RestPagination {
    private int page;
    private int perPage;
    private int total;

    public RestPagination() {
    }

    public RestPagination(int page, int perPage, int total) {
        this.page = page;
        this.perPage = perPage;
        this.total = total;
    }

    public int getPage() {
        return page;
    }

    public RestPagination setPage(int page) {
        this.page = page;
        return this;
    }

    public int getPerPage() {
        return perPage;
    }

    public RestPagination setPerPage(int perPage) {
        this.perPage = perPage;
        return this;
    }

    public int getTotal() {
        return total;
    }

    public RestPagination setTotal(int total) {
        this.total = total;
        return this;
    }
}
