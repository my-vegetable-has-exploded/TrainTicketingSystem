package ticketingsystem;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class TicketingDS implements TicketingSystem {
	Route[] routes;
	int routenum;
	int coachnum;
	int seatnum;
	int stationnum;
	int threadnum;

	public TicketingDS(int routenum, int coachnum, int seatnum, int stationnum, int threadnum) {
		routes = new Route[routenum + 1];
		// activeIds = new HashSet<Long>();
		for (int i = 1; i <= routenum; i++) {
			routes[i] = new Route(i, coachnum, seatnum, stationnum, threadnum);
		}
		this.routenum = routenum;
		this.coachnum = coachnum;
		this.seatnum = seatnum;
		this.threadnum = threadnum;
		this.stationnum = stationnum;
		ThreadID.reset();
	}

	public Ticket buyTicket(String passenger, int route, int departure, int arrival) {
		return routes[route].buyTicket(passenger, departure, arrival);
	}

	public int inquiry(int route, int departure, int arrival) {
		return routes[route].inquiry(departure, arrival);
	}

	public boolean refundTicket(Ticket ticket) {
		return routes[ticket.route].refundTicket(ticket);
	}

	public boolean buyTicketReplay(Ticket ticket) {
		return false;
	}

	public boolean refundTicketReplay(Ticket ticket) {
		return false;
	}

}
