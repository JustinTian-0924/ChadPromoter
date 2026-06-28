package basementhost.randomchad.listener;

import basementhost.randomchad.manager.DataManager;
import basementhost.randomchad.manager.PromoteManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class PlayerJoinListener implements Listener {

	private final JavaPlugin plugin;
	private final DataManager dataManager;
	private final PromoteManager promoteManager;

	public PlayerJoinListener(JavaPlugin plugin, DataManager dataManager, PromoteManager promoteManager) {
		this.plugin = plugin;
		this.dataManager = dataManager;
		this.promoteManager = promoteManager;
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();

		boolean alreadyHadData = dataManager.hasPlayer(player.getUniqueId());
		String code = promoteManager.getOrCreatePromoteCode(player);

		String messagePath = alreadyHadData ? "messages.existing-code" : "messages.generated-code";
		String message = plugin.getConfig().getString(messagePath, "&a你的 ChadPromoter 邀请码是: &e%code%");

		player.sendMessage(color(message.replace("%code%", code)));
	}

	private String color(String message) {
		return ChatColor.translateAlternateColorCodes('&', message);
	}
}