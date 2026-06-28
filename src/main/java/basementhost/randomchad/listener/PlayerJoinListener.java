package basementhost.randomchad.listener;

import basementhost.randomchad.lang.LangManager;
import basementhost.randomchad.manager.DataManager;
import basementhost.randomchad.manager.PromoteManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.Map;

public class PlayerJoinListener implements Listener {

	private final DataManager dataManager;
	private final PromoteManager promoteManager;
	private final LangManager langManager;

	public PlayerJoinListener(DataManager dataManager, PromoteManager promoteManager, LangManager langManager) {
		this.dataManager = dataManager;
		this.promoteManager = promoteManager;
		this.langManager = langManager;
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();

		boolean alreadyHadData = dataManager.hasPlayer(player.getUniqueId());
		String code = promoteManager.getOrCreatePromoteCode(player);

		String messagePath = alreadyHadData ? "code-existing" : "code-generated";
		langManager.sendMessage(player, messagePath, Map.of("%code%", code));
	}
}