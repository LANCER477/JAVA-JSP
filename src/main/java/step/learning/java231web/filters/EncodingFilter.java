package step.learning.java231web.filters;

import com.google.inject.Singleton;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Сервлетний фільтр для узгодження кодування символів
 * @author Lector
 */
@Singleton
public class EncodingFilter implements Filter {
    private FilterConfig filterConfig;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // при створенні ланцюга фільтрів (Middleware) передається
        // конфігурація, що містить деталі оточення - файлові шляхи,
        // імена проєктів тощо. Для можливості використання у подальшому
        // ці дані слід зберегти
        this.filterConfig = filterConfig;
    }

    @Override
    public void doFilter(          // Метод фільтру - логіка його роботи.
            ServletRequest sreq,   // Фільтри більш загальні об'єкти, ніж НТТР
            ServletResponse sresp, // тому параметри методу узагальнені. 
            FilterChain chain      // Посилання на ланцюг фільтрів         
    ) throws IOException, ServletException {
        // Оскільки використовуються фільтри в НТТР-проєкті, слід
        // змінити типізацію
        HttpServletRequest req = (HttpServletRequest) sreq;
        HttpServletResponse resp = (HttpServletResponse) sresp;
        
        // "Прямий хід" - від одержання запиту
        
        // кодування можна змінювати (встановлювати) лише до того, як почалось
        // читання чи запис даних. Відповідно, даний фільтр має бути якомога
        // першим у ланцюгу
        req.setCharacterEncoding( StandardCharsets.UTF_8 );
        resp.setCharacterEncoding( StandardCharsets.UTF_8 );
        req.setAttribute( "charset", StandardCharsets.UTF_8 );
        
        chain.doFilter(sreq, sresp);  // передача управління наступному фільтру
        
        // "Зворотній хід" - після роботи решти фільтрів - формування відповіді
    }

    @Override
    public void destroy() {
        // завершальні дії з руйнування об'єкту фільтру
        // коли застосунок зупиняється, у т.ч. перепублікується
        this.filterConfig = null;
    }
    
}
/*
Сервлетні фільтри (пізніше - Middleware) - архітектурні елементи,
що утворюють послідовність (ланцюг) обробників, кожен з яких
передає (або не передає) обробку наступному.
Аналогічно до сервлетів фільтри описуються класами та підключаються
одним з трьох поширених способів
- web.xml
- annotation
- IoC
*/