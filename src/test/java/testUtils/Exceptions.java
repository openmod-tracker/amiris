// SPDX-FileCopyrightText: 2022 German Aerospace Center <amiris@dlr.de>
//
// SPDX-License-Identifier: Apache-2.0
package testUtils;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertThrows;
import org.junit.function.ThrowingRunnable;

/** Utility functions to test that exceptions are thrown with proper error messages
 *
 * @author Christoph Schimeczek */
public final class Exceptions {
	/** asserts that given function throws an Exception of the given type containing the given message in its error message */
	public static void assertThrowsMessage(Class<? extends Exception> exceptionType, String message,
			ThrowingRunnable runnable) {
		Exception exception = assertThrows(exceptionType, runnable);
		assertThat(exception.getMessage(), containsString(message));
	}

	/** asserts that given function throws a RuntimeException containing the given message in its error message */
	public static void assertThrowsFatalMessage(String message, ThrowingRunnable runnable) {
		assertThrowsMessage(RuntimeException.class, message, runnable);
	}
}
