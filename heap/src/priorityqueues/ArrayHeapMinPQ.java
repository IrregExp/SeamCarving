package priorityqueues;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * @see ExtrinsicMinPQ
 */
public class ArrayHeapMinPQ<T> implements ExtrinsicMinPQ<T> {
    // IMPORTANT: Do not rename these fields or change their visibility.
    // We access these during grading to test your code.
    static final int START_INDEX = 0;
    List<PriorityNode<T>> items;

    // Used to keep track of current size of heap.
    int size;

    //Key: Item, Value: Location. Used to find current index of an item.
    HashMap<T, Integer> locMap;


    public ArrayHeapMinPQ() {
        items = new ArrayList<>();
        this.size = 0;
        locMap = new HashMap<>();
    }

    // Here's a method stub that may be useful. Feel free to change or remove it, if you wish.
    // You'll probably want to add more helper methods like this one to make your code easier to read.

    /**
     * A helper method for swapping the items at two indices of the array heap.
     */
    private void swap(int a, int b) {
        PriorityNode<T> itemA = this.items.get(a);
        PriorityNode<T> itemB = this.items.get(b);

        // Swap positions in items.
        this.items.set(b, itemA);
        this.items.set(a, itemB);

        // Then update the locations in locMap.
        // This map gets updated everytime swap is called,
        // percolateUp/Down use this method to swap parent and child nodes
        // so elements already present in locMap don't need to be updated.
        this.locMap.put(itemA.getItem(), b);
        this.locMap.put(itemB.getItem(), a);
    }

    private void percolateUp(int startIdx) {
        int parentIdx = (startIdx - 1) / 2;

        PriorityNode<T> parentNode = this.items.get(parentIdx);
        PriorityNode<T> childNode = this.items.get(startIdx);

        // While the heap invariant is broken
        while (parentNode.getPriority() > childNode.getPriority()) {
            // Swap the child and parent nodes.
            this.swap(startIdx, parentIdx);

            // Determine the new parent node and repeat until invariant is fixed.
            childNode = items.get(parentIdx);
            parentNode = items.get((parentIdx - 1) / 2);

            startIdx = parentIdx;
            parentIdx = (startIdx - 1) / 2;
        }
    }

    private void percolateDown(int startIdx) {
        int leftChildIdx = (2 * startIdx) + 1;
        int rightChildIdx = (2 * startIdx) + 2;

        // If there is no leftChildIdx, do nothing.
        // Either the node at "startIdx" is a leaf
        // or the node may not exist.
        if (this.size <= leftChildIdx) {
            return;
        }

        // parent is now guaranteed to have a left child
        // because of first "if" check above.
        PriorityNode<T> parent = this.items.get(startIdx);
        PriorityNode<T> leftChild = this.items.get(leftChildIdx);
        PriorityNode<T> rightChild; // May not exist.

        // If there is only a left child
        if (this.size == rightChildIdx) {
            // Swap the 2 nodes if heap invariant is broken.
            if (parent.getPriority() > leftChild.getPriority()) {
                this.swap(startIdx, leftChildIdx);
            }
            // Otherwise there are both left and right children.
        } else {
            rightChild = this.items.get(rightChildIdx);

            // Fix broken heap invariant: Parent priority must less than both children
            while ((parent.getPriority() > leftChild.getPriority()) ||
                (parent.getPriority() > rightChild.getPriority())) {

                // Swap with the lowest priority child
                // Parent index becomes the respective child's index.
                if (leftChild.getPriority() > rightChild.getPriority()) {
                    this.swap(rightChildIdx, startIdx);
                    startIdx = rightChildIdx;
                } else {
                    this.swap(leftChildIdx, startIdx);
                    startIdx = leftChildIdx;
                }

                leftChildIdx = 2 * startIdx + 1;
                rightChildIdx = 2 * startIdx + 2;

                // Same conditions as above. If there is no left child, this is a leaf node.
                if (this.size <= leftChildIdx) {
                    break;
                    // Otherwise, if parent only has a left child. Swap if needed.
                } else if (this.size == rightChildIdx) {
                    // Check if leftChildIdx needs to swap. Last operation before break.
                    leftChild = this.items.get(leftChildIdx);
                    if (parent.getPriority() > leftChild.getPriority()) {
                        this.swap(startIdx, leftChildIdx);
                    }
                    break;
                }

                parent = items.get(startIdx);
                leftChild = items.get(leftChildIdx);
                rightChild = items.get(rightChildIdx);
            }
        }
    }

    @Override
    public void add(T item, double priority) {
        // Add new node to end of array (last row in heap).
        // Can't add item if already present.
        if (this.locMap.containsKey(item)) {
            throw new IllegalArgumentException("Item already present");
        }

        PriorityNode<T> newNode = new PriorityNode<>(item, priority);

        this.items.add(newNode);
        int insertedIdx = this.size;  // New node inserted at last position
        ++this.size;
        this.locMap.put(item, insertedIdx);
        this.percolateUp(insertedIdx);  // Node added, heap invariant potentially broken.

    }

    @Override
    public boolean contains(T item) {
        return locMap.containsKey(item);
    }

    @Override
    public T peekMin() {
        if (this.size == 0) {
            throw new NoSuchElementException();
        }
        return this.items.get(START_INDEX).getItem();
    }

    @Override
    public T removeMin() {
        if (this.size == 0) {
            throw new NoSuchElementException("Heap is empty");
        }

        T min = this.items.get(START_INDEX).getItem();

        this.swap(this.size - 1, START_INDEX);

        // Remove the min element from both array and hashmap.
        this.locMap.remove(min);
        this.items.remove(this.size - 1);
        --this.size;
        this.percolateDown(START_INDEX);

        return min;
    }

    @Override
    public void changePriority(T item, double priority) {
        //throw new NoSuchElementException("not implemented");

        if (!this.locMap.containsKey(item)) {
            throw new NoSuchElementException("Item not present");
        }

        int location = this.locMap.get(item); // Used to find the location of the node being updated.

        PriorityNode<T> updatedNode = new PriorityNode<>(item, priority);

        // Find the node to be updated, given the location.
        PriorityNode<T> node = this.items.get(location);

        this.items.set(location, updatedNode);

        // Only need to percolate starting from location.
        // Changing priority may break heap invariant,
        // depending on new priority either pecolate up or down.
        if (node.getPriority() >= priority) {
            this.percolateUp(location);
        } else {
            this.percolateDown(location);
        }
    }

    @Override
    public int size() {
        return this.size;
    }
}
