package com.zifang.util.ml.sequence;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * HMMTest类。
 */
public class HMMTest {

    @Test
    /**
     * testHMMViterbi方法。
     */
    public void testHMMViterbi() {
        // Simple HMM: 2 states, 3 observations
        HMM hmm = new HMM(2, 3);

        // Train with simple sequence
        int[] observations = new int[]{0, 1, 2, 1, 0};
        int[] states = new int[]{0, 1, 0, 1, 0};

        hmm.fit(observations, states, 1);

        // Predict optimal state sequence
        int[] predictedStates = hmm.predict(observations);

        assertNotNull(predictedStates);
        assertEquals(observations.length, predictedStates.length);

        // All states should be valid (0 or 1)
        for (int state : predictedStates) {
            assertTrue(state >= 0 && state < 2);
        }
    }

    @Test
    /**
     * testHMMScore方法。
     */
    public void testHMMScore() {
        HMM hmm = new HMM(2, 3);

        int[] observations = new int[]{0, 1, 2};
        int[] states = new int[]{0, 1, 0};

        hmm.fit(observations, states, 1);

        // Score should return a finite value
        double score = hmm.score(observations);
        assertTrue(Double.isFinite(score), "Score should be finite");
    }

    @Test
    /**
     * testHMMParameters方法。
     */
    public void testHMMParameters() {
        HMM hmm = new HMM(3, 4);

        assertEquals(3, hmm.getNStates());
        assertEquals(4, hmm.getNObservations());

        double[] pi = hmm.getPi();
        assertEquals(3, pi.length);

        double[][] A = hmm.getA();
        assertEquals(3, A.length);
        assertEquals(3, A[0].length);

        double[][] B = hmm.getB();
        assertEquals(3, B.length);
        assertEquals(4, B[0].length);
    }
}
