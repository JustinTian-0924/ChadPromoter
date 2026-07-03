package basementhost.randomchad.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

import java.util.ArrayList;
import java.util.List;

public class ChadPromoterCommand implements TabExecutor {

	private final PlayerCommandHandler playerCommandHandler;
	private final AdminCommandHandler adminCommandHandler;
	private final ReloadCommandHandler reloadCommandHandler;

	public ChadPromoterCommand(
			PlayerCommandHandler playerCommandHandler,
			AdminCommandHandler adminCommandHandler,
			ReloadCommandHandler reloadCommandHandler
	) {
		this.playerCommandHandler = playerCommandHandler;
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

		playerCommandHandler.handlePlayerCommand(sender, args);
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
		if (args.length == 1) {
			List<String> completions = new ArrayList<>();

			completions.add("use");

			if (sender.hasPermission("chadpromoter.reload")) {
				completions.add("reload");
			}

			if (sender.hasPermission("chadpromoter.admin")) {
				completions.add("admin");
			}

			return filterStartsWith(completions, args[0]);
		}

		if (args.length >= 2 && args[0].equalsIgnoreCase("admin")) {
			return adminCommandHandler.tabComplete(sender, args);
		}

		return playerCommandHandler.tabComplete(sender, args);
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