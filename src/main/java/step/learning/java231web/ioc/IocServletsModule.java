package step.learning.java231web.ioc;

import com.google.inject.servlet.ServletModule;
import step.learning.java231web.filters.AuthFilter;
import step.learning.java231web.filters.EncodingFilter;
import step.learning.java231web.servlets.AboutServlet;
import step.learning.java231web.servlets.GsonServlet;
import step.learning.java231web.servlets.HomeServlet;

/**
 * Модуль конфігурації сервлетів (і фільтрів) інжектора
 * @author Lector
 */
public class IocServletsModule extends ServletModule {

    @Override
    protected void configureServlets() {
        // З переходом на ІоС необхідно зняти всі @Web-анотації
        // з фільтрів та сервлетів, а також прибрати їх з web.xml
        // До фільтрів та сервлетів НЕОБХІДНО додати анотацію 
        // @Singleton
        filter("/*").through( EncodingFilter.class );
        filter("/*").through( AuthFilter.class     );
        
        serve("/"     ).with( HomeServlet.class  );
        serve("/about").with( AboutServlet.class );
        serve("/gson" ).with( GsonServlet.class  );
    }
    
}
