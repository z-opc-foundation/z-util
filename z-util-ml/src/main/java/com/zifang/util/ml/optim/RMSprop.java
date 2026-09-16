package com.zifang.util.ml.optim;

import com.zifang.util.numpy.DType;
import com.zifang.util.numpy.NdArray;

/**
 * RMSprop optimizer.
 * <p>
 * RMSprop uses a moving average of squared gradients to normalize the gradient,
 * similar to Adagrad but with a decaying average that prevents the learning rate
 * from shrinking too quickly.
 * <p>
 * The update rule:
 * v = rho * v + (1 - rho) * grad^2
 * param = param - lr * grad / (sqrt(v) + eps)
 * <p>
 * Optionally supports momentum:
 * if momentum > 0:
 * m = momentum * m + lr * grad / (sqrt(v) + eps)
 * param = param - m
 */
public class RMSprop extends Optimizer {

    private double rho;
    private double eps;
    private double momentum;
    private boolean centered;

    /**
     * RMSprop方法。
     * * @param learningRate double类型参数
     */
    public RMSprop(double learningRate) {
        this(learningRate, 0.9, 1e-10, 0.0, false);
    }

    /**
     * RMSprop方法。
     * * @param learningRate double类型参数
     *
     * @param rho      double类型参数
     * @param eps      double类型参数
     * @param momentum double类型参数
     * @param centered boolean类型参数
     */
    public RMSprop(double learningRate, double rho, double eps, double momentum, boolean centered) {
        super(learningRate, 0.0);
        this.rho = rho;
        this.eps = eps;
        this.momentum = momentum;
        this.centered = centered;
    }

    @Override
    /**
     * step方法。
     */
    public void step() {
        for (String name : parameters.keySet()) {
            NdArray param = parameters.get(name);
            NdArray grad = gradients.get(name);

            if (grad == null) {
                continue;
            }

            String squareAvgKey = "square_avg_" + name;
            String momentumKey = "momentum_buffer_" + name;
            String gradAvgKey = "grad_avg_" + name;  // for centered version

            NdArray squareAvg = state.get(squareAvgKey);

            if (squareAvg == null) {
                squareAvg = NdArray.zeros(param.getShape(), DType.FLOAT64);
                state.put(squareAvgKey, squareAvg);
            }

            // Apply weight decay if specified
            NdArray effectiveGrad = grad;
            if (weightDecay > 0) {
                effectiveGrad = add(scalarOp(param, weightDecay), grad);
            }

            // v = rho * v + (1 - rho) * grad^2
            NdArray gradSquared = multiply(effectiveGrad, effectiveGrad);
            NdArray oneMinusRhoTimesGradSq = scalarOp(gradSquared, 1.0 - rho);
            NdArray newSquareAvg = add(scalarOp(squareAvg, rho), oneMinusRhoTimesGradSq);
            state.put(squareAvgKey, newSquareAvg);

            NdArray avg = newSquareAvg;
            if (centered) {
                // Compute centered RMSprop
                NdArray gradAvg = state.get(gradAvgKey);
                if (gradAvg == null) {
                    gradAvg = NdArray.zeros(param.getShape(), DType.FLOAT64);
                    state.put(gradAvgKey, gradAvg);
                }
                // grad_avg = rho * grad_avg + (1 - rho) * grad
                NdArray newGradAvg = add(scalarOp(gradAvg, rho), scalarOp(effectiveGrad, 1.0 - rho));
                state.put(gradAvgKey, newGradAvg);
                // avg = square_avg - grad_avg^2
                NdArray gradAvgSquared = multiply(newGradAvg, newGradAvg);
                avg = subtract(newSquareAvg, gradAvgSquared);
            }

            // Compute update: grad / (sqrt(avg) + eps)
            NdArray avgSqrt = sqrt(addScalar(sqrt(max(avg, eps)), eps));
            NdArray update = divide(effectiveGrad, avgSqrt);

            if (momentum > 0) {
                NdArray momentumBuffer = state.get(momentumKey);
                if (momentumBuffer == null) {
                    momentumBuffer = NdArray.zeros(param.getShape(), DType.FLOAT64);
                    state.put(momentumKey, momentumBuffer);
                }
                // m = momentum * m + lr * update
                NdArray newMomentum = add(scalarOp(momentumBuffer, momentum), scalarOp(update, learningRate));
                state.put(momentumKey, newMomentum);
                param = subtract(param, newMomentum);
            } else {
                param = subtract(param, scalarOp(update, learningRate));
            }

            parameters.put(name, param);
        }
    }

