package announce4j;

import java.util.Collection;
import java.util.function.BiConsumer;



/**
 * @author ciprian.teodorov
 * Announcements is a framework for event notification developed originally by Vassili Bykov for VisualWorks Smalltalk.
 * It has been ported to several Smalltalk implementations and now to Java.
 * Announcements provides a simple and generic implementation of the Observer pattern.
 */
public class Announcer {
	private SubscriptionRegistry registry = new SubscriptionRegistry();
	
	public Announcer() {}
	
	public Announcer(boolean isOrdered) {
		this.registry = new SubscriptionRegistry();
	}
	
	public Object announce(Object announce) {
		if (registry != null) {
			registry.deliver(announce);
		}
		return announce;
	}
	
	public int numberOfSubscriptions() {
		return registry.size();
	}
	
	public ISubscription<?> remove(ISubscription<?> subscription) {
		return registry.remove(subscription);
	}
	
	public Collection<ISubscription<?>> remove(Object subscriber) {
		return registry.remove(subscriber);
	}
	
	public ISubscription<?> replace(ISubscription<?> old, ISubscription<?> newOne) {
		return registry.replace(old, newOne);
	}
	
	public <T> ISubscription<?> when(Class<T> announcementClass, BiConsumer<Announcer, T> consumerAction, Object subscriber) {
		AnnouncementSubscription<T> subscription = new AnnouncementSubscription<T>();
		subscription.announcer = this;
		subscription.announcementClass = announcementClass;
		subscription.consumerAction = consumerAction;
		subscription.subscriber = subscriber;
		return registry.add(subscription);
	}
	
	public <T> ISubscription<?> when(Class<T> announcementClass, BiConsumer<Announcer, T> consumerAction) {
		return when(announcementClass, consumerAction, null);
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public ISubscription when(Collection<Class<?>> announcementClasses, BiConsumer<Announcer, Object> consumerAction, Object subscriber) {
		AnnouncementListSubscription subscription = new AnnouncementListSubscription();
		subscription.announcer = this;
		subscription.announcementClasses = announcementClasses;
		subscription.consumerAction = consumerAction;
		subscription.subscriber = subscriber;
		return registry.add(subscription);
	}
	
	@SuppressWarnings("rawtypes")
	public ISubscription when(Collection<Class<?>> announcementClasses, BiConsumer<Announcer, Object> consumerAction) {
		return when(announcementClasses, consumerAction, null);
	}
}
