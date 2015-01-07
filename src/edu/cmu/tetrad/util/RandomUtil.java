///////////////////////////////////////////////////////////////////////////////
// For information as to what this class does, see the Javadoc, below.       //
// Copyright (C) 1998, 1999, 2000, 2001, 2002, 2003, 2004, 2005, 2006,       //
// 2007, 2008, 2009, 2010 by Peter Spirtes, Richard Scheines, Joseph Ramsey, //
// and Clark Glymour.                                                        //
//                                                                           //
// This program is free software; you can redistribute it and/or modify      //
// it under the terms of the GNU General Public License as published by      //
// the Free Software Foundation; either version 2 of the License, or         //
// (at your option) any later version.                                       //
//                                                                           //
// This program is distributed in the hope that it will be useful,           //
// but WITHOUT ANY WARRANTY; without even the implied warranty of            //
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the             //
// GNU General Public License for more details.                              //
//                                                                           //
// You should have received a copy of the GNU General Public License         //
// along with this program; if not, write to the Free Software               //
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA //
///////////////////////////////////////////////////////////////////////////////

package edu.cmu.tetrad.util;

import cern.jet.random.*;
import cern.jet.random.engine.MersenneTwister64;
import cern.jet.random.engine.RandomEngine;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.random.RandomDataGenerator;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well44497b;

import java.util.Date;

/**
 * Provides a common random number generator to be used throughout Tetrad, to avoid problems that happen when random
 * number generators are created more often than once per millisecond. When this happens, the generators are synced, and
 * there is less randomness than expected.
 * <p/>
 * A seed can be set for the generator using the <code>setSeed</code> method. This is useful if an experiment needs to
 * be repeated under different conditions. The seed for an experiment can be printed using the <code>getSeed</code>
 * method.
 * <p/>
 * The 64-bit Mersenne Twister implementation from the COLT library is used to generate random numbers.
 * <p/>
 * To see what distributions are currently supported, look at the methods of the class. These many change over time.
 *
 * @author Joseph Ramsey
 */
public class RandomUtil {

    /**
     * The singleton instance.
     */
    private static final transient RandomUtil INSTANCE = new RandomUtil();

    // Random number generator from the Apache library.
    private RandomGenerator apacheGen;

    // Used to sample some distributions.
    private RandomDataGenerator generator;

    // Used to sample some remaining distributions. (Keep these?)
    private RandomEngine engine;

    // Constructing apache NormalDistribution takes a while, so do it once.
    private NormalDistribution normal = new NormalDistribution(0, 1);
    private long seed;


    //========================================CONSTRUCTORS===================================//

    /**
     * Constructs a new random number generator based on the getModel date in milliseconds.
     */
    private RandomUtil() {
        this(new Date().getTime());
    }

    /**
     * Constructs a new random number generator using the given seed.
     *
     * @param seed A long value.
     */
    private RandomUtil(long seed) {
        setSeed(seed);
    }

    /**
     * @return the singleton instance of this class.
     */
    public static RandomUtil getInstance() {
        return INSTANCE;
    }

    //=======================================PUBLIC METHODS=================================//

    /**
     * Returns an integer in the range 0 to n - 1, inclusive.
     *
     * @param n Ibid.
     * @return Ibid.
     */
    public int nextInt(int n) {
        return apacheGen.nextInt(n);

        // This seems to be problematic for bootstrap sampling
        // with large sample sizes. jdramsey 12/13/2005
//        return uniform.nextIntFromTo(0, n - 1);
    }

    public double nextDouble() {
        return apacheGen.nextDouble();
    }

    /**
     * 
     * Returns a random double from U(low, high).
     *
     * @param low  Ibid.
     * @param high Ibid.
     * @return Ibid.
     */
    public double nextUniform(double low, double high) {
        return generator.nextUniform(low, high);
    }

    /**
     * Returns a random gaussian from N(mean, sd).
     *
     * @param mean The mean of the Normal.
     * @param sd   The standard deviation of the Normal.
     * @return Ibid.
     */
    public double nextNormal(double mean, double sd) {
        if (sd < 0) {
            throw new IllegalArgumentException("Standard deviation must be non-negative: " + sd);
        }

        return generator.nextGaussian(mean, sd);
    }

    /**
     * Returns a random gaussian from N(mean, sd).
     *
     * @param mean The mean of the Normal.
     * @param sd   The standard deviation of the Normal.
     * @return Ibid.
     */
    public double nextTruncatedNormal(double mean, double sd, double low, double high) {
        if (sd < 0) {
            throw new IllegalArgumentException("Standard deviation must be non-negative: " + sd);
        }

        if (low >= high) {
            throw new IllegalArgumentException("Low must be less than high.");
        }

        double d;

        while (true) {
            d = nextNormal(mean, sd);
            if (d >= low && d <= high) break;
        }

        return d;
    }

