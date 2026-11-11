package step.learning.java231web.models;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import step.learning.java231web.data.dto.Cart;
import step.learning.java231web.data.dto.CartItem;

public class CartTypeModel {
    public static final SimpleDateFormat dateFormat = new SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ss", Locale.ROOT
    );

    private List<CartItem> cartItems = new ArrayList<>();
    private double price;
    private String id;
    private String createDt;
    private String deleteDt;
    private String orderDt;
    private Integer status;

    public CartTypeModel() {
    }

    public static CartTypeModel fromCart(Cart cart) {
        return fromCart(cart, "http://localhost:8080/java231web");
    }

    public static CartTypeModel fromCart(Cart cart, String baseUrl) {
        if (cart == null) {
            return null;
        }

        String host = (baseUrl != null && !baseUrl.trim().isEmpty()) ? baseUrl : "http://localhost:8080/java231web";

        Date openDate = cart.getCreateDt();
        if (openDate == null) {
            openDate = cart.getCreatedAt();
        }
        if (openDate == null && cart.getOpenAt() != null) {
            try {
                openDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(cart.getOpenAt());
            } catch (Exception ignored) {}
        }
        if (openDate == null) {
            openDate = new Date();
        }

        Date deleteDate = cart.getDeleteDt();
        if (deleteDate == null && Objects.equals(cart.getStatus(), 0)) {
            deleteDate = cart.getClosedAt();
            if (deleteDate == null && cart.getCloseAt() != null) {
                try {
                    deleteDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(cart.getCloseAt());
                } catch (Exception ignored) {}
            }
        }

        Date orderDate = cart.getOrderDt();
        if (orderDate == null && Objects.equals(cart.getStatus(), 1)) {
            orderDate = cart.getClosedAt();
            if (orderDate == null && cart.getCloseAt() != null) {
                try {
                    orderDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(cart.getCloseAt());
                } catch (Exception ignored) {}
            }
        }

        String createStr = dateFormat.format(openDate);
        String deleteStr = deleteDate != null ? dateFormat.format(deleteDate) : null;
        String orderStr = orderDate != null ? dateFormat.format(orderDate) : null;

        Integer st = cart.getStatus();
        if (st == null) {
            if (orderStr != null) st = 1;
            else if (deleteStr != null) st = 0;
        }

        List<CartItem> preparedItems = new ArrayList<>();
        if (cart.getCartItems() != null) {
            for (CartItem ci : cart.getCartItems()) {
                if (ci == null) continue;
                if (ci.getProduct() != null) {
                    ci.setProduct(ci.getProduct());
                    if (ci.getProduct().getImageUrl() != null) {
                        ci.getProduct().setImageUrl(fixImageUrl(ci.getProduct().getImageUrl(), host));
                    }
                }
                if (ci.getProductVersion() != null) {
                    ci.setProductVersion(ci.getProductVersion());
                    if (ci.getProductVersion().getImageUrl() != null) {
                        ci.getProductVersion().setImageUrl(fixImageUrl(ci.getProductVersion().getImageUrl(), host));
                    }
                }
                preparedItems.add(ci);
            }
        }

        return new CartTypeModel()
                .setCartItems(preparedItems)
                .setId(cart.getId() != null ? cart.getId().toString() : null)
                .setPrice(cart.getPrice())
                .setCreateDt(createStr)
                .setDeleteDt(deleteStr)
                .setOrderDt(orderStr)
                .setStatus(st);
    }

    public static String fixImageUrl(String raw, String baseUrl) {
        if (raw == null || raw.trim().isEmpty() || raw.equals("null")) {
            return null;
        }
        String host = (baseUrl != null && !baseUrl.trim().isEmpty()) ? baseUrl : "http://localhost:8080/java231web";
        if (raw.startsWith("http://localhost:8080/") || raw.startsWith("http://127.0.0.1:8080/")) {
            return raw;
        }
        String filename = raw;
        int slash = filename.lastIndexOf('/');
        if (slash >= 0) {
            filename = filename.substring(slash + 1);
        }
        int q = filename.indexOf('?');
        if (q >= 0) {
            filename = filename.substring(0, q);
        }
        if (filename.isEmpty()) {
            return null;
        }
        return host + "/storage/" + filename;
    }

    public List<CartItem> getCartItems() {
        return cartItems;
    }

    public CartTypeModel setCartItems(List<CartItem> cartItems) {
        this.cartItems = cartItems != null ? cartItems : new ArrayList<>();
        return this;
    }

    public double getPrice() {
        return price;
    }

    public CartTypeModel setPrice(double price) {
        this.price = price;
        return this;
    }

    public String getId() {
        return id;
    }

    public CartTypeModel setId(String id) {
        this.id = id;
        return this;
    }

    public String getCreateDt() {
        return createDt;
    }

    public CartTypeModel setCreateDt(String createDt) {
        this.createDt = createDt;
        return this;
    }

    public String getDeleteDt() {
        return deleteDt;
    }

    public CartTypeModel setDeleteDt(String deleteDt) {
        this.deleteDt = deleteDt;
        return this;
    }

    public String getOrderDt() {
        return orderDt;
    }

    public CartTypeModel setOrderDt(String orderDt) {
        this.orderDt = orderDt;
        return this;
    }

    public Integer getStatus() {
        if (status != null) return status;
        if (orderDt != null) return 1;
        if (deleteDt != null) return 0;
        return null;
    }

    public CartTypeModel setStatus(Integer status) {
        this.status = status;
        return this;
    }

    public String getCloseAt() {
        return orderDt != null ? orderDt : deleteDt;
    }
}