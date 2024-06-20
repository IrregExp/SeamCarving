package maps;

import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * @see AbstractIterableMap
 * @see Map
 */
public class ChainedHashMap<K, V> extends AbstractIterableMap<K, V> {

    // Lambda = n/c, point at which map will be rehashed
    private static final double DEFAULT_RESIZING_LOAD_FACTOR_THRESHOLD = 0.75;
    private static final int DEFAULT_INITIAL_CHAIN_COUNT = 10;
    private static final int DEFAULT_INITIAL_CHAIN_CAPACITY = 10;

    /*
    Warning:
    You may not rename this field or change its type.
    We will be inspecting it in our secret tests.
     */
    AbstractIterableMap<K, V>[] chains;

    public int totalElements; //Total number of keys in the HashMap.
    double loadFactorThreshold; // lambda = n/c == totalElements/buckets
    public int buckets; // Total number of buckets in the map
    public int bucketCapacity; // Capacity of each bucket

    // You're encouraged to add extra fields (and helper methods) though!

    /**
     * Constructs a new ChainedHashMap with default resizing load factor threshold,
     * default initial chain count, and default initial chain capacity.
     */
    public ChainedHashMap() {
        this(DEFAULT_RESIZING_LOAD_FACTOR_THRESHOLD, DEFAULT_INITIAL_CHAIN_COUNT, DEFAULT_INITIAL_CHAIN_CAPACITY);
    }

    /**
     * Constructs a new ChainedHashMap with the given parameters.
     *
     * @param resizingLoadFactorThreshold the load factor threshold for resizing. When the load factor
     *                                    exceeds this value, the hash table resizes. Must be > 0.
     * @param initialChainCount the initial number of chains for your hash table. Must be > 0.
     * @param chainInitialCapacity the initial capacity of each ArrayMap chain created by the map.
     *                             Must be > 0.
     */
    public ChainedHashMap(double resizingLoadFactorThreshold, int initialChainCount, int chainInitialCapacity) {
        // Check parameters are all greater than 0
        if (resizingLoadFactorThreshold <= 0.0) {
            resizingLoadFactorThreshold = DEFAULT_RESIZING_LOAD_FACTOR_THRESHOLD;
        }

        if (initialChainCount <= 0) {
            initialChainCount = DEFAULT_INITIAL_CHAIN_COUNT;
        }

        if (chainInitialCapacity <= 0) {
            chainInitialCapacity = DEFAULT_INITIAL_CHAIN_CAPACITY;
        }

        this.loadFactorThreshold = resizingLoadFactorThreshold;
        this.buckets = initialChainCount;
        this.bucketCapacity = chainInitialCapacity;
        this.chains = createArrayOfChains(this.buckets);
        this.totalElements = 0;
    }

    /**
     * This method will return a new, empty array of the given size that can contain
     * {@code AbstractIterableMap<K, V>} objects.
     *
     * Note that each element in the array will initially be null.
     *
     * Note: You do not need to modify this method.
     * @see ArrayMap createArrayOfEntries method for more background on why we need this method
     */
    @SuppressWarnings("unchecked")
    private AbstractIterableMap<K, V>[] createArrayOfChains(int arraySize) {
        return (AbstractIterableMap<K, V>[]) new AbstractIterableMap[arraySize];
    }

    /**
     * Returns a new chain.
     *
     * This method will be overridden by the grader so that your ChainedHashMap implementation
     * is graded using our solution ArrayMaps.
     *
     * Note: You do not need to modify this method.
     */
    protected AbstractIterableMap<K, V> createChain(int initialSize) {
        return new ArrayMap<>(initialSize);
    }

    private void resize(int newCapacity) {
        // Create a new chain with double the capacity.
        AbstractIterableMap<K, V>[] newChains = createArrayOfChains(newCapacity);
        AbstractIterableMap<K, V>[] prevChains = this.chains;

        // Update variables
        this.chains = newChains;
        int prevBucketCount = this.buckets;
        this.buckets *= 2;
        this.totalElements = 0;

        for (int i = 0; i < prevBucketCount; ++i) {
            // Iterate over the buckets until a non-null chain is found.
            if (prevChains[i] != null) {
                AbstractIterableMap<K, V> chain = prevChains[i];

                // Then put each entry in the chain into the new chain.
                for (Entry<K, V> entry : chain) {
                    if (entry != null) {
                        this.put(entry.getKey(), entry.getValue());
                    }
                }
            }
        }
    }

