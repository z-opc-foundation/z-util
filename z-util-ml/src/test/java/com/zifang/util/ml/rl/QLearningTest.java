package com.zifang.util.ml.rl;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * QLearningTest类。
 */
public class QLearningTest {

    @Test
    /**
     * testQLearningConverges方法。
     */
    public void testQLearningConverges() {
        // Simple grid world: 2 states, 2 actions
        // State 0 -> State 1 with reward 1, State 1 is terminal
        QLearning.State startState = new SimpleState(0);

        QLearning agent = new QLearning(0.5, 0.9, 0.1, 100);
        agent.learn(startState);

        // After learning, Q-values should reflect learned rewards
        double q0a0 = agent.getQValue(startState, 0);
        double q0a1 = agent.getQValue(startState, 1);

        // At least one Q-value should be non-default (0)
        assertTrue(q0a0 != 0.0 || q0a1 != 0.0, "Q-values should have been updated");
    }

    @Test
    /**
     * testQLearningUpdate方法。
     */
    public void testQLearningUpdate() {
        // Test that TD update changes Q-values
        QLearning agent = new QLearning(0.5, 0.9, 0.0, 1);

        SimpleState s0 = new SimpleState(0);
        SimpleState s1 = new SimpleState(1); // terminal state

        double initialQ = agent.getQValue(s0, 0);

        // Update with reward
        agent.update(s0, 0, 1.0, s1);

        double updatedQ = agent.getQValue(s0, 0);

        // Q-value should have changed after update
        assertNotEquals(initialQ, updatedQ, "Q-value should change after TD update");
    }

    @Test
    /**
     * testQLearningBestAction方法。
     */
    public void testQLearningBestAction() {
        QLearning agent = new QLearning(0.5, 0.9, 0.0, 1);

        SimpleState state = new SimpleState(0);

        // Get best action - should not throw
        Object bestAction = agent.getBestAction(state);
        assertNotNull(bestAction);
    }

    // Simple State implementation for testing
    private static class SimpleState implements QLearning.State {
        private final int id;

        SimpleState(int id) {
            this.id = id;
        }

        @Override
        /**
         * getState方法。
         * @return Object类型返回值
         */
        public Object getState() {
            return id;
        }

        @Override
        /**
         * getAvailableActions方法。
         * @return List<QLearning.ActionResult>类型返回值
         */
        public List<QLearning.ActionResult> getAvailableActions() {
            List<QLearning.ActionResult> actions = new ArrayList<>();
            // SimpleState 0 -> state 1 with reward 1
            // SimpleState 1 -> terminal (no actions)
            if (id == 0) {
                actions.add(new SimpleActionResult(0, new SimpleState(1), 1.0, false));
                actions.add(new SimpleActionResult(1, new SimpleState(1), 0.0, false));
            }
            return actions;
        }
    }

    // Simple ActionResult implementation for testing
    private static class SimpleActionResult implements QLearning.ActionResult {
        private final Object action;
        private final QLearning.State nextState;
        private final double reward;
        private final boolean terminal;

        SimpleActionResult(Object action, QLearning.State nextState, double reward, boolean terminal) {
            this.action = action;
            this.nextState = nextState;
            this.reward = reward;
            this.terminal = terminal;
        }

        @Override
        /**
         * getAction方法。
         * @return Object类型返回值
         */
        public Object getAction() {
            return action;
        }

        @Override
        /**
         * getNextState方法。
         * @return QLearning.State类型返回值
         */
        public QLearning.State getNextState() {
            return nextState;
        }

        @Override
        /**
         * getReward方法。
         * @return double类型返回值
         */
        public double getReward() {
            return reward;
        }

        @Override
        /**
         * isTerminal方法。
         * @return boolean类型返回值
         */
        public boolean isTerminal() {
            return terminal;
        }
    }
}
