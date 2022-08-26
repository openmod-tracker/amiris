// SPDX-FileCopyrightText: 2022 German Aerospace Center <amiris@dlr.de>
//
// SPDX-License-Identifier: Apache-2.0
package agents.policy;

import de.dlr.gitlab.fame.agent.input.Make;
import de.dlr.gitlab.fame.agent.input.Tree;

public class CfdInfo extends MpvarInfo {
	public static final Tree parameters = Make.newTree().add(Make.newSeries("Lcoe").optional()).buildTree();
}
