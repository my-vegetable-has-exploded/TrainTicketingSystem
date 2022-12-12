package ticketingsystem;

import java.util.HashSet;

public class View {
	Long viewId;	
	HashSet<Long> activeIds;
	Long minId;
	
	public View(Long viewId, HashSet<Long> activeIds){
		this.viewId=viewId;
		this.activeIds=activeIds;
		minId=Long.MAX_VALUE;
		for (Long id : activeIds) {
			minId=Long.min(id, minId);
		}
	}

}
