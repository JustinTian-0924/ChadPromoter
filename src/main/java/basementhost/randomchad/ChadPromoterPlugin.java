package basementhost.randomchad;

import basementhost.randomchad.command.ChadPromoterCommand;
import basementhost.randomchad.lang.LangManager;
import basementhost.randomchad.listener.PlayerJoinListener;
import basementhost.randomchad.manager.DataManager;
import basementhost.randomchad.manager.PromoteManager;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public final class ChadPromoterPlugin extends JavaPlugin {

	private DataManager dataManager;
	private PromoteManager promoteManager;
	private LangManager langManager;
	private Economy economy;

	@Override
	public void onEnable() {
		saveDefaultConfig();

		this.langManager = new LangManager(this);
		this.dataManager = new DataManager(this);
		this.promoteManager = new PromoteManager(this, dataManager);

		setupVaultEconomy();
		registerListeners();
		registerCommands();

		getLogger().info("ChadPromoter plugin is enabled");
	}

	@Override
	public void onDisable() {
		if (dataManager != null) {
			dataManager.savePlayersData();
		}

		getLogger().info("ChadPromoter plugin is disabled");
	}

	private void registerListeners() {
		getServer().getPluginManager().registerEvents(
				new PlayerJoinListener(dataManager, promoteManager, langManager),
				this
		);
	}

	private void registerCommands() {
		ChadPromoterCommand command = new ChadPromoterCommand(promoteManager, langManager);

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
}