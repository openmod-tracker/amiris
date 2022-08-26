// SPDX-FileCopyrightText: 2022 German Aerospace Center <amiris@dlr.de>
//
// SPDX-License-Identifier: Apache-2.0
package agents.forecast;

import java.util.Random;
import de.dlr.gitlab.fame.agent.input.Make;
import de.dlr.gitlab.fame.agent.input.ParameterData;
import de.dlr.gitlab.fame.agent.input.ParameterData.MissingDataException;
import de.dlr.gitlab.fame.agent.input.Tree;

/** Calculates random errors for power forecasting
 * 
 * @author Johannes Kochems, Christoph Schimeczek */
public class PowerForecastError {

	public static final Tree parameters = Make.newTree()
			.add(Make.newDouble("Mean").optional(), Make.newDouble("Variance").optional()).buildTree();

	private double mean;
	private double variance;
	private Random rng;

	/** Creates a {@link PowerForecastError}
	 * 
	 * @param input parameter group according to {@link #parameters}
	 * @param random random number generator - use FAME's RNG creation function to ensure reproducibility on identical seeds
	 * @throws MissingDataException if any required data is not provided */
	public PowerForecastError(ParameterData input, Random random) throws MissingDataException {
		mean = input.getDouble("Mean");
		variance = input.getDouble("Variance");
		rng = random;
	}

	/** @return a random error from a normal distribution */
	public double getNextError() {
		return rng.nextGaussian() * variance + mean;
	}
}