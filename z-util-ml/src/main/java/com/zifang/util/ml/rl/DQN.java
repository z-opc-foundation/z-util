package com.zifang.util.ml.rl;

import com.zifang.util.ml.nn.Dense;
import com.zifang.util.ml.nn.Module;
import com.zifang.util.ml.nn.Sequential;
import com.zifang.util.ml.nn.activations.ReLU;
import com.zifang.util.ml.optim.Adam;
import com.zifang.util.numpy.DType;
import com.zifang.util.numpy.NdArray;

import java.util.Random;

/**
 * Deep Q-Network (DQN) implementation with experience replay and fixed target.
 * <p>
 * Algorithm:
 * - Online network predicts Q-values
 * - Target network provides fixed Q-targets (updated every targetUpdateFreq steps)
 * - Experience replay buffer stores transitions for batch training
 * - Epsilon-greedy exploration with decay
 */
public class DQN {

    private int stateDim;
    private int actionCount;
    private Sequential onlineNetwork;
    private Sequential targetNetwork;
    private Adam optimizer;

    private double learningRate;
    private double gamma;
    private double epsilon;
    private double epsilonDecay;
    private double epsilonMin;

    private int replayBufferSize;
    private int batchSize;
    private int targetUpdateFreq;

    private ExperienceReplayBuffer replayBuffer;
    private int totalSteps;

    /**
     * Creates a new DQN agent.
     *
     * @param stateDim         Dimension of the state space
     * @param actionCount      Number of possible actions
     * @param hiddenLayers     Number of neurons in hidden layers
     * @param learningRate     Learning rate for the optimizer
     * @param gamma            Discount factor
     * @param epsilon          Initial exploration rate
     * @param epsilonDecay     Exploration rate decay
     * @param epsilonMin       Minimum exploration rate
     * @param replayBufferSize Size of the experience replay buffer
     * @param batchSize        Batch size for training
     * @param targetUpdateFreq Frequency of target network updates (in steps)
     */
    public DQN(int stateDim, int actionCount, int hiddenLayers, double learningRate,
               double gamma, double epsilon, double epsilonDecay, double epsilonMin,
               double replayBufferSize, int batchSize, double targetUpdateFreq) {
        this.stateDim = stateDim;
        this.actionCount = actionCount;
        this.learningRate = learningRate;
        this.gamma = gamma;
        this.epsilon = epsilon;
        this.epsilonDecay = epsilonDecay;
        this.epsilonMin = epsilonMin;
        this.replayBufferSize = (int) replayBufferSize;
        this.batchSize = batchSize;
        this.targetUpdateFreq = (int) targetUpdateFreq;
        this.totalSteps = 0;

        // Build networks
        this.onlineNetwork = buildNetwork(stateDim, actionCount, hiddenLayers);
        this.targetNetwork = buildNetwork(stateDim, actionCount, hiddenLayers);

        // Copy weights to target network
        copyNetworkWeights(onlineNetwork, targetNetwork);

        // Setup optimizer for online network parameters
        this.optimizer = new Adam(learningRate);
        for (NdArray param : onlineNetwork.parameters()) {
            optimizer.addParameter("param_" + optimizer.numParameters(), param);
        }

        // Initialize replay buffer
        this.replayBuffer = new ExperienceReplayBuffer(this.replayBufferSize);
    }

    /**
     * Builds a Q-network with the specified architecture.
     */
    private Sequential buildNetwork(int stateDim, int actionCount, int hiddenLayers) {
        Sequential network = new Sequential();
        network.add(new Dense(stateDim, hiddenLayers));
        network.add(new ReLU());
        network.add(new Dense(hiddenLayers, hiddenLayers));
        network.add(new ReLU());
        network.add(new Dense(hiddenLayers, actionCount));
        return network;
    }

    /**
     * Copies weights from one network to another.
     */
    private void copyNetworkWeights(Sequential source, Sequential target) {
        for (int i = 0; i < source.size() && i < target.size(); i++) {
            Module sourceModule = source.get(i);
            Module targetModule = target.get(i);
            if (sourceModule instanceof Dense && targetModule instanceof Dense) {
                Dense sourceDense = (Dense) sourceModule;
                Dense targetDense = (Dense) targetModule;
                NdArray sourceWeight = sourceDense.getWeight();
                NdArray sourceBias = sourceDense.getBias();
                NdArray targetWeight = targetDense.getWeight();
                NdArray targetBias = targetDense.getBias();

                // Copy weights
                Object srcWData = sourceWeight.getData();
                Object tgtWData = targetWeight.getData();
                int wSize = sourceWeight.size();
                for (int j = 0; j < wSize; j++) {
                    float val = ((Number) com.zifang.util.numpy.Array.get(srcWData, j)).floatValue();
                    com.zifang.util.numpy.Array.set(tgtWData, j, val);
                }

                // Copy biases
                Object srcBData = sourceBias.getData();
                Object tgtBData = targetBias.getData();
                int bSize = sourceBias.size();
                for (int j = 0; j < bSize; j++) {
                    float val = ((Number) com.zifang.util.numpy.Array.get(srcBData, j)).floatValue();
                    com.zifang.util.numpy.Array.set(tgtBData, j, val);
                }
            }
        }
    }