    /**
     * Sets the seed to the given value.
     *
     * @param seed A long value. Once this seed is set, the behavior of the random number generator is deterministic, so
     *             setting the seed can be used to repeat previous behavior.
     */
    public void setSeed(long seed) {

        // Apache offers several random number generators; picking one that works pretty well.
//        apacheGen = new MersenneTwister(seed);
        apacheGen = new Well44497b(seed);
        generator = new RandomDataGenerator(apacheGen);

        // Still used for a few things...
        engine = new MersenneTwister64(new Date(seed));
        this.seed = seed;
    }

    /**
     * Returns the next random number drawn from a Poisson distribution with the given mean.
     *
     * @param lambda A positive real number equal to the expected number of occurrences during a given interval. See
     *               Wikipedia.
     * @return Ibid.
     */
    public double nextPoisson(double lambda) {
        return generator.nextPoisson(lambda);
    }

    /**
     * Returns Normal PDF value for a normal with the given mean and standard deviation.
     *
     * @param mean  The mean of the normal to be used.
     * @param sd    The standard deviation of the normal to be used.
     * @param value The domain value for the PDF.
     * @return Ibid.
     */
    public double normalPdf(double mean, double sd, double value) {
//        return new Normal(mean, sd, engine).pdf(value);
//        return new NormalDistribution(mean, sd).density(value);
        value = (value - mean) / sd;
        return normal.density(value);
    }

    /**
     * Returns Normal CDF value for a normal with the given mean and standard deviation.
     *
     * @param mean  The mean of the normal to be used.
     * @param sd    The standard deviation of the normal to be used.
     * @param value The domain value for the CDF.
     * @return Ibid.
     */
    public double normalCdf(double mean, double sd, double value) {
//        return new Normal(mean, sd, engine).cdf(value);
//
        value = (value - mean) / sd;
//        return ProbUtils.normalCdf(value);
//        return new NormalDistribution(0, 1).cumulativeProbability(value);
//
        return normal.cumulativeProbability(value);
    }

    /**
     * Returns the next random number drawn from the Beta distribution with the given alpha and beta values. By changing
     * the alpha and beta parameters, radically different distibutions can be achieved. The Beta function is related to
     * order statistics.
     *
     * @param alpha See Wikipedia. This is the first parameter.
     * @param beta  See Wikipedia. This is the second parameter.
     * @return Ibid.
     */
    public double nextBeta(double alpha, double beta) {
        return generator.nextBeta(alpha, beta);
    }

    /**
     * Returns the next random number drawn from the von Mises distrbution with the given degrees of freedom. Note that
     * mu is assumed to be zero. See Wikipedia.
     *
     * @param kappa The measure of concentration.
     * @return Ibid.
     */
    public double nextVonMises(double kappa) {
        return new VonMises(kappa, engine).nextDouble();
    }

    /**
     * Returns the next random number drawn from the Student T distribution with the given degrees of freedom.
     *
     * @param df The degrees of freedom. See any stats book.
     * @return Ibid.
     */
    public double nextT(double df) {
        return generator.nextT(df);
    }

    /**
     * Returns the next random number drawn from the Exponential distribution with the given lambda.
     *
     * @param lambda The rate parameter. See Wikipedia.
     * @return Ibid.
     */
    public double nextExponential(double lambda) {
        return generator.nextExponential(lambda);
    }

    /**
     * Returns the next random number drawn from the Eponential Power distribution with the given tau.
     *
     * @param tau The shape parameter. See Wikipedia.
     * @return Ibid.
     */
    public double nextExponentialPower(double tau) {
        return new ExponentialPower(tau, engine).nextDouble();
    }

    /**
     * Returns the next random number drawn from the given Chi Square distrubution with the given degrees of freedom.
     *
     * @param df The degrees of freedom.
     * @return Ibid.
     */
    public double nextChiSquare(double df) {
        return generator.nextChiSquare(df);
    }

    /**
     * Returns the next random number drawn from the Gamma distribution with the given alpha and lambda.
     *
     * @param shape The shape parameter.
     * @param scale The scale parameter.
     * @return Ibid.
     */
    public double nextGamma(double shape, double scale) {
        return generator.nextGamma(shape, scale);
    }

    /**
     * Returns the next random number drawn from the Logarithmic distribution, with the given p.
     *
     * @param p Between 0 and 1.
     * @return Ibid.
     */
    public double nextLogarithmic(double p) {
        return new Logarithmic(p, engine).nextDouble();
    }

    /**
     * Returns the next random number drawn from the Breit Wigner distribution with the given mean, gamma, and cut. I'm
     * not sure of the identity of these parameters, sorry.
     *
     * @param mean  Center of mass energy?
     * @param gamma Mass?
     * @param cut   Resonance width?
     * @return Ibid.
     */
    public double nextBreitWigner(double mean, double gamma, double cut) {
        return new BreitWigner(mean, gamma, cut, engine).nextDouble();
    }

    /**
     * Returns the next random number drawn from the given Hyperbolic distribution with the given alpha and gamma.
     *
     * @param alpha Real.
     * @param gamma Assymmetry parameter.
     * @return Ibid.
     */
    public double nextHyperbolic(double alpha, double gamma) {
        return new Hyperbolic(alpha, gamma, engine).nextDouble();
    }

    public long getSeed() {
        return seed;
    }
}