    private boolean needsResize() {
        // Lambda = totalElements/buckets = loadFactorThreshold
        return (double) this.totalElements / this.buckets >= this.loadFactorThreshold;
    }

    @Override
    public V get(Object key) {
        V value = null;
        // No hashcode for null.
        // If key is null, set key to 0, otherwise find the absolute value of hashCode (if negative)
        // This is done for all methods needing a bucket.
        int bucket = key == null ? 0 : Math.abs(key.hashCode()) % this.buckets;
        AbstractIterableMap<K, V> chain = this.chains[bucket];
        if (chain != null) {
            value = chain.get(key);
        }

        return value;
    }

    @Override
    public V put(K key, V value) {

        int bucket = key == null ? 0 : Math.abs(key.hashCode()) % this.buckets;

        if (this.chains[bucket] == null) {
            this.chains[bucket] = createChain(this.bucketCapacity);
        }

        AbstractIterableMap<K, V> chain = this.chains[bucket];

        V prevValue = chain.put(key, value);

        if (prevValue == null) {
            ++this.totalElements;
        }

        if (this.needsResize()) {
            this.resize(this.buckets * 2);
        }

        return prevValue;

    }

    @Override
    public V remove(Object key) {

        int bucket = key == null ? 0 : Math.abs(key.hashCode()) % this.buckets;
        AbstractIterableMap<K, V> chain = this.chains[bucket];

        if (chain != null) {
            V removedValue = chain.remove(key);
            if (removedValue != null) {
                --this.totalElements;
            }

            if (chain.size() == 0) {
                this.chains[bucket] = null;
            }
            return removedValue;
        }
        return null;
    }

    @Override
    public void clear() {
        this.chains = createArrayOfChains(this.buckets);
        this.totalElements = 0;
    }

    @Override
    public boolean containsKey(Object key) {

        int bucket = key == null ? 0 : Math.abs(key.hashCode()) % this.buckets;
        AbstractIterableMap<K, V> chain = this.chains[bucket];
        if (chain != null) {
            return chain.containsKey(key);
        }
        return false;

    }

    @Override
    public int size() {
        return this.totalElements;
    }

    @Override
    public Iterator<Map.Entry<K, V>> iterator() {
        // Note: you won't need to change this method (unless you add more constructor parameters)
        return new ChainedHashMapIterator<>(this.chains);
    }


    /*
    @Override
    public String toString() {
        return super.toString();
    }
    */


    /*
    See the assignment webpage for tips and restrictions on implementing this iterator.
     */
    private static class ChainedHashMapIterator<K, V> implements Iterator<Map.Entry<K, V>> {
        private AbstractIterableMap<K, V>[] chains;
        // You may add more fields and constructor parameters
        private int currBucket;
        private int currPos;

        public ChainedHashMapIterator(AbstractIterableMap<K, V>[] chains) {
            this.chains = chains;
            this.currBucket = 0;
            this.currPos = 0;
        }

        @Override
        public boolean hasNext() {
            AbstractIterableMap<K, V> chain = this.chains[currBucket];

            if (chain == null || currPos >= chain.size() - 1) {
                if (this.currBucket == this.chains.length - 1) {
                    return false;
                }

                for (int i = currBucket + 1; i < this.chains.length; ++i) {
                    if (this.chains[i] != null) {
                        return true;
                    }
                }
                return false;
            }
            return true;
        }

        @Override
        public Map.Entry<K, V> next() {

            if (!this.hasNext()) {
                throw new NoSuchElementException("No next");
            }

            if (this.chains[this.currBucket] == null || currPos >= this.chains[currBucket].size()) {

                for (int i = currBucket + 1; i < this.chains.length; ++i) {
                    if (this.chains[i] != null) {
                        this.currBucket = i;
                        this.currPos = 0;
                        break;
                    }
                }
            }

            AbstractIterableMap<K, V> chain = this.chains[this.currBucket];

            int count = 0;
            for (K key : chain.keySet()) {
                if (count == currPos) {
                    ++currPos;
                    return Map.entry(key, chain.get(key));
                }
                ++count;
            }
            throw new NoSuchElementException("Something went wrong");
        }
    }
}