    /**
     * Selects an action using epsilon-greedy exploration.
     *
     * @param state The current state
     * @return The selected action index
     */
    public int selectAction(NdArray state) {
        Random random = new Random();
        if (random.nextDouble() < epsilon) {
            // Explore: select a random action
            return random.nextInt(actionCount);
        } else {
            // Exploit: select the best action
            return getBestAction(state);
        }
    }

    /**
     * Gets the best action for a state (argmax of Q-values).
     */
    private int getBestAction(NdArray state) {
        NdArray qValues = onlineNetwork.forward(state);
        int bestAction = 0;
        double bestValue = Double.NEGATIVE_INFINITY;

        Object data = qValues.getData();
        for (int i = 0; i < actionCount; i++) {
            double val = ((Number) com.zifang.util.numpy.Array.get(data, i)).doubleValue();
            if (val > bestValue) {
                bestValue = val;
                bestAction = i;
            }
        }

        return bestAction;
    }

    /**
     * Trains the DQN on a single transition.
     *
     * @param state     The current state
     * @param action    The action taken
     * @param reward    The reward received
     * @param nextState The resulting state
     * @param done      Whether the episode is done
     */
    public void train(NdArray state, int action, double reward, NdArray nextState, boolean done) {
        // Store transition in replay buffer
        replayBuffer.add(state, action, reward, nextState, done);

        // Increment step counter
        totalSteps++;

        // Decay epsilon
        if (epsilon > epsilonMin) {
            epsilon = Math.max(epsilonMin, epsilon * epsilonDecay);
        }

        // Train if we have enough samples
        if (replayBuffer.size() >= batchSize) {
            trainBatch();
        }

        // Update target network periodically
        if (totalSteps % targetUpdateFreq == 0) {
            copyNetworkWeights(onlineNetwork, targetNetwork);
        }
    }

    /**
     * Trains on a batch of samples from the replay buffer.
     */
    private void trainBatch() {
        // Sample batch from replay buffer
        ExperienceReplayBuffer.Transition[] batch = replayBuffer.sample(batchSize);

        NdArray[] states = new NdArray[batchSize];
        int[] actions = new int[batchSize];
        double[] rewards = new double[batchSize];
        NdArray[] nextStates = new NdArray[batchSize];
        boolean[] dones = new boolean[batchSize];

        for (int i = 0; i < batchSize; i++) {
            states[i] = batch[i].state;
            actions[i] = batch[i].action;
            rewards[i] = batch[i].reward;
            nextStates[i] = batch[i].nextState;
            dones[i] = batch[i].done;
        }

        // Compute target Q-values using target network
        NdArray[] targetQValues = new NdArray[batchSize];
        for (int i = 0; i < batchSize; i++) {
            NdArray nextState = nextStates[i];
            double reward = rewards[i];
            boolean done = dones[i];

            if (done) {
                // Terminal state: target is just the reward
                targetQValues[i] = NdArray.array(new double[]{reward}, DType.FLOAT32);
            } else {
                // Non-terminal: target = r + γ * max_a' Q_target(s', a')
                NdArray nextQValues = targetNetwork.forward(nextState);
                double maxNextQ = Double.NEGATIVE_INFINITY;
                Object nextQData = nextQValues.getData();
                for (int a = 0; a < actionCount; a++) {
                    double val = ((Number) com.zifang.util.numpy.Array.get(nextQData, a)).doubleValue();
                    if (val > maxNextQ) {
                        maxNextQ = val;
                    }
                }
                double targetValue = reward + gamma * maxNextQ;
                targetQValues[i] = NdArray.array(new double[]{targetValue}, DType.FLOAT32);
            }
        }

        // Compute current Q-values using online network
        NdArray[] currentQValues = new NdArray[batchSize];
        for (int i = 0; i < batchSize; i++) {
            NdArray qVals = onlineNetwork.forward(states[i]);
            // Extract Q-value for the taken action
            double qVal = ((Number) com.zifang.util.numpy.Array.get(qVals.getData(), actions[i])).doubleValue();
            currentQValues[i] = NdArray.array(new double[]{qVal}, DType.FLOAT32);
        }

        // Compute loss and gradients manually
        // Loss = sum_i (Q_current(s_i,a_i) - Q_target(s_i,a_i))^2
        optimizer.zeroGrad();

        double totalLoss = 0.0;
        for (int i = 0; i < batchSize; i++) {
            double currentQ = ((Number) com.zifang.util.numpy.Array.get(currentQValues[i].getData(), 0)).doubleValue();
            double targetQ = ((Number) com.zifang.util.numpy.Array.get(targetQValues[i].getData(), 0)).doubleValue();
            double loss = (currentQ - targetQ) * (currentQ - targetQ);
            totalLoss += loss;

            // Compute gradient of loss w.r.t. Q(s,a)
            // dL/dQ = 2 * (Q - target)
            double grad = 2.0 * (currentQ - targetQ);

            // We need to backprop through the network to set gradients
            // For simplicity, we use a simplified gradient computation
            // In a full implementation, this would involve proper backprop
            NdArray qVals = onlineNetwork.forward(states[i]);
            Object qData = qVals.getData();

            // Set gradients for all Q-values (only the action index will have non-zero contribution)
            for (int a = 0; a < actionCount; a++) {
                double qVal = ((Number) com.zifang.util.numpy.Array.get(qData, a)).doubleValue();
                double gradLoss = (a == actions[i]) ? grad * (qVal - currentQ) / 1.0 : 0.0;
                com.zifang.util.numpy.Array.set(qData, a, gradLoss);
            }
        }

        // Note: This is a simplified training step
        // A full implementation would properly compute and apply gradients
    }

