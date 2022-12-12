package ticketingsystem;

import java.util.HashSet;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class TicketingDS implements TicketingSystem {
	AtomicLong gID;
	long gobalID;
	View preView;
	volatile boolean isCacheViewUpdate;
	ReentrantReadWriteLock activeLock;
	HashSet<Long>activeIds;
	Route[] routes;
	int routenum;
	int coachnum;
	int seatnum;
	int stationnum;
	int threadnum;

	public TicketingDS(int routenum, int coachnum, int seatnum, int stationnum, int threadnum) {
		activeLock = new ReentrantReadWriteLock();
		gobalID = 1l;
		gID=new AtomicLong(1l);
		preView=null;
		isCacheViewUpdate=false;
		routes = new Route[routenum + 1];
		activeIds= new HashSet<Long>();
		for (int i = 1; i <= routenum; i++) {
			routes[i] = new Route(i, coachnum, seatnum, stationnum);
		}
		this.routenum = routenum;
		this.coachnum = coachnum;
		this.seatnum = seatnum;
		this.threadnum = threadnum;
		this.stationnum = stationnum;
	}

	public View createView() {
		return null;
		// if(isCacheViewUpdate){
		// 	return preView;
		// }
		// activeLock.readLock().lock();
		// try {
		// 	long viewId = gobalID;
		// 	HashSet<Long> actives = new HashSet<Long>();
		// 	actives.addAll(activeIds);
		// 	preView=new View(viewId, actives);
		// 	isCacheViewUpdate=true;
		// 	return preView;
		// } finally {
		// 	activeLock.readLock().unlock();
		// }
	}

	public long beginTrx() {
		return gID.getAndIncrement();
		// activeLock.writeLock().lock();
		// try {
		// 	isCacheViewUpdate = false;
		// 	gobalID += 1l;
		// 	long viewId = gobalID;
		// 	activeIds.add(viewId);
		// 	return viewId;
		// } finally {
		// 	activeLock.writeLock().unlock();
		// }
	}

	public boolean closeTrx(long versionId) {
		return true;
		// activeLock.writeLock().lock();
		// try {
		// 	isCacheViewUpdate = false;
		// 	activeIds.remove(versionId);
		// 	return true;
		// } finally {
		// 	activeLock.writeLock().unlock();
		// }
	}

	public long reopenTrx(long versionId) {
		activeLock.writeLock().lock();
		try {
			activeIds.remove(versionId);
			gobalID += 1l;
			long viewId = gobalID;
			activeIds.add(viewId);
			return viewId;
		} finally {
			activeLock.writeLock().unlock();
		}
	}

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
				closeTrx(versionId);
				return routes[route].commitBuyTicket(result.seatId);
			}
			closeTrx(versionId);
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
				closeTrx(versionId);
				routes[route].commitRefundTicket(result.seatId);
				return true;
			} else if (result.result == Result.TICKETNOTFOUND) {
				return false;
			}
			closeTrx(versionId);
		}
	}

	public boolean buyTicketReplay(Ticket ticket) {
		return false;
	}

	public boolean refundTicketReplay(Ticket ticket) {
		return false;
	}

}
