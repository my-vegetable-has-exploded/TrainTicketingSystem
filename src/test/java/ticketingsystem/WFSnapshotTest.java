/*
 * WFSnapshotTest.java
 * JUnit based test
 *
 * Created on January 12, 2006, 8:27 PM
 */
package ticketingsystem;

import java.util.ArrayList;

import junit.framework.*;

/**
 * Crude & inadequate test of snapshot class.
 * 
 * @author Maurice Herlihy
 */
public class WFSnapshotTest extends TestCase {
	private final static int THREADS = 2;
	private final static int FIRST = 11;
	private final static int SECOND = 22;
	Thread[] thread = new Thread[THREADS];
	int[][] results = new int[THREADS][THREADS];

	WFSnapshot<Integer> instance = new WFSnapshot<Integer>(THREADS, 0);

	public WFSnapshotTest(String testName) {
		super(testName);
	}

	public static Test suite() {
		TestSuite suite = new TestSuite(WFSnapshotTest.class);

		return suite;
	}

	/**
	 * Sequential calls.
	 */
	public void testSequential() {
		System.out.println("sequential");
		instance.update(FIRST);
		ArrayList<Integer> result = instance.scan();
		assertEquals(result.get(ThreadID.get()), Integer.valueOf(FIRST));
	}

	/**
	 * Parallel reads
	 */
	public void testParallel() throws Exception {
		System.out.println("parallel");
		ThreadID.reset();
		for (int i = 0; i < THREADS; i++) {
			thread[i] = new MyThread();
		}
		for (int i = 0; i < THREADS; i++) {
			thread[i].start();
		}
		for (int i = 0; i < THREADS; i++) {
			thread[i].join();
		}
		for (int i = 0; i < THREADS; i++) {
			for (int j = 0; j < i; j++) {
				assert (comparable(results[i], results[j]));
			}
		}
	}

	private boolean comparable(int[] a, int[] b) {
		boolean leq = false;
		boolean geq = false;
		for (int i = 0; i < a.length; i++) {
			if (a[i] < b[i]) {
				leq = true;
			} else if (a[i] > b[i]) {
				geq = true;
			}
		}
		return !(leq && geq);
	}

	class MyThread extends Thread {
		public void run() {
			instance.update(FIRST);
			instance.update(SECOND);
			ArrayList<Integer> a = instance.scan();
			for (Integer x : a) {
				int me = ThreadID.get();
				for (int j = 0; j < THREADS; j++) {
					results[me][j] = (Integer) x;
				}
			}
		}
	}
}
