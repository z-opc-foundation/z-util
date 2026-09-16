package com.zifang.util.ml.rl;

import com.zifang.util.ml.nn.Dense;
import com.zifang.util.ml.nn.Sequential;
import com.zifang.util.ml.nn.activations.ReLU;
import com.zifang.util.ml.optim.Adam;
import com.zifang.util.numpy.DType;
import com.zifang.util.numpy.NdArray;

import java.util.Random;

/**
 * Policy Gradient (REINFORCE) algorithm implementation.
 * <p>
 * Algorithm:
 * - π(a|s;θ) = softmax over learned Q-values (policy network)
 * - Loss = -sum_t [log π(a_t|s_t) * G_t] where G_t = sum_{t'=t}^T γ^{t'-t} * r_{t'}
 * - Uses neural network to approximate the policy
 * <p>
 * This is a Monte Carlo policy gradient method that updates parameters
 * based on complete episode trajectories.
 */
public class PolicyGradient {

    private int stateDim;
    private int actionCount;
    private Sequential policyNetwork;
    private Adam optimizer;

    private double learningRate;
    private double gamma;

    /**
     * Creates a new Policy Gradient (REINFORCE) agent.
     *
     * @param stateDim     Dimension of the state space
     * @param actionCount  Number of possible actions
     * @param hiddenLayers Number of neurons in hidden layers
     * @param learningRate Learning rate for the optimizer
     * @param gamma        Discount factor
     */
    public PolicyGradient(int stateDim, int actionCount, int hiddenLayers,
                          double learningRate, double gamma) {
        this.stateDim = stateDim;
        this.actionCount = actionCount;
        this.learningRate = learningRate;
        this.gamma = gamma;

        // Build policy network: state -> Q-values -> softmax -> action probabilities
        this.policyNetwork = buildPolicyNetwork(stateDim, actionCount, hiddenLayers);

        // Setup optimizer for policy network parameters
        this.optimizer = new Adam(learningRate);
        for (NdArray param : policyNetwork.parameters()) {
            optimizer.addParameter("param_" + optimizer.numParameters(), param);
        }
    }

    /**
     * Computes discounted rewards from rewards array.
     *
     * @param rewards Array of rewards
     * @return Array of discounted rewards G_t
     */
    public static double[] computeDiscountedRewards(double[] rewards, double gamma) {
        int length = rewards.length;
        double[] discountedRewards = new double[length];
        double cumulative = 0.0;

        for (int t = length - 1; t >= 0; t--) {
            cumulative = rewards[t] + gamma * cumulative;
            discountedRewards[t] = cumulative;
        }

        return discountedRewards;
    }

    /**
     * Builds the policy network.
     * Architecture: state -> Dense -> ReLU -> Dense -> ReLU -> Dense -> actionScores -> softmax
     */
    private Sequential buildPolicyNetwork(int stateDim, int actionCount, int hiddenLayers) {
        Sequential network = new Sequential();
        network.add(new Dense(stateDim, hiddenLayers));
        network.add(new ReLU());
        network.add(new Dense(hiddenLayers, hiddenLayers));
        network.add(new ReLU());
        network.add(new Dense(hiddenLayers, actionCount));
        // Softmax is applied during action selection for numerical stability
        return network;
    }

    /**
     * Selects an action by sampling from the policy distribution.
     *
     * @param state The current state
     * @return The sampled action index
     */
    public int selectAction(NdArray state) {
        // Get action scores from network
        NdArray actionScores = policyNetwork.forward(state);

        // Apply softmax to get probabilities
        NdArray actionProbs = softmax(actionScores);

        // Sample from the distribution
        Random random = new Random();
        double r = random.nextDouble();
        double cumulative = 0.0;

        Object probData = actionProbs.getData();
        for (int i = 0; i < actionCount; i++) {
            double prob = ((Number) com.zifang.util.numpy.Array.get(probData, i)).doubleValue();
            cumulative += prob;
            if (r <= cumulative) {
                return i;
            }
        }

        // Fallback (should not reach here normally)
        return actionCount - 1;
    }

    /**
     * Applies softmax activation to the input.
     */
    private NdArray softmax(NdArray input) {
        int size = input.size();
        NdArray output = NdArray.zeros(input.getShape(), DType.FLOAT32);

        Object inData = input.getData();
        Object outData = output.getData();

        // Find max for numerical stability
        float maxVal = Float.NEGATIVE_INFINITY;
        for (int i = 0; i < size; i++) {
            float val = ((Number) com.zifang.util.numpy.Array.get(inData, i)).floatValue();
            if (val > maxVal) {
                maxVal = val;
            }
        }

        // Compute exp and sum
        float sum = 0.0f;
        float[] expVals = new float[size];
        for (int i = 0; i < size; i++) {
            float val = ((Number) com.zifang.util.numpy.Array.get(inData, i)).floatValue();
            float expVal = (float) Math.exp(val - maxVal);
            expVals[i] = expVal;
            sum += expVal;
        }

        // Normalize
        for (int i = 0; i < size; i++) {
            com.zifang.util.numpy.Array.set(outData, i, expVals[i] / sum);
        }

        return output;
    }

    /**
     * Computes the probability of a specific action given a state.
     */
    private double getActionProbability(NdArray state, int action) {
        NdArray actionScores = policyNetwork.forward(state);
        NdArray actionProbs = softmax(actionScores);

        Object probData = actionProbs.getData();
        return ((Number) com.zifang.util.numpy.Array.get(probData, action)).doubleValue();
    }

