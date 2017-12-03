package announce4j;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.function.BiConsumer;

import org.junit.Before;
import org.junit.Test;

import announce4j.Announcer;

/**
 * Created by Ciprian TEODOROV on 25/03/17.
 */

class AnnouncementMockA {}
class AnnouncementMockAA extends AnnouncementMockA {}
class AnnouncementMockB {}

public class AnnouncerTest {
	Announcer announcer;
	@Before
	public void setup() {
		announcer = new Announcer();
	}
	
	@Test
	public void testAnnounceInstance() {
		Object ann = new AnnouncementMockA();
		assertEquals(ann, announcer.announce(ann));
	}
	
	@Test
	public void testWhenBlock() {
		int x[] = new int[] {0};
		announcer.when(AnnouncementMockA.class, (a, b) -> x[0]++ );
		announcer.announce(new AnnouncementMockA());
		assertEquals(1, x[0]);
	}
	
	@Test
	public void removeSubscriber() {
		announcer.when(AnnouncementMockA.class, (ann, b) -> {}, this);
		assertEquals(1, announcer.numberOfSubscriptions());
		announcer.remove(this);
		assertEquals(0, announcer.numberOfSubscriptions());
	}
	
	
	@Test
	public void cannotRemoveSubscription() {
		announcer.when(AnnouncementMockA.class, (ann, b) -> {});
		assertEquals(1, announcer.numberOfSubscriptions());
		announcer.remove(this);
		assertEquals(1, announcer.numberOfSubscriptions());
		announcer.when(AnnouncementMockA.class, (ann, b) -> {});
		assertEquals(2, announcer.numberOfSubscriptions());
	}
	
	@Test
	public void testSubscribe() {
		Object instance;
		Object annonce[] = new Object[1];
		announcer.when(AnnouncementMockA.class, (ann, a) -> annonce[0] = a);
		
		annonce[0] = null;
		instance = announcer.announce(new AnnouncementMockA());
		assertEquals(annonce[0], instance);
		
		annonce[0] = null;
		instance = announcer.announce(new AnnouncementMockB());
		assertNull(annonce[0]);
	}
	
	@Test
	public void testSubscribeSubclass() {
		Object instance;
		Object annonce[] = new Object[1];
		announcer.when(AnnouncementMockA.class, (ann, a) -> annonce[0] = a, this);
		
		annonce[0] = null;
		instance = announcer.announce(new AnnouncementMockAA());
		assertEquals(annonce[0], instance);
		
		announcer.remove(this);
		announcer.when(AnnouncementMockAA.class, (ann, a) -> annonce[0] = a);
		
		annonce[0] = null;
		instance = announcer.announce(new AnnouncementMockA());
		assertNull(annonce[0]);
	}
	
	@Test
	public void testCollection() {
		Object instance;
		Object annonce[] = new Object[1];
		announcer.when(Arrays.asList(AnnouncementMockAA.class, AnnouncementMockB.class), (ann, a) -> annonce[0] = a );
		
		annonce[0] = null;
		instance = announcer.announce(new AnnouncementMockAA());
		assertEquals(annonce[0], instance);
		
		annonce[0] = null;
		instance = announcer.announce(new AnnouncementMockB());
		assertEquals(annonce[0], instance);
		
		annonce[0] = null;
		instance = announcer.announce(new AnnouncementMockA());
		assertNull(annonce[0]);
	}

	@Test
	public void testRemoveAction() {
		Object instance;
		Object annonce[] = new Object[1];
		BiConsumer<Announcer, AnnouncementMockA> subscriptionAction;
		announcer.when(AnnouncementMockA.class, subscriptionAction = (ann, a) -> annonce[0] = a );

		annonce[0] = null;
		instance = announcer.announce(new AnnouncementMockA());
		assertEquals(annonce[0], instance);

		announcer.remove(subscriptionAction);

		annonce[0] = null;
		instance = announcer.announce(new AnnouncementMockA());
		assertNull(annonce[0]);
	}

	@Test
	public void testRemoveSubscription() {
		boolean annonce[] = new boolean[] {false};
		ISubscription subscription = announcer.when(AnnouncementMockA.class, (ann, a) -> annonce[0] = true );

		annonce[0] = false;
		announcer.announce(new AnnouncementMockA());
		assertTrue(annonce[0]);

		subscription.remove();

		annonce[0] = false;
		announcer.announce(new AnnouncementMockA());
		assertFalse(annonce[0]);

	}
}
