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

public class SelfTaskRewardManager {

	private final JavaPlugin plugin;
	private final DataManager dataManager;
	private final LangManager langManager;
	private final TaskManager taskManager;
	private final TaskProgressManager taskProgressManager;

	private File rewardsFile;
	private FileConfiguration rewardsConfig;
	private final Map<String, SelfTaskReward> rewards = new HashMap<>();

	public SelfTaskRewardManager(
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
		rewardsFile = new File(plugin.getDataFolder(), "self-task-rewards.yml");

		if (!rewardsFile.exists()) {
			plugin.saveResource("self-task-rewards.yml", false);
		}
	}

	public void reload() {
		loadRewards();
	}

	private void loadRewards() {
		rewards.clear();
		rewardsConfig = YamlConfiguration.loadConfiguration(rewardsFile);

		ConfigurationSection rewardsSection = rewardsConfig.getConfigurationSection("self-task-rewards");

		if (rewardsSection == null) {
			plugin.getLogger().warning("No self-task-rewards section found in self-task-rewards.yml.");
			return;
		}

		for (String taskId : rewardsSection.getKeys(false)) {
			ConfigurationSection rewardSection = rewardsSection.getConfigurationSection(taskId);

			if (rewardSection == null) {
				continue;
			}

			double money = rewardSection.getDouble("money", 0);
			List<String> commands = rewardSection.getStringList("commands");

			rewards.put(taskId, new SelfTaskReward(taskId, money, commands));
		}

		plugin.getLogger().info("Loaded " + rewards.size() + " self task reward(s).");
	}

	public boolean hasReward(String taskId) {
		return rewards.containsKey(taskId);
	}

	public boolean isClaimable(Player player, String taskId) {
		if (!hasReward(taskId)) {
			return false;
		}

		if (taskManager.getTask(taskId) == null) {
			return false;
		}

		if (!taskProgressManager.isTaskCompleted(player.getUniqueId(), taskId)) {
			return false;
		}

		return !dataManager.hasClaimedSelfReward(player.getUniqueId(), taskId);
	}

	public void claimReward(Player player, String taskId) {
		SelfTaskReward reward = rewards.get(taskId);

		if (reward == null) {
			langManager.sendMessage(player, "self-task-reward.no-reward-configured");
			return;
		}

		if (!taskProgressManager.isTaskCompleted(player.getUniqueId(), taskId)) {
			langManager.sendMessage(player, "self-task-reward.not-completed");
			return;
		}

		if (dataManager.hasClaimedSelfReward(player.getUniqueId(), taskId)) {
			langManager.sendMessage(player, "self-task-reward.already-claimed");
			return;
		}

		giveMoney(player, reward.getMoney());
		runCommands(player, reward.getCommands());

		dataManager.addClaimedSelfReward(player.getUniqueId(), taskId);

		langManager.sendMessage(player, "self-task-reward.claim-success", Map.of(
				"%task_id%", taskId
		));
	}

	private void giveMoney(Player player, double money) {
		if (money <= 0) {
			return;
		}

		Economy economy = getEconomy();

		if (economy == null) {
			plugin.getLogger().warning("Could not give self task money reward because Vault economy is unavailable.");
			return;
		}

		economy.depositPlayer(player, money);

		langManager.sendMessage(player, "self-task-reward.money-received", Map.of(
				"%money%", String.valueOf(money)
		));
	}

	private void runCommands(Player player, List<String> commands) {
		for (String command : commands) {
			String parsedCommand = command.replace("%player%", player.getName());

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

	private static class SelfTaskReward {

		private final String taskId;
		private final double money;
		private final List<String> commands;

		public SelfTaskReward(String taskId, double money, List<String> commands) {
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