package basementhost.randomchad.command;

import basementhost.randomchad.lang.LangManager;
import basementhost.randomchad.manager.GuiManager;
import basementhost.randomchad.manager.PromoteManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

public class PlayerCommandHandler {

	private final PromoteManager promoteManager;
	private final LangManager langManager;
	private final GuiManager guiManager;

	public PlayerCommandHandler(
			PromoteManager promoteManager,
			LangManager langManager,
			GuiManager guiManager
	) {
		this.promoteManager = promoteManager;
		this.langManager = langManager;
		this.guiManager = guiManager;
	}

	public void handlePlayerCommand(CommandSender sender, String[] args) {
		if (!(sender instanceof Player player)) {
			langManager.sendMessage(sender, "player-only");
			return;
		}

		if (args.length == 0) {
			guiManager.openMainGui(player);
			return;
		}

		if (args.length == 2 && args[0].equalsIgnoreCase("use")) {
			handleUseCodeCommand(player, args[1]);
			return;
		}

		langManager.sendMessageList(player, "usage");
	}

	private void handleUseCodeCommand(Player player, String code) {
		PromoteManager.UseCodeResult result = promoteManager.useCode(player, code);

		switch (result) {
			case SUCCESS -> langManager.sendMessage(
					player,
					"use-success",
					Map.of("%promoter%", promoteManager.getPromoterNameByCode(code))
			);
			case OWN_CODE -> langManager.sendMessage(player, "use-failed-own-code");
			case ALREADY_USED -> langManager.sendMessage(player, "use-failed-already-used");
			case CODE_NOT_FOUND -> langManager.sendMessage(player, "use-failed-code-not-found");
			case NOT_NEW_PLAYER -> langManager.sendMessage(player, "use-failed-not-new-player");
		}
	}

	public List<String> tabComplete(CommandSender sender, String[] args) {
		if (args.length == 1) {
			return filterStartsWith(List.of("use"), args[0]);
		}

		return List.of();
	}

	private List<String> filterStartsWith(List<String> options, String prefix) {
		if (prefix == null || prefix.isBlank()) {
			return options;
		}

		String lowerPrefix = prefix.toLowerCase();

		return options.stream()
				.filter(option -> option.toLowerCase().startsWith(lowerPrefix))
				.toList();
	}
}