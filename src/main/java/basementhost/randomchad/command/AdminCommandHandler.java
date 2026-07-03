package basementhost.randomchad.command;

import basementhost.randomchad.lang.LangManager;
import basementhost.randomchad.manager.DataManager;
import basementhost.randomchad.playtime.PlaytimeManager;
import basementhost.randomchad.taskmodule.TaskCondition;
import basementhost.randomchad.taskmodule.TaskDefinition;
import basementhost.randomchad.taskmodule.TaskManager;
import basementhost.randomchad.taskmodule.TaskProgressManager;
import basementhost.randomchad.util.TimeUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

public class AdminCommandHandler {

	private final LangManager langManager;
	private final DataManager dataManager;
	private final PlaytimeManager playtimeManager;
	private final ReloadCommandHandler reloadCommandHandler;
	private final TaskManager taskManager;
	private final TaskProgressManager taskProgressManager;

	public AdminCommandHandler(
			LangManager langManager,
			DataManager dataManager,
			PlaytimeManager playtimeManager,
			ReloadCommandHandler reloadCommandHandler,
			TaskManager taskManager,
			TaskProgressManager taskProgressManager
	) {
		this.langManager = langManager;
		this.dataManager = dataManager;
		this.playtimeManager = playtimeManager;
		this.reloadCommandHandler = reloadCommandHandler;
		this.taskManager = taskManager;
		this.taskProgressManager = taskProgressManager;
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

		if (subCommand.equalsIgnoreCase("taskinfo")) {
			handleAdminTaskInfo(sender, targetUuid);
			return;
		}
		if (subCommand.equalsIgnoreCase("addprogress")) {
			handleAdminAddProgress(sender, targetUuid, targetName, args);
			return;
		}
		if (subCommand.equalsIgnoreCase("setprogress")) {
			handleAdminSetProgress(sender, targetUuid, targetName, args);
			return;
		}
		if (subCommand.equalsIgnoreCase("resetprogress")) {
			handleAdminResetProgress(sender, targetUuid, targetName, args);
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
			return filterStartsWith(
					List.of(
							"info",
							"resetbind",
							"resetrewards",
							"reload",
							"taskinfo",
							"addprogress",
							"setprogress",
							"resetprogress"
					),
					args[1]
			);
		}

		if (args.length == 3) {
			String subCommand = args[1];

			if (
					subCommand.equalsIgnoreCase("info")
							|| subCommand.equalsIgnoreCase("resetbind")
							|| subCommand.equalsIgnoreCase("resetrewards")
							|| subCommand.equalsIgnoreCase("taskinfo")
							|| subCommand.equalsIgnoreCase("addprogress")
							|| subCommand.equalsIgnoreCase("setprogress")
							|| subCommand.equalsIgnoreCase("resetprogress")
			) {
				return filterStartsWith(getKnownAndOnlinePlayerNames(), args[2]);
			}
		}

		if (args.length == 4) {
			String subCommand = args[1];

			if (
					subCommand.equalsIgnoreCase("addprogress")
							|| subCommand.equalsIgnoreCase("setprogress")
							|| subCommand.equalsIgnoreCase("resetprogress")
			) {
				return filterStartsWith(getTaskIds(), args[3]);
			}
		}

		if (args.length == 5) {
			String subCommand = args[1];

			if (
					subCommand.equalsIgnoreCase("addprogress")
							|| subCommand.equalsIgnoreCase("setprogress")
			) {
				String taskId = args[3];
				return filterStartsWith(getConditionIds(taskId), args[4]);
			}
		}

		if (args.length == 6) {
			String subCommand = args[1];

			if (
					subCommand.equalsIgnoreCase("addprogress")
							|| subCommand.equalsIgnoreCase("setprogress")
			) {
				return filterStartsWith(List.of("1", "10", "64", "100", "1000"), args[5]);
			}
		}

		return List.of();
	}
	private List<String> getTaskIds() {
		List<String> taskIds = new ArrayList<>();

		for (TaskDefinition taskDefinition : taskManager.getTasks()) {
			taskIds.add(taskDefinition.getId());
		}

		return taskIds;
	}

	private List<String> getConditionIds(String taskId) {
		TaskDefinition taskDefinition = taskManager.getTask(taskId);

		if (taskDefinition == null) {
			return List.of();
		}

		List<String> conditionIds = new ArrayList<>();

		for (TaskCondition condition : taskDefinition.getConditions()) {
			conditionIds.add(condition.getId());
		}

		return conditionIds;
	}
	private List<String> getKnownAndOnlinePlayerNames() {
		Set<String> playerNames = new LinkedHashSet<>();

		playerNames.addAll(dataManager.getKnownPlayerNames());

		Bukkit.getOnlinePlayers().forEach(player -> playerNames.add(player.getName()));

		return new ArrayList<>(playerNames);
	}

