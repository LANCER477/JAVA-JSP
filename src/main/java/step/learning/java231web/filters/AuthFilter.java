package step.learning.java231web.filters;

import com.google.inject.Singleton;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import java.io.IOException;

/**
 * Фільтр перевірки авторизації
 * @author Lector
 */

// @WebFilter("/*")  // анотації спрощують реєстрацію фільтрів, але
// !! не дозволяють регулювати їх порядок
// тоді як web.xml чи IoC дозволяють

// через ІоС анотацію змінено
@Singleton
public class AuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest sr, ServletResponse sr1, FilterChain fc) throws IOException, ServletException {
        sr.setAttribute("auth", "checked");
        fc.doFilter(sr, sr1);
    }
    
}
/*
Д.З. Додати фільтр-логер, який виводитиме на консоль
всі запити, що надходять: дата-час, адреса запиту
Зареєструвати через web.xml
До звіту додати скріншоти консолі
*/