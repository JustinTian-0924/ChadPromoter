package basementhost.randomchad.command;

import basementhost.randomchad.lang.LangManager;
import basementhost.randomchad.manager.GuiManager;
import basementhost.randomchad.manager.PromoteManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

public class ChadPromoterCommand implements TabExecutor {

	private final PromoteManager promoteManager;
	private final LangManager langManager;
	private final GuiManager guiManager;
	private final AdminCommandHandler adminCommandHandler;
	private final ReloadCommandHandler reloadCommandHandler;

	public ChadPromoterCommand(
			PromoteManager promoteManager,
			LangManager langManager,
			GuiManager guiManager,
			AdminCommandHandler adminCommandHandler,
			ReloadCommandHandler reloadCommandHandler
	) {
		this.promoteManager = promoteManager;
		this.langManager = langManager;
		this.guiManager = guiManager;
		this.adminCommandHandler = adminCommandHandler;
		this.reloadCommandHandler = reloadCommandHandler;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
			reloadCommandHandler.handleReload(sender);
			return true;
		}

		if (args.length >= 1 && args[0].equalsIgnoreCase("admin")) {
			adminCommandHandler.handleAdminCommand(sender, args);
			return true;
		}

		if (!(sender instanceof Player player)) {
			langManager.sendMessage(sender, "player-only");
			return true;
		}

		if (args.length == 0) {
			guiManager.openMainGui(player);
			return true;
		}

		if (args.length == 2 && args[0].equalsIgnoreCase("use")) {
			handleUseCodeCommand(player, args[1]);
			return true;
		}

		langManager.sendMessageList(player, "usage");
		return true;
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

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
		if (args.length == 1) {
			if (sender.hasPermission("chadpromoter.admin")) {
				return List.of("use", "reload", "admin");
			}

			if (sender.hasPermission("chadpromoter.reload")) {
				return List.of("use", "reload");
			}

			return List.of("use");
		}

		if (args.length >= 2 && args[0].equalsIgnoreCase("admin")) {
			return adminCommandHandler.tabComplete(sender, args);
		}

		return List.of();
	}
}