    /**
     * Updates the policy network using a trajectory.
     * <p>
     * Uses REINFORCE algorithm:
     * ∇θ J(θ) = E[∇θ log π(a|s;θ) * G_t]
     * where G_t is the discounted return from time t.
     *
     * @param trajectoryStates  Array of states in the trajectory
     * @param trajectoryActions Array of actions taken
     * @param trajectoryRewards Array of rewards received
     */
    public void update(NdArray[] trajectoryStates, int[] trajectoryActions, double[] trajectoryRewards) {
        int trajectoryLength = trajectoryStates.length;

        // Compute discounted returns (G_t) for each time step
        double[] discountedReturns = new double[trajectoryLength];
        double cumulativeReturn = 0.0;

        for (int t = trajectoryLength - 1; t >= 0; t--) {
            cumulativeReturn = trajectoryRewards[t] + gamma * cumulativeReturn;
            discountedReturns[t] = cumulativeReturn;
        }

        // Normalize returns (optional, can help stabilize training)
        double meanReturn = 0.0;
        double sqDiffSum = 0.0;
        for (int t = 0; t < trajectoryLength; t++) {
            meanReturn += discountedReturns[t];
        }
        meanReturn /= trajectoryLength;
        for (int t = 0; t < trajectoryLength; t++) {
            double diff = discountedReturns[t] - meanReturn;
            sqDiffSum += diff * diff;
        }
        double stdReturn = Math.sqrt(sqDiffSum / trajectoryLength);

        // Normalize if standard deviation is not zero
        if (stdReturn > 1e-8) {
            for (int t = 0; t < trajectoryLength; t++) {
                discountedReturns[t] = (discountedReturns[t] - meanReturn) / stdReturn;
            }
        }

        // Compute policy gradient loss
        // Loss = -sum_t [log π(a_t|s_t;θ) * G_t]
        optimizer.zeroGrad();

        double totalLoss = 0.0;
        for (int t = 0; t < trajectoryLength; t++) {
            NdArray state = trajectoryStates[t];
            int action = trajectoryActions[t];
            double return_t = discountedReturns[t];

            // Get action probability
            double actionProb = getActionProbability(state, action);
            actionProb = Math.max(actionProb, 1e-8); // Clamp for numerical stability

            // Compute log probability
            double logProb = Math.log(actionProb);

            // Accumulate loss (negative because we want to maximize)
            totalLoss -= logProb * return_t;
        }

        // Note: This is a simplified policy gradient update.
        // A full implementation would compute gradients through the network
        // and apply them using the optimizer.

        // For demonstration, we perform a basic gradient descent step
        // In practice, you would compute proper gradients via backpropagation
        applyPolicyGradientUpdate(trajectoryStates, trajectoryActions, discountedReturns);
    }

    /**
     * Applies a policy gradient update to the network parameters.
     * <p>
     * This simplified implementation performs a basic update using
     * numerical gradients.
     */
    private void applyPolicyGradientUpdate(NdArray[] states, int[] actions, double[] returns) {
        // Simplified gradient update
        // In a full implementation, this would use proper backpropagation

        double learningRate = this.learningRate;

        for (NdArray param : policyNetwork.parameters()) {
            Object data = param.getData();
            int size = param.size();

            for (int i = 0; i < size; i++) {
                // Approximate gradient using finite differences
                double originalValue = ((Number) com.zifang.util.numpy.Array.get(data, i)).doubleValue();

                // Compute numerical gradient
                double eps = 1e-4;
                com.zifang.util.numpy.Array.set(data, i, originalValue + eps);

                double lossPlus = computeLoss(states, actions, returns);

                com.zifang.util.numpy.Array.set(data, i, originalValue - eps);
                double lossMinus = computeLoss(states, actions, returns);

                double gradient = (lossPlus - lossMinus) / (2 * eps);

                // Restore original value
                com.zifang.util.numpy.Array.set(data, i, originalValue);

                // Apply gradient descent (negative because we minimize loss)
                double newValue = originalValue - learningRate * gradient;
                com.zifang.util.numpy.Array.set(data, i, newValue);
            }
        }
    }

    /**
     * Computes the policy loss for a trajectory.
     */
    private double computeLoss(NdArray[] states, int[] actions, double[] returns) {
        double loss = 0.0;

        for (int t = 0; t < states.length; t++) {
            double actionProb = getActionProbability(states[t], actions[t]);
            actionProb = Math.max(actionProb, 1e-8);
            double logProb = Math.log(actionProb);
            loss -= logProb * returns[t];
        }

        return loss;
    }

    /**
     * Computes discounted rewards for a batch of trajectories.
     *
     * @param states            Array of state arrays (trajectories)
     * @param actions           Array of action arrays
     * @param discountedRewards Array to fill with discounted rewards
     */
    public void fit(NdArray[] states, int[] actions, double[] discountedRewards) {
        // Compute policy gradient update for the batch
        int batchSize = states.length;

        optimizer.zeroGrad();

        double totalLoss = 0.0;
        for (int i = 0; i < batchSize; i++) {
            NdArray state = states[i];
            int action = actions[i];
            double return_t = discountedRewards[i];

            double actionProb = getActionProbability(state, action);
            actionProb = Math.max(actionProb, 1e-8);
            double logProb = Math.log(actionProb);

            totalLoss -= logProb * return_t;
        }

        // Apply simplified gradient update
        applyPolicyGradientUpdate(states, actions, discountedRewards);
    }

    /**
     * Gets the policy network.
     */
    public Sequential getPolicyNetwork() {
        return policyNetwork;
    }

    /**
     * Gets the learning rate.
     */
    public double getLearningRate() {
        return learningRate;
    }

    /**
     * Gets the discount factor.
     */
    public double getGamma() {
        return gamma;
    }
}
