package ticketingsystem;

public class VersionState {
	long version;
	long state;

	public VersionState(long version, long state) {
		this.version = version;
		this.state = state;
	}

	public Boolean isVisable(View view) {
		if (version > view.viewId) {
			return false;
		} else if (version < view.minId) {
			return true;
		} else {
			if (view.activeIds.contains(Long.valueOf(version))) {
				return false;
			} else {
				return true;
			}
		}
	}
}
