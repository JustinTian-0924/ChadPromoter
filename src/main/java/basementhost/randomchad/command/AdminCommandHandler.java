package basementhost.randomchad.command;

import basementhost.randomchad.lang.LangManager;
import basementhost.randomchad.manager.DataManager;
import basementhost.randomchad.playtime.PlaytimeManager;
import basementhost.randomchad.util.TimeUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class AdminCommandHandler {

	private final LangManager langManager;
	private final DataManager dataManager;
	private final PlaytimeManager playtimeManager;
	private final ReloadCommandHandler reloadCommandHandler;

	public AdminCommandHandler(
			LangManager langManager,
			DataManager dataManager,
			PlaytimeManager playtimeManager,
			ReloadCommandHandler reloadCommandHandler
	) {
		this.langManager = langManager;
		this.dataManager = dataManager;
		this.playtimeManager = playtimeManager;
		this.reloadCommandHandler = reloadCommandHandler;
	}

	public void handleAdminCommand(CommandSender sender, String[] args) {
		if (!sender.hasPermission("chadpromoter.admin")) {
			langManager.sendMessage(sender, "admin.no-permission");
			return;
		}

		if (args.length == 2 && args[1].equalsIgnoreCase("reload")) {
			reloadCommandHandler.handleReload(sender);
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

	private String valueOrNone(String value) {
		if (value == null || value.isBlank()) {
			return langManager.getRawMessage("admin.none");
		}

		return value;
	}

	public List<String> tabComplete(CommandSender sender, String[] args) {
		if (!sender.hasPermission("chadpromoter.admin")) {
			return List.of();
		}

		if (args.length == 2) {
			return List.of("info", "resetbind", "resetrewards", "reload");
		}

		if (args.length == 3) {
			String subCommand = args[1];

			if (
					subCommand.equalsIgnoreCase("info")
							|| subCommand.equalsIgnoreCase("resetbind")
							|| subCommand.equalsIgnoreCase("resetrewards")
			) {
				return Bukkit.getOnlinePlayers()
						.stream()
						.map(Player::getName)
						.toList();
			}
		}

		return List.of();
	}
}