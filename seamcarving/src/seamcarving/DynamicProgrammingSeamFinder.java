package seamcarving;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Dynamic programming implementation of the {@link SeamFinder} interface.
 *
 * @see SeamFinder
 * @see SeamCarver
 */
public class DynamicProgrammingSeamFinder implements SeamFinder {
    @Override
    public List<Integer> findHorizontalSeam(double[][] energies) {
        //throw new UnsupportedOperationException("Not implemented yet.");

        int rows = energies[0].length;
        int cols = energies.length;
        // Array of weights
        double[][] weights = new double[cols][rows];
        // First column of weights is just the energies of the first column
        weights[0] = energies[0];

        List<Integer> seam = new ArrayList<>(rows);

        // First, iterate over the energies array and calculate the minimum weights of a column based
        // on the energies of the previous adjacent energies.
        for (int j = 1; j < cols; ++j) {
            for (int i = 0; i < rows; ++i) {
                // Top edge case
                if (i == 0) {
                    // Find the minimum adjacent previous weight and add it to the corresponding energy.
                    weights[j][i] = Math.min(weights[j - 1][i], weights[j - 1][i + 1]) + energies[j][i];
                    // Bottom edge
                } else if (i == rows - 1) {
                    // Find the minimum adjacent previous weight and add it to the corresponding energy.
                    weights[j][i] = Math.min(weights[j - 1][i], weights[j - 1][i - 1]) + energies[j][i];
                } else {
                    // Find the minimum adjacent previous weight and add it to the corresponding energy.
                    // There are three adjacent weights instead of two.
                    weights[j][i] = Math.min(weights[j - 1][i - 1],
                        Math.min(weights[j - 1][i], weights[j - 1][i + 1])) + energies[j][i];
                }
            }
        }

        // Next, find the corresponding index of the minimum weight in the last column.
        // This determines the endpoint of the seam.
        double minWeight = weights[cols - 1][0];
        int minIdx = 0;
        // Start at the right most column
        for (int i = 1; i < rows; ++i) {
            if (weights[cols - 1][i] < minWeight) {
                minWeight = weights[cols - 1][i];
                minIdx = i;
            }
        }
        seam.add(minIdx);

        int prevMinIdx = minIdx;
        // Now the last index with the minimum weight has been found.
        // Backtrack starting at this index, finding the minimum adjacent weight in the previous column
        for (int j = cols - 2; j >= 0; --j) {
            // If the min index is on the top row, two possible adjacent weights
            if (prevMinIdx == 0) {
                // Row of minimum adjacent weight is either 0 or 1.
                prevMinIdx = weights[j][0] < weights[j][1] ? 0 : 1;
            } else if (prevMinIdx == rows - 1) {
                // Row of minimum adjacent weight is either the last row or second to last row.
                prevMinIdx = weights[j][rows - 1] < weights[j][rows - 2] ? rows - 1 : rows - 2;
            } else {
                // Temp variable, determines the minimum of y and y + 1 previous element
                int prevMinIdx1 = weights[j][prevMinIdx + 1] < weights[j][prevMinIdx] ? prevMinIdx + 1 : prevMinIdx;
                // Then find the actual minimum from prevMinIdx and the y - 1 element.
                prevMinIdx = weights[j][prevMinIdx1] < weights[j][prevMinIdx - 1] ? prevMinIdx1 : prevMinIdx - 1;
            }
            seam.add(prevMinIdx);
        }

        Collections.reverse(seam);
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
    // This is the same strategy as the DijkstraSeamFinder program.
    @Override
    public List<Integer> findVerticalSeam(double[][] energies) {
        double[][] transpose = transpose(energies);
        return findHorizontalSeam(transpose);
    }
}
