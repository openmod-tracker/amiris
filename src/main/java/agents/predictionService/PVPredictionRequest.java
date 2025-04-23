// SPDX-FileCopyrightText: 2025 German Aerospace Center <amiris@dlr.de>
//
// SPDX-License-Identifier: Apache-2.0
package agents.predictionService;

import java.util.Collections;
import java.util.Map;

/** Encapsulates a prediction request of a general prediction model. This class is designed primarily for time series prediction,
 * where each variable's value is associated with a time reference (or an ordered index).
 * 
 * @author A. Achraf El Ghazi, Felix Nitsch, Ulrich Frey */
public class PVPredictionRequest {
	private String modelId;
	private long predictionStart;
	private Map<Long, Double> energyGenerationPerMW = Collections.emptyMap();
	private Map<Long, Double> storedMWh = Collections.emptyMap();
	private Map<Long, Double> prosumersLoadInMW = Collections.emptyMap();
	private Map<Long, Double> aggregatorSalesPriceInEURperMWH = Collections.emptyMap();
	private Map<Long, Double> prosumersGridInteraction = Collections.emptyMap();

	/** Creates a {@link PVPredictionRequest} based on given input parameters
	 * 
	 * @param modelId ID of the ML model that should perform the prediction
	 * @param predictionStart time point of the prediction
	 * @param EnergyGenerationPerMW energy generation profile
	 * @param StoredMWh available energy in the battery
	 * @param ProsumersLoadInMW self load/demand
	 * @param AggregatorSalesPriceInEURperMWH end-user electricity price
	 * @param ProsumersGridInteraction net load of the house hold */
	public PVPredictionRequest(String modelId, long predictionStart, Map<Long, Double> EnergyGenerationPerMW,
			Map<Long, Double> StoredMWh,
			Map<Long, Double> ProsumersLoadInMW, Map<Long, Double> AggregatorSalesPriceInEURperMWH,
			Map<Long, Double> ProsumersGridInteraction) {
		this.modelId = modelId;
		this.predictionStart = predictionStart;
		this.energyGenerationPerMW = EnergyGenerationPerMW;
		this.storedMWh = StoredMWh;
		this.prosumersLoadInMW = ProsumersLoadInMW;
		this.aggregatorSalesPriceInEURperMWH = AggregatorSalesPriceInEURperMWH;
		this.prosumersGridInteraction = ProsumersGridInteraction;
	}

	/** @return the modelId */
	public String getModelId() {
		return modelId;
	}

	/** @return the energyGenerationPerMW */
	public Map<Long, Double> getEnergyGenerationPerMW() {
		return energyGenerationPerMW;
	}

	/** @return the storedMWh */
	public Map<Long, Double> getStoredMWh() {
		return storedMWh;
	}

	/** @return the prosumersLoadInMW */
	public Map<Long, Double> getProsumersLoadInMW() {
		return prosumersLoadInMW;
	}

	/** @return the aggregatorSalesPriceInEURperMWH */
	public Map<Long, Double> getAggregatorSalesPriceInEURperMWH() {
		return aggregatorSalesPriceInEURperMWH;
	}

	/** @return the prosumersGridInteraction */
	public Map<Long, Double> getProsumersGridInteraction() {
		return prosumersGridInteraction;
	}

	/** @return the predictionStart */
	public long getPredictionStart() {
		return predictionStart;
	}
}
