package dev.simplified.collection;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class ConcurrentEqualityConcurrencyTest {

	private static final int ELEMENTS = 200;
	private static final int READS = 20_000;
	private static final long JOIN_TIMEOUT_MILLIS = 10_000L;
	private static final String SENTINEL = "sentinel";

	private static List<String> payload() {
		List<String> payload = new ArrayList<>(ELEMENTS);

		for (int i = 0; i < ELEMENTS; i++)
			payload.add("e" + i);

		return payload;
	}

	@Test
	void arrayList_equalsSurvivesConcurrentMutationOfTheOtherSide() throws InterruptedException {
		ConcurrentArrayList<String> left = Concurrent.newList(payload());
		ConcurrentArrayList<String> right = Concurrent.newList(payload());

		race(left, right, () -> {
			right.add(SENTINEL);
			right.remove(SENTINEL);
		});
	}

	@Test
	void linkedList_equalsSurvivesConcurrentMutationOfTheOtherSide() throws InterruptedException {
		ConcurrentLinkedList<String> left = Concurrent.newLinkedList(payload());
		ConcurrentLinkedList<String> right = Concurrent.newLinkedList(payload());

		race(left, right, () -> {
			right.add(SENTINEL);
			right.remove(SENTINEL);
		});
	}

	@Test
	void hashSet_equalsSurvivesConcurrentMutationOfTheOtherSide() throws InterruptedException {
		ConcurrentHashSet<String> left = Concurrent.newSet(payload());
		ConcurrentHashSet<String> right = Concurrent.newSet(payload());

		race(left, right, () -> {
			right.add(SENTINEL);
			right.remove(SENTINEL);
		});
	}

	@Test
	void treeMap_comparatorOrderedPeersStillCompareEqual() {
		Comparator<String> caseInsensitive = String.CASE_INSENSITIVE_ORDER;
		ConcurrentTreeMap<String, Integer> left = Concurrent.newTreeMap(caseInsensitive);
		ConcurrentTreeMap<String, Integer> right = Concurrent.newTreeMap(caseInsensitive);

		left.put("A", 1);
		left.put("B", 2);
		right.put("a", 1);
		right.put("b", 2);

		// no hashCode assertion here - a comparator inconsistent with equals makes these maps
		// equal while their keys hash apart, which java.util.SortedMap explicitly permits
		assertTrue(left.equals(right), "a comparator-ordered map must still match a peer under the same ordering");
		assertTrue(right.equals(left), "the comparison must hold in both directions");
	}

	@Test
	void treeMap_comparatorOrderedPeerWithForeignKeysIsNotEqual() {
		Comparator<String> caseInsensitive = String.CASE_INSENSITIVE_ORDER;
		ConcurrentTreeMap<String, Integer> left = Concurrent.newTreeMap(caseInsensitive);
		ConcurrentTreeMap<String, Integer> right = Concurrent.newTreeMap(caseInsensitive);

		left.put("A", 1);
		right.put("z", 1);

		assertFalse(left.equals(right));
		assertFalse(right.equals(left));
	}

	/**
	 * Runs {@code left.equals(right)} on one thread while {@code mutation} repeatedly rewrites
	 * {@code right} on another, both released from a shared latch, and fails if anything escapes
	 * {@code equals}.
	 *
	 * @param left the collection whose {@code equals} is exercised
	 * @param right the collection mutated underneath the comparison
	 * @param mutation the mutation applied in a tight loop for the duration of the reads
	 * @throws InterruptedException if the test thread is interrupted while joining
	 */
	private static void race(Object left, Object right, Runnable mutation) throws InterruptedException {
		CountDownLatch start = new CountDownLatch(1);
		AtomicReference<Throwable> readerFailure = new AtomicReference<>();
		AtomicReference<Throwable> writerFailure = new AtomicReference<>();
		AtomicBoolean racing = new AtomicBoolean(true);
		AtomicInteger reads = new AtomicInteger();
		AtomicInteger mutations = new AtomicInteger();

		Thread reader = new Thread(() -> {
			try {
				start.await();

				for (int i = 0; i < READS; i++) {
					left.equals(right);
					reads.incrementAndGet();
				}
			} catch (Throwable throwable) {
				readerFailure.set(throwable);
			} finally {
				racing.set(false);
			}
		}, "equality-reader");

		Thread writer = new Thread(() -> {
			try {
				start.await();

				while (racing.get()) {
					mutation.run();
					mutations.incrementAndGet();
				}
			} catch (Throwable throwable) {
				writerFailure.set(throwable);
			}
		}, "equality-writer");

		reader.setDaemon(true);
		writer.setDaemon(true);
		reader.start();
		writer.start();
		start.countDown();

		reader.join(JOIN_TIMEOUT_MILLIS);
		racing.set(false);
		writer.join(JOIN_TIMEOUT_MILLIS);

		Throwable thrown = readerFailure.get();
		if (thrown != null) fail("equals threw while the compared collection was being mutated", thrown);

		Throwable writerThrown = writerFailure.get();
		if (writerThrown != null) fail("the mutating thread failed, so the race was never exercised", writerThrown);

		assertFalse(reader.isAlive(), "the comparing thread never finished");
		assertFalse(writer.isAlive(), "the mutating thread never finished");
		assertEquals(READS, reads.get(), "the comparing thread did not complete every read");
		assertTrue(mutations.get() > 0, "the mutating thread never ran, so nothing was raced");
	}

}
