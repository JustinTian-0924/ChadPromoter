package basementhost.randomchad.taskmodule.listener;

import basementhost.randomchad.taskmodule.TaskNotificationManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class TaskNotificationListener implements Listener {

	private final JavaPlugin plugin;
	private final TaskNotificationManager taskNotificationManager;

	public TaskNotificationListener(
			JavaPlugin plugin,
			TaskNotificationManager taskNotificationManager
	) {
		this.plugin = plugin;
		this.taskNotificationManager = taskNotificationManager;
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();

		plugin.getServer().getScheduler().runTaskLater(
				plugin,
				() -> taskNotificationManager.notifyOnLogin(player),
				40L
		);
	}
}