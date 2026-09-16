package com.zifang.util.ml.sequence;

import com.zifang.util.numpy.NdArray;

import java.util.function.BiFunction;

/**
 * Linear-chain Conditional Random Field (CRF) for sequence labeling.
 * <p>
 * Uses feature-based potentials:
 * - State potential: score(y_i -> s) for each tag s at position i
 * - Transition potential: score(y_{i-1}=s', y_i=s) for transition s'->s
 * <p>
 * Training uses gradient descent on conditional log-likelihood.
 * Inference uses Viterbi algorithm for best tag sequence.
 */
public class CRF {

    private int nTags;
    private int nFeatures;
    private double learningRate;
    private double lambda;

    // Tag weights: [nTags][nFeatures]
    private double[][] tagWeights;

    // Transition weights: [nTags][nTags]
    // transitionWeights[s'][s] = score of transitioning from tag s' to tag s
    private double[][] transitionWeights;

    // Feature extractor: (feature vector at position, position) -> double[]
    // For sequence labeling, features are typically extracted based on 
    // observation at position i and surrounding context
    private BiFunction<double[], Integer, double[]> featureExtractor;

    /**
     * Create a new Linear-chain CRF.
     *
     * @param nTags        Number of possible tags/labels
     * @param nFeatures    Dimension of feature vectors
     * @param learningRate Learning rate for SGD
     * @param lambda       L2 regularization parameter
     */
    public CRF(int nTags, int nFeatures, double learningRate, double lambda) {
        this.nTags = nTags;
        this.nFeatures = nFeatures;
        this.learningRate = learningRate;
        this.lambda = lambda;

        // Initialize weights with small random values
        this.tagWeights = new double[nTags][nFeatures];
        this.transitionWeights = new double[nTags][nTags];

        // Xavier-like initialization
        double tagWeightScale = Math.sqrt(2.0 / (nFeatures + nTags));
        double transWeightScale = Math.sqrt(2.0 / (nTags + nTags));

        java.util.Random random = new java.util.Random(42);
        for (int i = 0; i < nTags; i++) {
            for (int j = 0; j < nFeatures; j++) {
                tagWeights[i][j] = (random.nextDouble() * 2 - 1) * tagWeightScale;
            }
            for (int j = 0; j < nTags; j++) {
                transitionWeights[i][j] = (random.nextDouble() * 2 - 1) * transWeightScale;
            }
        }
    }

    /**
     * Set the feature extractor function.
     *
     * @param extractor Function that takes (featureVector, position) and returns feature array
     */
    public void setFeatureExtractor(BiFunction<double[], Integer, double[]> extractor) {
        this.featureExtractor = extractor;
    }

    /**
     * Compute the feature vector for a position using the feature extractor.
     *
     * @param features Full feature matrix [sequenceLength, featureDim]
     * @param position Position in the sequence
     * @return Feature vector at the position
     */
    private double[] getFeatureVector(NdArray features, int position) {
        if (featureExtractor != null) {
            double[] obsFeature = extractRawFeature(features, position);
            return featureExtractor.apply(obsFeature, position);
        }
        return extractRawFeature(features, position);
    }

    /**
     * Extract raw features from NdArray at a position.
     */
    private double[] extractRawFeature(NdArray features, int position) {
        int featureDim = features.getShape().get(1);
        double[] result = new double[featureDim];
        for (int j = 0; j < featureDim; j++) {
            Object val = features.get(position, j);
            result[j] = val instanceof Number ? ((Number) val).doubleValue() : 0.0;
        }
        return result;
    }

    /**
     * Compute the unnormalized score for a tag sequence.
     *
     * @param features Feature matrix [sequenceLength, nFeatures]
     * @param tags     Tag sequence
     * @return Unnormalized score
     */
    private double computeUnnormalizedScore(NdArray features, int[] tags) {
        int T = tags.length;
        double score = 0.0;

        // State scores
        for (int t = 0; t < T; t++) {
            int tag = tags[t];
            double[] feat = getFeatureVector(features, t);
            for (int k = 0; k < nFeatures; k++) {
                score += tagWeights[tag][k] * feat[k];
            }
        }

        // Transition scores
        for (int t = 1; t < T; t++) {
            int prevTag = tags[t - 1];
            int currTag = tags[t];
            score += transitionWeights[prevTag][currTag];
        }

        return score;
    }

