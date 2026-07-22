package basementhost.randomchad.taskmodule;

import basementhost.randomchad.lang.LangManager;
import basementhost.randomchad.manager.DataManager;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PromoterTaskRewardManager {

	private final JavaPlugin plugin;
	private final DataManager dataManager;
	private final LangManager langManager;
	private final TaskManager taskManager;
	private final TaskProgressManager taskProgressManager;

	private File rewardsFile;
	private FileConfiguration rewardsConfig;
	private final Map<String, PromoterTaskReward> rewards = new HashMap<>();

	public PromoterTaskRewardManager(
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

		saveDefaultRewardsFile();
		loadRewards();
	}

	private void saveDefaultRewardsFile() {
		rewardsFile = new File(plugin.getDataFolder(), "promoter-task-rewards.yml");

		if (!rewardsFile.exists()) {
			plugin.saveResource("promoter-task-rewards.yml", false);
		}
	}

	public void reload() {
		loadRewards();
	}

	private void loadRewards() {
		rewards.clear();
		rewardsConfig = YamlConfiguration.loadConfiguration(rewardsFile);

		ConfigurationSection rewardsSection = rewardsConfig.getConfigurationSection("promoter-task-rewards");

		if (rewardsSection == null) {
			plugin.getLogger().warning("No promoter-task-rewards section found in promoter-task-rewards.yml.");
			return;
		}

		for (String taskId : rewardsSection.getKeys(false)) {
			ConfigurationSection rewardSection = rewardsSection.getConfigurationSection(taskId);

			if (rewardSection == null) {
				continue;
			}

			double money = rewardSection.getDouble("money", 0);
			List<String> commands = rewardSection.getStringList("commands");

			rewards.put(taskId, new PromoterTaskReward(taskId, money, commands));
		}

		plugin.getLogger().info("Loaded " + rewards.size() + " promoter task reward(s).");
	}

	public boolean hasReward(String taskId) {
		return rewards.containsKey(taskId);
	}

	public boolean isClaimable(Player promoter, UUID promotedPlayerUuid, String taskId) {
		if (!hasReward(taskId)) {
			return false;
		}

		if (taskManager.getTask(taskId) == null) {
			return false;
		}

		if (!isPromotedBy(promoter.getUniqueId(), promotedPlayerUuid)) {
			return false;
		}

		if (!taskProgressManager.isTaskCompleted(promotedPlayerUuid, taskId)) {
			return false;
		}

		return !dataManager.hasClaimedReward(promoter.getUniqueId(), promotedPlayerUuid, taskId);
	}

	public void claimReward(Player promoter, UUID promotedPlayerUuid, String taskId) {
		PromoterTaskReward reward = rewards.get(taskId);

		if (reward == null) {
			langManager.sendMessage(promoter, "promoter-task-reward.no-reward-configured");
			return;
		}

		if (!isPromotedBy(promoter.getUniqueId(), promotedPlayerUuid)) {
			langManager.sendMessage(promoter, "promoter-task-reward.not-your-promoted-player");
			return;
		}

		if (!taskProgressManager.isTaskCompleted(promotedPlayerUuid, taskId)) {
			langManager.sendMessage(promoter, "promoter-task-reward.not-completed");
			return;
		}

		if (dataManager.hasClaimedReward(promoter.getUniqueId(), promotedPlayerUuid, taskId)) {
			langManager.sendMessage(promoter, "promoter-task-reward.already-claimed");
			return;
		}

		giveMoney(promoter, reward.getMoney());
		runCommands(promoter, promotedPlayerUuid, reward.getCommands());

		dataManager.addClaimedReward(promoter.getUniqueId(), promotedPlayerUuid, taskId);

		langManager.sendMessage(promoter, "promoter-task-reward.claim-success", Map.of(
				"%promoted%", dataManager.getPlayerName(promotedPlayerUuid),
				"%task_id%", taskId
		));
	}

	private boolean isPromotedBy(UUID promoterUuid, UUID promotedPlayerUuid) {
		String promotedBy = dataManager.getPromotedBy(promotedPlayerUuid);

		if (promotedBy == null || promotedBy.isBlank()) {
			return false;
		}

		return promotedBy.equalsIgnoreCase(promoterUuid.toString());
	}

	private void giveMoney(Player promoter, double money) {
		if (money <= 0) {
			return;
		}

		Economy economy = getEconomy();

		if (economy == null) {
			plugin.getLogger().warning("Could not give promoter task money reward because Vault economy is unavailable.");
			return;
		}

		economy.depositPlayer(promoter, money);

		langManager.sendMessage(promoter, "promoter-task-reward.money-received", Map.of(
				"%money%", String.valueOf(money)
		));
	}

	private void runCommands(Player promoter, UUID promotedPlayerUuid, List<String> commands) {
		String promotedName = dataManager.getPlayerName(promotedPlayerUuid);

		for (String command : commands) {
			String parsedCommand = command
					.replace("%promoter%", promoter.getName())
					.replace("%promoted%", promotedName);

			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), parsedCommand);
		}
	}

	private Economy getEconomy() {
		RegisteredServiceProvider<Economy> registration = plugin
				.getServer()
				.getServicesManager()
				.getRegistration(Economy.class);

		if (registration == null) {
			return null;
		}

		return registration.getProvider();
	}

	private static class PromoterTaskReward {

		private final String taskId;
		private final double money;
		private final List<String> commands;

		public PromoterTaskReward(String taskId, double money, List<String> commands) {
			this.taskId = taskId;
			this.money = money;
			this.commands = commands;
		}

		public String getTaskId() {
			return taskId;
		}

		public double getMoney() {
			return money;
		}

		public List<String> getCommands() {
			return commands;
		}
	}
}