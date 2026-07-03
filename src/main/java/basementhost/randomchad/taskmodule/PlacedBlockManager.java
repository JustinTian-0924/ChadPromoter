package basementhost.randomchad.taskmodule;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class PlacedBlockManager {

	private final JavaPlugin plugin;
	private final Map<String, Long> placedBlocks = new HashMap<>();
	private final Set<Material> neverExpireMaterials = new HashSet<>();

	private File placedBlocksFile;
	private FileConfiguration placedBlocksConfig;
	private int cleanupTaskId = -1;

	public PlacedBlockManager(JavaPlugin plugin) {
		this.plugin = plugin;
		loadNeverExpireMaterials();
		loadPlacedBlocks();
		startCleanupTask();
	}

	public void reload() {
		loadNeverExpireMaterials();
		loadPlacedBlocks();
		restartCleanupTask();
	}

	public void shutdown() {
		if (cleanupTaskId != -1) {
			Bukkit.getScheduler().cancelTask(cleanupTaskId);
			cleanupTaskId = -1;
		}

		cleanupExpiredBlocks();
		savePlacedBlocks();
	}

	public boolean isEnabled() {
		return plugin.getConfig().getBoolean("placed-block-tracking.enabled", true);
	}

	public void markPlaced(Block block) {
		if (!isEnabled()) {
			return;
		}

		placedBlocks.put(toKey(block), System.currentTimeMillis());

		if (shouldSaveImmediately()) {
			savePlacedBlocks();
		}
	}

	public boolean isPlayerPlaced(Block block) {
		if (!isEnabled()) {
			return false;
		}

		String key = toKey(block);
		Long placedTime = placedBlocks.get(key);

		if (placedTime == null) {
			return false;
		}

		if (isNeverExpireMaterial(block.getType())) {
			return true;
		}

		if (isExpired(placedTime)) {
			placedBlocks.remove(key);

			if (shouldSaveImmediately()) {
				savePlacedBlocks();
			}

			return false;
		}

		return true;
	}

	public void remove(Block block) {
		if (!isEnabled()) {
			return;
		}

		placedBlocks.remove(toKey(block));

		if (shouldSaveImmediately()) {
			savePlacedBlocks();
		}
	}

	public void cleanupExpiredBlocks() {
		if (!isEnabled()) {
			return;
		}

		int before = placedBlocks.size();

		placedBlocks.entrySet().removeIf(entry -> isExpired(entry.getValue()));

		int removed = before - placedBlocks.size();

		if (removed > 0) {
			plugin.getLogger().info("Cleaned up " + removed + " expired placed block record(s).");
			savePlacedBlocks();
		}
	}

	private void loadPlacedBlocks() {
		placedBlocks.clear();

		placedBlocksFile = new File(plugin.getDataFolder(), "placed-blocks.yml");

		if (!placedBlocksFile.exists()) {
			try {
				if (!placedBlocksFile.createNewFile()) {
					plugin.getLogger().warning("Could not create placed-blocks.yml.");
				}
			} catch (IOException exception) {
				plugin.getLogger().warning("Could not create placed-blocks.yml: " + exception.getMessage());
			}
		}

		placedBlocksConfig = YamlConfiguration.loadConfiguration(placedBlocksFile);

		ConfigurationSection blocksSection = placedBlocksConfig.getConfigurationSection("blocks");

		if (blocksSection == null) {
			return;
		}

		for (String key : blocksSection.getKeys(false)) {
			long placedTime = blocksSection.getLong(key);

			if (!isExpired(placedTime)) {
				placedBlocks.put(key, placedTime);
			}
		}

		plugin.getLogger().info("Loaded " + placedBlocks.size() + " placed block record(s).");
	}

	public void savePlacedBlocks() {
		if (placedBlocksFile == null) {
			return;
		}

		YamlConfiguration newConfig = new YamlConfiguration();

		for (Map.Entry<String, Long> entry : placedBlocks.entrySet()) {
			newConfig.set("blocks." + entry.getKey(), entry.getValue());
		}

		try {
			newConfig.save(placedBlocksFile);
		} catch (IOException exception) {
			plugin.getLogger().warning("Could not save placed-blocks.yml: " + exception.getMessage());
		}
	}

	private void loadNeverExpireMaterials() {
		neverExpireMaterials.clear();

		for (String materialName : plugin.getConfig().getStringList("placed-block-tracking.never-expire-materials")) {
			Material material = Material.matchMaterial(materialName);

			if (material != null) {
				neverExpireMaterials.add(material);
			} else {
				plugin.getLogger().warning("Invalid never-expire material in config.yml: " + materialName);
			}
		}
	}

	private void startCleanupTask() {
		if (!isEnabled()) {
			return;
		}

		long intervalMinutes = plugin.getConfig().getLong("placed-block-tracking.cleanup-interval-minutes", 30);

		if (intervalMinutes <= 0) {
			intervalMinutes = 30;
		}

		long intervalTicks = intervalMinutes * 60L * 20L;

		cleanupTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(
				plugin,
				this::cleanupExpiredBlocks,
				intervalTicks,
				intervalTicks
		);
	}

	private void restartCleanupTask() {
		if (cleanupTaskId != -1) {
			Bukkit.getScheduler().cancelTask(cleanupTaskId);
			cleanupTaskId = -1;
		}

		startCleanupTask();
	}

	private boolean isExpired(long placedTimeMillis) {
		long ttlMinutes = plugin.getConfig().getLong("placed-block-tracking.ttl-minutes", 1440);

		if (ttlMinutes <= 0) {
			return false;
		}

		long ttlMillis = ttlMinutes * 60L * 1000L;
		return System.currentTimeMillis() - placedTimeMillis > ttlMillis;
	}

	private boolean shouldSaveImmediately() {
		return plugin.getConfig().getBoolean("placed-block-tracking.save-immediately", false);
	}

	private boolean isNeverExpireMaterial(Material material) {
		return neverExpireMaterials.contains(material);
	}

	private String toKey(Block block) {
		UUID worldUuid = block.getWorld().getUID();

		return worldUuid
				+ ";" + block.getX()
				+ ";" + block.getY()
				+ ";" + block.getZ();
	}
}