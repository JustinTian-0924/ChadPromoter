package basementhost.randomchad.manager;

import basementhost.randomchad.ChadPromoterPlugin;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class RewardManager {

	private final ChadPromoterPlugin plugin;
	private final DataManager dataManager;
	private File rewardsFile;
	private FileConfiguration rewardsConfig;
	private final Map<String, RewardDefinition> rewards = new LinkedHashMap<>();

	public RewardManager(ChadPromoterPlugin plugin, DataManager dataManager) {
		this.plugin = plugin;
		this.dataManager = dataManager;
		saveDefaultRewardsFile();
		loadRewards();
	}

	private void saveDefaultRewardsFile() {
		rewardsFile = new File(plugin.getDataFolder(), "rewards.yml");

		if (!rewardsFile.exists()) {
			plugin.saveResource("rewards.yml", false);
		}
	}

	public void reload() {
		loadRewards();
	}

	private void loadRewards() {
		rewards.clear();
		rewardsConfig = YamlConfiguration.loadConfiguration(rewardsFile);

		ConfigurationSection rewardsSection = rewardsConfig.getConfigurationSection("rewards");

		if (rewardsSection == null) {
			plugin.getLogger().warning("No rewards found in rewards.yml");
			return;
		}

		for (String rewardId : rewardsSection.getKeys(false)) {
			String path = "rewards." + rewardId;

			String type = rewardsConfig.getString(path + ".type", "playtime");
			long requiredPlaytimeSeconds = rewardsConfig.getLong(path + ".required-playtime-seconds", 0);
			Material material = Material.matchMaterial(rewardsConfig.getString(path + ".material", "CHEST"));

			if (material == null) {
				material = Material.CHEST;
			}

			String displayNameLangKey = rewardsConfig.getString(path + ".display-name-lang-key", rewardId);
			String loreLangKey = rewardsConfig.getString(path + ".lore-lang-key", rewardId);
			double money = rewardsConfig.getDouble(path + ".money", 0.0);
			List<String> commands = rewardsConfig.getStringList(path + ".commands");

			RewardDefinition reward = new RewardDefinition(
					rewardId,
					type,
					requiredPlaytimeSeconds,
					material,
					displayNameLangKey,
					loreLangKey,
					money,
					commands
			);

			rewards.put(rewardId, reward);
		}

		plugin.getLogger().info("Loaded " + rewards.size() + " reward(s).");
	}

	public Collection<RewardDefinition> getRewards() {
		return rewards.values();
	}

	public RewardDefinition getReward(String rewardId) {
		return rewards.get(rewardId);
	}

	public boolean isUnlocked(RewardDefinition reward, UUID promotedPlayerUuid, long promotedPlaytimeSeconds) {
		if (!reward.getType().equalsIgnoreCase("playtime")) {
			return false;
		}

		return promotedPlaytimeSeconds >= reward.getRequiredPlaytimeSeconds();
	}

	public ClaimResult claimReward(UUID promoterUuid, UUID promotedPlayerUuid, RewardDefinition reward, long promotedPlaytimeSeconds) {
		if (dataManager.hasClaimedReward(promoterUuid, promotedPlayerUuid, reward.getId())) {
			return ClaimResult.ALREADY_CLAIMED;
		}

		if (!isUnlocked(reward, promotedPlayerUuid, promotedPlaytimeSeconds)) {
			return ClaimResult.NOT_UNLOCKED;
		}

		Economy economy = plugin.getEconomy();

		if (reward.getMoney() > 0) {
			if (economy == null) {
				return ClaimResult.VAULT_UNAVAILABLE;
			}

			economy.depositPlayer(Bukkit.getOfflinePlayer(promoterUuid), reward.getMoney());
		}

		String promoterName = dataManager.getPlayerName(promoterUuid);
		String promotedName = dataManager.getPlayerName(promotedPlayerUuid);

		for (String command : reward.getCommands()) {
			String parsedCommand = command
					.replace("%promoter%", promoterName)
					.replace("%promoter_uuid%", promoterUuid.toString())
					.replace("%promoted%", promotedName)
					.replace("%promoted_uuid%", promotedPlayerUuid.toString());

			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), parsedCommand);
		}

		dataManager.markRewardClaimed(promoterUuid, promotedPlayerUuid, reward.getId());
		return ClaimResult.SUCCESS;
	}

	public enum ClaimResult {
		SUCCESS,
		ALREADY_CLAIMED,
		NOT_UNLOCKED,
		VAULT_UNAVAILABLE
	}
}