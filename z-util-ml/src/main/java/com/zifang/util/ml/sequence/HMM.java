package com.zifang.util.ml.sequence;

import java.util.Arrays;
import java.util.Random;

/**
 * Hidden Markov Model with Viterbi decoding and Baum-Welch training.
 * <p>
 * Supports:
 * - Supervised training (fit with observations and hidden states)
 * - Viterbi algorithm for finding the most likely hidden state sequence
 * - Forward algorithm for computing observation probabilities
 * - Baum-Welch algorithm for unsupervised training (EM)
 */
public class HMM {

    private int nStates;
    private int nObservations;
    private double[] pi;
    private double[][] A;
    private double[][] B;
    private Random random;

    /**
     * Create a new Hidden Markov Model.
     *
     * @param nStates       Number of hidden states
     * @param nObservations Number of possible observations
     */
    public HMM(int nStates, int nObservations) {
        this.nStates = nStates;
        this.nObservations = nObservations;
        this.random = new Random(42);
        initializeParameters();
    }

    /**
     * Initialize parameters with random values (uniform distribution).
     */
    private void initializeParameters() {
        // Initialize initial state probabilities (uniform)
        this.pi = new double[nStates];
        Arrays.fill(pi, 1.0 / nStates);

        // Initialize transition matrix (uniform)
        this.A = new double[nStates][nStates];
        for (int i = 0; i < nStates; i++) {
            Arrays.fill(A[i], 1.0 / nStates);
        }

        // Initialize emission matrix (uniform)
        this.B = new double[nStates][nObservations];
        for (int i = 0; i < nStates; i++) {
            Arrays.fill(B[i], 1.0 / nObservations);
        }
    }

    /**
     * Supervised training using maximum likelihood estimation.
     * Counts occurrences and normalizes to get probabilities.
     *
     * @param observations Array of observation indices
     * @param states       Array of corresponding hidden state indices
     * @param nIterations  Number of iterations (for Baum-Welch component if used)
     */
    public void fit(int[] observations, int[] states, int nIterations) {
        if (observations.length != states.length) {
            throw new IllegalArgumentException("observations and states must have same length");
        }

        int T = observations.length;

        // Count initial state occurrences
        double[] piCounts = new double[nStates];
        for (int t = 0; t < T; t++) {
            piCounts[states[t]]++;
        }

        // Count transition probabilities
        double[][] ACounts = new double[nStates][nStates];
        for (int t = 0; t < T - 1; t++) {
            int s = states[t];
            int sNext = states[t + 1];
            ACounts[s][sNext]++;
        }

        // Count emission probabilities
        double[][] BCounts = new double[nStates][nObservations];
        for (int t = 0; t < T; t++) {
            int s = states[t];
            int o = observations[t];
            BCounts[s][o]++;
        }

        // Normalize to get probabilities (add smoothing)
        double smoothing = 1e-6;
        for (int i = 0; i < nStates; i++) {
            pi[i] = (piCounts[i] + smoothing) / (T + smoothing * nStates);
            for (int j = 0; j < nStates; j++) {
                double rowSum = 0;
                for (int k = 0; k < nStates; k++) {
                    rowSum += ACounts[k][j];
                }
                A[i][j] = (ACounts[i][j] + smoothing) / (rowSum + smoothing * nStates);
            }
            for (int o = 0; o < nObservations; o++) {
                double colSum = 0;
                for (int k = 0; k < nStates; k++) {
                    colSum += BCounts[k][o];
                }
                B[i][o] = (BCounts[i][o] + smoothing) / (colSum + smoothing * nObservations);
            }
        }
    }

    /**
     * Supervised training with single observation sequence.
     *
     * @param observations Array of observation indices
     * @param nIterations  Number of iterations (for Baum-Welch if implementing unsupervised)
     */
    public void fit(int[] observations, int nIterations) {
        // For supervised learning, we need states. This method is for
        // Baum-Welch (unsupervised) training which is optional.
        // Here we just perform one iteration of EM-like update if states were provided.
        throw new UnsupportedOperationException(
                "Supervised fit requires states. Use fit(int[] observations, int[] states, int nIterations)");
    }

