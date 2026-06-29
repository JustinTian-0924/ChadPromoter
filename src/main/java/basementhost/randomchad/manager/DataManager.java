package basementhost.randomchad.manager;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class DataManager {

	private final JavaPlugin plugin;
	private final File playersFile;
	private FileConfiguration playersData;

	public DataManager(JavaPlugin plugin) {
		this.plugin = plugin;
		File dataFolder = new File(plugin.getDataFolder(), "data");
		this.playersFile = new File(dataFolder, "players.yml");

		createPlayersFile();
		loadPlayersData();
	}

	private void createPlayersFile() {
		File dataFolder = playersFile.getParentFile();

		if (!dataFolder.exists()) {
			dataFolder.mkdirs();
		}

		migrateOldPlayersFile();

		if (!playersFile.exists()) {
			try {
				playersFile.createNewFile();
			} catch (IOException exception) {
				plugin.getLogger().severe("Could not create data/players.yml");
				exception.printStackTrace();
			}
		}
	}

	private void migrateOldPlayersFile() {
		File oldPlayersFile = new File(plugin.getDataFolder(), "players.yml");

		if (!oldPlayersFile.exists() || playersFile.exists()) {
			return;
		}

		if (oldPlayersFile.renameTo(playersFile)) {
			plugin.getLogger().info("Moved players.yml to data/players.yml");
		} else {
			plugin.getLogger().warning("Could not move old players.yml to data/players.yml automatically.");
		}
	}

	public void loadPlayersData() {
		this.playersData = YamlConfiguration.loadConfiguration(playersFile);
	}

	public void savePlayersData() {
		try {
			playersData.save(playersFile);
		} catch (IOException exception) {
			plugin.getLogger().severe("Could not save players.yml");
			exception.printStackTrace();
		}
	}

	public FileConfiguration getPlayersData() {
		return playersData;
	}

	public boolean hasPlayer(UUID playerUuid) {
		return playersData.contains("players." + playerUuid);
	}

	public String getPlayerName(UUID playerUuid) {
		return playersData.getString("players." + playerUuid + ".name", "Unknown");
	}

	public String getPromoteCode(UUID playerUuid) {
		return playersData.getString("players." + playerUuid + ".promote-code");
	}

	public boolean hasUsedCode(UUID playerUuid) {
		return playersData.getString("players." + playerUuid + ".used-code") != null;
	}

	public void createPlayer(UUID playerUuid, String playerName, String promoteCode) {
		String path = "players." + playerUuid;

		playersData.set(path + ".name", playerName);
		playersData.set(path + ".promote-code", promoteCode);
		playersData.set(path + ".used-code", null);
		playersData.set(path + ".promoted-by", null);
		playersData.set(path + ".total-playtime-seconds", 0);
		playersData.set(path + ".session-start-millis", null);

		savePlayersData();
	}

	public void updatePlayerName(UUID playerUuid, String playerName) {
		playersData.set("players." + playerUuid + ".name", playerName);
		savePlayersData();
	}

	public boolean isCodeUsed(String code) {
		return findPlayerUuidByCode(code) != null;
	}

	public UUID findPlayerUuidByCode(String code) {
		Set<String> playerUuids = playersData.getConfigurationSection("players") == null
				? Set.of()
				: playersData.getConfigurationSection("players").getKeys(false);

		for (String playerUuid : playerUuids) {
			String existingCode = playersData.getString("players." + playerUuid + ".promote-code");

			if (existingCode != null && code.equalsIgnoreCase(existingCode)) {
				return UUID.fromString(playerUuid);
			}
		}

		return null;
	}

	public void bindPromoter(UUID playerUuid, String usedCode, UUID promoterUuid) {
		String path = "players." + playerUuid;

		playersData.set(path + ".used-code", usedCode);
		playersData.set(path + ".promoted-by", promoterUuid.toString());

		savePlayersData();
	}

	public String getUsedCode(UUID playerUuid) {
		return playersData.getString("players." + playerUuid + ".used-code");
	}

	public int getPromotedPlayerCount(UUID promoterUuid) {
		Set<String> playerUuids = playersData.getConfigurationSection("players") == null
				? Set.of()
				: playersData.getConfigurationSection("players").getKeys(false);

		int count = 0;

		for (String playerUuid : playerUuids) {
			String promotedBy = playersData.getString("players." + playerUuid + ".promoted-by");

			if (promoterUuid.toString().equals(promotedBy)) {
				count++;
			}
		}

		return count;
	}

	public List<UUID> getPromotedPlayerUuids(UUID promoterUuid) {
		Set<String> playerUuids = playersData.getConfigurationSection("players") == null
				? Set.of()
				: playersData.getConfigurationSection("players").getKeys(false);
		List<UUID> promotedPlayers = new ArrayList<>();
		for (String playerUuid : playerUuids) {
			String promotedBy = playersData.getString("players." + playerUuid + ".promoted-by");
			if (promoterUuid.toString().equals(promotedBy)) {
				promotedPlayers.add(UUID.fromString(playerUuid));
			}
		}
		return promotedPlayers;
	}

	public void startPlaytimeSession(UUID playerUuid) {
		playersData.set("players." + playerUuid + ".session-start-millis", System.currentTimeMillis());
		savePlayersData();
	}

	public void stopPlaytimeSession(UUID playerUuid) {
		String path = "players." + playerUuid;
		long sessionStartMillis = playersData.getLong(path + ".session-start-millis", 0);

		if (sessionStartMillis <= 0) {
			return;
		}

		long onlineSeconds = (System.currentTimeMillis() - sessionStartMillis) / 1000;
		long totalPlaytimeSeconds = playersData.getLong(path + ".total-playtime-seconds", 0);

		playersData.set(path + ".total-playtime-seconds", totalPlaytimeSeconds + onlineSeconds);
		playersData.set(path + ".session-start-millis", null);

		savePlayersData();
	}

	public long getTotalPlaytimeSeconds(UUID playerUuid) {
		String path = "players." + playerUuid;
		long totalPlaytimeSeconds = playersData.getLong(path + ".total-playtime-seconds", 0);
		long sessionStartMillis = playersData.getLong(path + ".session-start-millis", 0);

		if (sessionStartMillis <= 0) {
			return totalPlaytimeSeconds;
		}

		long currentSessionSeconds = (System.currentTimeMillis() - sessionStartMillis) / 1000;
		return totalPlaytimeSeconds + currentSessionSeconds;
	}

	public boolean hasClaimedReward(UUID promoterUuid, UUID promotedPlayerUuid, String rewardId) {
		String path = "players." + promotedPlayerUuid + ".claimed-rewards." + promoterUuid;
		return playersData.getStringList(path).contains(rewardId);
	}
	public void markRewardClaimed(UUID promoterUuid, UUID promotedPlayerUuid, String rewardId) {
		String path = "players." + promotedPlayerUuid + ".claimed-rewards." + promoterUuid;
		List<String> claimedRewards = new ArrayList<>(playersData.getStringList(path));
		if (!claimedRewards.contains(rewardId)) {
			claimedRewards.add(rewardId);
			playersData.set(path, claimedRewards);
			savePlayersData();
		}
	}
}