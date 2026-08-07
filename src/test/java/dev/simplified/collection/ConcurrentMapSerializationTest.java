package dev.simplified.collection;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers Java serialization of the {@code Concurrent*} maps.
 * <p>
 * {@link ConcurrentMap} declares {@link java.io.Serializable}, so anything that persists one as a
 * blob - a Hibernate column of a serializable type, a distributed cache, an object stream - writes
 * every non-transient field. A monitor held for lazy view creation is not serializable and is not
 * state worth carrying, so it has to stay out of the serialized form and be restored on the way
 * back in.
 */
class ConcurrentMapSerializationTest {

    @Test
    @DisplayName("a map round-trips through an object stream with its views intact")
    void roundTripsThroughObjectStream() throws Exception {
        ConcurrentMap<String, Integer> map = Concurrent.newMap();
        map.put("health", 100);
        map.put("defense", 50);

        ConcurrentMap<String, Integer> restored = roundTrip(map);

        assertEquals(map, restored);
        assertEquals(2, restored.size());
        assertEquals(100, restored.get("health"));

        // the views are transient, so a restored map builds them fresh - and the monitor that
        // guards building them has to exist by then
        assertNotNull(restored.entrySet());
        assertEquals(2, restored.entrySet().size());
        assertEquals(2, restored.keySet().size());
        assertEquals(2, restored.values().size());
        assertTrue(restored.keySet().contains("defense"));

        restored.put("strength", 25);
        assertEquals(3, restored.entrySet().size());
    }

    @Test
    @DisplayName("a linked map keeps its order across an object stream")
    void roundTripsLinkedMap() throws Exception {
        ConcurrentLinkedMap<String, Integer> map = Concurrent.newLinkedMap();
        map.put("first", 1);
        map.put("second", 2);
        map.put("third", 3);

        ConcurrentMap<String, Integer> restored = roundTrip(map);

        assertEquals(map, restored);
        assertEquals(List.of("first", "second", "third"), List.copyOf(restored.keySet()));
    }

    @SuppressWarnings("unchecked")
    private static @NotNull ConcurrentMap<String, Integer> roundTrip(@NotNull ConcurrentMap<String, Integer> map) throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();

        try (ObjectOutputStream out = new ObjectOutputStream(bytes)) {
            out.writeObject(map);
        }

        try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) {
            return (ConcurrentMap<String, Integer>) in.readObject();
        }
    }

}
