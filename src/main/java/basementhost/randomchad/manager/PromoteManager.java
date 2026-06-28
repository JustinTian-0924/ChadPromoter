package basementhost.randomchad.manager;

import basementhost.randomchad.playtime.PlaytimeManager;
import basementhost.randomchad.util.CodeUtil;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

public class PromoteManager {

	private final JavaPlugin plugin;
	private final DataManager dataManager;
	private final PlaytimeManager playtimeManager;

	public PromoteManager(JavaPlugin plugin, DataManager dataManager, PlaytimeManager playtimeManager) {
		this.plugin = plugin;
		this.dataManager = dataManager;
		this.playtimeManager = playtimeManager;
	}

	public String getOrCreatePromoteCode(Player player) {
		if (dataManager.hasPlayer(player.getUniqueId())) {
			dataManager.updatePlayerName(player.getUniqueId(), player.getName());
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

	public UseCodeResult useCode(Player player, String code) {
		getOrCreatePromoteCode(player);

		if (!isNewPlayer(player)) {
			return UseCodeResult.NOT_NEW_PLAYER;
		}

		if (dataManager.hasUsedCode(player.getUniqueId())) {
			return UseCodeResult.ALREADY_USED;
		}

		UUID promoterUuid = dataManager.findPlayerUuidByCode(code);

		if (promoterUuid == null) {
			return UseCodeResult.CODE_NOT_FOUND;
		}

		if (promoterUuid.equals(player.getUniqueId())) {
			return UseCodeResult.OWN_CODE;
		}

		dataManager.bindPromoter(player.getUniqueId(), code, promoterUuid);
		return UseCodeResult.SUCCESS;
	}

	public String getPromoterNameByCode(String code) {
		UUID promoterUuid = dataManager.findPlayerUuidByCode(code);

		if (promoterUuid == null) {
			return "Unknown";
		}

		return dataManager.getPlayerName(promoterUuid);
	}

	private boolean isNewPlayer(Player player) {
		int maxPlaytimeSeconds = plugin.getConfig().getInt("new-player-max-playtime-seconds", 3600);
		long playtimeSeconds = playtimeManager.getPlaytimeSeconds(player.getUniqueId());
		return playtimeSeconds <= maxPlaytimeSeconds;
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

	public enum UseCodeResult {
		SUCCESS,
		OWN_CODE,
		ALREADY_USED,
		CODE_NOT_FOUND,
		NOT_NEW_PLAYER
	}
}