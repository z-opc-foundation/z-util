package com.zifang.util.ml.optim;

import com.zifang.util.numpy.DType;
import com.zifang.util.numpy.NdArray;

/**
 * Adam and AdamW (Adam with weight decay) optimizer.
 * <p>
 * AdamW decouples weight decay from the gradient-based update.
 * <p>
 * The update rule for Adam:
 * m = beta1 * m + (1 - beta1) * grad
 * v = beta2 * v + (1 - beta2) * grad^2
 * m_hat = m / (1 - beta1^t)
 * v_hat = v / (1 - beta2^t)
 * param = param - lr * m_hat / (sqrt(v_hat) + eps)
 * <p>
 * For AdamW, weight decay is applied separately:
 * param = param - lr * (weight_decay * param + m_hat / (sqrt(v_hat) + eps))
 */
public class Adam extends Optimizer {

    private double beta1;
    private double beta2;
    private double eps;
    private boolean adamw;
    private int timeStep;

    /**
     * Adam方法。
     * * @param learningRate double类型参数
     */
    public Adam(double learningRate) {
        this(learningRate, 0.9, 0.999, 1e-8, false);
    }

    /**
     * Adam方法。
     * * @param learningRate double类型参数
     *
     * @param beta1 double类型参数
     * @param beta2 double类型参数
     * @param eps   double类型参数
     * @param adamw boolean类型参数
     */
    public Adam(double learningRate, double beta1, double beta2, double eps, boolean adamw) {
        super(learningRate, 0.0);
        this.beta1 = beta1;
        this.beta2 = beta2;
        this.eps = eps;
        this.adamw = adamw;
        this.timeStep = 0;
    }

    /**
     * AdamW方法。
     * * @param learningRate double类型参数
     *
     * @return static Adam类型返回值
     */
    public static Adam AdamW(double learningRate) {
        return new Adam(learningRate, 0.9, 0.999, 1e-8, true);
    }

    /**
     * AdamW方法。
     * * @param learningRate double类型参数
     *
     * @param beta1 double类型参数
     * @param beta2 double类型参数
     * @param eps   double类型参数
     * @return static Adam类型返回值
     */
    public static Adam AdamW(double learningRate, double beta1, double beta2, double eps) {
        return new Adam(learningRate, beta1, beta2, eps, true);
    }

    @Override
    /**
     * step方法。
     */
    public void step() {
        timeStep++;
        double biasCorrection1 = 1.0 - Math.pow(beta1, timeStep);
        double biasCorrection2 = 1.0 - Math.pow(beta2, timeStep);
        double biasCorrection1Root = Math.sqrt(biasCorrection2) / biasCorrection2;
        double lr = learningRate * Math.sqrt(biasCorrection2) / biasCorrection1;

        for (String name : parameters.keySet()) {
            NdArray param = parameters.get(name);
            NdArray grad = gradients.get(name);

            if (grad == null) {
                continue;
            }

            String mKey = "exp_avg_" + name;
            String vKey = "exp_avg_sq_" + name;

            NdArray m = state.get(mKey);
            NdArray v = state.get(vKey);

            if (m == null) {
                m = NdArray.zeros(param.getShape(), param.getDtype());
                state.put(mKey, m);
            }
            if (v == null) {
                v = NdArray.zeros(param.getShape(), param.getDtype());
                state.put(vKey, v);
            }

            // m = beta1 * m + (1-beta1) * grad  (in-place on m)
            multiplyInPlace(m, beta1);
            // gradCopy = (1-beta1) * grad  (temporary)
            NdArray gradCopy = scalarOp(grad, 1.0 - beta1);
            addInPlace(m, gradCopy);

            // v = beta2 * v + (1-beta2) * grad^2  (in-place on v)
            multiplyInPlace(v, beta2);
            squareInPlace(gradCopy);
            multiplyInPlace(gradCopy, 1.0 - beta2);
            addInPlace(v, gradCopy);

            // bias-corrected estimates: m_hat = m / biasCorrection1, v_hat = v / biasCorrection2
            NdArray mHat = scalarOp(m, 1.0 / biasCorrection1);
            NdArray vHat = scalarOp(v, 1.0 / biasCorrection2);

            // update = lr * m_hat / (sqrt(v_hat) + eps)
            sqrtInPlace(vHat);
            addScalarInPlace(vHat, eps);
            divideInPlace(mHat, vHat);
            multiplyInPlace(mHat, lr);

            // param = param - update
            subtractInPlace(param, mHat);
        }
    }

