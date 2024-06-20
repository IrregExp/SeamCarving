package graphs.shortestpaths;

import priorityqueues.ExtrinsicMinPQ;
import priorityqueues.NaiveMinPQ;
import graphs.BaseEdge;
import graphs.Graph;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Computes shortest paths using Dijkstra's algorithm.
 * @see SPTShortestPathFinder for more documentation.
 */
public class DijkstraShortestPathFinder<G extends Graph<V, E>, V, E extends BaseEdge<V, E>>
    extends SPTShortestPathFinder<G, V, E> {

    protected <T> ExtrinsicMinPQ<T> createMinPQ() {
        return new NaiveMinPQ<>();
        /*
        If you have confidence in your heap implementation, you can disable the line above
        and enable the one below.
         */
        //return new ArrayHeapMinPQ<>();

        /*
        Otherwise, do not change this method.
        We override this during grading to test your code using our correct implementation so that
        you don't lose extra points if your implementation is buggy.
         */
    }

    @Override
    protected Map<V, E> constructShortestPathsTree(G graph, V start, V end) {
        Set<V> known = new HashSet<>();

        Map<V, Double> distTo = new HashMap<>(); // for each vertex, store shortest distance.
        Map<V, E> spt = new HashMap<>();
        ExtrinsicMinPQ<E> heap = createMinPQ();

        known.add(start);
        distTo.put(start, 0.0);
        V curr = start;

        while (!known.contains(end)) {
            for (E edge : graph.outgoingEdgesFrom(curr)) {
                // Only process edges not already known.
                if (!known.contains(edge.to())) {
                    double newDist;
                    double oldDist;
                    newDist = distTo.get(curr) + edge.weight();
                    // Doesn't contain key == infinity
                    oldDist = distTo.getOrDefault(edge.to(), Double.POSITIVE_INFINITY);

                    // If edge not present in heap, newDist will ALWAYS be less than oldDist
                    // Otherwise, may need to changePriority if multiple edges with different weight
                    if (newDist < oldDist) {
                        distTo.put(edge.to(), newDist);
                        if (!heap.contains(edge)) {
                            heap.add(edge, newDist);
                        } else {
                            heap.changePriority(edge, newDist);
                        }

                    }
                }
            }

            // If heap is empty, the graph has been processed.
            if (heap.isEmpty()) {
                break;
            }

            // Otherwise, removeMin() until the next unknown vertex is found
            while (known.contains(heap.peekMin().to())) {
                heap.removeMin();
                if (heap.isEmpty()) {
                    return spt;
                }
            }

            // This is the next unknown vertex, make it known.
            E smallestEdge = heap.removeMin();
            curr = smallestEdge.to();
            known.add(curr);
            spt.put(curr, smallestEdge);
        }

        // System.out.println("Spt is: ");
        // for (V vertex : spt.keySet()) {
        //     System.out.println(spt.get(vertex));
        // }
        //
        // for (V vertex: distTo.keySet()) {
        //     System.out.println(distTo.get(vertex));
        // }
        return spt;

    }

    @Override
    protected ShortestPath<V, E> extractShortestPath(Map<V, E> spt, V start, V end) {

        if (spt == null) {
            return new ShortestPath.Failure<>();
        }

        E edge = spt.get(end);

        if (Objects.equals(start, end)) {
            return new ShortestPath.SingleVertex<>(start);
        }


        List<E> shortestPath = new ArrayList<>();

        if (edge == null) {
            return new ShortestPath.Failure<>();
        }


        //while this edge does not point to start, keep going
        while (edge != null && edge.from() != start) {
            shortestPath.add(0, edge);
            edge = spt.get(edge.from());
        }
        if (edge != null) {
            shortestPath.add(0, edge);
        }
        return new ShortestPath.Success<>(shortestPath);

    }
}
