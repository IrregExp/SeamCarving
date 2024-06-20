package maps;

import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * @see AbstractIterableMap
 * @see Map
 */
public class ArrayMap<K, V> extends AbstractIterableMap<K, V> {
    private static final int DEFAULT_INITIAL_CAPACITY = 10;
    /*
    Warning:
    You may not rename this field or change its type.
    We will be inspecting it in our secret tests.
     */
    SimpleEntry<K, V>[] entries;
    int size;
    int capacity;
    // You may add extra fields or helper methods though!

    private boolean isFull() {
        return this.size == this.capacity - 1;
    }

    private SimpleEntry<K, V>[] resize(int newCapacity) {
        SimpleEntry<K, V>[] newMap = createArrayOfEntries(newCapacity);

        // Style warning: "Manual Array copy". Fixed with:
        if (capacity >= 0) {
            System.arraycopy(entries, 0, newMap, 0, capacity);
        }
        this.capacity = newCapacity;
        return newMap;

    }

    /**
     * Constructs a new ArrayMap with default initial capacity.
     */
    public ArrayMap() {
        this(DEFAULT_INITIAL_CAPACITY);
    }

    /**
     * Constructs a new ArrayMap with the given initial capacity (i.e., the initial
     * size of the internal array).
     *
     * @param initialCapacity the initial capacity of the ArrayMap. Must be > 0.
     */
    public ArrayMap(int initialCapacity) {
        if (initialCapacity <= 0) {
            this.entries = this.createArrayOfEntries(DEFAULT_INITIAL_CAPACITY);
            this.capacity = DEFAULT_INITIAL_CAPACITY;
        } else {
            this.entries = this.createArrayOfEntries(initialCapacity);
            this.capacity = initialCapacity;
        }
        this.size = 0;
    }

    /**
     * This method will return a new, empty array of the given size that can contain
     * {@code Entry<K, V>} objects.
     *
     * Note that each element in the array will initially be null.
     *
     * Note: You do not need to modify this method.
     */
    @SuppressWarnings("unchecked")
    private SimpleEntry<K, V>[] createArrayOfEntries(int arraySize) {
        /*
        It turns out that creating arrays of generic objects in Java is complicated due to something
        known as "type erasure."

        We've given you this helper method to help simplify this part of your assignment. Use this
        helper method as appropriate when implementing the rest of this class.

        You are not required to understand how this method works, what type erasure is, or how
        arrays and generics interact.
        */
        return (SimpleEntry<K, V>[]) (new SimpleEntry[arraySize]);
    }

    @Override
    public V get(Object key) {
        V value = null;
        for (SimpleEntry<K, V> entry : entries) {
            if (entry != null && java.util.Objects.equals(entry.getKey(), key)) {
                value = entry.getValue();
                break;
            }
        }
        return value;
    }

    @Override
    public V put(K key, V value) {
        V prevValue = null;

        for (SimpleEntry<K, V> entry : entries) {
            if (entry != null && Objects.equals(entry.getKey(), key)) {
                prevValue = entry.getValue();
                entry.setValue(value);
                break;
            }
        }

        if (prevValue == null) {
            entries[this.size] = new SimpleEntry<>(key, value);
            ++this.size;
        }

        if (isFull()) {
            this.entries = this.resize(this.capacity * 2);
        }

        return prevValue;
    }

    @Override
    public V remove(Object key) {
        int keyPos = 0;
        V value = null;

        for (SimpleEntry<K, V> entry : entries) {
            // Last element was found, key not present
            if (entry == null) {
                break;
            } else if (java.util.Objects.equals(entry.getKey(), key)) {
                value = entry.getValue();
                break;
            }
            ++keyPos;
        }

        if (value != null) {
            // Copy the last element into the position that the key was found
            entries[keyPos] = entries[this.size - 1];
            entries[this.size - 1] = null;
            --this.size;
        }
        return value;
    }

    @Override
    public void clear() {
        this.entries = createArrayOfEntries(this.capacity);
        this.size = 0;
    }

    @Override
    public boolean containsKey(Object key) {

        boolean found = false;
        for (SimpleEntry<K, V> entry : entries) {
            if (entry == null) {
                break;
            } else if (java.util.Objects.equals(entry.getKey(), key)) {
                found = true;
                break;
            }
        }
        return found;
    }

    @Override
    public int size() {
        return this.size;
    }

    @Override
    public Iterator<Map.Entry<K, V>> iterator() {
        // Note: You may or may not need to change this method, depending on whether you
        // add any parameters to the ArrayMapIterator constructor.

        return new ArrayMapIterator<>(this.entries);

    }

    /*
    @Override
    public String toString() {
        StringBuilder out = new StringBuilder();
        for (SimpleEntry<K, V> entry : entries) {
            out.append(entry);
            out.append("\n");
        }
        return out.toString();
    }
    */


    private static class ArrayMapIterator<K, V> implements Iterator<Map.Entry<K, V>> {
        private final SimpleEntry<K, V>[] entries;
        private int currPos;
        // You may add more fields and constructor parameters

        public ArrayMapIterator(SimpleEntry<K, V>[] entries) {
            this.entries = entries;
            this.currPos = 0;
        }

        @Override
        public boolean hasNext() {
            return this.entries[currPos] != null;
        }

        @Override
        public Map.Entry<K, V> next() {

            SimpleEntry<K, V> next = entries[currPos];

            if (next == null || currPos >= entries.length - 1) {
                throw new NoSuchElementException("No such element");
            }
            ++currPos;
            return next;
        }
    }
}
