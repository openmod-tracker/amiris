// SPDX-FileCopyrightText: 2025 German Aerospace Center <amiris@dlr.de>
//
// SPDX-License-Identifier: Apache-2.0
package agents.policy.hydrogen;

import de.dlr.gitlab.fame.communication.Product;

public interface HydrogenSupportClient {
	@Product
	public enum Products {
		/** Request for support information for contracted technology set(s) */
		SupportInfoRequest,
		/** Request to obtain support payments for contracted technology set(s) */
		SupportPayoutRequest
	}
}
