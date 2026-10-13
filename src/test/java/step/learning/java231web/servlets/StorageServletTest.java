package step.learning.java231web.servlets;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import step.learning.java231web.services.storage.IStorageService;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class StorageServletTest {

    @Test
    public void testGetStorageFileReturnsContent() throws Exception {
        byte[] testBytes = "fake image data".getBytes();
        IStorageService mockStorage = new IStorageService() {
            @Override
            public String save(org.apache.commons.fileupload2.core.FileItem item) {
                return "test.webp";
            }

            @Override
            public InputStream getStream(String filename) {
                if ("test.webp".equals(filename)) {
                    return new ByteArrayInputStream(testBytes);
                }
                return null;
            }
        };

        StorageServlet servlet = new StorageServlet(mockStorage);

        AtomicInteger status = new AtomicInteger(200);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        HttpServletRequest req = (HttpServletRequest) Proxy.newProxyInstance(
                HttpServletRequest.class.getClassLoader(),
                new Class<?>[]{HttpServletRequest.class},
                (proxy, method, args) -> {
                    if ("getPathInfo".equals(method.getName())) return "/test.webp";
                    return null;
                }
        );

        HttpServletResponse resp = (HttpServletResponse) Proxy.newProxyInstance(
                HttpServletResponse.class.getClassLoader(),
                new Class<?>[]{HttpServletResponse.class},
                (proxy, method, args) -> {
                    if ("setStatus".equals(method.getName())) {
                        status.set((Integer) args[0]);
                        return null;
                    }
                    if ("getOutputStream".equals(method.getName())) {
                        return new jakarta.servlet.ServletOutputStream() {
                            @Override
                            public boolean isReady() {
                                return true;
                            }

                            @Override
                            public void setWriteListener(jakarta.servlet.WriteListener writeListener) {
                            }

                            @Override
                            public void write(int b) {
                                out.write(b);
                            }
                        };
                    }
                    return null;
                }
        );

        servlet.doGet(req, resp);

        assertEquals(200, status.get());
        assertArrayEquals(testBytes, out.toByteArray());
    }

    @Test
    public void testGetStorageFileNotFoundReturns404() throws Exception {
        IStorageService mockStorage = new IStorageService() {
            @Override
            public String save(org.apache.commons.fileupload2.core.FileItem item) {
                return null;
            }

            @Override
            public InputStream getStream(String filename) {
                return null;
            }
        };

        StorageServlet servlet = new StorageServlet(mockStorage);

        AtomicInteger status = new AtomicInteger(200);

        HttpServletRequest req = (HttpServletRequest) Proxy.newProxyInstance(
                HttpServletRequest.class.getClassLoader(),
                new Class<?>[]{HttpServletRequest.class},
                (proxy, method, args) -> {
                    if ("getPathInfo".equals(method.getName())) return "/notfound.webp";
                    return null;
                }
        );

        HttpServletResponse resp = (HttpServletResponse) Proxy.newProxyInstance(
                HttpServletResponse.class.getClassLoader(),
                new Class<?>[]{HttpServletResponse.class},
                (proxy, method, args) -> {
                    if ("setStatus".equals(method.getName())) {
                        status.set((Integer) args[0]);
                        return null;
                    }
                    return null;
                }
        );

        servlet.doGet(req, resp);

        assertEquals(404, status.get());
    }
}