    /**
     * Unsupervised training using Baum-Welch algorithm (EM).
     *
     * @param observations Array of observation indices
     * @param nIterations  Number of EM iterations
     */
    public void fitUnsupervised(int[] observations, int nIterations) {
        int T = observations.length;

        for (int iter = 0; iter < nIterations; iter++) {
            // E-step: Compute forward and backward variables
            double[][] alpha = forward(observations);
            double[][] beta = backward(observations);

            // Compute gamma (posterior probability of being in state i at time t)
            double[][] gamma = new double[T][nStates];
            for (int t = 0; t < T; t++) {
                double denom = 0;
                for (int i = 0; i < nStates; i++) {
                    denom += alpha[t][i] * beta[t][i];
                }
                for (int i = 0; i < nStates; i++) {
                    gamma[t][i] = alpha[t][i] * beta[t][i] / (denom + 1e-300);
                }
            }

            // Compute xi (posterior probability of transition from i to j at time t)
            double[][][] xi = new double[T - 1][nStates][nStates];
            for (int t = 0; t < T - 1; t++) {
                double denom = 0;
                for (int i = 0; i < nStates; i++) {
                    for (int j = 0; j < nStates; j++) {
                        denom += alpha[t][i] * A[i][j] * B[j][observations[t + 1]] * beta[t + 1][j];
                    }
                }
                for (int i = 0; i < nStates; i++) {
                    for (int j = 0; j < nStates; j++) {
                        xi[t][i][j] = alpha[t][i] * A[i][j] * B[j][observations[t + 1]] * beta[t + 1][j];
                        xi[t][i][j] /= (denom + 1e-300);
                    }
                }
            }

            // M-step: Update parameters
            // Update pi
            for (int i = 0; i < nStates; i++) {
                pi[i] = gamma[0][i];
            }

            // Update A
            for (int i = 0; i < nStates; i++) {
                double denom = 0;
                for (int t = 0; t < T - 1; t++) {
                    denom += gamma[t][i];
                }
                for (int j = 0; j < nStates; j++) {
                    double numer = 0;
                    for (int t = 0; t < T - 1; t++) {
                        numer += xi[t][i][j];
                    }
                    A[i][j] = numer / (denom + 1e-300);
                }
            }

            // Update B
            for (int i = 0; i < nStates; i++) {
                double denom = 0;
                for (int t = 0; t < T; t++) {
                    denom += gamma[t][i];
                }
                for (int o = 0; o < nObservations; o++) {
                    double numer = 0;
                    for (int t = 0; t < T; t++) {
                        if (observations[t] == o) {
                            numer += gamma[t][i];
                        }
                    }
                    B[i][o] = numer / (denom + 1e-300);
                }
            }
        }
    }

    /**
     * Forward algorithm: compute P(o_1,...,o_t, s_t=i) for each t,i
     *
     * @param observations Array of observations
     * @return alpha[t][i] = P(o_1,...,o_t, s_t=i)
     */
    private double[][] forward(int[] observations) {
        int T = observations.length;
        double[][] alpha = new double[T][nStates];

        // Initialization
        int o0 = observations[0];
        for (int i = 0; i < nStates; i++) {
            alpha[0][i] = pi[i] * B[i][o0];
        }

        // Recursion
        for (int t = 1; t < T; t++) {
            int ot = observations[t];
            for (int j = 0; j < nStates; j++) {
                double sum = 0;
                for (int i = 0; i < nStates; i++) {
                    sum += alpha[t - 1][i] * A[i][j];
                }
                alpha[t][j] = sum * B[j][ot];
            }
        }

        return alpha;
    }

    /**
     * Backward algorithm: compute P(o_{t+1},...,o_T | s_t=i)
     *
     * @param observations Array of observations
     * @return beta[t][i] = P(o_{t+1},...,o_T | s_t=i)
     */
    private double[][] backward(int[] observations) {
        int T = observations.length;
        double[][] beta = new double[T][nStates];

        // Initialization (beta[T-1][i] = 1)
        Arrays.fill(beta[T - 1], 1.0);

        // Recursion (going backwards)
        for (int t = T - 2; t >= 0; t--) {
            int ot1 = observations[t + 1];
            for (int i = 0; i < nStates; i++) {
                double sum = 0;
                for (int j = 0; j < nStates; j++) {
                    sum += A[i][j] * B[j][ot1] * beta[t + 1][j];
                }
                beta[t][i] = sum;
            }
        }

        return beta;
    }

