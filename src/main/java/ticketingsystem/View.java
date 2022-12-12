package ticketingsystem;

import java.util.HashSet;

public class View {
	long viewId;
	HashSet<Long>activeIds;
	long minId;

	public View(long viewId, HashSet<Long> activeIds){
		this.viewId=viewId;
		this.activeIds=activeIds;
		minId=Long.MAX_VALUE;
		for (long id : activeIds) {
			minId=Long.min(id, minId);
		}
	}

}
