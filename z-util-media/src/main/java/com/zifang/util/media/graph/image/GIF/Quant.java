package com.zifang.util.media.graph.image.GIF;

/**
 * GIF 图像颜色量化器（基于神经网络的八叉树算法）。
 * 将真彩色图像（最多 1670 万色）量化到 256 色的调色板。
 *
 * <p>该算法通过自组织神经网络学习图像颜色分布，
 * 将相似的颜色归类到同一个神经元，最终生成 256 色的调色板。
 */
public class Quant {
    protected static final int netsize = 256; /* number of colours used */

    /* four primes near 500 - assume no image has a length so large */
    /* that it is divisible by all four primes */
    protected static final int prime1 = 499;
    protected static final int prime2 = 491;
    protected static final int prime3 = 487;
    protected static final int prime4 = 503;

    /**
     * =方法。
     * * @param prime4 3类型参数
     *
     * @return static final int minpicturebytes类型返回值
     */
    protected static final int minpicturebytes = (3 * prime4);
    /* minimum size for input image */

	/* Program Skeleton
	   ----------------
	   [select samplefac in range 1..30]
	   [read image from input file]
	   pic = (unsigned char*) malloc(3*width*height);
	   initnet(pic,3*width*height,samplefac);
	   learn();
	   unbiasnet();
	   [write output image header, using writecolourmap(f)]
	   inxbuild();
	   write output image using inxsearch(b,g,r)      */

	/* Network Definitions
	   ------------------- */

    /**
     * =方法。
     * * @param 1 netsize类型参数
     *
     * @return static final int maxnetpos类型返回值
     */
    protected static final int maxnetpos = (netsize - 1);
    protected static final int netbiasshift = 4; /* bias for colour values */
    protected static final int ncycles = 100; /* no. of learning cycles */

    /* defs for freq and bias */
    protected static final int intbiasshift = 16; /* bias for fractions */
    protected static final int betashift = 10;
    protected static final int gammashift = 10; /* gamma = 1024 */
    /**
     * =方法。
     * * @param intbiasshift 1类型参数
     *
     * @return static final int intbias类型返回值
     */
    protected static final int intbias = (1 << intbiasshift);
    /**
     * =方法。
     * * @param betashift intbias类型参数
     *
     * @return static final int beta类型返回值
     */
    protected static final int beta = (intbias >> betashift); /* beta = 1/1024 */
    protected static final int betagamma =
            (intbias << (gammashift - betashift));
    /**
     * =方法。
     * * @param gammashift 1类型参数
     *
     * @return static final int gamma类型返回值
     */
    protected static final int gamma = (1 << gammashift);

    /* defs for decreasing radius factor */
    /**
     * =方法。
     * * @param 3 netsize类型参数
     *
     * @return static final int initrad类型返回值
     */
    protected static final int initrad = (netsize >> 3); /* for 256 cols, radius starts */
    protected static final int radiusbiasshift = 6; /* at 32.0 biased by 6 bits */
    /**
     * =方法。
     * * @param radiusbiasshift 1类型参数
     *
     * @return static final int radiusbias类型返回值
     */
    protected static final int radiusbias = (1 << radiusbiasshift);
    /**
     * =方法。
     * * @param radiusbias initrad类型参数
     *
     * @return static final int initradius类型返回值
     */
    protected static final int initradius = (initrad * radiusbias); /* and decreases by a */
    protected static final int radiusdec = 30; /* factor of 1/30 each cycle */

    /* defs for decreasing alpha factor */
    protected static final int alphabiasshift = 10; /* alpha starts at 1.0 */
    /**
     * =方法。
     * * @param alphabiasshift 1类型参数
     *
     * @return static final int initalpha类型返回值
     */
    protected static final int initalpha = (1 << alphabiasshift);
    /* radbias and alpharadbias used for radpower calculation */
    protected static final int radbiasshift = 8;
    /**
     * =方法。
     * * @param radbiasshift 1类型参数
     *
     * @return static final int radbias类型返回值
     */
    protected static final int radbias = (1 << radbiasshift);
    /**
     * =方法。
     * * @param radbiasshift alphabiasshift类型参数
     *
     * @return static final int alpharadbshift类型返回值
     */
    protected static final int alpharadbshift = (alphabiasshift + radbiasshift);
    /**
     * =方法。
     * * @param alpharadbshift 1类型参数
     *
     * @return static final int alpharadbias类型返回值
     */
    protected static final int alpharadbias = (1 << alpharadbshift);
    protected int alphadec; /* biased by 10 bits */

