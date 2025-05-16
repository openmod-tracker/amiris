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
		assessor = new FlexibilityAssessor(100., 2);
		assertEquals(1.0, assessor.getMultiplier(0L), 1E-12);
	}

	@Test
	public void getMultiplier_someClientsRegisterButNotTheRequested_returnsOne() {
		assessor = new FlexibilityAssessor(100., 2);
		registerClients(100, 200);
		assertEquals(1.0, assessor.getMultiplier(42L), 1E-12);
	}

	/** Registers clients with increasing ID (starting at 0) and given power(s) */
	private void registerClients(double... powers) {
		for (int clientId = 0; clientId < powers.length; clientId++) {
			assessor.registerClient(clientId, powers[clientId]);
		}
		assessor.processInput();
	}

	@Test
	public void getMultiplier_clientRegisteredNoPower_returnsOne() {
		assessor = new FlexibilityAssessor(100., 2);
		registerClients(0, 100);
		assertEquals(1.0, assessor.getMultiplier(0L), 1E-12);
	}

	@Test
	public void getMultiplier_clientRegisteredSimilar_returnsOneOverInstallShare() {
		assessor = new FlexibilityAssessor(100., 2);
		registerClients(100, 100);
		assertEquals(2, assessor.getMultiplier(0L), 1E-12);
	}

	@Test
	public void getMultiplier_clientRegisteredDifferent_returnsOneOverInstallShare() {
		assessor = new FlexibilityAssessor(100., 2);
		registerClients(100, 200);
		assertEquals(3, assessor.getMultiplier(0L), 1E-12);
		assertEquals(1.5, assessor.getMultiplier(1L), 1E-12);
	}

	@Test
	public void getMultiplier_clientRegisteredMultipleTimes_returnsShareBasedOnFirstRegistration() {
		assessor = new FlexibilityAssessor(100., 2);
		assessor.registerClient(0L, 100);
		assessor.registerClient(1L, 200);
		assessor.registerClient(0L, 10000);
		assessor.processInput();
		assertEquals(3, assessor.getMultiplier(0L), 1E-12);
		assertEquals(1.5, assessor.getMultiplier(1L), 1E-12);
	}

	@Test
	public void getMultiplier_initialWeightOneAndOneRoundOfAwards_returnsInverseShareAverage() {
		assessor = new FlexibilityAssessor(100., 1);
		registerClients(100, 100);
		saveAwards(0L, 20, 80);
		assessor.processInput();
		assertEquals((2 + 5) / 2., assessor.getMultiplier(0L), 1E-12);
		assertEquals((2 + 1.25) / 2., assessor.getMultiplier(1L), 1E-12);
	}

	/** Save awards at given time to clients with increasing ID starting at 0 */
	private void saveAwards(long timeStep, double... amounts) {
		for (int clientId = 0; clientId < amounts.length; clientId++) {
			var award = new AmountAtTime(new TimeStamp(timeStep), amounts[clientId]);
			assessor.saveAward((long) clientId, award);
		}
	}

	@Test
	public void getMultiplier_initialWeightTwoAndOneRoundOfAwards_returnsInverseShareAverage() {
		assessor = new FlexibilityAssessor(100., 2);
		registerClients(100, 100);
		saveAwards(0L, 20, 80);
		assessor.processInput();
		assertEquals((2 * 2 + 5) / 3., assessor.getMultiplier(0L), 1E-12);
		assertEquals((2 * 2 + 1.25) / 3., assessor.getMultiplier(1L), 1E-12);
	}

	@Test
	public void getMultiplier_initialWeightNegative_returnsInverseShareForWeightOne() {
		assessor = new FlexibilityAssessor(100., -1);
		registerClients(100, 100);
		saveAwards(0L, 20, 80);
		assessor.processInput();
		assertEquals((2 * 1 + 5) / 2., assessor.getMultiplier(0L), 1E-12);
		assertEquals((2 * 1 + 1.25) / 2., assessor.getMultiplier(1L), 1E-12);
	}

	@Test
	public void getMultiplier_clientSentAwardsOneRoundTwoTimes_returnsInverseShareAverage() {
		assessor = new FlexibilityAssessor(100., 3);
		registerClients(100, 100);
		saveAwards(0L, 50, 50);
		saveAwards(1L, 20, 80);
		assessor.processInput();
		assertEquals((2 * 3 + 2 + 5) / 5., assessor.getMultiplier(0L), 1E-12);
		assertEquals((2 * 3 + 2 + 1.25) / 5., assessor.getMultiplier(1L), 1E-12);
	}

	@Test
	public void getMultiplier_clientSentAwardsTwoRounds_returnsInverseShareAverage() {
		assessor = new FlexibilityAssessor(100., 5);
		registerClients(10, 90);
		saveAwards(0L, 20, 30);
		assessor.processInput();
		saveAwards(1L, 10, 40);
		assessor.processInput();
		assertEquals((10 * 5 + 5. / 2. + 5.) / 7., assessor.getMultiplier(0L), 1E-12);
		assertEquals((10. / 9. * 5 + 5. / 3. + 5. / 4.) / 7., assessor.getMultiplier(1L), 1E-12);
	}

	@Test
	public void getMultiplier_awardsCompressed_returnsInverseShareAverage() {
		assessor = new FlexibilityAssessor(100., 2);
		registerClients(100, 200);
		saveAwards(0L, 50, 50);
		saveAwards(1L, 20, 80);
		assessor.processInput();
		assessor.clearBefore(new TimeStamp(2L));
		assertEquals((3 * 2 + 2 + 5) / 4., assessor.getMultiplier(0L), 1E-12);
		assertEquals((3. / 2. * 2 + 2 + 1.25) / 4., assessor.getMultiplier(1L), 1E-12);
	}

	@Test
	public void getMultiplier_awardsCompressedPlusAnotherRound_returnsInverseShareAverage() {
		assessor = new FlexibilityAssessor(100., 1);
		registerClients(100, 400);
		saveAwards(0L, 50, 50);
		saveAwards(1L, 20, 80);
		assessor.processInput();
		assessor.clearBefore(new TimeStamp(2L));
		assessor.processInput();
		saveAwards(3L, 10, 90);
		assessor.processInput();
		assertEquals((5 + 2. + 5. + 10.) / 4., assessor.getMultiplier(0L), 1E-12);
		assertEquals((5. / 4. + 2. + 10. / 8. + 10. / 9.) / 4., assessor.getMultiplier(1L), 1E-12);
	}

	@Test
	public void getMultiplier_zeroAward_ignoredForCalculations() {
		assessor = new FlexibilityAssessor(100., 1);
		registerClients(100, 200);
		saveAwards(0L, 0, 50);
		saveAwards(1L, 20, 80);
		assessor.processInput();
		assertEquals((3 + 5.) / 2., assessor.getMultiplier(0L), 1E-12);
		assertEquals((3. / 2. + 1 + 10. / 8.) / 3., assessor.getMultiplier(1L), 1E-12);
	}
}
