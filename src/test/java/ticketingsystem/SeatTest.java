package ticketingsystem;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import junit.framework.*;

public class SeatTest {

	// public SeatTest(String testName) {
	// super(testName);
	// }

	// public static Test suite() {
	// TestSuite suite = new TestSuite(TicketingSystemTest.class);
	// return suite;
	// }

	@Test
	public void testcheckState() {
		assertEquals(Seat.checkState(1l, 2, 3), true);
		assertEquals(Seat.checkState(24l, 1, 3), true);
		assertEquals(Seat.toIntervalState(4, 5), Long.valueOf(8l));
		assertEquals(Seat.stateBuy(0l, 4, 5), Long.valueOf(8l));
		assertEquals(Seat.stateRefund(8l, 4, 5), Long.valueOf(0l));
	}
}
