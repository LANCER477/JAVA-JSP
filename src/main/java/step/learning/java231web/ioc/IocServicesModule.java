package step.learning.java231web.ioc;

import com.google.inject.AbstractModule;
import step.learning.java231web.services.hash.IHashService;
import step.learning.java231web.services.hash.Md5HashService;
import step.learning.java231web.services.time.ITimeService;
import step.learning.java231web.services.time.ITimestampService;
import step.learning.java231web.services.time.SystemTimeService;

/**
 * Модуль конфігурації сервісів (служб) інжектора
 * @author Lector
 */
public class IocServicesModule extends AbstractModule {

    @Override
    protected void configure() {
        bind( IHashService.class )
                .to( Md5HashService.class );

        bind( ITimeService.class )
                .to( SystemTimeService.class );

        bind( ITimestampService.class )
                .to( SystemTimeService.class );
    }
    
}