    /* Types and Global Variables
    -------------------------- */
    protected byte[] thepicture; /* the input image itself */
    protected int lengthcount; /* lengthcount = H*W*3 */

    protected int samplefac; /* sampling factor 1..30 */

    //   typedef int pixel[4];                /* BGRc */
    protected int[][] network; /* the network itself - [netsize][4] */

    protected int[] netindex = new int[256];
    /* for network lookup - really 256 */

    protected int[] bias = new int[netsize];
    /* bias and freq arrays for learning */
    protected int[] freq = new int[netsize];
    protected int[] radpower = new int[initrad];
    /* radpower for precomputation */

    /* Initialise network in range (0,0,0) to (255,255,255) and set parameters
       ----------------------------------------------------------------------- */

    /**
     * 初始化网络，将神经元权重初始化为 (0,0,0) 到 (255,255,255) 范围内的值。
     *
     * @param thepic 输入图像的像素数据
     * @param len    像素数据长度
     * @param sample 采样因子 1..30
     */
    public Quant(byte[] thepic, int len, int sample) {

        int i;
        int[] p;

        thepicture = thepic;
        lengthcount = len;
        samplefac = sample;

        network = new int[netsize][];
        for (i = 0; i < netsize; i++) {
            network[i] = new int[4];
            p = network[i];
            p[0] = p[1] = p[2] = (i << (netbiasshift + 8)) / netsize;
            freq[i] = intbias / netsize; /* 1/netsize */
            bias[i] = 0;
        }
    }

    /**
     * 获取颜色映射表。
     *
     * @return 256 色调色板，每 3 个字节表示一个 RGB 颜色
     */
    public byte[] colorMap() {
        byte[] map = new byte[3 * netsize];
        int[] index = new int[netsize];
        for (int i = 0; i < netsize; i++)
            index[network[i][3]] = i;
        int k = 0;
        for (int i = 0; i < netsize; i++) {
            int j = index[i];
            map[k++] = (byte) (network[j][0]);
            map[k++] = (byte) (network[j][1]);
            map[k++] = (byte) (network[j][2]);
        }
        return map;
    }

    /**
     * 插入排序：对网络进行排序并构建快速查找表 netindex。
     * 在 unbias 操作之后调用。
     */
    public void inxbuild() {

        int i, j, smallpos, smallval;
        int[] p;
        int[] q;
        int previouscol, startpos;

        previouscol = 0;
        startpos = 0;
        for (i = 0; i < netsize; i++) {
            p = network[i];
            smallpos = i;
            smallval = p[1]; /* index on g */
            /* find smallest in i..netsize-1 */
            for (j = i + 1; j < netsize; j++) {
                q = network[j];
                if (q[1] < smallval) { /* index on g */
                    smallpos = j;
                    smallval = q[1]; /* index on g */
                }
            }
            q = network[smallpos];
            /* swap p (i) and q (smallpos) entries */
            if (i != smallpos) {
                j = q[0];
                q[0] = p[0];
                p[0] = j;
                j = q[1];
                q[1] = p[1];
                p[1] = j;
                j = q[2];
                q[2] = p[2];
                p[2] = j;
                j = q[3];
                q[3] = p[3];
                p[3] = j;
            }
            /* smallval entry is now in position i */
            if (smallval != previouscol) {
                netindex[previouscol] = (startpos + i) >> 1;
                for (j = previouscol + 1; j < smallval; j++)
                    netindex[j] = i;
                previouscol = smallval;
                startpos = i;
            }
        }
        netindex[previouscol] = (startpos + maxnetpos) >> 1;
        for (j = previouscol + 1; j < 256; j++)
            netindex[j] = maxnetpos; /* really 256 */
    }

