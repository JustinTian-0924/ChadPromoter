package basementhost.randomchad.playtime;

import basementhost.randomchad.manager.DataManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

public class PlaytimeManager {

	private final JavaPlugin plugin;
	private final DataManager dataManager;
	private PlaytimeProvider provider;
	private PlaytimeProvider fallbackProvider;

	public PlaytimeManager(JavaPlugin plugin, DataManager dataManager) {
		this.plugin = plugin;
		this.dataManager = dataManager;
		reload();
	}

	public void reload() {
		this.fallbackProvider = new InternalPlaytimeProvider(dataManager);

		String source = plugin.getConfig().getString("playtime.source", "internal").toLowerCase();

		switch (source) {
			case "minecraft_statistic" -> this.provider = new MinecraftStatisticPlaytimeProvider();
			case "essentialsx" -> {
				plugin.getLogger().warning("EssentialsX playtime provider is not implemented yet. Using minecraft_statistic for now.");
				this.provider = new MinecraftStatisticPlaytimeProvider();
			}
			case "internal" -> this.provider = new InternalPlaytimeProvider(dataManager);
			default -> {
				plugin.getLogger().warning("Unknown playtime source: " + source + ". Using internal.");
				this.provider = new InternalPlaytimeProvider(dataManager);
			}
		}

		plugin.getLogger().info("Using playtime source: " + provider.getName());
	}

	public long getPlaytimeSeconds(UUID playerUuid) {
		try {
			return provider.getPlaytimeSeconds(playerUuid);
		} catch (Exception exception) {
			boolean fallback = plugin.getConfig().getBoolean("playtime.fallback-to-internal", true);

			if (!fallback) {
				throw exception;
			}

			plugin.getLogger().warning("Failed to read playtime from " + provider.getName() + ", using internal.");
			return fallbackProvider.getPlaytimeSeconds(playerUuid);
		}
	}

	public String getProviderName() {
		return provider.getName();
	}
}