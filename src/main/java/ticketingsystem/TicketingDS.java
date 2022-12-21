package ticketingsystem;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class TicketingDS implements TicketingSystem {
	AtomicLong gobalID;
	// long gobalID;
	View preView;
	volatile boolean isCacheViewUpdate;
	ReentrantReadWriteLock activeLock;
	// HashSet<Long> activeIds;
	Route[] routes;
	int routenum;
	int coachnum;
	int seatnum;
	int stationnum;
	int threadnum;
	WFSnapshot<Long> theadViewId;

	public TicketingDS(int routenum, int coachnum, int seatnum, int stationnum, int threadnum) {
		activeLock = new ReentrantReadWriteLock();
		gobalID = new AtomicLong(1);
		preView = null;
		isCacheViewUpdate = false;
		routes = new Route[routenum + 1];
		// activeIds = new HashSet<Long>();
		for (int i = 1; i <= routenum; i++) {
			routes[i] = new Route(i, coachnum, seatnum, stationnum);
		}
		this.routenum = routenum;
		this.coachnum = coachnum;
		this.seatnum = seatnum;
		this.threadnum = threadnum;
		this.stationnum = stationnum;
		this.theadViewId = new WFSnapshot<Long>(threadnum, Long.valueOf(0));
		ThreadID.reset();
	}

	public View createView() {
		if (isCacheViewUpdate) {
			return preView;
		}
		Long viewId = gobalID.get();
		HashSet<Long> actives = new HashSet<Long>();
		ArrayList<Long> activeIds = theadViewId.scan();
		for (Long id : activeIds) {
			if (id != 0) {
				actives.add(id);
			}
		}
		preView = new View(viewId, actives);
		isCacheViewUpdate = true;
		return preView;
	}

	public long beginTrx() {
		Long viewId = gobalID.incrementAndGet();
		theadViewId.update(viewId);
		isCacheViewUpdate = false;
		return viewId;
	}

	public boolean closeTrx() {
		theadViewId.update(0L);
		isCacheViewUpdate = false;
		return true;
	}

	// public long reopenTrx(long versionId) {
	// 	activeLock.writeLock().lock();
	// 	try {
	// 		activeIds.remove(versionId);
	// 		gobalID += 1l;
	// 		long viewId = gobalID;
	// 		activeIds.add(viewId);
	// 		return viewId;
	// 	} finally {
	// 		activeLock.writeLock().unlock();
	// 	}
	// }

	public Ticket buyTicket(String passenger, int route, int departure, int arrival) {
		while (true) {
			View view = createView();
			int pos = routes[route].checkInquiry(view, departure, arrival);
			if (pos == -1) {
				return null;
			}
			long versionId = beginTrx();
			ResultSeat result = routes[route].tryBuyTicket(pos, versionId, passenger, departure, arrival);
			if (result.result == Result.SUCCESSED) {
				closeTrx();
				return routes[route].commitBuyTicket(result.seatId);
			}
			closeTrx();
		}
	}

	public int inquiry(int route, int departure, int arrival) {
		View view = createView();
		return routes[route].inquiry(view, departure, arrival);
	}

	public boolean refundTicket(Ticket ticket) {
		while (true) {
			long versionId = beginTrx();
			int route = ticket.route;
			ResultSeat result = routes[route].tryRefundTicket(versionId, ticket);
			if (result.result == Result.SUCCESSED) {
				closeTrx();
				routes[route].commitRefundTicket(result.seatId);
				return true;
			} else if (result.result == Result.TICKETNOTFOUND) {
				return false;
			}
			closeTrx();
		}
	}

	public boolean buyTicketReplay(Ticket ticket) {
		return false;
	}

	public boolean refundTicketReplay(Ticket ticket) {
		return false;
	}

}
