package com.zifang.util.ml.optim;

import com.zifang.util.numpy.DType;
import com.zifang.util.numpy.NdArray;

/**
 * Stochastic Gradient Descent optimizer with momentum, weight decay,
 * and Nesterov accelerated gradient support.
 * <p>
 * The update rule is:
 * v = momentum * v + lr * (grad + weight_decay * param)  [standard]
 * v = momentum * v + lr * (grad + weight_decay * param)  [Nesterov]
 * param = param - v  [standard]
 * param = param - (momentum * v + lr * (grad + weight_decay * param))  [Nesterov]
 */
public class SGD extends Optimizer {

    private double momentum;
    private boolean nesterov;
    private double dampening;

    /**
     * SGD方法。
     * * @param learningRate double类型参数
     */
    public SGD(double learningRate) {
        this(learningRate, 0.0);
    }

    /**
     * SGD方法。
     * * @param learningRate double类型参数
     *
     * @param momentum double类型参数
     */
    public SGD(double learningRate, double momentum) {
        this(learningRate, momentum, 0.0);
    }

    /**
     * SGD方法。
     * * @param learningRate double类型参数
     *
     * @param momentum    double类型参数
     * @param weightDecay double类型参数
     */
    public SGD(double learningRate, double momentum, double weightDecay) {
        this(learningRate, momentum, weightDecay, false);
    }

    /**
     * SGD方法。
     * * @param learningRate double类型参数
     *
     * @param momentum    double类型参数
     * @param weightDecay double类型参数
     * @param nesterov    boolean类型参数
     */
    public SGD(double learningRate, double momentum, double weightDecay, boolean nesterov) {
        super(learningRate, weightDecay);
        this.momentum = momentum;
        this.nesterov = nesterov;
        this.dampening = 0.0;
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

            String momentumKey = "momentum_buffer_" + name;

            // Apply weight decay (L2 regularization)
            NdArray effectiveGrad = grad;
            if (weightDecay > 0) {
                effectiveGrad = add(multiply(param, weightDecay), grad);
            }

            if (momentum > 0) {
                NdArray buf = state.get(momentumKey);

                if (buf == null) {
                    buf = NdArray.zeros(param.getShape(), param.getDtype());
                    state.put(momentumKey, buf);
                }

                if (nesterov) {
                    // Nesterov: buf = momentum * buf + grad; update = momentum * buf + lr * grad
                    multiplyInPlace(buf, momentum);
                    addInPlace(buf, effectiveGrad);
                    multiplyInPlace(buf, momentum);
                    addInPlace(buf, effectiveGrad);
                    multiplyInPlace(buf, learningRate);
                    subtractInPlace(param, buf);
                } else {
                    // Standard momentum: buf = momentum * buf + grad; param = param - lr * buf
                    multiplyInPlace(buf, momentum);
                    addInPlace(buf, effectiveGrad);
                    multiplyInPlace(buf, learningRate);
                    subtractInPlace(param, buf);
                }
            } else {
                // Simple gradient descent: param = param - lr * grad
                multiplyInPlace(effectiveGrad, learningRate);
                subtractInPlace(param, effectiveGrad);
            }
        }
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
     * Set dampening for momentum.
     */
    public void setDampening(double dampening) {
        this.dampening = dampening;
    }

    /**
     * Check if Nesterov is enabled.
     */
    public boolean isNesterov() {
        return nesterov;
    }

    /**
     * Enable or disable Nesterov acceleration.
     */
    public void setNesterov(boolean nesterov) {
        this.nesterov = nesterov;
    }

    // Helper methods for element-wise operations on NdArray
    private NdArray multiply(NdArray a, double scalar) {
        return elementWiseOp(a, scalar, (x, s) -> ((Number) x).doubleValue() * s);
    }

    private NdArray addScalar(NdArray a, double scalar) {
        return elementWiseOp(a, scalar, (x, s) -> ((Number) x).doubleValue() + s);
    }

