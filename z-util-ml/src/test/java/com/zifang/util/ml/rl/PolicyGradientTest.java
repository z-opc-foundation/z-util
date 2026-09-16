package com.zifang.util.ml.rl;

import com.zifang.util.numpy.DType;
import com.zifang.util.numpy.NdArray;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * PolicyGradientTest类。
 */
public class PolicyGradientTest {

    @Test
    /**
     * testPolicyGradientUpdate方法。
     */
    public void testPolicyGradientUpdate() {
        PolicyGradient agent = new PolicyGradient(4, 3, 16, 0.001, 0.99);

        // Simple trajectory
        NdArray[] states = new NdArray[3];
        int[] actions = new int[]{1, 2, 0};
        double[] rewards = new double[]{1.0, 0.5, 0.1};

        for (int i = 0; i < states.length; i++) {
            states[i] = NdArray.array(new float[]{1.0f, 2.0f, 3.0f, 4.0f}, DType.FLOAT32).reshape(1, 4);
        }

        // Update should not throw
        agent.update(states, actions, rewards);

        // Agent should still be functional
        int action = agent.selectAction(states[0]);
        assertTrue(action >= 0 && action < 3);
    }

    @Test
    /**
     * testPolicyGradientSelectAction方法。
     */
    public void testPolicyGradientSelectAction() {
        PolicyGradient agent = new PolicyGradient(4, 3, 16, 0.001, 0.99);

        NdArray state = NdArray.array(new float[]{1.0f, 2.0f, 3.0f, 4.0f}, DType.FLOAT32).reshape(1, 4);

        int action = agent.selectAction(state);
        assertTrue(action >= 0 && action < 3);
    }

    @Test
    /**
     * testPolicyGradientDiscountedRewards方法。
     */
    public void testPolicyGradientDiscountedRewards() {
        double[] rewards = new double[]{1.0, 2.0, 3.0};
        double gamma = 0.9;

        double[] discounted = PolicyGradient.computeDiscountedRewards(rewards, gamma);

        // G_0 = r_0 + gamma * r_1 + gamma^2 * r_2 = 1 + 0.9*2 + 0.81*3 = 1 + 1.8 + 2.43 = 5.23
        assertTrue(discounted.length == 3);
        assertTrue(discounted[0] > discounted[1]);
        assertTrue(discounted[1] > discounted[2]);
    }
}
