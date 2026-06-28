package basementhost.randomchad.manager;

import basementhost.randomchad.util.CodeUtil;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class PromoteManager {

	private final JavaPlugin plugin;
	private final DataManager dataManager;

	public PromoteManager(JavaPlugin plugin, DataManager dataManager) {
		this.plugin = plugin;
		this.dataManager = dataManager;
	}

	public String getOrCreatePromoteCode(Player player) {
		if (dataManager.hasPlayer(player.getUniqueId())) {
			return dataManager.getPromoteCode(player.getUniqueId());
		}

		String code = generateUniqueCode();

		dataManager.createPlayer(
				player.getUniqueId(),
				player.getName(),
				code
		);

		return code;
	}

	private String generateUniqueCode() {
		int length = plugin.getConfig().getInt("code.length", 6);
		String characters = plugin.getConfig().getString(
				"code.characters",
				"ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
		);

		String code;

		do {
			code = CodeUtil.generateCode(length, characters);
		} while (dataManager.isCodeUsed(code));

		return code;
	}
}