package basementhost.randomchad;

import basementhost.randomchad.command.ChadPromoterCommand;
import basementhost.randomchad.lang.LangManager;
import basementhost.randomchad.listener.GuiClickListener;
import basementhost.randomchad.listener.PlayerJoinListener;
import basementhost.randomchad.listener.PlayerQuitListener;
import basementhost.randomchad.manager.DataManager;
import basementhost.randomchad.manager.GuiManager;
import basementhost.randomchad.manager.PromoteManager;
import basementhost.randomchad.manager.RewardManager;
import basementhost.randomchad.playtime.PlaytimeManager;
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

	@Override
	public void onEnable() {
		saveDefaultConfig();

		this.langManager = new LangManager(this);
		this.dataManager = new DataManager(this);
		this.playtimeManager = new PlaytimeManager(this, dataManager);
		this.promoteManager = new PromoteManager(this, dataManager, playtimeManager);
		this.rewardManager = new RewardManager(this, dataManager);
		this.guiManager = new GuiManager(
				dataManager,
				promoteManager,
				langManager,
				rewardManager,
				playtimeManager
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
		getLogger().info("ChadPromoter plugin is disabled");
	}

	private void registerListeners() {
		getServer().getPluginManager().registerEvents(
				new PlayerJoinListener(dataManager, promoteManager, langManager),
				this
		);

		getServer().getPluginManager().registerEvents(
				new GuiClickListener(guiManager),
				this
		);

		getServer().getPluginManager().registerEvents(
				new PlayerQuitListener(dataManager),
				this
		);
	}

	private void registerCommands() {
		ChadPromoterCommand command = new ChadPromoterCommand(
				this,
				promoteManager,
				langManager,
				guiManager,
				playtimeManager,
				dataManager,
				rewardManager
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
}