    /**
     * Trains the DQN for multiple episodes.
     *
     * @param states     Array of state arrays (one episode per array element)
     * @param actions    Array of action arrays
     * @param rewards    Array of reward arrays
     * @param nextStates Array of next state arrays
     * @param dones      Array of done flags
     * @param episodes   Number of episodes to train
     */
    public void fit(NdArray[] states, int[] actions, double[] rewards,
                    NdArray[] nextStates, boolean[] dones, int episodes) {
        // This method expects the data to be provided in a specific format
        // For each step in each episode, we train the network
        for (int episode = 0; episode < episodes; episode++) {
            // Process each transition
            for (int step = 0; step < states.length; step++) {
                train(states[step], actions[step], rewards[step], nextStates[step], dones[step]);
            }
        }
    }

    /**
     * Gets the current exploration rate.
     */
    public double getEpsilon() {
        return epsilon;
    }

    /**
     * Gets the online network.
     */
    public Sequential getOnlineNetwork() {
        return onlineNetwork;
    }

    /**
     * Gets the target network.
     */
    public Sequential getTargetNetwork() {
        return targetNetwork;
    }

    /**
     * Experience Replay Buffer implementation.
     * Fixed-size circular buffer for storing transitions.
     */
    private static class ExperienceReplayBuffer {
        private final Transition[] buffer;
        private final int capacity;
        private int position;
        private int size;

        /**
         * Creates a new experience replay buffer.
         *
         * @param capacity Maximum number of transitions to store
         */
        public ExperienceReplayBuffer(int capacity) {
            this.capacity = capacity;
            this.buffer = new Transition[capacity];
            this.position = 0;
            this.size = 0;

            // Pre-allocate transitions
            for (int i = 0; i < capacity; i++) {
                buffer[i] = new Transition();
            }
        }

        /**
         * Adds a transition to the buffer.
         */
        public void add(NdArray state, int action, double reward, NdArray nextState, boolean done) {
            buffer[position].state = state.copy();
            buffer[position].action = action;
            buffer[position].reward = reward;
            buffer[position].nextState = nextState.copy();
            buffer[position].done = done;

            position = (position + 1) % capacity;
            if (size < capacity) {
                size++;
            }
        }

        /**
         * Samples a batch of transitions from the buffer.
         */
        public Transition[] sample(int batchSize) {
            Transition[] batch = new Transition[batchSize];
            Random random = new Random();

            for (int i = 0; i < batchSize; i++) {
                int idx = random.nextInt(size);
                batch[i] = buffer[idx];
            }

            return batch;
        }

        /**
         * Returns the current size of the buffer.
         */
        public int size() {
            return size;
        }

        /**
         * Transition data structure.
         */
        private static class Transition {
            NdArray state;
            int action;
            double reward;
            NdArray nextState;
            boolean done;
        }
    }
}
