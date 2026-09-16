package com.zifang.util.ml.optim;

import com.zifang.util.numpy.DType;
import com.zifang.util.numpy.NdArray;

/**
 * Adagrad optimizer.
 * <p>
 * Adagrad adapts the learning rate for each parameter based on the
 * historical gradients. Parameters that have had large gradients get
 * smaller learning rates, while parameters with small gradients get
 * larger learning rates.
 * <p>
 * The update rule:
 * grad_sum = grad_sum + grad^2
 * param = param - lr * grad / (sqrt(grad_sum) + eps)
 */
public class Adagrad extends Optimizer {

    private double eps;
    private double initialAccumulatorValue;

    /**
     * Adagrad方法。
     * * @param learningRate double类型参数
     */
    public Adagrad(double learningRate) {
        this(learningRate, 1e-10, 0.0);
    }

    /**
     * Adagrad方法。
     * * @param learningRate double类型参数
     *
     * @param eps double类型参数
     */
    public Adagrad(double learningRate, double eps) {
        this(learningRate, eps, 0.0);
    }

    /**
     * Adagrad方法。
     * * @param learningRate double类型参数
     *
     * @param eps         double类型参数
     * @param weightDecay double类型参数
     */
    public Adagrad(double learningRate, double eps, double weightDecay) {
        super(learningRate, weightDecay);
        this.eps = eps;
        this.initialAccumulatorValue = 0.0;
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

            String sumKey = "grad_sum_" + name;

            NdArray gradSum = state.get(sumKey);

            if (gradSum == null) {
                // Initialize accumulator with zeros (Adagrad standard initialization)
                gradSum = NdArray.zeros(param.getShape(), DType.FLOAT64);
                state.put(sumKey, gradSum);
            }

            // Apply weight decay if specified
            NdArray effectiveGrad = grad;
            if (weightDecay > 0) {
                effectiveGrad = add(scalarOp(param, weightDecay), grad);
            }

            // grad_sum = grad_sum + grad^2
            NdArray gradSquared = multiply(effectiveGrad, effectiveGrad);
            NdArray newGradSum = add(gradSum, gradSquared);
            state.put(sumKey, newGradSum);

            // Compute update: grad / (sqrt(grad_sum) + eps)
            NdArray denom = addScalar(sqrt(newGradSum), eps);
            NdArray update = divide(effectiveGrad, denom);

            // param = param - lr * update
            param = subtract(param, scalarOp(update, learningRate));

            parameters.put(name, param);
        }
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
     * Get initial accumulator value.
     */
    public double getInitialAccumulatorValue() {
        return initialAccumulatorValue;
    }

    /**
     * Set initial accumulator value.
     */
    public void setInitialAccumulatorValue(double value) {
        this.initialAccumulatorValue = value;
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