    /**
     * 主学习循环。
     * 通过迭代调整神经网络的权重，使其学习输入图像的颜色分布。
     */
    public void learn() {

        int i, j, b, g, r;
        int radius, rad, alpha, step, delta, samplepixels;
        byte[] p;
        int pix, lim;

        if (lengthcount < minpicturebytes)
            samplefac = 1;
        alphadec = 30 + ((samplefac - 1) / 3);
        p = thepicture;
        pix = 0;
        lim = lengthcount;
        samplepixels = lengthcount / (3 * samplefac);
        delta = samplepixels / ncycles;
        alpha = initalpha;
        radius = initradius;

        rad = radius >> radiusbiasshift;
        if (rad <= 1)
            rad = 0;
        for (i = 0; i < rad; i++)
            radpower[i] =
                    alpha * (((rad * rad - i * i) * radbias) / (rad * rad));

        //fprintf(stderr,"beginning 1D learning: initial radius=%d\n", rad);

        if (lengthcount < minpicturebytes)
            step = 3;
        else if ((lengthcount % prime1) != 0)
            step = 3 * prime1;
        else {
            if ((lengthcount % prime2) != 0)
                step = 3 * prime2;
            else {
                if ((lengthcount % prime3) != 0)
                    step = 3 * prime3;
                else
                    step = 3 * prime4;
            }
        }

        i = 0;
        while (i < samplepixels) {
            b = (p[pix + 0] & 0xff) << netbiasshift;
            g = (p[pix + 1] & 0xff) << netbiasshift;
            r = (p[pix + 2] & 0xff) << netbiasshift;
            j = contest(b, g, r);

            altersingle(alpha, j, b, g, r);
            if (rad != 0)
                alterneigh(rad, j, b, g, r); /* alter neighbours */

            pix += step;
            if (pix >= lim)
                pix -= lengthcount;

            i++;
            if (delta == 0)
                delta = 1;
            if (i % delta == 0) {
                alpha -= alpha / alphadec;
                radius -= radius / radiusdec;
                rad = radius >> radiusbiasshift;
                if (rad <= 1)
                    rad = 0;
                for (j = 0; j < rad; j++)
                    radpower[j] =
                            alpha * (((rad * rad - j * j) * radbias) / (rad * rad));
            }
        }
        //fprintf(stderr,"finished 1D learning: final alpha=%f !\n",((float)alpha)/initalpha);
    }

    /**
     * 查找与给定 BGR 值最接近的颜色在调色板中的索引。
     *
     * @param b 蓝色分量 0..255
     * @param g 绿色分量 0..255
     * @param r 红色分量 0..255
     * @return 调色板中最接近颜色的索引
     */
    public int map(int b, int g, int r) {

        int i, j, dist, a, bestd;
        int[] p;
        int best;

        bestd = 1000; /* biggest possible dist is 256*3 */
        best = -1;
        i = netindex[g]; /* index on g */
        j = i - 1; /* start at netindex[g] and work outwards */

        while ((i < netsize) || (j >= 0)) {
            if (i < netsize) {
                p = network[i];
                dist = p[1] - g; /* inx key */
                if (dist >= bestd)
                    i = netsize; /* stop iter */
                else {
                    i++;
                    if (dist < 0)
                        dist = -dist;
                    a = p[0] - b;
                    if (a < 0)
                        a = -a;
                    dist += a;
                    if (dist < bestd) {
                        a = p[2] - r;
                        if (a < 0)
                            a = -a;
                        dist += a;
                        if (dist < bestd) {
                            bestd = dist;
                            best = p[3];
                        }
                    }
                }
            }
            if (j >= 0) {
                p = network[j];
                dist = g - p[1]; /* inx key - reverse dif */
                if (dist >= bestd)
                    j = -1; /* stop iter */
                else {
                    j--;
                    if (dist < 0)
                        dist = -dist;
                    a = p[0] - b;
                    if (a < 0)
                        a = -a;
                    dist += a;
                    if (dist < bestd) {
                        a = p[2] - r;
                        if (a < 0)
                            a = -a;
                        dist += a;
                        if (dist < bestd) {
                            bestd = dist;
                            best = p[3];
                        }
                    }
                }
            }
        }
        return (best);
    }

    /**
     * 执行完整的量化流程。
     *
     * @return 量化后的 256 色调色板
     */
    public byte[] process() {
        learn();
        unbiasnet();
        inxbuild();
        return colorMap();
    }