	private List<String> filterStartsWith(List<String> options, String prefix) {
		if (prefix == null || prefix.isBlank()) {
			return options;
		}

		String lowerPrefix = prefix.toLowerCase();

		return options.stream()
				.filter(option -> option.toLowerCase().startsWith(lowerPrefix))
				.toList();
	}

	private void handleAdminTaskInfo(CommandSender sender, UUID targetUuid) {
		String playerName = dataManager.getPlayerName(targetUuid);

		langManager.sendMessage(sender, "admin.taskinfo-header", Map.of(
				"%player%", playerName
		));

		for (TaskDefinition taskDefinition : taskManager.getTasks()) {
			boolean completed = taskProgressManager.isTaskCompleted(targetUuid, taskDefinition.getId());

			String status = completed
					? langManager.getRawMessage("admin.taskinfo-completed")
					: langManager.getRawMessage("admin.taskinfo-not-completed");

			langManager.sendMessage(sender, "admin.taskinfo-task", Map.of(
					"%task_id%", taskDefinition.getId(),
					"%status%", status
			));

			for (TaskCondition condition : taskDefinition.getConditions()) {
				long current = taskProgressManager.getProgress(
						targetUuid,
						taskDefinition.getId(),
						condition.getId()
				);

				langManager.sendMessage(sender, "admin.taskinfo-condition", Map.of(
						"%condition_id%", condition.getId(),
						"%current%", String.valueOf(current),
						"%required%", String.valueOf(condition.getAmount())
				));
			}
		}

		langManager.sendMessage(sender, "admin.taskinfo-footer");
	}

	private void handleAdminAddProgress(CommandSender sender, UUID targetUuid, String targetName, String[] args) {
		if (args.length < 6) {
			langManager.sendMessageList(sender, "admin.usage");
			return;
		}

		String taskId = args[3];
		String conditionId = args[4];

		Long amount = parseAmount(sender, args[5]);

		if (amount == null) {
			return;
		}

		if (!validateTaskAndCondition(sender, taskId, conditionId)) {
			return;
		}

		taskProgressManager.addProgress(targetUuid, taskId, conditionId, amount);

		langManager.sendMessage(sender, "admin.addprogress-success", Map.of(
				"%player%", targetName,
				"%task_id%", taskId,
				"%condition_id%", conditionId,
				"%amount%", String.valueOf(amount)
		));
	}

	private void handleAdminSetProgress(CommandSender sender, UUID targetUuid, String targetName, String[] args) {
		if (args.length < 6) {
			langManager.sendMessageList(sender, "admin.usage");
			return;
		}

		String taskId = args[3];
		String conditionId = args[4];

		Long amount = parseAmount(sender, args[5]);

		if (amount == null) {
			return;
		}

		if (!validateTaskAndCondition(sender, taskId, conditionId)) {
			return;
		}

		taskProgressManager.setProgress(targetUuid, taskId, conditionId, amount);

		langManager.sendMessage(sender, "admin.setprogress-success", Map.of(
				"%player%", targetName,
				"%task_id%", taskId,
				"%condition_id%", conditionId,
				"%amount%", String.valueOf(amount)
		));
	}

	private void handleAdminResetProgress(CommandSender sender, UUID targetUuid, String targetName, String[] args) {
		if (args.length < 4) {
			langManager.sendMessageList(sender, "admin.usage");
			return;
		}

		String taskId = args[3];

		TaskDefinition taskDefinition = taskManager.getTask(taskId);

		if (taskDefinition == null) {
			langManager.sendMessage(sender, "admin.task-not-found", Map.of(
					"%task_id%", taskId
			));
			return;
		}

		taskProgressManager.resetTaskProgress(targetUuid, taskId);

		langManager.sendMessage(sender, "admin.resetprogress-success", Map.of(
				"%player%", targetName,
				"%task_id%", taskId
		));
	}

	private Long parseAmount(CommandSender sender, String input) {
		try {
			long amount = Long.parseLong(input);

			if (amount < 0) {
				langManager.sendMessage(sender, "admin.invalid-amount");
				return null;
			}

			return amount;
		} catch (NumberFormatException exception) {
			langManager.sendMessage(sender, "admin.invalid-amount");
			return null;
		}
	}

	private boolean validateTaskAndCondition(CommandSender sender, String taskId, String conditionId) {
		TaskDefinition taskDefinition = taskManager.getTask(taskId);

		if (taskDefinition == null) {
			langManager.sendMessage(sender, "admin.task-not-found", Map.of(
					"%task_id%", taskId
			));
			return false;
		}

		TaskCondition condition = findCondition(taskDefinition, conditionId);

		if (condition == null) {
			langManager.sendMessage(sender, "admin.condition-not-found", Map.of(
					"%task_id%", taskId,
					"%condition_id%", conditionId
			));
			return false;
		}

		return true;
	}

	private TaskCondition findCondition(TaskDefinition taskDefinition, String conditionId) {
		for (TaskCondition condition : taskDefinition.getConditions()) {
			if (condition.getId().equalsIgnoreCase(conditionId)) {
				return condition;
			}
		}

		return null;
	}
}