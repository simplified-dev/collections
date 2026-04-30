package dev.simplified.collection.gson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import dev.simplified.collection.Concurrent;
import dev.simplified.collection.ConcurrentCollection;
import dev.simplified.collection.ConcurrentDeque;
import dev.simplified.collection.ConcurrentLinkedHashMap;
import dev.simplified.collection.ConcurrentLinkedHashSet;
import dev.simplified.collection.ConcurrentLinkedList;
import dev.simplified.collection.ConcurrentLinkedMap;
import dev.simplified.collection.ConcurrentLinkedSet;
import dev.simplified.collection.ConcurrentList;
import dev.simplified.collection.ConcurrentMap;
import dev.simplified.collection.ConcurrentQueue;
import dev.simplified.collection.ConcurrentSet;
import dev.simplified.collection.ConcurrentTreeMap;
import dev.simplified.collection.ConcurrentTreeSet;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ServiceLoader;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class ConcurrentTypeAdapterFactoryTest {

    private static final Gson GSON = new GsonBuilder()
        .registerTypeAdapterFactory(new ConcurrentTypeAdapterFactory())
        .create();

    @Nested
    class CollectionTypes {

        @Test
        void deserialize_concurrentCollection() {
            ConcurrentCollection<Integer> result = GSON.fromJson(
                "[1,2,3]",
                new TypeToken<ConcurrentCollection<Integer>>() {}.getType()
            );
            assertThat(result, instanceOf(ConcurrentCollection.class));
            assertThat(result, containsInAnyOrder(1, 2, 3));
        }

        @Test
        void deserialize_concurrentList() {
            ConcurrentList<String> result = GSON.fromJson(
                "[\"a\",\"b\",\"c\"]",
                new TypeToken<ConcurrentList<String>>() {}.getType()
            );
            assertThat(result, instanceOf(ConcurrentList.class));
            assertThat(result, contains("a", "b", "c"));
        }

        @Test
        void deserialize_concurrentLinkedList() {
            ConcurrentLinkedList<Integer> result = GSON.fromJson(
                "[3,1,2]",
                new TypeToken<ConcurrentLinkedList<Integer>>() {}.getType()
            );
            assertThat(result, instanceOf(ConcurrentLinkedList.class));
            assertThat(result, contains(3, 1, 2));
        }

        @Test
        void deserialize_concurrentSet() {
            ConcurrentSet<Integer> result = GSON.fromJson(
                "[1,2,2,3]",
                new TypeToken<ConcurrentSet<Integer>>() {}.getType()
            );
            assertThat(result, instanceOf(ConcurrentSet.class));
            assertThat(result, containsInAnyOrder(1, 2, 3));
        }

        @Test
        void deserialize_concurrentLinkedSet() {
            ConcurrentSet<String> result = GSON.fromJson(
                "[\"a\",\"b\"]",
                new TypeToken<ConcurrentLinkedSet<String>>() {}.getType()
            );
            assertThat(result, instanceOf(ConcurrentLinkedHashSet.class));
            assertThat(result, containsInAnyOrder("a", "b"));
        }

        @Test
        void deserialize_concurrentTreeSet() {
            ConcurrentTreeSet<Integer> result = GSON.fromJson(
                "[3,1,2]",
                new TypeToken<ConcurrentTreeSet<Integer>>() {}.getType()
            );
            assertThat(result, instanceOf(ConcurrentTreeSet.class));
            assertThat(result, contains(1, 2, 3));
        }

        @Test
        void deserialize_concurrentQueue() {
            ConcurrentQueue<Integer> result = GSON.fromJson(
                "[1,2,3]",
                new TypeToken<ConcurrentQueue<Integer>>() {}.getType()
            );
            assertThat(result, instanceOf(ConcurrentQueue.class));
            assertThat(result, contains(1, 2, 3));
        }

        @Test
        void deserialize_concurrentDeque() {
            ConcurrentDeque<Integer> result = GSON.fromJson(
                "[1,2,3]",
                new TypeToken<ConcurrentDeque<Integer>>() {}.getType()
            );
            assertThat(result, instanceOf(ConcurrentDeque.class));
            assertThat(result, contains(1, 2, 3));
        }

    }

    @Nested
    class MapTypes {

        @Test
        void deserialize_concurrentMap() {
            ConcurrentMap<String, Integer> result = GSON.fromJson(
                "{\"a\":1,\"b\":2}",
                new TypeToken<ConcurrentMap<String, Integer>>() {}.getType()
            );
            assertThat(result, instanceOf(ConcurrentMap.class));
            assertThat(result, hasEntry("a", 1));
            assertThat(result, hasEntry("b", 2));
        }

        @Test
        void deserialize_concurrentLinkedMap() {
            ConcurrentMap<String, Integer> result = GSON.fromJson(
                "{\"a\":1,\"b\":2}",
                new TypeToken<ConcurrentLinkedMap<String, Integer>>() {}.getType()
            );
            assertThat(result, instanceOf(ConcurrentLinkedHashMap.class));
            assertThat(result, hasEntry("a", 1));
        }

        @Test
        void deserialize_concurrentTreeMap() {
            ConcurrentTreeMap<String, Integer> result = GSON.fromJson(
                "{\"b\":2,\"a\":1}",
                new TypeToken<ConcurrentTreeMap<String, Integer>>() {}.getType()
            );
            assertThat(result, instanceOf(ConcurrentTreeMap.class));
            assertThat(result.firstKey(), is("a"));
            assertThat(result.lastKey(), is("b"));
        }

    }

    @Nested
    class RoundTrip {

        @Test
        void list_roundTrip_preservesOrderAndContent() {
            ConcurrentList<Integer> source = Concurrent.newList(1, 2, 3, 4);
            String json = GSON.toJson(source);
            ConcurrentList<Integer> result = GSON.fromJson(json, new TypeToken<ConcurrentList<Integer>>() {}.getType());

            assertThat(result, contains(1, 2, 3, 4));
        }

        @Test
        void map_roundTrip_preservesEntries() {
            ConcurrentMap<String, Integer> source = Concurrent.newMap();
            source.put("a", 1);
            source.put("b", 2);

            String json = GSON.toJson(source);
            ConcurrentMap<String, Integer> result = GSON.fromJson(json, new TypeToken<ConcurrentMap<String, Integer>>() {}.getType());

            assertThat(result, hasEntry("a", 1));
            assertThat(result, hasEntry("b", 2));
        }

    }

    @Nested
    class Spi {

        @Test
        void serviceLoader_discoversFactoryFromMetaInf() {
            boolean found = false;

            for (TypeAdapterFactory factory : ServiceLoader.load(TypeAdapterFactory.class))
                if (factory instanceof ConcurrentTypeAdapterFactory) {
                    found = true;
                    break;
                }

            assertThat("ConcurrentTypeAdapterFactory should be discoverable via ServiceLoader", found, is(true));
        }

        @Test
        void unrelatedRawType_returnsNull() {
            ConcurrentTypeAdapterFactory factory = new ConcurrentTypeAdapterFactory();
            assertThat(factory.create(GSON, TypeToken.get(String.class)), is(nullValue()));
        }

    }

}
