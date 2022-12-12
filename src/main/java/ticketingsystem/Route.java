package ticketingsystem;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

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
	Seat[] seats;

	public Route(int routeId, int coachnum, int seatnum, int stationnum) {
		this.routeId = routeId;
		this.coachnum = coachnum;
		this.seatnum = seatnum;
		totalseats = coachnum * seatnum;
		seats = new Seat[totalseats];
		for (int i = 0; i < coachnum; i++) {
			for (int j = 0; j < seatnum; j++) {
				seats[i * seatnum + j] = new Seat(routeId, i + 1, j + 1, stationnum);
			}
		}
	}

	public int inquiry(View view, int departure, int arrival) {
		int restTicket = 0;
		for (int i = 0; i < totalseats; i += 1) {
			if (seats[i].isViewedAvaliable(view, departure, arrival)) {
				restTicket += 1;
			}
		}
		return restTicket;
	}

	public int checkInquiry(View view, int departure, int arrival) {
		int position = ThreadLocalRandom.current().nextInt(0, totalseats);
		for (int i = 0; i < coachnum * seatnum; i += 1) {
			if (seats[position].isViewedAvaliable(view, departure, arrival)) {
				return position;
			}
			position = (position + 1) % totalseats;
		}
		return -1;
	}

	public ResultSeat tryBuyTicket(int startPos, Long version, String passenger,int departure, int arrival) {
		int position = startPos;
		for (int i = 0; i < coachnum * seatnum; i += 1) {
			if (seats[position].tryBuyTicket(version, passenger, departure, arrival)==Result.SUCCESSED) {
				return new ResultSeat(Result.SUCCESSED, position);
			}
			position = (position + 1) % totalseats;
		}
		return new ResultSeat(Result.NOTAVAILABLE, -1);
	}
	
	public Ticket commitBuyTicket(int position){
		return seats[position].commitBuyTicket();
	}

	public ResultSeat tryRefundTicket(Long version, Ticket ticket){
		int position=(ticket.coach-1)*seatnum+ticket.seat-1;
		Result result=seats[position].tryRefundTicket(version, ticket);
		return new ResultSeat(result, position);
	}
	
	
	public Ticket commitRefundTicket(int position){
		return seats[position].commitBuyTicket();
	}

}
