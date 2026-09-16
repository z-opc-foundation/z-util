package com.zifang.util.ml.rl;

import java.util.HashMap;
import java.util.List;
import java.util.Random;

/**
 * Q-Learning: Off-policy TD control algorithm.
 * <p>
 * Q(s,a) = Q(s,a) + α * (r + γ * max_a' Q(s',a') - Q(s,a))
 * <p>
 * Uses epsilon-greedy action selection and a HashMap-based Q-table.
 */
public class QLearning {

    private double learningRate;
    private double gamma;
    private double epsilon;
    private int nEpisodes;
    private HashMap<String, Double> qTable;

    /**
     * Creates a new Q-Learning agent.
     *
     * @param learningRate Learning rate (α)
     * @param gamma        Discount factor (γ)
     * @param epsilon      Exploration rate for epsilon-greedy
     * @param nEpisodes    Number of episodes to train
     */
    public QLearning(double learningRate, double gamma, double epsilon, int nEpisodes) {
        this.learningRate = learningRate;
        this.gamma = gamma;
        this.epsilon = epsilon;
        this.nEpisodes = nEpisodes;
        this.qTable = new HashMap<>();
    }

    /**
     * Generates a unique key for a state-action pair.
     */
    private String stateActionKey(State state, Object action) {
        return stateKey(state) + "|" + action.toString();
    }

    /**
     * Generates a unique key for a state.
     */
    private String stateKey(State s) {
        return s.getState().toString();
    }

    /**
     * Gets the Q-value for a state-action pair.
     *
     * @param state  The current state
     * @param action The action taken
     * @return The Q-value Q(s,a)
     */
    public double getQValue(State state, Object action) {
        String key = stateActionKey(state, action);
        return qTable.getOrDefault(key, 0.0);
    }

    /**
     * Sets the Q-value for a state-action pair.
     */
    private void setQValue(State state, Object action, double value) {
        String key = stateActionKey(state, action);
        qTable.put(key, value);
    }

    /**
     * Gets the best action for a state (argmax over actions).
     *
     * @param state The current state
     * @return The action with the highest Q-value
     */
    public Object getBestAction(State state) {
        List<ActionResult> availableActions = state.getAvailableActions();
        if (availableActions.isEmpty()) {
            throw new IllegalStateException("No available actions for state: " + stateKey(state));
        }

        Object bestAction = null;
        double bestValue = Double.NEGATIVE_INFINITY;

        for (ActionResult ar : availableActions) {
            Object action = ar.getAction();
            double qValue = getQValue(state, action);
            if (qValue > bestValue) {
                bestValue = qValue;
                bestAction = action;
            }
        }

        return bestAction;
    }

    /**
     * Selects an action using epsilon-greedy exploration.
     *
     * @param state The current state
     * @return The selected action
     */
    private Object selectAction(State state) {
        Random random = new Random();
        if (random.nextDouble() < epsilon) {
            // Explore: select a random action
            List<ActionResult> availableActions = state.getAvailableActions();
            return availableActions.get(random.nextInt(availableActions.size())).getAction();
        } else {
            // Exploit: select the best action
            return getBestAction(state);
        }
    }

    /**
     * Updates the Q-value using the TD update rule.
     * <p>
     * Q(s,a) = Q(s,a) + α * (r + γ * max_a' Q(s',a') - Q(s,a))
     *
     * @param state     The current state
     * @param action    The action taken
     * @param reward    The reward received
     * @param nextState The resulting state
     */
    public void update(State state, Object action, double reward, State nextState) {
        double currentQ = getQValue(state, action);

        double maxNextQ = 0.0;
        if (!nextState.getAvailableActions().isEmpty()) {
            maxNextQ = Double.NEGATIVE_INFINITY;
            for (ActionResult ar : nextState.getAvailableActions()) {
                double q = getQValue(nextState, ar.getAction());
                if (q > maxNextQ) {
                    maxNextQ = q;
                }
            }
            // If all Q-values are -infinity (default), maxNextQ stays -infinity
            if (maxNextQ == Double.NEGATIVE_INFINITY) {
                maxNextQ = 0.0;
            }
        }

        // TD update
        double newQ = currentQ + learningRate * (reward + gamma * maxNextQ - currentQ);
        setQValue(state, action, newQ);
    }

    /**
     * Runs the Q-Learning algorithm for the specified number of episodes.
     *
     * @param startState The initial state for each episode
     */
    public void learn(State startState) {
        for (int episode = 0; episode < nEpisodes; episode++) {
            State currentState = startState;

            while (!isTerminal(currentState)) {
                List<ActionResult> availableActions = currentState.getAvailableActions();
                if (availableActions.isEmpty()) {
                    break;
                }

                // Epsilon-greedy action selection
                Object action = selectAction(currentState);

                // Find the ActionResult for the selected action
                ActionResult selectedResult = null;
                for (ActionResult ar : availableActions) {
                    if (ar.getAction().equals(action)) {
                        selectedResult = ar;
                        break;
                    }
                }

                if (selectedResult == null) {
                    break;
                }

                // Get next state and reward
                State nextState = selectedResult.getNextState();
                double reward = selectedResult.getReward();
                boolean terminal = selectedResult.isTerminal();

                // Update Q-value
                update(currentState, action, reward, nextState);

                // Move to next state
                currentState = nextState;

                // Handle terminal state
                if (terminal) {
                    break;
                }
            }
        }
    }

    /**
     * Checks if a state is terminal (no available actions or marked as terminal).
     */
    private boolean isTerminal(State state) {
        if (state.getAvailableActions().isEmpty()) {
            return true;
        }
        for (ActionResult ar : state.getAvailableActions()) {
            if (ar.isTerminal()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets the current Q-table (for inspection).
     */
    public HashMap<String, Double> getQTable() {
        return new HashMap<>(qTable);
    }

    /**
     * Gets the learning rate.
     */
    public double getLearningRate() {
        return learningRate;
    }

    /**
     * Sets the learning rate.
     */
    public void setLearningRate(double learningRate) {
        this.learningRate = learningRate;
    }

    /**
     * Gets the discount factor.
     */
    public double getGamma() {
        return gamma;
    }

    /**
     * Gets the epsilon exploration rate.
     */
    public double getEpsilon() {
        return epsilon;
    }

    /**
     * Sets the epsilon exploration rate.
     */
    public void setEpsilon(double epsilon) {
        this.epsilon = epsilon;
    }

    /**
     * Gets the number of episodes.
     */
    public int getnEpisodes() {
        return nEpisodes;
    }

    /**
     * Interface for states in the RL environment.
     */
    public interface State {
        Object getState();

        List<ActionResult> getAvailableActions();
    }

    /**
     * Interface for action results containing next state, reward, and terminal info.
     */
    public interface ActionResult {
        Object getAction();

        State getNextState();

        double getReward();

        boolean isTerminal();
    }
}
