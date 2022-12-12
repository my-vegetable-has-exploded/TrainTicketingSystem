package ticketingsystem;

public class VersionState {
	Long version;
	Long state;
	public VersionState(Long version, Long state) {
		this.version = version;
		this.state = state;
	}
	
	public Boolean isVisable(View view){
		if (version>view.viewId){
			return false;
		}else if (version<view.minId){
			return true;
		}else{
			if(view.activeIds.contains(version)){
				return false;
			}else{
				return true;
			}
		}
	}
}