    /**
     * Forward algorithm to compute partition function Z(x).
     * Uses dynamic programming to compute sum of all possible tag sequences.
     *
     * @param features Feature matrix [sequenceLength, nFeatures]
     * @return Partition function value
     */
    private double computePartitionFunction(NdArray features) {
        int T = features.getShape().get(0);

        // alpha[t][s] = log sum of scores of all paths ending in tag s at position t
        double[][] alpha = new double[T][nTags];

        // Initialize at t=0
        double[] feat0 = getFeatureVector(features, 0);
        for (int s = 0; s < nTags; s++) {
            double score = 0;
            for (int k = 0; k < nFeatures; k++) {
                score += tagWeights[s][k] * feat0[k];
            }
            alpha[0][s] = score;
        }

        // Forward pass
        for (int t = 1; t < T; t++) {
            double[] feat = getFeatureVector(features, t);
            for (int currTag = 0; currTag < nTags; currTag++) {
                double maxScore = Double.NEGATIVE_INFINITY;
                for (int prevTag = 0; prevTag < nTags; prevTag++) {
                    double score = alpha[t - 1][prevTag] + transitionWeights[prevTag][currTag];
                    if (score > maxScore) {
                        maxScore = score;
                    }
                }
                // Add state score
                double stateScore = 0;
                for (int k = 0; k < nFeatures; k++) {
                    stateScore += tagWeights[currTag][k] * feat[k];
                }
                alpha[t][currTag] = maxScore + stateScore;
            }
        }

        // Log-sum-exp at final position
        double maxFinal = Double.NEGATIVE_INFINITY;
        for (int s = 0; s < nTags; s++) {
            if (alpha[T - 1][s] > maxFinal) {
                maxFinal = alpha[T - 1][s];
            }
        }

        double sum = 0;
        for (int s = 0; s < nTags; s++) {
            sum += Math.exp(alpha[T - 1][s] - maxFinal);
        }

        return maxFinal + Math.log(sum);
    }

    /**
     * Compute the conditional log-likelihood (pseudo-log-likelihood).
     *
     * @param features Feature matrix [sequenceLength, nFeatures]
     * @param labels   True tag sequence
     * @return Negative log-likelihood
     */
    public double score(NdArray features, int[] labels) {
        // Score = log P(y|x) = score(x,y) - Z(x)
        double unnormScore = computeUnnormalizedScore(features, labels);
        double partition = computePartitionFunction(features);
        return unnormScore - partition;
    }

