package basementhost.randomchad.taskmodule;

import basementhost.randomchad.lang.LangManager;
import basementhost.randomchad.manager.DataManager;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.UUID;

public class TaskNotificationManager {

	private final JavaPlugin plugin;
	private final DataManager dataManager;
	private final LangManager langManager;
	private final TaskManager taskManager;
	private final TaskProgressManager taskProgressManager;

	public TaskNotificationManager(
			JavaPlugin plugin,
			DataManager dataManager,
			LangManager langManager,
			TaskManager taskManager,
			TaskProgressManager taskProgressManager
	) {
		this.plugin = plugin;
		this.dataManager = dataManager;
		this.langManager = langManager;
		this.taskManager = taskManager;
		this.taskProgressManager = taskProgressManager;
	}

	public void notifyTaskCompleted(Player completedPlayer, TaskDefinition completedTask) {
		if (completedTask.isVisibleInPanel(TaskPanelType.SELF)) {
			notifySelfIfHasUnclaimedRewards(completedPlayer, true);
		}

		if (completedTask.isVisibleInPanel(TaskPanelType.PROMOTER)) {
			notifyPromoterForPromotedPlayer(completedPlayer.getUniqueId(), true);
		}
	}

	public void notifyOnLogin(Player player) {
		notifySelfIfHasUnclaimedRewards(player, false);
		notifyPromoterAllUnclaimedRewards(player, false);
	}

	private void notifySelfIfHasUnclaimedRewards(Player player, boolean completionSound) {
		if (!hasUsedInviteCode(player.getUniqueId())) {
			return;
		}

		int count = countUnclaimedSelfTaskRewards(player.getUniqueId());

		if (count <= 0) {
			return;
		}

		langManager.sendMessage(player, "task-notification.self-unclaimed", Map.of(
				"%count%", String.valueOf(count)
		));

		playNotificationSound(player, completionSound);
	}

	private void notifyPromoterForPromotedPlayer(UUID promotedPlayerUuid, boolean completionSound) {
		UUID promoterUuid = getPromoterUuid(promotedPlayerUuid);

		if (promoterUuid == null) {
			return;
		}

		Player promoter = Bukkit.getPlayer(promoterUuid);

		if (promoter == null || !promoter.isOnline()) {
			return;
		}

		int count = countUnclaimedPromoterTaskRewards(promoterUuid, promotedPlayerUuid);

		if (count <= 0) {
			return;
		}

		langManager.sendMessage(promoter, "task-notification.promoter-unclaimed", Map.of(
				"%count%", String.valueOf(count),
				"%player%", dataManager.getPlayerName(promotedPlayerUuid)
		));

		playNotificationSound(promoter, completionSound);
	}

	private void notifyPromoterAllUnclaimedRewards(Player promoter, boolean completionSound) {
		for (UUID promotedPlayerUuid : dataManager.getPromotedPlayerUuids(promoter.getUniqueId())) {
			int count = countUnclaimedPromoterTaskRewards(promoter.getUniqueId(), promotedPlayerUuid);

			if (count <= 0) {
				continue;
			}

			langManager.sendMessage(promoter, "task-notification.promoter-unclaimed", Map.of(
					"%count%", String.valueOf(count),
					"%player%", dataManager.getPlayerName(promotedPlayerUuid)
			));

			playNotificationSound(promoter, completionSound);
		}
	}

	private int countUnclaimedSelfTaskRewards(UUID playerUuid) {
		int count = 0;

		for (TaskDefinition taskDefinition : taskManager.getTasksByPanel(TaskPanelType.SELF)) {
			if (!taskProgressManager.isTaskCompleted(playerUuid, taskDefinition.getId())) {
				continue;
			}

			if (dataManager.hasClaimedSelfReward(playerUuid, taskDefinition.getId())) {
				continue;
			}

			count++;
		}

		return count;
	}

	private int countUnclaimedPromoterTaskRewards(UUID promoterUuid, UUID promotedPlayerUuid) {
		int count = 0;

		for (TaskDefinition taskDefinition : taskManager.getTasksByPanel(TaskPanelType.PROMOTER)) {
			if (!taskProgressManager.isTaskCompleted(promotedPlayerUuid, taskDefinition.getId())) {
				continue;
			}

			if (dataManager.hasClaimedReward(promoterUuid, promotedPlayerUuid, taskDefinition.getId())) {
				continue;
			}

			count++;
		}

		return count;
	}

	private boolean hasUsedInviteCode(UUID playerUuid) {
		String usedCode = dataManager.getUsedCode(playerUuid);
		return usedCode != null && !usedCode.isBlank();
	}

	private UUID getPromoterUuid(UUID promotedPlayerUuid) {
		String promoterUuidText = dataManager.getPromotedBy(promotedPlayerUuid);

		if (promoterUuidText == null || promoterUuidText.isBlank()) {
			return null;
		}

		try {
			return UUID.fromString(promoterUuidText);
		} catch (IllegalArgumentException exception) {
			plugin.getLogger().warning("Invalid promoter UUID for player " + promotedPlayerUuid + ": " + promoterUuidText);
			return null;
		}
	}

	private void playNotificationSound(Player player, boolean completionSound) {
		if (completionSound) {
			player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0F, 1.2F);
			return;
		}

		player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0F, 1.0F);
	}
}