package ticketingsystem;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

class ResultSeat {
	Result result;
	int seatId;

	public ResultSeat(Result result, int seatId) {
		this.result = result;
		this.seatId = seatId;
	}
}

public class Route {
	int routeId;
	int coachnum;
	int seatnum;
	int stationnum;
	int totalseats;
	int threadnum;
	Seat[] seats;
	AtomicLong gobalID;
	View preView;
	WFSnapshot<Long> theadViewId;
	// long gobalID;
	// ReentrantReadWriteLock activeLock;

	public Route(int routeId, int coachnum, int seatnum, int stationnum, int threadnum) {
		this.routeId = routeId;
		this.coachnum = coachnum;
		this.seatnum = seatnum;
		this.stationnum = stationnum;
		this.threadnum = threadnum;
		totalseats = coachnum * seatnum;
		seats = new Seat[totalseats];
		for (int i = 0; i < coachnum; i++) {
			for (int j = 0; j < seatnum; j++) {
				seats[i * seatnum + j] = new Seat(routeId, i + 1, j + 1, stationnum);
			}
		}
		gobalID = new AtomicLong(1);
		preView = null;
		this.theadViewId = new WFSnapshot<Long>(threadnum, Long.valueOf(0));
	}

	public View createView() {
		// if (isCacheViewUpdate) {
		// return preView;
		// }
		Long viewId = gobalID.get();
		HashSet<Long> actives = new HashSet<Long>();
		ArrayList<Long> activeIds = theadViewId.scan();
		for (Long id : activeIds) {
			if (id != 0) {
				actives.add(id);
			}
		}
		return new View(viewId, actives);
		// isCacheViewUpdate = true;
		// return preView;
	}

	public long beginTrx() {
		Long viewId = gobalID.incrementAndGet();
		theadViewId.update(viewId);
		// isCacheViewUpdate = false;
		return viewId;
	}

	public boolean closeTrx() {
		theadViewId.update(0L);
		// isCacheViewUpdate = false;
		return true;
	}

	// public long reopenTrx(long versionId) {
	// activeLock.writeLock().lock();
	// try {
	// activeIds.remove(versionId);
	// gobalID += 1l;
	// long viewId = gobalID;
	// activeIds.add(viewId);
	// return viewId;
	// } finally {
	// activeLock.writeLock().unlock();
	// }
	// }

	public Ticket buyTicket(String passenger, int departure, int arrival) {
		while (true) {
			int pos = checkInquiry(departure, arrival);
			if (pos == -1) {
				return null;
			}
			long versionId = beginTrx();
			ResultSeat result = tryBuyTicket(pos, versionId, passenger, departure, arrival);
			if (result.result == Result.SUCCESSED) {
				closeTrx();
				return commitBuyTicket(result.seatId);
			}
			closeTrx();
		}
	}

	public boolean refundTicket(Ticket ticket) {
		while (true) {
			long versionId = beginTrx();
			ResultSeat result = tryRefundTicket(versionId, ticket);
			if (result.result == Result.SUCCESSED) {
				closeTrx();
				commitRefundTicket(result.seatId);
				return true;
			} else if (result.result == Result.TICKETNOTFOUND) {
				closeTrx();
				return false;
			}
			closeTrx();
		}
	}

	public int inquiry(int departure, int arrival) {
		View view = createView();
		int restTicket = 0;
		for (int i = 0; i < totalseats; i += 1) {
			if (seats[i].isViewedAvaliable(view, departure, arrival)) {
				restTicket += 1;
			}
		}
		return restTicket;
	}

	public int checkInquiry(int departure, int arrival) {
		View view = createView();
		int position = ThreadLocalRandom.current().nextInt(0, totalseats);
		for (int i = 0; i < coachnum * seatnum; i += 1) {
			if (seats[position].isViewedAvaliable(view, departure, arrival)) {
				return position;
			}
			position = (position + 1) % totalseats;
		}
		return -1;
	}

	public ResultSeat tryBuyTicket(int startPos, long version, String passenger, int departure, int arrival) {
		int position = startPos;
		for (int i = 0; i < coachnum * seatnum; i += 1) {
			if (seats[position].tryBuyTicket(version, passenger, departure, arrival) == Result.SUCCESSED) {
				return new ResultSeat(Result.SUCCESSED, position);
			}
			position = (position + 1) % totalseats;
		}
		return new ResultSeat(Result.NOTAVAILABLE, -1);
	}

	public Ticket commitBuyTicket(int position) {
		return seats[position].commitBuyTicket();
	}

	public ResultSeat tryRefundTicket(long version, Ticket ticket) {
		int position = (ticket.coach - 1) * seatnum + ticket.seat - 1;
		Result result = seats[position].tryRefundTicket(version, ticket);
		return new ResultSeat(result, position);
	}

	public Ticket commitRefundTicket(int position) {
		return seats[position].commitBuyTicket();
	}

}
