package seamcarving;

import graphs.Edge;
import graphs.Graph;
import graphs.shortestpaths.DijkstraShortestPathFinder;
import graphs.shortestpaths.ShortestPath;
import graphs.shortestpaths.ShortestPathFinder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DijkstraSeamFinder implements SeamFinder {
    //private final ShortestPathFinder<Graph<Object, Edge<Object>>, Object, Edge<Object>> pathFinder;
    //private final ShortestPathFinder<Graph<Pixel, Edge<Pixel>>, Pixel, Edge<Pixel>> pathFinder;
    private final ShortestPathFinder<Graph<Integer, Edge<Integer>>, Integer, Edge<Integer>> pathFinder;

    public DijkstraSeamFinder() {
        this.pathFinder = createPathFinder();
    }

    protected <G extends Graph<V, Edge<V>>, V> ShortestPathFinder<G, V, Edge<V>> createPathFinder() {
        /*
        We override this during grading to test your code using our correct implementation so that
        you don't lose extra points if your implementation is buggy.
        */
        return new DijkstraShortestPathFinder<>();
    }

    // Since each sub-array represents a column, findHorizontalSeam represents "base" seam
    @Override
    public List<Integer> findHorizontalSeam(double[][] energies) {

        int cols = energies.length;
        int rows = energies[0].length;

        List<Integer> seam = new ArrayList<>();
        // Start and end at position out of bounds of picture.
        // Each pixel is represented by an Integer, starting at top  left (0), to bottom right (rows*cols)-1
        Integer start = -1;
        Integer end = rows * cols;

        EnergyGraph energyGraph = new EnergyGraph(cols, rows, energies);
        ShortestPath<Integer, Edge<Integer>> spt = pathFinder.findShortestPath(energyGraph, start, end);

        // Undo the pixel representation for the .to() vertex to get the y-coordinate
        for (int i = 0; i < cols; ++i) {
            seam.add(spt.edges().get(i).to() % rows);
        }

        return seam;
    }

    // Helper function for findVerticalSeam()
    // Used so findHorizontalSeam() can be implemented on the vertical seam.
    public double[][] transpose(double[][] matrix) {
        int cols = matrix.length;
        int rows = matrix[0].length;

        double[][] transpose = new double[rows][cols];

        for (int i = 0; i < rows; ++i) {
            for (int j = 0; j < cols; ++j) {
                transpose[i][j] = matrix[j][i];
            }
        }

        return transpose;
    }

    // Simply transpose and find horizontalSeam.
    // Reduces to horizontalSeam.
    @Override
    public List<Integer> findVerticalSeam(double[][] energies) {
        double[][] transpose = transpose(energies);
        return findHorizontalSeam(transpose);
    }

    private static class EnergyGraph implements Graph<Integer, Edge<Integer>> {
        int width;
        int height;

        double[][] energies;

        public EnergyGraph(int width, int height, double[][] energies) {
            this.width = width;
            this.height = height;
            this.energies = energies;
        }

        @Override
        public Collection<Edge<Integer>> outgoingEdgesFrom(Integer vertex) {
            Collection<Edge<Integer>> edges = new ArrayList<>();

            // Note that all edge weights represent the energy of the pixel the edge points from.

            // Pixels are represented as Integer from 0 -> width*height - 1, increasing top to bottom and right to left.
            /*
           -1 (start)  0  6  12  18  24  30 (end)
                       1  7  13  19  25
                       2  8  14  20  26
                       3  9  15  21  27
                       4  10 16  22  28
                       5  11 17  23  29
             */

            // Starting dummy node. Loosen restriction to vertex < 0
            // to potentially prevent index out of bounds errors.
            if (vertex < 0) {
                for (int i = 0; i < height; ++i) {
                    // Start has no weight, represent with a 0. Outgoing edges == height
                    edges.add(new Edge<>(-1, i, 0));
                }
                // Ending dummy node. Again, loosen restriction to vertex >= w*h
                // to potentially prevent index out of bounds errors.
            } else if (vertex >= width * height) {
                // Nothing to do, no outgoing edges from end vertex.
                return edges;
                // Last column
            } else if (vertex < width * height && vertex > ((width - 1) * height)) {
                edges.add(new Edge<>(vertex, width * height, this.energies[width - 1][vertex % height]));
                // At the top row, only 2 outgoing edges.
            } else if (vertex % height == 0) {
                for (int i = 0; i < 2; ++i) {
                    edges.add(new Edge<>(vertex, vertex + height + i, this.energies[vertex / height][0]));
                }
                // At the bottom row, only 2 outgoing edges
            } else if ((vertex + 1) % height == 0) {
                for (int i = -1; i < 1; ++i) {
                    edges.add(new Edge<>(vertex, vertex + height + i, this.energies[vertex / height][height - 1]));
                }
                // Any other pixel, 3 outgoing edges.
            } else {
                for (int i = -1; i < 2; ++i) {
                    edges.add(new Edge<>(vertex, vertex + height + i, this.energies[vertex / height][vertex % height]));
                }
            }

            return edges;
        }
    }
}

