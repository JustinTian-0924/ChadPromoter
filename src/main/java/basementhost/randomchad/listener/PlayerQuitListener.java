package basementhost.randomchad.listener;

import basementhost.randomchad.manager.DataManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {

	private final DataManager dataManager;

	public PlayerQuitListener(DataManager dataManager) {
		this.dataManager = dataManager;
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		dataManager.stopPlaytimeSession(event.getPlayer().getUniqueId());
	}
}