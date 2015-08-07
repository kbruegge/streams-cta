package streams.hexmap.ui;

import com.google.common.eventbus.EventBus;

/**
 * Singleton for the eventbus. Cause I dont have Guice
 * Created by kaibrugge on 02.06.14.
 */
public class Bus {

    public static EventBus eventBus = new EventBus((throwable, subscriberExceptionContext) -> throwable.printStackTrace());
    private Bus(){

    }
}