    /**
     * Get rho coefficient.
     */
    public double getRho() {
        return rho;
    }

    /**
     * Set rho (decay rate) coefficient.
     */
    public void setRho(double rho) {
        this.rho = rho;
    }

    /**
     * Get epsilon.
     */
    public double getEps() {
        return eps;
    }

    /**
     * Set epsilon for numerical stability.
     */
    public void setEps(double eps) {
        this.eps = eps;
    }

    /**
     * Get momentum coefficient.
     */
    public double getMomentum() {
        return momentum;
    }

    /**
     * Set momentum coefficient.
     */
    public void setMomentum(double momentum) {
        this.momentum = momentum;
    }

    /**
     * Check if centered version is enabled.
     */
    public boolean isCentered() {
        return centered;
    }

    /**
     * Enable or disable centered version.
     */
    public void setCentered(boolean centered) {
        this.centered = centered;
    }

    // Helper methods for element-wise operations on NdArray
    private NdArray multiply(NdArray a, NdArray b) {
        return elementWiseOp2(a, b, (x, y) -> ((Number) x).doubleValue() * ((Number) y).doubleValue());
    }

    private NdArray add(NdArray a, NdArray b) {
        return elementWiseOp2(a, b, (x, y) -> ((Number) x).doubleValue() + ((Number) y).doubleValue());
    }

    private NdArray subtract(NdArray a, NdArray b) {
        return elementWiseOp2(a, b, (x, y) -> ((Number) x).doubleValue() - ((Number) y).doubleValue());
    }

    private NdArray divide(NdArray a, NdArray b) {
        return elementWiseOp2(a, b, (x, y) -> ((Number) x).doubleValue() / ((Number) y).doubleValue());
    }

    private NdArray sqrt(NdArray a) {
        return elementWiseOp(a, 0, (x, s) -> Math.sqrt(((Number) x).doubleValue()));
    }

    private NdArray max(NdArray a, double threshold) {
        return elementWiseOp(a, threshold, (x, t) -> Math.max(((Number) x).doubleValue(), t));
    }

    private NdArray scalarOp(NdArray input, double scalar) {
        return elementWiseOp(input, scalar, (x, s) -> ((Number) x).doubleValue() * s);
    }

    private NdArray addScalar(NdArray a, double scalar) {
        return elementWiseOp(a, scalar, (x, s) -> ((Number) x).doubleValue() + s);
    }

    private NdArray elementWiseOp(NdArray input, double scalar, Op op) {
        Object data = input.getData();
        DType dtype = input.getDtype();
        int size = input.size();

        if (data instanceof double[]) {
            double[] arr = (double[]) data;
            double[] result = new double[size];
            for (int i = 0; i < size; i++) {
                result[i] = op.apply(arr[i], scalar);
            }
            return NdArray.create(result, dtype, input.getShape());
        }

        throw new IllegalArgumentException("Unsupported dtype: " + dtype);
    }

    private NdArray elementWiseOp2(NdArray a, NdArray b, Op2 op) {
        Object dataA = a.getData();
        Object dataB = b.getData();
        DType dtype = a.getDtype();
        int size = a.size();

        if (dataA instanceof double[] && dataB instanceof double[]) {
            double[] arrA = (double[]) dataA;
            double[] arrB = (double[]) dataB;
            double[] result = new double[size];
            for (int i = 0; i < size; i++) {
                result[i] = op.apply(arrA[i], arrB[i]);
            }
            return NdArray.create(result, dtype, a.getShape());
        }

        throw new IllegalArgumentException("Unsupported dtype combination");
    }

    @FunctionalInterface
    private interface Op {
        double apply(double x, double s);
    }

    @FunctionalInterface
    private interface Op2 {
        double apply(double x, double y);
    }
}
