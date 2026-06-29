package basementhost.randomchad.command;

import basementhost.randomchad.lang.LangManager;
import basementhost.randomchad.manager.DataManager;
import basementhost.randomchad.manager.GuiManager;
import basementhost.randomchad.manager.PromoteManager;
import basementhost.randomchad.manager.RewardManager;
import basementhost.randomchad.playtime.PlaytimeManager;
import basementhost.randomchad.util.TimeUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Map;
import java.util.UUID;

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
			handleReload(sender);
			return true;
		}

		if (args.length >= 1 && args[0].equalsIgnoreCase("admin")) {
			handleAdminCommand(sender, args);
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

	private void handleAdminCommand(CommandSender sender, String[] args) {
		if (!sender.hasPermission("chadpromoter.admin")) {
			langManager.sendMessage(sender, "admin.no-permission");
			return;
		}

		if (args.length == 2 && args[1].equalsIgnoreCase("reload")) {
			handleReload(sender);
			return;
		}

		if (args.length < 3) {
			langManager.sendMessageList(sender, "admin.usage");
			return;
		}

		String subCommand = args[1];
		String targetName = args[2];

		UUID targetUuid = dataManager.findPlayerUuidByName(targetName);

		if (targetUuid == null) {
			langManager.sendMessage(sender, "admin.player-not-found", Map.of("%player%", targetName));
			return;
		}

		if (subCommand.equalsIgnoreCase("info")) {
			handleAdminInfo(sender, targetUuid);
			return;
		}

		if (subCommand.equalsIgnoreCase("resetbind")) {
			handleAdminResetBind(sender, targetUuid);
			return;
		}

		if (subCommand.equalsIgnoreCase("resetrewards")) {
			handleAdminResetRewards(sender, targetUuid);
			return;
		}

		langManager.sendMessageList(sender, "admin.usage");
	}

	private void handleAdminInfo(CommandSender sender, UUID targetUuid) {
		String playerName = dataManager.getPlayerName(targetUuid);
		String code = valueOrNone(dataManager.getPromoteCode(targetUuid));
		String usedCode = valueOrNone(dataManager.getUsedCode(targetUuid));
		String promotedBy = valueOrNone(dataManager.getPromotedBy(targetUuid));
		int invitedCount = dataManager.getPromotedPlayerCount(targetUuid);
		long playtimeSeconds = playtimeManager.getPlaytimeSeconds(targetUuid);

		langManager.sendMessageList(sender, "admin.info", Map.of(
				"%player%", playerName,
				"%uuid%", targetUuid.toString(),
				"%code%", code,
				"%used_code%", usedCode,
				"%promoted_by%", promotedBy,
				"%invited_count%", String.valueOf(invitedCount),
				"%playtime_source%", playtimeManager.getProviderName(),
				"%playtime%", TimeUtil.formatSeconds(langManager, playtimeSeconds)
		));
	}

	private void handleAdminResetBind(CommandSender sender, UUID targetUuid) {
		dataManager.resetBind(targetUuid);

		langManager.sendMessage(sender, "admin.resetbind-success", Map.of(
				"%player%", dataManager.getPlayerName(targetUuid)
		));
	}

	private void handleAdminResetRewards(CommandSender sender, UUID targetUuid) {
		dataManager.resetClaimedRewards(targetUuid);

		langManager.sendMessage(sender, "admin.resetrewards-success", Map.of(
				"%player%", dataManager.getPlayerName(targetUuid)
		));
	}

	private void handleReload(CommandSender sender) {
		if (!sender.hasPermission("chadpromoter.reload") && !sender.hasPermission("chadpromoter.admin")) {
			langManager.sendMessage(sender, "reload-no-permission");
			return;
		}

		plugin.reloadConfig();
		langManager.reload();
		dataManager.loadPlayersData();
		playtimeManager.reload();
		rewardManager.reload();

		langManager.sendMessage(sender, "reload-success");
	}

	private String valueOrNone(String value) {
		if (value == null || value.isBlank()) {
			return langManager.getRawMessage("admin.none");
		}
		return value;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
		if (args.length == 1) {
			if (sender.hasPermission("chadpromoter.admin")) {
				return List.of("use", "reload", "admin");
			}

			if (sender.hasPermission("chadpromoter.reload")) {
				return List.of("use", "reload");
			}

			return List.of("use");
		}

		if (args.length == 2 && args[0].equalsIgnoreCase("admin")) {
			if (sender.hasPermission("chadpromoter.admin")) {
				return List.of("info", "resetbind", "resetrewards", "reload");
			}
		}

		return List.of();
	}
}