    private NdArray add(NdArray a, NdArray b) {
        return elementWiseOp2(a, b, (x, y) -> ((Number) x).doubleValue() + ((Number) y).doubleValue());
    }

    private NdArray subtract(NdArray a, NdArray b) {
        return elementWiseOp2(a, b, (x, y) -> ((Number) x).doubleValue() - ((Number) y).doubleValue());
    }

    // In-place operations: modify NdArray data directly
    private void multiplyInPlace(NdArray a, double scalar) {
        Object data = a.getData();
        int size = a.size();
        if (data instanceof double[]) {
            double[] arr = (double[]) data;
            for (int i = 0; i < size; i++) arr[i] *= scalar;
        } else if (data instanceof float[]) {
            float[] arr = (float[]) data;
            for (int i = 0; i < size; i++) arr[i] = (float) (arr[i] * scalar);
        } else {
            throw new IllegalArgumentException("Unsupported dtype: " + a.getDtype());
        }
    }

    private void addInPlace(NdArray a, NdArray b) {
        Object dataA = a.getData();
        Object dataB = b.getData();
        int size = a.size();
        if (dataA instanceof double[] && dataB instanceof double[]) {
            double[] arrA = (double[]) dataA;
            double[] arrB = (double[]) dataB;
            for (int i = 0; i < size; i++) arrA[i] += arrB[i];
        } else if (dataA instanceof float[] && dataB instanceof float[]) {
            float[] arrA = (float[]) dataA;
            float[] arrB = (float[]) dataB;
            for (int i = 0; i < size; i++) arrA[i] += arrB[i];
        } else {
            throw new IllegalArgumentException("Unsupported dtype combination for addInPlace");
        }
    }

    private void subtractInPlace(NdArray a, NdArray b) {
        Object dataA = a.getData();
        Object dataB = b.getData();
        int size = a.size();
        if (dataA instanceof double[] && dataB instanceof double[]) {
            double[] arrA = (double[]) dataA;
            double[] arrB = (double[]) dataB;
            for (int i = 0; i < size; i++) arrA[i] -= arrB[i];
        } else if (dataA instanceof float[] && dataB instanceof float[]) {
            float[] arrA = (float[]) dataA;
            float[] arrB = (float[]) dataB;
            for (int i = 0; i < size; i++) arrA[i] -= arrB[i];
        } else {
            throw new IllegalArgumentException("Unsupported dtype combination for subtractInPlace");
        }
    }

    private NdArray elementWiseOp(NdArray input, double scalar, Op op) {
        Object data = input.getData();
        Object newData;
        DType dtype = input.getDtype();
        int size = input.size();

        if (data instanceof double[]) {
            double[] arr = (double[]) data;
            double[] result = new double[size];
            for (int i = 0; i < size; i++) {
                result[i] = op.apply(arr[i], scalar);
            }
            newData = result;
        } else if (data instanceof float[]) {
            float[] arr = (float[]) data;
            float[] result = new float[size];
            for (int i = 0; i < size; i++) {
                result[i] = (float) op.apply(arr[i], scalar);
            }
            newData = result;
        } else {
            throw new IllegalArgumentException("Unsupported dtype: " + dtype);
        }

        // Preserve input dtype (casting scalar result to float if needed)
        if (dtype == DType.FLOAT32 && !(data instanceof float[])) {
            float[] floatResult = new float[size];
            for (int i = 0; i < size; i++) {
                floatResult[i] = (float) ((double[]) newData)[i];
            }
            newData = floatResult;
        }

        return NdArray.create(newData, dtype, input.getShape());
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

        if (dataA instanceof float[] && dataB instanceof float[]) {
            float[] arrA = (float[]) dataA;
            float[] arrB = (float[]) dataB;
            float[] result = new float[size];
            for (int i = 0; i < size; i++) {
                result[i] = (float) op.apply(arrA[i], arrB[i]);
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