    /**
     * Compute gradient of the conditional log-likelihood.
     *
     * @param features Feature matrix [sequenceLength, nFeatures]
     * @param labels   True tag sequence
     * @return Tuple of (tagGradient, transitionGradient)
     */
    private Gradient computeGradient(NdArray features, int[] labels) {
        int T = labels.length;

        double[][] tagGradient = new double[nTags][nFeatures];
        double[][] transitionGradient = new double[nTags][nTags];

        // Expected counts (under current model)
        double[][] expectedTagCounts = new double[nTags][nFeatures];
        double[][] expectedTransCounts = new double[nTags][nTags];

        // Forward pass
        double[][] alpha = new double[T][nTags];
        double[] feat0 = getFeatureVector(features, 0);
        for (int s = 0; s < nTags; s++) {
            double score = 0;
            for (int k = 0; k < nFeatures; k++) {
                score += tagWeights[s][k] * feat0[k];
            }
            alpha[0][s] = score;
        }

        for (int t = 1; t < T; t++) {
            double[] feat = getFeatureVector(features, t);
            for (int currTag = 0; currTag < nTags; currTag++) {
                double maxScore = Double.NEGATIVE_INFINITY;
                for (int prevTag = 0; prevTag < nTags; prevTag++) {
                    double score = alpha[t - 1][prevTag] + transitionWeights[prevTag][currTag];
                    if (score > maxScore) {
                        maxScore = score;
                    }
                }
                double stateScore = 0;
                for (int k = 0; k < nFeatures; k++) {
                    stateScore += tagWeights[currTag][k] * feat[k];
                }
                alpha[t][currTag] = maxScore + stateScore;
            }
        }

        // Backward pass
        double[][] beta = new double[T][nTags];
        for (int s = 0; s < nTags; s++) {
            beta[T - 1][s] = 0;
        }

        for (int t = T - 2; t >= 0; t--) {
            double[] feat = getFeatureVector(features, t + 1);
            for (int prevTag = 0; prevTag < nTags; prevTag++) {
                double maxScore = Double.NEGATIVE_INFINITY;
                for (int currTag = 0; currTag < nTags; currTag++) {
                    double stateScore = 0;
                    for (int k = 0; k < nFeatures; k++) {
                        stateScore += tagWeights[currTag][k] * feat[k];
                    }
                    double score = transitionWeights[prevTag][currTag] + stateScore + beta[t + 1][currTag];
                    if (score > maxScore) {
                        maxScore = score;
                    }
                }
                beta[t][prevTag] = maxScore;
            }
        }

        // Compute expected counts
        // For each position, compute probability of being in each tag
        for (int t = 0; t < T; t++) {
            double[] feat = getFeatureVector(features, t);
            double maxAlphaBeta = Double.NEGATIVE_INFINITY;
            for (int s = 0; s < nTags; s++) {
                double val = alpha[t][s] + beta[t][s];
                if (val > maxAlphaBeta) {
                    maxAlphaBeta = val;
                }
            }

            double sum = 0;
            for (int s = 0; s < nTags; s++) {
                double val = alpha[t][s] + beta[t][s] - maxAlphaBeta;
                sum += Math.exp(val);
            }
            double logZ = maxAlphaBeta + Math.log(sum + 1e-300);

            for (int s = 0; s < nTags; s++) {
                double prob = Math.exp(alpha[t][s] + beta[t][s] - logZ);
                for (int k = 0; k < nFeatures; k++) {
                    expectedTagCounts[s][k] += prob * feat[k];
                }
            }
        }

        // Expected transition counts
        for (int t = 0; t < T - 1; t++) {
            double[] feat = getFeatureVector(features, t + 1);
            double maxAlphaBeta = Double.NEGATIVE_INFINITY;
            for (int prevTag = 0; prevTag < nTags; prevTag++) {
                for (int currTag = 0; currTag < nTags; currTag++) {
                    double val = alpha[t][prevTag] + transitionWeights[prevTag][currTag];
                    for (int k = 0; k < nFeatures; k++) {
                        val += tagWeights[currTag][k] * feat[k];
                    }
                    val += beta[t + 1][currTag];
                    if (val > maxAlphaBeta) {
                        maxAlphaBeta = val;
                    }
                }
            }

            double sum = 0;
            for (int prevTag = 0; prevTag < nTags; prevTag++) {
                for (int currTag = 0; currTag < nTags; currTag++) {
                    double val = alpha[t][prevTag] + transitionWeights[prevTag][currTag];
                    for (int k = 0; k < nFeatures; k++) {
                        val += tagWeights[currTag][k] * feat[k];
                    }
                    val += beta[t + 1][currTag] - maxAlphaBeta;
                    sum += Math.exp(val);
                }
            }
            double logZ = maxAlphaBeta + Math.log(sum + 1e-300);

            for (int prevTag = 0; prevTag < nTags; prevTag++) {
                for (int currTag = 0; currTag < nTags; currTag++) {
                    double val = alpha[t][prevTag] + transitionWeights[prevTag][currTag];
                    for (int k = 0; k < nFeatures; k++) {
                        val += tagWeights[currTag][k] * feat[k];
                    }
                    val += beta[t + 1][currTag] - logZ;
                    double prob = Math.exp(val);
                    expectedTransCounts[prevTag][currTag] += prob;
                }
            }
        }

        // Observed counts (from true labels)
        // For gradient: we want to maximize log P(y|x)
        // gradient = observed_features - expected_features (for each tag)

        // Observed tag features
        for (int t = 0; t < T; t++) {
            int tag = labels[t];
            double[] feat = getFeatureVector(features, t);
            for (int k = 0; k < nFeatures; k++) {
                tagGradient[tag][k] += feat[k];
            }
        }

        // Observed transitions
        for (int t = 0; t < T - 1; t++) {
            int prevTag = labels[t];
            int currTag = labels[t + 1];
            transitionGradient[prevTag][currTag] += 1.0;
        }

        // Subtract expected counts
        for (int s = 0; s < nTags; s++) {
            for (int k = 0; k < nFeatures; k++) {
                tagGradient[s][k] -= expectedTagCounts[s][k];
            }
            for (int s2 = 0; s2 < nTags; s2++) {
                transitionGradient[s][s2] -= expectedTransCounts[s][s2];
            }
        }

        return new Gradient(tagGradient, transitionGradient);
    }

