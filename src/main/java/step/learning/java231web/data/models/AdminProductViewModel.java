package step.learning.java231web.data.models;

import java.util.ArrayList;
import java.util.List;
import step.learning.java231web.data.dto.Product;
import step.learning.java231web.data.dto.ProductSection;

public class AdminProductViewModel {
    private List<ProductSection> productSections = new ArrayList<>();
    private List<Product> products = new ArrayList<>();
    private String okMessage;
    private String exceptionMessage;

    public AdminProductViewModel() {
    }

    public List<ProductSection> getProductSections() {
        return productSections;
    }

    public AdminProductViewModel setProductSections(List<ProductSection> productSections) {
        this.productSections = productSections != null ? productSections : new ArrayList<>();
        return this;
    }

    public List<Product> getProducts() {
        return products;
    }

    public AdminProductViewModel setProducts(List<Product> products) {
        this.products = products != null ? products : new ArrayList<>();
        return this;
    }

    public String getOkMessage() {
        return okMessage;
    }

    public AdminProductViewModel setOkMessage(String okMessage) {
        this.okMessage = okMessage;
        return this;
    }

    public String getExceptionMessage() {
        return exceptionMessage;
    }

    public AdminProductViewModel setExceptionMessage(String exceptionMessage) {
        this.exceptionMessage = exceptionMessage;
        return this;
    }
}
