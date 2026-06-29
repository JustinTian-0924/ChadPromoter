package basementhost.randomchad.command;

import basementhost.randomchad.lang.LangManager;
import basementhost.randomchad.manager.DataManager;
import basementhost.randomchad.manager.GuiManager;
import basementhost.randomchad.manager.PromoteManager;
import basementhost.randomchad.manager.RewardManager;
import basementhost.randomchad.playtime.PlaytimeManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Map;

public class ChadPromoterCommand implements TabExecutor {

	private final PromoteManager promoteManager;
	private final LangManager langManager;
	private final GuiManager guiManager;
	private final JavaPlugin plugin;
	private final PlaytimeManager playtimeManager;
	private final DataManager dataManager;
	private final RewardManager rewardManager;

	public ChadPromoterCommand(
			JavaPlugin plugin,
			PromoteManager promoteManager,
			LangManager langManager,
			GuiManager guiManager,
			PlaytimeManager playtimeManager,
			DataManager dataManager,
			RewardManager rewardManager
	) {
		this.plugin = plugin;
		this.promoteManager = promoteManager;
		this.langManager = langManager;
		this.guiManager = guiManager;
		this.playtimeManager = playtimeManager;
		this.dataManager = dataManager;
		this.rewardManager = rewardManager;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
			if (!sender.hasPermission("chadpromoter.reload")) {
				langManager.sendMessage(sender, "reload-no-permission");
				return true;
			}
			plugin.reloadConfig();
			langManager.reload();
			dataManager.loadPlayersData();
			playtimeManager.reload();
			rewardManager.reload();
			langManager.sendMessage(sender, "reload-success");
			return true;
		}
		if (!(sender instanceof Player player)) {
			langManager.sendMessage(sender, "player-only");
			return true;
		}

		if (args.length == 0) {
			guiManager.openMainGui(player);
			return true;
		}

		if (args.length == 2 && args[0].equalsIgnoreCase("use")) {
			String code = args[1];
			PromoteManager.UseCodeResult result = promoteManager.useCode(player, code);

			switch (result) {
				case SUCCESS -> langManager.sendMessage(
						player,
						"use-success",
						Map.of("%promoter%", promoteManager.getPromoterNameByCode(code))
				);
				case OWN_CODE -> langManager.sendMessage(player, "use-failed-own-code");
				case ALREADY_USED -> langManager.sendMessage(player, "use-failed-already-used");
				case CODE_NOT_FOUND -> langManager.sendMessage(player, "use-failed-code-not-found");
				case NOT_NEW_PLAYER -> langManager.sendMessage(player, "use-failed-not-new-player");
			}

			return true;
		}

		langManager.sendMessageList(player, "usage");
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
		if (args.length == 1) {
			if (sender.hasPermission("chadpromoter.reload")) {
				return List.of("use", "reload");
			}
			return List.of("use");
		}
		return List.of();
	}
}