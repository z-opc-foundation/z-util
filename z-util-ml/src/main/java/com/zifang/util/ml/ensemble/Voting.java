package com.zifang.util.ml.ensemble;

import com.zifang.util.numpy.DType;
import com.zifang.util.numpy.NdArray;

/**
 * Voting Classifier for combining multiple estimators.
 * Supports HARD voting (majority vote) and SOFT voting (average probabilities).
 */
public class Voting {
    public static final String HARD = "HARD";
    public static final String SOFT = "SOFT";

    private Estimator[] estimators;
    private String voting;
    private int nClasses;

    /**
     * Create a Voting classifier.
     *
     * @param estimators Array of estimators
     * @param voting     Voting mode: "HARD" for majority vote, "SOFT" for average probabilities
     */
    public Voting(Estimator[] estimators, String voting) {
        this.estimators = estimators;
        this.voting = voting;
    }

    /**
     * Fit all estimators on training data.
     *
     * @param X Training features
     * @param y Training labels
     */
    public void fit(NdArray X, int[] y) {
        this.nClasses = findMaxValue(y) + 1;

        for (Estimator est : estimators) {
            est.fit(X, y);
        }
    }

    /**
     * Predict class labels.
     *
     * @param X Features to predict
     * @return Predicted class labels
     */
    public int[] predict(NdArray X) {
        int nSamples = X.getShape().get(0);
        int nEst = estimators.length;

        if (HARD.equalsIgnoreCase(voting)) {
            return hardVoting(X, nSamples, nEst);
        } else {
            return softVotingPredict(X, nSamples);
        }
    }

    /**
     * Predict class probabilities.
     *
     * @param X Features to predict
     * @return Probability array of shape (n_samples, n_classes)
     */
    public NdArray predictProba(NdArray X) {
        if (!SOFT.equalsIgnoreCase(voting)) {
            throw new UnsupportedOperationException(
                    "predictProba is only supported for SOFT voting. Use voting=SOFT.");
        }

        int nSamples = X.getShape().get(0);
        int nEst = estimators.length;
        double[][] avgProba = new double[nSamples][nClasses];

        // Collect probabilities from all estimators
        for (int i = 0; i < nEst; i++) {
            NdArray proba = estimators[i].predictProba(X);
            double[][] probaData = toDouble2D(proba);
            for (int j = 0; j < nSamples; j++) {
                for (int c = 0; c < nClasses; c++) {
                    avgProba[j][c] += probaData[j][c];
                }
            }
        }

        // Average probabilities
        for (int i = 0; i < nSamples; i++) {
            for (int c = 0; c < nClasses; c++) {
                avgProba[i][c] /= nEst;
            }
        }

        return createNdArray(avgProba, nSamples, nClasses);
    }

    private int[] hardVoting(NdArray X, int nSamples, int nEst) {
        int[][] allPredictions = new int[nEst][nSamples];

        // Get predictions from all estimators
        for (int i = 0; i < nEst; i++) {
            Object preds = estimators[i].predict(X);
            if (preds instanceof int[]) {
                allPredictions[i] = (int[]) preds;
            } else {
                throw new RuntimeException("Estimator " + i + " returned non-int predictions for hard voting");
            }
        }

        // Majority vote
        int[] finalPredictions = new int[nSamples];
        for (int i = 0; i < nSamples; i++) {
            int[] votes = new int[nClasses];
            for (int j = 0; j < nEst; j++) {
                int pred = allPredictions[j][i];
                if (pred >= 0 && pred < nClasses) {
                    votes[pred]++;
                }
            }

            int maxVote = 0;
            int bestClass = 0;
            for (int c = 0; c < nClasses; c++) {
                if (votes[c] > maxVote) {
                    maxVote = votes[c];
                    bestClass = c;
                }
            }
            finalPredictions[i] = bestClass;
        }

        return finalPredictions;
    }

    private int[] softVotingPredict(NdArray X, int nSamples) {
        int nEst = estimators.length;
        double[][] avgProba = new double[nSamples][nClasses];

        // Collect probabilities from all estimators
        for (int i = 0; i < nEst; i++) {
            NdArray proba = estimators[i].predictProba(X);
            double[][] probaData = toDouble2D(proba);
            for (int j = 0; j < nSamples; j++) {
                for (int c = 0; c < nClasses; c++) {
                    avgProba[j][c] += probaData[j][c];
                }
            }
        }

        // Average and find best class
        int[] predictions = new int[nSamples];
        for (int i = 0; i < nSamples; i++) {
            for (int c = 0; c < nClasses; c++) {
                avgProba[i][c] /= nEst;
            }

            // Find class with highest average probability
            int bestClass = 0;
            double maxProb = avgProba[i][0];
            for (int c = 1; c < nClasses; c++) {
                if (avgProba[i][c] > maxProb) {
                    maxProb = avgProba[i][c];
                    bestClass = c;
                }
            }
            predictions[i] = bestClass;
        }

        return predictions;
    }

    private int findMaxValue(int[] arr) {
        int max = arr[0];
        for (int val : arr) {
            if (val > max) max = val;
        }
        return max;
    }

    private double[][] toDouble2D(NdArray arr) {
        Object data = arr.getData();
        int nRows = arr.getShape().get(0);
        int nCols = arr.getShape().get(1);

        double[][] result = new double[nRows][nCols];

        if (data instanceof double[][]) {
            double[][] d2 = (double[][]) data;
            for (int i = 0; i < nRows; i++) {
                for (int j = 0; j < nCols; j++) {
                    result[i][j] = d2[i][j];
                }
            }
        } else if (data instanceof double[]) {
            double[] d1 = (double[]) data;
            for (int i = 0; i < nRows; i++) {
                for (int j = 0; j < nCols; j++) {
                    result[i][j] = d1[i * nCols + j];
                }
            }
        } else if (data instanceof float[][]) {
            float[][] f2 = (float[][]) data;
            for (int i = 0; i < nRows; i++) {
                for (int j = 0; j < nCols; j++) {
                    result[i][j] = f2[i][j];
                }
            }
        } else if (data instanceof float[]) {
            float[] f1 = (float[]) data;
            for (int i = 0; i < nRows; i++) {
                for (int j = 0; j < nCols; j++) {
                    result[i][j] = f1[i * nCols + j];
                }
            }
        } else {
            for (int i = 0; i < nRows; i++) {
                for (int j = 0; j < nCols; j++) {
                    result[i][j] = ((Number) arr.get(i, j)).doubleValue();
                }
            }
        }

        return result;
    }

    private NdArray createNdArray(double[][] data, int rows, int cols) {
        double[] flat = new double[rows * cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                flat[i * cols + j] = data[i][j];
            }
        }
        return NdArray.array(flat, DType.FLOAT64).reshape(rows, cols);
    }

    /**
     * getEstimators方法。
     *
     * @return Estimator[]类型返回值
     */
    public Estimator[] getEstimators() {
        return estimators;
    }

    /**
     * getVoting方法。
     *
     * @return String类型返回值
     */
    public String getVoting() {
        return voting;
    }
}
