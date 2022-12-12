package ticketingsystem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.locks.ReentrantLock;

public class Seat {
	ReentrantLock seatLock;
	HashMap<Long,Ticket>soldTickets;
	ArrayList<VersionState> versionStates;
	int routeId;
	int coachId;
	int seatId;
	int stationnum;
	volatile int tail;

	public Seat(int routeId, int coachId, int seatId, int stationnum) {
		seatLock = new ReentrantLock();
		soldTickets = new HashMap<Long, Ticket>();
		this.routeId = routeId;
		this.coachId = coachId;
		this.seatId = seatId;
		this.stationnum = stationnum;
		versionStates = new ArrayList<VersionState>();
		versionStates.add(new VersionState(0l, 0l));
		tail = 0;
	}

	public Ticket issueTicket(String passenger, long tid, int route, int coach, int seat, int departure, int arrival) {
		Ticket ticket = new Ticket();
		ticket.tid = tid;
		ticket.route = route;
		ticket.coach = coach;
		ticket.seat = seat;
		ticket.departure = departure;
		ticket.arrival = arrival;
		ticket.passenger = passenger;
		return ticket;
	}

	public boolean isLocked() {
		return seatLock.isLocked();
	}

	public long readView(View view) {
		int localTail = tail;
		while (localTail >= 0) {
			if (versionStates.get(localTail).isVisable(view)) {
				break;
			}
			localTail -= 1;
		}
		return versionStates.get(localTail).state;
	}

	static long toIntervalState(int departure, int arrival) {
		return (1l << (arrival - 1)) - (1l << (departure - 1));
	}

	public static boolean checkState(long state, int departure, int arrival) {
		// long intervalState = toIntervalState(departure, arrival);
		return (state & (1l << (arrival - 1)) - (1l << (departure - 1))) == 0;
	}

	public static long stateBuy(long state, int departure, int arrival) {
		return state | toIntervalState(departure, arrival);
	}

	public static long stateRefund(long state, int departure, int arrival) {
		return state & (~toIntervalState(departure, arrival));
	}

	public boolean isViewedAvaliable(View view, int departure, int arrival) {
		long viewState = readView(view);
		return checkState(viewState, departure, arrival);
	}

	public boolean isCurrentAvaliable(View view, int departure, int arrival) {
		long state = versionStates.get(tail).state;
		return !isLocked() && checkState(state, departure, arrival);
	}

	public Result tryBuyTicket(long version, String passenger, int departure, int arrival) {
		VersionState lastState = versionStates.get(tail);
		if (lastState.version > version) { // TODO optimize control
			return Result.SMALLVERSION;
		}
		if (!checkState(lastState.state, departure, arrival)) {
			return Result.NOTAVAILABLE;
		}
		seatLock.lock();
		lastState = versionStates.get(tail);
		if (lastState.version > version) {
			seatLock.unlock();
			return Result.SMALLVERSION;
		}
		if (checkState(lastState.state, departure, arrival)) {
			versionStates.add(new VersionState(version, stateBuy(lastState.state, departure, arrival)));
			tail += 1;
			soldTickets.put(version, issueTicket(passenger, version, routeId, coachId, seatId, departure, arrival));
			return Result.SUCCESSED;
		} else {
			seatLock.unlock();
			return Result.NOTAVAILABLE;
		}
	}

	public Result tryRefundTicket(long version, Ticket ticket) {
		VersionState lastState = versionStates.get(tail);
		if (lastState.version > version) {
			return Result.SMALLVERSION;
		}
		if (!soldTickets.containsKey(ticket.tid)) {
			return Result.TICKETNOTFOUND;
		}
		seatLock.lock();
		lastState = versionStates.get(tail);
		if (lastState.version > version) { // TODO need?
			seatLock.unlock();
			return Result.SMALLVERSION;
		}
		if (soldTickets.containsKey(ticket.tid)) {
			versionStates.add(new VersionState(version,
					stateRefund(lastState.state, ticket.departure, ticket.arrival)));
			tail += 1;
			soldTickets.remove(ticket.tid);
			return Result.SUCCESSED;
		} else {
			seatLock.unlock();
			return Result.TICKETNOTFOUND;
		}
	}

	// public Result tryBuyTicket(long version, String passenger, int tid, int
	// departure, int arrival) {
	// // TODO check first;
	// if (seatLock.tryLock()) {
	// VersionState lastState = versionStates.get(tail);
	// if (lastState.version > version) {
	// seatLock.unlock();
	// return Result.SMALLVERSION;
	// }
	// if (checkState(lastState.state, departure, arrival)) {
	// versionStates.add(new VersionState(version, lastState.state |
	// toIntervalState(departure, arrival)));
	// tail += 1;
	// soldTickets.put(version, issueTicket(passenger, version, routeId, coachId,
	// seatId, departure, arrival));
	// return Result.SUCCESSED;
	// } else {
	// seatLock.unlock();
	// return Result.NOTAVAILABLE;
	// }
	// } else {
	// return Result.LOCKFAILED;
	// }
	// }

	// public Result tryRefundTicket(long version, Ticket ticket) {
	// if (seatLock.tryLock()) {
	// VersionState lastState = versionStates.get(tail);
	// if (lastState.version > version) {
	// seatLock.unlock();
	// return Result.SMALLVERSION;
	// }
	// if (soldTickets.containsKey(ticket.tid)) {
	// versionStates.add(new VersionState(version,
	// lastState.state & (~toIntervalState(ticket.departure, ticket.arrival))));
	// tail += 1;
	// soldTickets.remove(ticket.tid);
	// return Result.SUCCESSED;
	// } else {
	// seatLock.unlock();
	// return Result.TICKETNOTFOUND;
	// }
	// } else {
	// return Result.LOCKFAILED;
	// }
	// }

	public Ticket commitBuyTicket() {
		VersionState lastState = versionStates.get(tail);
		Ticket ticket = soldTickets.get(lastState.version);
		seatLock.unlock();
		return ticket;
	}

	public Boolean commitRefundTicket() {
		seatLock.unlock();
		return true;
	}
}
