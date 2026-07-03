package basementhost.randomchad;

import basementhost.randomchad.command.AdminCommandHandler;
import basementhost.randomchad.command.ChadPromoterCommand;
import basementhost.randomchad.command.PlayerCommandHandler;
import basementhost.randomchad.command.ReloadCommandHandler;
import basementhost.randomchad.lang.LangManager;
import basementhost.randomchad.listener.GuiClickListener;
import basementhost.randomchad.listener.PlayerJoinListener;
import basementhost.randomchad.listener.PlayerQuitListener;
import basementhost.randomchad.manager.*;
import basementhost.randomchad.playtime.PlaytimeManager;
import basementhost.randomchad.taskmodule.PlacedBlockManager;
import basementhost.randomchad.taskmodule.TaskManager;
import basementhost.randomchad.taskmodule.TaskProgressManager;
import basementhost.randomchad.taskmodule.listener.TaskBlockListener;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public final class ChadPromoterPlugin extends JavaPlugin {

	private DataManager dataManager;
	private PromoteManager promoteManager;
	private LangManager langManager;
	private Economy economy;
	private GuiManager guiManager;
	private PlaytimeManager playtimeManager;
	private RewardManager rewardManager;
	private GuiConfigManager guiConfigManager;
	private TaskManager taskManager;
	private TaskProgressManager taskProgressManager;
	private PlacedBlockManager placedBlockManager;

	@Override
	public void onEnable() {
		saveDefaultConfig();

		this.langManager = new LangManager(this);
		this.dataManager = new DataManager(this);
		this.guiConfigManager = new GuiConfigManager(this);
		this.playtimeManager = new PlaytimeManager(this, dataManager);
		this.promoteManager = new PromoteManager(this, dataManager, playtimeManager);
		this.rewardManager = new RewardManager(this, dataManager);
		this.taskManager = new TaskManager(this);
		this.taskProgressManager = new TaskProgressManager(dataManager, taskManager);
		this.placedBlockManager = new PlacedBlockManager(this);
		this.guiManager = new GuiManager(
				dataManager,
				promoteManager,
				langManager,
				rewardManager,
				playtimeManager,
				guiConfigManager
		);

		setupVaultEconomy();
		registerListeners();
		registerCommands();

		getLogger().info("ChadPromoter plugin is enabled");
	}

	@Override
	public void onDisable() {
		if (dataManager != null) {
			getServer().getOnlinePlayers().forEach(player ->
					dataManager.stopPlaytimeSession(player.getUniqueId())
			);
			dataManager.savePlayersData();
		}

		if (placedBlockManager != null) {
			placedBlockManager.shutdown();
		}
		getLogger().info("ChadPromoter plugin is disabled");
	}

	private void registerListeners() {
		getServer().getPluginManager().registerEvents(
				new PlayerJoinListener(dataManager, promoteManager, langManager),
				this
		);

		getServer().getPluginManager().registerEvents(
				new GuiClickListener(guiManager, guiConfigManager),
				this
		);

		getServer().getPluginManager().registerEvents(
				new PlayerQuitListener(dataManager),
				this
		);

		getServer().getPluginManager().registerEvents(
				new TaskBlockListener(taskManager, taskProgressManager, placedBlockManager),
				this
		);
	}

	private void registerCommands() {
		ReloadCommandHandler reloadCommandHandler = new ReloadCommandHandler(
				this,
				langManager,
				dataManager,
				playtimeManager,
				rewardManager,
				guiConfigManager,
				taskManager,
				placedBlockManager
		);

		AdminCommandHandler adminCommandHandler = new AdminCommandHandler(
				langManager,
				dataManager,
				playtimeManager,
				reloadCommandHandler,
				taskManager,
				taskProgressManager
		);

		PlayerCommandHandler playerCommandHandler = new PlayerCommandHandler(
				promoteManager,
				langManager,
				guiManager
		);

		ChadPromoterCommand command = new ChadPromoterCommand(
				playerCommandHandler,
				adminCommandHandler,
				reloadCommandHandler
		);

		if (getCommand("chadpromoter") != null) {
			getCommand("chadpromoter").setExecutor(command);
			getCommand("chadpromoter").setTabCompleter(command);
		}
	}

	private void setupVaultEconomy() {
		if (getServer().getPluginManager().getPlugin("Vault") == null) {
			getLogger().warning("Vault not found. Economy rewards will be unavailable.");
			return;
		}

		RegisteredServiceProvider<Economy> registration = getServer()
				.getServicesManager()
				.getRegistration(Economy.class);

		if (registration == null) {
			getLogger().warning("No economy plugin found through Vault.");
			return;
		}

		this.economy = registration.getProvider();
		getLogger().info("Vault economy hooked successfully.");
	}

	public DataManager getDataManager() {
		return dataManager;
	}

	public PromoteManager getPromoteManager() {
		return promoteManager;
	}

	public LangManager getLangManager() {
		return langManager;
	}

	public Economy getEconomy() {
		return economy;
	}

	public GuiManager getGuiManager() {
		return guiManager;
	}

	public PlaytimeManager getPlaytimeManager() {
		return playtimeManager;
	}

	public RewardManager getRewardManager() {
		return rewardManager;
	}

	public GuiConfigManager getGuiConfigManager() {
		return guiConfigManager;
	}

	public TaskManager getTaskManager() {
		return taskManager;
	}

	public TaskProgressManager getTaskProgressManager() {
		return taskProgressManager;
	}

	public PlacedBlockManager getPlacedBlockManager() {
		return placedBlockManager;
	}
}