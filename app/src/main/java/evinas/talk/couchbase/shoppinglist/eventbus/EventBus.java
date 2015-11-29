package evinas.talk.couchbase.shoppinglist.eventbus;

import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

public class EventBus {

    private static Bus sInstance = new Bus(ThreadEnforcer.ANY);;

    private EventBus() {
    }

    public static void post(Object event) {
        synchronized (event) {
            sInstance.post(event);
        }
    }

    public static void register(Object subscriber) {
        sInstance.register(subscriber);
    }

    public static void unregister(Object subscriber) {
        sInstance.unregister(subscriber);
    }

}
