// SPDX-FileCopyrightText: 2022 German Aerospace Center <amiris@dlr.de>
//
// SPDX-License-Identifier: Apache-2.0
package communications.message;

import agents.plantOperator.RenewablePlantOperator.SetType;
import agents.policy.PolicyItem.SupportInstrument;
import agents.policy.SupportPolicy.EnergyCarrier;
import de.dlr.gitlab.fame.communication.message.DataItem;
import de.dlr.gitlab.fame.protobuf.Agent.ProtoDataItem;
import de.dlr.gitlab.fame.protobuf.Agent.ProtoDataItem.Builder;

/** Info needed for registration of a producer for support payments
 * 
 * @author Johannes Kochems, Christoph Schimeczek */
public class TechnologySet extends DataItem {
	/** the set type */
	public final SetType setType;
	/** the energy carrier */
	public final EnergyCarrier energyCarrier;
	/** the support instrument for the technology set */
	public final SupportInstrument supportInstrument;
	/** installed capacity at this set - assumed constant */
	public final double installedCapacity;

	public TechnologySet(SetType technologySetType, EnergyCarrier energyCarrier,
			SupportInstrument supportInstrument, double installedCapacity) {
		this.setType = technologySetType;
		this.energyCarrier = energyCarrier;
		this.supportInstrument = supportInstrument;
		this.installedCapacity = installedCapacity;
	}

	/** Mandatory for deserialisation of {@link DataItem}s
	 * 
	 * @param proto protobuf representation */
	public TechnologySet(ProtoDataItem proto) {
		energyCarrier = EnergyCarrier.values()[proto.getIntValue(0)];
		setType = getOrNull(SetType.values(), proto.getIntValue(1));
		supportInstrument = getOrNull(SupportInstrument.values(), proto.getIntValue(2));
		installedCapacity = proto.getDoubleValue(0);
	}

	/** @return if given index >= 0: item with corresponding index in given array of choices, null otherwise */
	private <T extends Enum<T>> T getOrNull(T[] choices, int index) {
		if (index >= 0) {
			return choices[index];
		} else {
			return null;
		}
	}

	@Override
	protected void fillDataFields(Builder builder) {
		builder.addIntValue(energyCarrier.ordinal());
		builder.addIntValue(setType == null ? -1 : setType.ordinal());
		builder.addIntValue(supportInstrument == null ? -1 : supportInstrument.ordinal());
		builder.addDoubleValue(installedCapacity);
	}
}
