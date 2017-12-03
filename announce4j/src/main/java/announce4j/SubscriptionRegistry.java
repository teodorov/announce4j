package announce4j;

import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by Ciprian TEODOROV on 25/03/17.
 */
abstract class AbstractSubscription<T> implements ISubscription<T> {
	Announcer announcer;
	Object subscriber;
	BiConsumer<Announcer, T> consumerAction;
	
	@Override
	public Object getSubscriber() {
		return subscriber == null ? consumerAction : subscriber;
	}

	@Override
	public void handle(T announce) {
		if (isHandlingAnnouncement(announce)) {
			consumerAction.accept(announcer, announce);
		}
	}
	@Override
	public void remove() {
		announcer.remove(this);
	}
}

class AnnouncementSubscription<T> extends AbstractSubscription<T> {
	Class<T> announcementClass;
	@Override
	public boolean isHandlingAnnouncement(Object announce) {
		return announce.getClass() == announcementClass || announcementClass.isInstance(announce);
	}
}

@SuppressWarnings("rawtypes")
class AnnouncementListSubscription extends AbstractSubscription {
	Collection<Class<?>> announcementClasses;
	@Override
	public boolean isHandlingAnnouncement(Object announce) {
		return announcementClasses.stream().anyMatch(c -> (announce.getClass() == c || c.isInstance(announce)));
	}
	
}

public class SubscriptionRegistry {
	public final boolean isOrdered;
	private Collection<ISubscription<?>> subscriptions;

	public SubscriptionRegistry() {
		this.isOrdered = false;
		reset();
	}
	
	public SubscriptionRegistry(boolean isOrdered) {
		this.isOrdered = isOrdered;
		reset();
	}
	
	public synchronized ISubscription<?> add(ISubscription<?> subscription) {
		subscriptions.add(subscription);
		return subscription;
	}
	
	synchronized ISubscription<?> remove(ISubscription<?> subscription) {
		subscriptions.remove(subscription);
		return subscription;
	}
	
	public synchronized  Collection<ISubscription<?>> remove(Object subscriber) {
		subscriptions = subscriptions.stream()
				.filter(subscription -> (subscription.getSubscriber() != subscriber))
				.collect(Collectors.toList());
		return subscriptions;
	}
	
	synchronized ISubscription<?> replace(ISubscription<?> oldOne, ISubscription<?> newOne) {
		//this changes the order (the new one is added irespetive of the possition of the old one)
		subscriptions.remove(oldOne);
		subscriptions.add(newOne);
		return newOne;
	}
	
	void reset() {
		if (isOrdered) {
			subscriptions = new LinkedList<>();
		}
		else {
			subscriptions = Collections.newSetFromMap(new IdentityHashMap<ISubscription<?>, Boolean>());
		}
	}
	
	public <T> SubscriptionRegistry deliver(T announce) {
		if (subscriptions.isEmpty()) return this;
		synchronized (this) {
			Stream<ISubscription<?>> interestedSubscriptions = interestedSubscribers(announce);
			deliver(announce, interestedSubscriptions);
		}
		return this;
	}

	private <T> Stream<ISubscription<?>> interestedSubscribers(T announce) {
		return subscriptions.stream().filter(subscription -> subscription.isHandlingAnnouncement(announce));
		
	}

	@SuppressWarnings("unchecked")
	private <T> void deliver(T announce, Stream<ISubscription<?>> interestedSubscriptions) {
		interestedSubscriptions.forEachOrdered(subscription -> { ((ISubscription<T>)subscription).handle(announce); });
	}
	
	public int size() {
		return subscriptions.size();
	}
}
