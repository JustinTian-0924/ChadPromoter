package basementhost.randomchad.playtime;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;

import java.util.UUID;

public class MinecraftStatisticPlaytimeProvider implements PlaytimeProvider {

	@Override
	public long getPlaytimeSeconds(UUID playerUuid) {
		OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerUuid);
		int ticks = offlinePlayer.getStatistic(Statistic.PLAY_ONE_MINUTE);

		return ticks / 20L;
	}

	@Override
	public String getName() {
		return "minecraft_statistic";
	}
}