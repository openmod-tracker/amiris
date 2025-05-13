// SPDX-FileCopyrightText: 2025 German Aerospace Center <amiris@dlr.de>
//
// SPDX-License-Identifier: Apache-2.0
package agents.forecast.sensitivity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import communications.message.AmountAtTime;
import de.dlr.gitlab.fame.time.TimeStamp;

public class FlexibilityAssessorTest {
	private FlexibilityAssessor assessor;

	@Test
	public void getMultiplier_noClientRegistered_returnsOne() {
		assessor = new FlexibilityAssessor(100.);
		assertEquals(1.0, assessor.getMultiplier(0L), 1E-12);
	}

	@Test
	public void getMultiplier_someClientsRegisterButNotTheRequested_returnsOne() {
		assessor = new FlexibilityAssessor(100.);
		assessor.registerInstalledPower(0L, 100);
		assessor.registerInstalledPower(1L, 200);
		assertEquals(1.0, assessor.getMultiplier(42L), 1E-12);
	}

	@Test
	public void getMultiplier_clientRegisteredNoPower_returnsOne() {
		assessor = new FlexibilityAssessor(100.);
		assessor.registerInstalledPower(0L, 0);
		assessor.registerInstalledPower(1L, 100);
		assertEquals(1.0, assessor.getMultiplier(0L), 1E-12);
	}

	@Test
	public void getMultiplier_clientRegisteredSimilar_returnsOneOverInstallShare() {
		assessor = new FlexibilityAssessor(100.);
		assessor.registerInstalledPower(0L, 100);
		assessor.registerInstalledPower(1L, 100);
		assertEquals(2, assessor.getMultiplier(0L), 1E-12);
	}

	@Test
	public void getMultiplier_clientRegisteredDifferent_returnsOneOverInstallShare() {
		assessor = new FlexibilityAssessor(100.);
		assessor.registerInstalledPower(0L, 100);
		assessor.registerInstalledPower(1L, 200);
		assertEquals(3, assessor.getMultiplier(0L), 1E-12);
		assertEquals(1.5, assessor.getMultiplier(1L), 1E-12);
	}

	@Test
	public void getMultiplier_clientSentAwardsOneRound_returnsInverseAwardShare() {
		assessor = new FlexibilityAssessor(100.);
		saveAwards(0L, 20, 80);
		assessor.processAwards();
		assertEquals(5., assessor.getMultiplier(0L), 1E-12);
		assertEquals(1.25, assessor.getMultiplier(1L), 1E-12);
	}

	private void saveAwards(long timeStep, double... amounts) {
		for (int clientId = 0; clientId < amounts.length; clientId++) {
			var award = new AmountAtTime(new TimeStamp(timeStep), amounts[clientId]);
			assessor.saveAward((long) clientId, award);
		}
	}

	@Test
	public void getMultiplier_clientSentAwardsOneRoundTwoTimes_returnsInverseAwardShareAverage() {
		assessor = new FlexibilityAssessor(100.);
		saveAwards(0L, 50, 50);
		saveAwards(1L, 20, 80);
		assessor.processAwards();
		assertEquals(3.5, assessor.getMultiplier(0L), 1E-12);
		assertEquals(1.625, assessor.getMultiplier(1L), 1E-12);
	}

	@Test
	public void getMultiplier_clientSentAwardsTwoRounds_returnsInverseAwardShareAverage() {
		assessor = new FlexibilityAssessor(100.);
		saveAwards(0L, 20, 30);
		assessor.processAwards();
		saveAwards(1L, 10, 40);
		assessor.processAwards();
		assertEquals((5. / 2. + 5.) / 2., assessor.getMultiplier(0L), 1E-12);
		assertEquals((5. / 3. + 5. / 4.) / 2., assessor.getMultiplier(1L), 1E-12);
	}

	@Test
	public void getMultiplier_awardsCompressed_returnsInverseAwardShareAverage() {
		assessor = new FlexibilityAssessor(100.);
		saveAwards(0L, 50, 50);
		saveAwards(1L, 20, 80);
		assessor.processAwards();
		assessor.clearBefore(new TimeStamp(2L));
		assertEquals(3.5, assessor.getMultiplier(0L), 1E-12);
		assertEquals(1.625, assessor.getMultiplier(1L), 1E-12);
	}

	@Test
	public void getMultiplier_awardsCompressedPlusAnotherRound_returnsInverseAwardShareAverage() {
		assessor = new FlexibilityAssessor(100.);
		saveAwards(0L, 50, 50);
		saveAwards(1L, 20, 80);
		assessor.processAwards();
		assessor.clearBefore(new TimeStamp(2L));
		assessor.processAwards();
		saveAwards(3L, 10, 90);
		assessor.processAwards();
		assertEquals((2. + 5. + 10.) / 3., assessor.getMultiplier(0L), 1E-12);
		assertEquals((2. + 10. / 8. + 10. / 9.) / 3., assessor.getMultiplier(1L), 1E-12);
	}
}
