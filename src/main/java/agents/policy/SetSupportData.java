package agents.policy;

import java.util.EnumMap;
import agents.policy.SupportPolicy.SupportInstrument;

/** Class holding all set-specific support information for the policy agent
 * 
 * @author Johannes Kochems, Christoph Schimeczek */
public class SetSupportData {
	private final EnumMap<SupportInstrument, PolicyInfo> infos = new EnumMap<>(SupportInstrument.class);

	public SetSupportData(FitInfo fitInfo, MpvarInfo mpvarInfo, MpfixInfo mpfixInfo, CfdInfo cfdInfo,
			CPInfo cpInfo) {
		infos.put(SupportInstrument.FIT, fitInfo);
		infos.put(SupportInstrument.MPVAR, mpvarInfo);
		infos.put(SupportInstrument.MPFIX, mpfixInfo);
		infos.put(SupportInstrument.CFD, cfdInfo);
		infos.put(SupportInstrument.CP, cpInfo);
	}

	public PolicyInfo getInfoFor(SupportInstrument instrument) {
		return infos.get(instrument);
	}
}
