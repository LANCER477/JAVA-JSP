package step.learning.java231web.rest;

public class RestPagination {
    private int page;
    private int pageSize;
    private int totalPages;
    private int totalItems;

    public RestPagination() {
        this.page = 1;
        this.pageSize = 10;
        this.totalPages = 1;
        this.totalItems = 0;
    }

    public RestPagination(int page, int pageSize, int totalItems) {
        this.page = page > 0 ? page : 1;
        this.pageSize = pageSize > 0 ? pageSize : 10;
        this.totalItems = Math.max(0, totalItems);
        updateTotalPages();
    }

    public int getPage() {
        return page;
    }

    public RestPagination setPage(int page) {
        this.page = page;
        return this;
    }

    public int getPerPage() {
        return pageSize;
    }

    public RestPagination setPerPage(int perPage) {
        this.pageSize = perPage;
        updateTotalPages();
        return this;
    }

    public int getPageSize() {
        return pageSize;
    }

    public RestPagination setPageSize(int pageSize) {
        this.pageSize = pageSize;
        updateTotalPages();
        return this;
    }

    public int getTotal() {
        return totalItems;
    }

    public RestPagination setTotal(int total) {
        this.totalItems = total;
        updateTotalPages();
        return this;
    }

    public int getTotalItems() {
        return totalItems;
    }

    public RestPagination setTotalItems(int totalItems) {
        this.totalItems = totalItems;
        updateTotalPages();
        return this;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public RestPagination setTotalPages(int totalPages) {
        this.totalPages = totalPages;
        return this;
    }

    private void updateTotalPages() {
        if (pageSize > 0) {
            this.totalPages = (int) Math.ceil((double) totalItems / pageSize);
            if (this.totalPages == 0) {
                this.totalPages = 1;
            }
        } else {
            this.totalPages = 1;
        }
    }
}