    /**
     * Get beta1 coefficient.
     */
    public double getBeta1() {
        return beta1;
    }

    /**
     * Set beta1 coefficient.
     */
    public void setBeta1(double beta1) {
        this.beta1 = beta1;
    }

    /**
     * Get beta2 coefficient.
     */
    public double getBeta2() {
        return beta2;
    }

    /**
     * Set beta2 coefficient.
     */
    public void setBeta2(double beta2) {
        this.beta2 = beta2;
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

    @Override
    /**
     * clearState方法。
     */
    public void clearState() {
        super.clearState();
        timeStep = 0;
    }

    // Helper methods for element-wise operations on NdArray
    private NdArray multiply(NdArray a, NdArray b) {
        return elementWiseOp2(a, b, (x, y) -> ((Number) x).doubleValue() * ((Number) y).doubleValue());
    }

    private NdArray multiply(NdArray a, double scalar) {
        return scalarOp(a, scalar);
    }

    private NdArray divide(NdArray a, NdArray b) {
        return elementWiseOp2(a, b, (x, y) -> ((Number) x).doubleValue() / ((Number) y).doubleValue());
    }

    private NdArray add(NdArray a, NdArray b) {
        return elementWiseOp2(a, b, (x, y) -> ((Number) x).doubleValue() + ((Number) y).doubleValue());
    }

    private NdArray subtract(NdArray a, NdArray b) {
        return elementWiseOp2(a, b, (x, y) -> ((Number) x).doubleValue() - ((Number) y).doubleValue());
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

        if (data instanceof float[]) {
            float[] arr = (float[]) data;
            float[] result = new float[size];
            for (int i = 0; i < size; i++) {
                result[i] = (float) op.apply(arr[i], scalar);
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

    // In-place operations
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
            throw new IllegalArgumentException("Unsupported dtype for addInPlace");
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
            throw new IllegalArgumentException("Unsupported dtype for subtractInPlace");
        }
    }

    private void addScalarInPlace(NdArray a, double scalar) {
        Object data = a.getData();
        int size = a.size();
        if (data instanceof double[]) {
            double[] arr = (double[]) data;
            for (int i = 0; i < size; i++) arr[i] += scalar;
        } else if (data instanceof float[]) {
            float[] arr = (float[]) data;
            for (int i = 0; i < size; i++) arr[i] = (float) (arr[i] + scalar);
        }
    }

    private void sqrtInPlace(NdArray a) {
        Object data = a.getData();
        int size = a.size();
        if (data instanceof double[]) {
            double[] arr = (double[]) data;
            for (int i = 0; i < size; i++) arr[i] = Math.sqrt(arr[i]);
        } else if (data instanceof float[]) {
            float[] arr = (float[]) data;
            for (int i = 0; i < size; i++) arr[i] = (float) Math.sqrt(arr[i]);
        }
    }

    private void divideInPlace(NdArray a, NdArray b) {
        Object dataA = a.getData();
        Object dataB = b.getData();
        int size = a.size();
        if (dataA instanceof double[] && dataB instanceof double[]) {
            double[] arrA = (double[]) dataA;
            double[] arrB = (double[]) dataB;
            for (int i = 0; i < size; i++) arrA[i] /= arrB[i];
        } else if (dataA instanceof float[] && dataB instanceof float[]) {
            float[] arrA = (float[]) dataA;
            float[] arrB = (float[]) dataB;
            for (int i = 0; i < size; i++) arrA[i] /= arrB[i];
        }
    }

    private void squareInPlace(NdArray a) {
        Object data = a.getData();
        int size = a.size();
        if (data instanceof double[]) {
            double[] arr = (double[]) data;
            for (int i = 0; i < size; i++) arr[i] *= arr[i];
        } else if (data instanceof float[]) {
            float[] arr = (float[]) data;
            for (int i = 0; i < size; i++) arr[i] *= arr[i];
        }
    }

    private NdArray copyOf(NdArray a) {
        Object data = a.getData();
        int size = a.size();
        if (data instanceof double[]) {
            double[] arr = (double[]) data;
            double[] result = new double[size];
            System.arraycopy(arr, 0, result, 0, size);
            return NdArray.create(result, DType.FLOAT64, a.getShape());
        } else if (data instanceof float[]) {
            float[] arr = (float[]) data;
            float[] result = new float[size];
            System.arraycopy(arr, 0, result, 0, size);
            return NdArray.create(result, DType.FLOAT32, a.getShape());
        }
        return null;
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
