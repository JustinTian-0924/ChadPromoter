package basementhost.randomchad.command;

import basementhost.randomchad.lang.LangManager;
import basementhost.randomchad.manager.DataManager;
import basementhost.randomchad.manager.GuiConfigManager;
import basementhost.randomchad.manager.RewardManager;
import basementhost.randomchad.playtime.PlaytimeManager;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class ReloadCommandHandler {

	private final JavaPlugin plugin;
	private final LangManager langManager;
	private final DataManager dataManager;
	private final PlaytimeManager playtimeManager;
	private final RewardManager rewardManager;
	private final GuiConfigManager guiConfigManager;

	public ReloadCommandHandler(
			JavaPlugin plugin,
			LangManager langManager,
			DataManager dataManager,
			PlaytimeManager playtimeManager,
			RewardManager rewardManager,
			GuiConfigManager guiConfigManager
	) {
		this.plugin = plugin;
		this.langManager = langManager;
		this.dataManager = dataManager;
		this.playtimeManager = playtimeManager;
		this.rewardManager = rewardManager;
		this.guiConfigManager = guiConfigManager;
	}

	public void handleReload(CommandSender sender) {
		if (!sender.hasPermission("chadpromoter.reload") && !sender.hasPermission("chadpromoter.admin")) {
			langManager.sendMessage(sender, "reload-no-permission");
			return;
		}

		plugin.reloadConfig();
		langManager.reload();
		dataManager.loadPlayersData();
		playtimeManager.reload();
		rewardManager.reload();
		guiConfigManager.reload();

		langManager.sendMessage(sender, "reload-success");
	}
}