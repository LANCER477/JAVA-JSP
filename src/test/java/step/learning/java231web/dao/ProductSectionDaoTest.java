package step.learning.java231web.dao;

import org.junit.jupiter.api.Test;
import step.learning.java231web.data.dao.ProductSectionDao;
import step.learning.java231web.data.dto.Product;
import step.learning.java231web.data.dto.ProductSection;
import step.learning.java231web.rest.RestPagination;
import step.learning.java231web.services.db.IDbService;

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class ProductSectionDaoTest {

    @Test
    public void testGetSectionBySlugCorrectsImageUrlForProductsAndSection() throws Exception {
        // Симулюємо ResultSet з розділом та 2 товарами
        AtomicInteger rowIndex = new AtomicInteger(0);

        ResultSet mockRs = (ResultSet) Proxy.newProxyInstance(
                ResultSet.class.getClassLoader(),
                new Class<?>[]{ResultSet.class},
                (proxy, method, args) -> {
                    String name = method.getName();
                    if ("next".equals(name)) {
                        return rowIndex.incrementAndGet() <= 2;
                    }
                    if ("getString".equals(name)) {
                        String col = (String) args[0];
                        if ("product_section_id".equals(col)) return "section-id-tv";
                        if ("product_section_title".equals(col)) return "Телевізори";
                        if ("product_section_description".equals(col)) return "Всі телевізори";
                        if ("product_section_slug".equals(col)) return "tv";
                        if ("product_section_image".equals(col)) return "tv_main.webp";
                        if ("product_id".equals(col)) {
                            return rowIndex.get() == 1 ? "prod-1" : "prod-2";
                        }
                        if ("product_title".equals(col)) {
                            return rowIndex.get() == 1 ? "Samsung TV" : "LG TV";
                        }
                        if ("product_slug".equals(col)) {
                            return rowIndex.get() == 1 ? "samsung-tv" : "lg-tv";
                        }
                        if ("product_image".equals(col)) {
                            return rowIndex.get() == 1 ? "samsung.webp" : "lg.webp";
                        }
                        return null;
                    }
                    if ("getInt".equals(name)) {
                        return 1;
                    }
                    if ("getDouble".equals(name)) {
                        return 19999.0;
                    }
                    if ("getTimestamp".equals(name)) {
                        return null;
                    }
                    if ("close".equals(name)) {
                        return null;
                    }
                    return null;
                }
        );

        PreparedStatement mockPrep = (PreparedStatement) Proxy.newProxyInstance(
                PreparedStatement.class.getClassLoader(),
                new Class<?>[]{PreparedStatement.class},
                (proxy, method, args) -> {
                    String name = method.getName();
                    if ("executeQuery".equals(name)) {
                        return mockRs;
                    }
                    if ("setString".equals(name) || "setLong".equals(name) || "setInt".equals(name) || "close".equals(name)) {
                        return null;
                    }
                    return null;
                }
        );

        Connection mockConn = (Connection) Proxy.newProxyInstance(
                Connection.class.getClassLoader(),
                new Class<?>[]{Connection.class},
                (proxy, method, args) -> {
                    if ("prepareStatement".equals(method.getName())) {
                        return mockPrep;
                    }
                    return null;
                }
        );

        IDbService mockDbService = () -> mockConn;

        ProductSectionDao dao = new ProductSectionDao(mockDbService);
        RestPagination pagination = new RestPagination(1, 10, 0);

        ProductSection section = dao.getSectionBySlug("tv", pagination);

        assertNotNull(section);
        assertEquals("tv", section.getSlug());

        // Д.З.: Перевіряємо коригування поля ImageUrl для самого розділу (section)
        assertNotNull(section.getImageUrl());
        assertEquals("tv_main.webp", section.getImageUrl());

        // Перевіряємо коригування поля ImageUrl для товарів (products)
        assertEquals(2, section.getProducts().size());
        Product p1 = section.getProducts().get(0);
        Product p2 = section.getProducts().get(1);

        assertNotNull(p1.getImageUrl());
        assertEquals("samsung.webp", p1.getImageUrl());

        assertNotNull(p2.getImageUrl());
        assertEquals("lg.webp", p2.getImageUrl());
    }
}
