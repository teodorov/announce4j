package announce4j;

/**
 * Created by Ciprian TEODOROV on 25/03/17.
 */
interface ISubscription<T> {
    Object getSubscriber();
    boolean isHandlingAnnouncement(Object announce);
    void handle(T announce);
    //remove this subscription from the registry
    void remove();
}