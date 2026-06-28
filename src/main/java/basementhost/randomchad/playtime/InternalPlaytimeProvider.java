package basementhost.randomchad.playtime;

import basementhost.randomchad.manager.DataManager;

import java.util.UUID;

public class InternalPlaytimeProvider implements PlaytimeProvider {

	private final DataManager dataManager;

	public InternalPlaytimeProvider(DataManager dataManager) {
		this.dataManager = dataManager;
	}

	@Override
	public long getPlaytimeSeconds(UUID playerUuid) {
		return dataManager.getTotalPlaytimeSeconds(playerUuid);
	}

	@Override
	public String getName() {
		return "internal";
	}
}