    /**
     * Fit the CRF model using SGD (Stochastic Gradient Descent).
     *
     * @param features    Feature matrix of shape [sequenceLength, nFeatures]
     * @param labels      Tag sequence of length sequenceLength
     * @param nIterations Number of training iterations
     */
    public void fit(NdArray features, int[] labels, int nIterations) {
        if (features.getShape().get(0) != labels.length) {
            throw new IllegalArgumentException("Feature sequence length must match labels length");
        }

        for (int iter = 0; iter < nIterations; iter++) {
            // Compute gradient
            Gradient grad = computeGradient(features, labels);

            // Update tag weights with L2 regularization and gradient descent
            for (int s = 0; s < nTags; s++) {
                for (int k = 0; k < nFeatures; k++) {
                    // Gradient descent with L2 regularization
                    // w = w + lr * (gradient - lambda * w)
                    tagWeights[s][k] += learningRate * (grad.tagGradient[s][k] - lambda * tagWeights[s][k]);
                }
            }

            // Update transition weights
            for (int s = 0; s < nTags; s++) {
                for (int s2 = 0; s2 < nTags; s2++) {
                    transitionWeights[s][s2] += learningRate * (grad.transitionGradient[s][s2] - lambda * transitionWeights[s][s2]);
                }
            }

            // Print progress occasionally
            if ((iter + 1) % 100 == 0 || iter == 0) {
                double ll = score(features, labels);
                System.out.println("Iteration " + (iter + 1) + ", Log-likelihood: " + ll);
            }
        }
    }

    /**
     * Predict the best tag sequence using Viterbi decoding.
     *
     * @param features Feature matrix [sequenceLength, nFeatures]
     * @return Best tag sequence
     */
    public int[] predict(NdArray features) {
        int T = features.getShape().get(0);

        // Viterbi algorithm
        double[][] delta = new double[T][nTags];
        int[][] psi = new int[T][nTags];

        // Initialize
        double[] feat0 = getFeatureVector(features, 0);
        for (int s = 0; s < nTags; s++) {
            double score = 0;
            for (int k = 0; k < nFeatures; k++) {
                score += tagWeights[s][k] * feat0[k];
            }
            delta[0][s] = score;
            psi[0][s] = 0;
        }

        // Recursion
        for (int t = 1; t < T; t++) {
            double[] feat = getFeatureVector(features, t);
            for (int currTag = 0; currTag < nTags; currTag++) {
                double maxProb = Double.NEGATIVE_INFINITY;
                int maxPrevTag = 0;

                for (int prevTag = 0; prevTag < nTags; prevTag++) {
                    double prob = delta[t - 1][prevTag] + transitionWeights[prevTag][currTag];
                    if (prob > maxProb) {
                        maxProb = prob;
                        maxPrevTag = prevTag;
                    }
                }

                double stateScore = 0;
                for (int k = 0; k < nFeatures; k++) {
                    stateScore += tagWeights[currTag][k] * feat[k];
                }

                delta[t][currTag] = maxProb + stateScore;
                psi[t][currTag] = maxPrevTag;
            }
        }

        // Backtrack
        int[] bestPath = new int[T];
        double maxFinal = Double.NEGATIVE_INFINITY;
        int maxFinalTag = 0;
        for (int s = 0; s < nTags; s++) {
            if (delta[T - 1][s] > maxFinal) {
                maxFinal = delta[T - 1][s];
                maxFinalTag = s;
            }
        }
        bestPath[T - 1] = maxFinalTag;

        for (int t = T - 2; t >= 0; t--) {
            bestPath[t] = psi[t + 1][bestPath[t + 1]];
        }

        return bestPath;
    }

    /**
     * Get tag weights.
     *
     * @return Copy of tag weights
     */
    public double[][] getTagWeights() {
        double[][] copy = new double[nTags][nFeatures];
        for (int i = 0; i < nTags; i++) {
            copy[i] = tagWeights[i].clone();
        }
        return copy;
    }

    /**
     * Get transition weights.
     *
     * @return Copy of transition weights
     */
    public double[][] getTransitionWeights() {
        double[][] copy = new double[nTags][nTags];
        for (int i = 0; i < nTags; i++) {
            copy[i] = transitionWeights[i].clone();
        }
        return copy;
    }

    /**
     * Get number of tags.
     *
     * @return Number of tags
     */
    public int getNTags() {
        return nTags;
    }

    /**
     * Get feature dimension.
     *
     * @return Number of features
     */
    public int getNFeatures() {
        return nFeatures;
    }

    /**
     * Gradient container.
     */
    private static class Gradient {
        double[][] tagGradient;
        double[][] transitionGradient;

        Gradient(double[][] tagGradient, double[][] transitionGradient) {
            this.tagGradient = tagGradient;
            this.transitionGradient = transitionGradient;
        }
    }
}