    /**
     * 去除网络偏差，将权重值偏移回 0..255 范围。
     * 并记录颜色编号，为排序做准备。
     */
    public void unbiasnet() {

        int i, j;

        for (i = 0; i < netsize; i++) {
            network[i][0] >>= netbiasshift;
            network[i][1] >>= netbiasshift;
            network[i][2] >>= netbiasshift;
            network[i][3] = i; /* record colour no */
        }
    }

    /**
     * 移动相邻神经元，调整权重。
     * 根据预计算的 alpha*(1-((i-j)^2/[r]^2)) 值调整半径 rad 范围内的相邻神经元。
     *
     * @param rad 调整半径
     * @param i   神经元索引
     * @param b   蓝色分量
     * @param g   绿色分量
     * @param r   红色分量
     */
    protected void alterneigh(int rad, int i, int b, int g, int r) {

        int j, k, lo, hi, a, m;
        int[] p;

        lo = i - rad;
        if (lo < -1)
            lo = -1;
        hi = i + rad;
        if (hi > netsize)
            hi = netsize;

        j = i + 1;
        k = i - 1;
        m = 1;
        while ((j < hi) || (k > lo)) {
            a = radpower[m++];
            if (j < hi) {
                p = network[j++];
                try {
                    p[0] -= (a * (p[0] - b)) / alpharadbias;
                    p[1] -= (a * (p[1] - g)) / alpharadbias;
                    p[2] -= (a * (p[2] - r)) / alpharadbias;
                } catch (Exception e) {
                } // prevents 1.3 miscompilation
            }
            if (k > lo) {
                p = network[k--];
                try {
                    p[0] -= (a * (p[0] - b)) / alpharadbias;
                    p[1] -= (a * (p[1] - g)) / alpharadbias;
                    p[2] -= (a * (p[2] - r)) / alpharadbias;
                } catch (Exception e) {
                }
            }
        }
    }

    /**
     * 移动单个神经元，使其向目标颜色靠近。
     *
     * @param alpha 学习因子
     * @param i     神经元索引
     * @param b     蓝色分量
     * @param g     绿色分量
     * @param r     红色分量
     */
    protected void altersingle(int alpha, int i, int b, int g, int r) {

        /* alter hit neuron */
        int[] n = network[i];
        n[0] -= (alpha * (n[0] - b)) / initalpha;
        n[1] -= (alpha * (n[1] - g)) / initalpha;
        n[2] -= (alpha * (n[2] - r)) / initalpha;
    }

    /**
     * 竞赛算法：查找最匹配神经元并更新频率。
     * 找到距离最近的神经元（最小距离）和最佳神经元（最小距离-偏差）。
     * 对于频繁选择的神经元，freq[i] 较高而 bias[i] 为负值。
     *
     * @param b 蓝色分量
     * @param g 绿色分量
     * @param r 红色分量
     * @return 最佳匹配神经元的索引
     */
    protected int contest(int b, int g, int r) {

        /* finds closest neuron (min dist) and updates freq */
        /* finds best neuron (min dist-bias) and returns position */
        /* for frequently chosen neurons, freq[i] is high and bias[i] is negative */
        /* bias[i] = gamma*((1/netsize)-freq[i]) */

        int i, dist, a, biasdist, betafreq;
        int bestpos, bestbiaspos, bestd, bestbiasd;
        int[] n;

        bestd = ~(1 << 31);
        bestbiasd = bestd;
        bestpos = -1;
        bestbiaspos = bestpos;

        for (i = 0; i < netsize; i++) {
            n = network[i];
            dist = n[0] - b;
            if (dist < 0)
                dist = -dist;
            a = n[1] - g;
            if (a < 0)
                a = -a;
            dist += a;
            a = n[2] - r;
            if (a < 0)
                a = -a;
            dist += a;
            if (dist < bestd) {
                bestd = dist;
                bestpos = i;
            }
            biasdist = dist - ((bias[i]) >> (intbiasshift - netbiasshift));
            if (biasdist < bestbiasd) {
                bestbiasd = biasdist;
                bestbiaspos = i;
            }
            betafreq = (freq[i] >> betashift);
            freq[i] -= betafreq;
            bias[i] += (betafreq << gammashift);
        }
        freq[bestpos] += beta;
        bias[bestpos] -= betagamma;
        return (bestbiaspos);
    }
}
