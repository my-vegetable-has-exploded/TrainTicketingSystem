package ticketingsystem;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import junit.framework.*;

public class TicketingSystemTest extends TestCase {
	private final static int ROUTE_NUM = 5;
	private final static int COACH_NUM = 8;
	private final static int SEAT_NUM = 100;
	private final static int STATION_NUM = 10;

	private final static int TEST_NUM = 10000;
	private final static int refund = 10;
	private final static int buy = 30;
	private final static int query = 100;
	private final static int thread = 128;
	private final static long[] buyTicketTime = new long[thread];
	private final static long[] refundTime = new long[thread];
	private final static long[] inquiryTime = new long[thread];

	private final static long[] buyTotal = new long[thread];
	private final static long[] refundTotal = new long[thread];
	private final static long[] inquiryTotal = new long[thread];

	private final static AtomicInteger threadId = new AtomicInteger(0);

	public TicketingSystemTest(String testName) {
		super(testName);
	}

	public static Test suite() {
		TestSuite suite = new TestSuite(TicketingSystemTest.class);
		return suite;
	}

	static String passengerName() {
		Random rand = new Random();
		long uid = rand.nextInt(TEST_NUM);
		return "passenger" + uid;
	}

	public void testmain() throws InterruptedException {
		// final int[] threadNums = { 4, 8, 16, 32, 64 };
		final int[] threadNums = { 1, 2, 4 };
		System.out.printf("%d routes %d coachs %d seats %d stations %d operator\n", ROUTE_NUM, COACH_NUM, SEAT_NUM,
				STATION_NUM, TEST_NUM);
		int p;
		for (p = 0; p < threadNums.length; ++p) {
			final TicketingDS tds = new TicketingDS(ROUTE_NUM, COACH_NUM, SEAT_NUM, STATION_NUM, threadNums[p]);
			Thread[] threads = new Thread[threadNums[p]];
			for (int i = 0; i < threadNums[p]; i++) {
				threads[i] = new Thread(new Runnable() {
					public void run() {
						Random rand = new Random();
						Ticket ticket = new Ticket();
						int id = threadId.getAndIncrement();
						ArrayList<Ticket> soldTicket = new ArrayList<Ticket>();
						for (int i = 0; i < TEST_NUM; i++) {
							int sel = rand.nextInt(query);
							if (0 <= sel && sel < refund && soldTicket.size() > 0) { // refund ticket 0-10
								int select = rand.nextInt(soldTicket.size());
								if ((ticket = soldTicket.remove(select)) != null) {
									long s = System.currentTimeMillis();
									tds.refundTicket(ticket);
									long e = System.currentTimeMillis();
									refundTime[id] += e - s;
									refundTotal[id] += 1;
								} else {
									System.out.println("ErrOfRefund2");
								}
							} else if (refund <= sel && sel < buy) { // buy ticket 10-30
								String passenger = passengerName();
								int route = rand.nextInt(ROUTE_NUM) + 1;
								int departure = rand.nextInt(STATION_NUM - 1) + 1;
								int arrival = departure + rand.nextInt(STATION_NUM - departure) + 1;
								long s = System.currentTimeMillis();
								ticket = tds.buyTicket(passenger, route, departure, arrival);
								long e = System.currentTimeMillis();
								buyTicketTime[id] += e - s;
								buyTotal[id] += 1;
								if (ticket != null) {
									soldTicket.add(ticket);
								}
							} else if (buy <= sel && sel < query) { // inquiry ticket 30-100
								int route = rand.nextInt(ROUTE_NUM) + 1;
								int departure = rand.nextInt(STATION_NUM - 1) + 1;
								int arrival = departure + rand.nextInt(STATION_NUM - departure) + 1;
								long s = System.currentTimeMillis();
								tds.inquiry(route, departure, arrival);
								long e = System.currentTimeMillis();
								inquiryTime[id] += e - s;
								inquiryTotal[id] += 1;
							}
						}
					}
				});
			}
			long start = System.currentTimeMillis();
			for (int i = 0; i < threadNums[p]; ++i)
				threads[i].start();

			for (int i = 0; i < threadNums[p]; i++) {
				threads[i].join();
			}
			long end = System.currentTimeMillis();
			long buyTotalTime = calculateTotal(buyTicketTime, threadNums[p]);
			long refundTotalTime = calculateTotal(refundTime, threadNums[p]);
			long inquiryTotalTime = calculateTotal(inquiryTime, threadNums[p]);

			long bTotal = calculateTotal(buyTotal, threadNums[p]);
			long rTotal = calculateTotal(refundTotal, threadNums[p]);
			long iTotal = calculateTotal(inquiryTotal, threadNums[p]);

			double buyAvgTime = (double) (buyTotalTime) / bTotal;
			double refundAvgTime = (double) (refundTotalTime) / rTotal;
			double inquiryAvgTime = (double) (inquiryTotalTime) / iTotal;

			long time = end - start;

			long t = (long) (threadNums[p] * TEST_NUM / (double) time); // 1000是从ms转换为s

			System.out.println(String.format(
					"ThreadNum: %d BuyAvgTime(ms): %.5f RefundAvgTime(ms): %.5f InquiryAvgTime(ms): %.5f ThroughOut(op/ms): %d",
					threadNums[p], buyAvgTime, refundAvgTime, inquiryAvgTime, t));
			clear();
		}
	}

	private static long calculateTotal(long[] array, int threadNums) {
		long res = 0;
		for (int i = 0; i < threadNums; ++i)
			res += array[i];
		return res;
	}

	private static void clear() {
		threadId.set(0);
		long[][] arrays = { buyTicketTime, refundTime, inquiryTime, buyTotal, refundTotal, inquiryTotal };
		for (int i = 0; i < arrays.length; ++i)
			for (int j = 0; j < arrays[i].length; ++j)
				arrays[i][j] = 0;
	}

}
