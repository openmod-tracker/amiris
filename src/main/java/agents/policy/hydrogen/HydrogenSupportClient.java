// SPDX-FileCopyrightText: 2025 German Aerospace Center <amiris@dlr.de>
//
// SPDX-License-Identifier: Apache-2.0
package agents.policy.hydrogen;

import java.util.ArrayList;
import java.util.List;
import agents.policy.hydrogen.PolicyItem.SupportInstrument;
import communications.message.AmountAtTime;
import communications.message.HydrogenPolicyRegistration;
import communications.portable.HydrogenSupportData;
import de.dlr.gitlab.fame.agent.AgentAbility;
import de.dlr.gitlab.fame.agent.input.Make;
import de.dlr.gitlab.fame.agent.input.ParameterData;
import de.dlr.gitlab.fame.agent.input.ParameterData.MissingDataException;
import de.dlr.gitlab.fame.agent.input.Tree;
import de.dlr.gitlab.fame.communication.CommUtils;
import de.dlr.gitlab.fame.communication.Contract;
import de.dlr.gitlab.fame.communication.Product;
import de.dlr.gitlab.fame.communication.message.Message;
import de.dlr.gitlab.fame.service.output.Output;

public interface HydrogenSupportClient extends AgentAbility {
	static final Tree parameters = Make.newTree().optional()
			.add(Make.newEnum("SupportInstrument", SupportInstrument.class),
					HydrogenSupportProvider.setParameter)
			.buildTree();

	@Product
	public enum Products {
		/** Request for support information for contracted technology set(s) */
		SupportInfoRequest,
		/** Request to obtain support payments for contracted technology set(s) */
		SupportPayoutRequest
	}

	/** Available output columns */
	@Output
	public static enum Outputs {
		/** Received support for hydrogen in EUR */
		ReceivedHydrogenSupportInEUR
	}

	public static HydrogenPolicyRegistration getRegistration(ParameterData input) throws MissingDataException {
		if (input != null) {
			return new HydrogenPolicyRegistration(HydrogenSupportProvider.readSet(input),
					input.getEnum("SupportInstrument", SupportInstrument.class));
		}
		return null;
	}

	public default void registerSupport(ArrayList<Message> __, List<Contract> contracts) {
		HydrogenPolicyRegistration registrationData = getRegistrationData();
		if (registrationData != null) {
			Contract contract = CommUtils.getExactlyOneEntry(contracts);
			fulfilNext(contract, registrationData);
		}
	}

	public abstract HydrogenPolicyRegistration getRegistrationData();

	public default void digestSupportInfo(ArrayList<Message> messages, List<Contract> __) {
		Message message = CommUtils.getExactlyOneEntry(messages);
		HydrogenSupportData hydrogenSupportData = message.getFirstPortableItemOfType(HydrogenSupportData.class);
		saveSupportData(hydrogenSupportData);
	}

	public abstract void saveSupportData(HydrogenSupportData hydrogenSupportData);

	public default void digestSupportPayout(ArrayList<Message> messages, List<Contract> __) {
		AmountAtTime support = CommUtils.getExactlyOneEntry(messages).getDataItemOfType(AmountAtTime.class);
		store(Outputs.ReceivedHydrogenSupportInEUR, support.amount);
	}
}