    /**
     * Viterbi algorithm for finding the most likely hidden state sequence.
     *
     * @param observations Array of observation indices
     * @return Array of most likely hidden state indices
     */
    public int[] predictOptimalStateSeq(int[] observations) {
        int T = observations.length;

        // delta[t][i] = max probability of being in state i at time t
        // with the most likely path
        double[][] delta = new double[T][nStates];
        int[][] psi = new int[T][nStates]; // Backpointer

        // Initialization
        int o0 = observations[0];
        for (int i = 0; i < nStates; i++) {
            delta[0][i] = pi[i] * B[i][o0];
            psi[0][i] = 0;
        }

        // Recursion
        for (int t = 1; t < T; t++) {
            int ot = observations[t];
            for (int j = 0; j < nStates; j++) {
                double maxProb = 0;
                int maxState = 0;
                for (int i = 0; i < nStates; i++) {
                    double prob = delta[t - 1][i] * A[i][j];
                    if (prob > maxProb) {
                        maxProb = prob;
                        maxState = i;
                    }
                }
                delta[t][j] = maxProb * B[j][ot];
                psi[t][j] = maxState;
            }
        }

        // Backtrack to find the optimal path
        int[] path = new int[T];
        double maxFinalProb = 0;
        int maxFinalState = 0;
        for (int i = 0; i < nStates; i++) {
            if (delta[T - 1][i] > maxFinalProb) {
                maxFinalProb = delta[T - 1][i];
                maxFinalState = i;
            }
        }
        path[T - 1] = maxFinalState;

        for (int t = T - 2; t >= 0; t--) {
            path[t] = psi[t + 1][path[t + 1]];
        }

        return path;
    }

    /**
     * Predict the most likely hidden state sequence using Viterbi decoding.
     *
     * @param observations Array of observation indices
     * @return Array of most likely hidden state indices
     */
    public int[] predict(int[] observations) {
        return predictOptimalStateSeq(observations);
    }

    /**
     * Compute the probability of an observation sequence given a state sequence.
     * P(o_1,...,o_T | s_1,...,s_T) = prod_t B[s_t, o_t]
     *
     * @param observations Array of observation indices
     * @param states       Array of hidden state indices
     * @return Probability of observation sequence given state sequence
     */
    public double score(int[] observations, int[] states) {
        if (observations.length != states.length) {
            throw new IllegalArgumentException("observations and states must have same length");
        }

        int T = observations.length;
        double prob = 1.0;

        // Initial probability
        prob *= pi[states[0]];

        // Product of emissions and transitions
        for (int t = 0; t < T - 1; t++) {
            int s = states[t];
            int sNext = states[t + 1];
            int o = observations[t];
            int oNext = observations[t + 1];

            prob *= A[s][sNext];
            prob *= B[s][o];
        }

        // Last emission
        prob *= B[states[T - 1]][observations[T - 1]];

        return prob;
    }

    /**
     * Compute the probability of an observation sequence using forward algorithm.
     * P(o_1,...,o_T) = sum_i alpha[T-1][i]
     *
     * @param observations Array of observation indices
     * @return Probability of observation sequence
     */
    public double score(int[] observations) {
        double[][] alpha = forward(observations);
        double prob = 0;
        for (int i = 0; i < nStates; i++) {
            prob += alpha[observations.length - 1][i];
        }
        return prob;
    }

    /**
     * Get initial state probabilities.
     *
     * @return Copy of initial state probabilities
     */
    public double[] getPi() {
        return pi.clone();
    }

    /**
     * Get transition matrix.
     *
     * @return Copy of transition matrix A where A[i][j] = P(s_j | s_i)
     */
    public double[][] getA() {
        double[][] copy = new double[nStates][nStates];
        for (int i = 0; i < nStates; i++) {
            copy[i] = A[i].clone();
        }
        return copy;
    }

    /**
     * Get emission matrix.
     *
     * @return Copy of emission matrix B where B[i][o] = P(o | s_i)
     */
    public double[][] getB() {
        double[][] copy = new double[nStates][nObservations];
        for (int i = 0; i < nStates; i++) {
            copy[i] = B[i].clone();
        }
        return copy;
    }

    /**
     * Get number of hidden states.
     *
     * @return Number of states
     */
    public int getNStates() {
        return nStates;
    }

    /**
     * Get number of possible observations.
     *
     * @return Number of observations
     */
    public int getNObservations() {
        return nObservations;
    }
}
