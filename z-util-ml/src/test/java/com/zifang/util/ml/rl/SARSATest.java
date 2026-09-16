package com.zifang.util.ml.rl;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * SARSATest类。
 */
public class SARSATest {

    @Test
    /**
     * testSARSALearns方法。
     */
    public void testSARSALearns() {
        // Simple grid world: 2 states, 2 actions
        // State 0 -> State 1 with reward 1, State 1 is terminal
        SARSA.State startState = new SimpleState(0);

        SARSA agent = new SARSA(0.5, 0.9, 0.1, 100);
        agent.learn(startState);

        // After learning, Q-values should be updated
        double q0a0 = agent.getQValue(startState, 0);
        double q0a1 = agent.getQValue(startState, 1);

        // At least one Q-value should be non-default
        assertTrue(q0a0 != 0.0 || q0a1 != 0.0, "Q-values should have been updated by SARSA");
    }

    @Test
    /**
     * testSARSABestAction方法。
     */
    public void testSARSABestAction() {
        SARSA agent = new SARSA(0.5, 0.9, 0.0, 1);

        SimpleState state = new SimpleState(0);

        Object bestAction = agent.getBestAction(state);
        assertNotNull(bestAction);
    }

    // Simple State implementation for testing
    private static class SimpleState implements SARSA.State {
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
         * @return List<SARSA.ActionResult>类型返回值
         */
        public List<SARSA.ActionResult> getAvailableActions() {
            List<SARSA.ActionResult> actions = new ArrayList<>();
            if (id == 0) {
                actions.add(new SimpleActionResult(0, new SimpleState(1), 1.0, false));
                actions.add(new SimpleActionResult(1, new SimpleState(1), 0.0, false));
            }
            return actions;
        }
    }

    // Simple ActionResult implementation for testing
    private static class SimpleActionResult implements SARSA.ActionResult {
        private final Object action;
        private final SARSA.State nextState;
        private final double reward;
        private final boolean terminal;

        SimpleActionResult(Object action, SARSA.State nextState, double reward, boolean terminal) {
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
         * @return SARSA.State类型返回值
         */
        public SARSA.State getNextState() {
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
