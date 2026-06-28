package basementhost.randomchad.playtime;

import java.util.UUID;

public interface PlaytimeProvider {

	long getPlaytimeSeconds(UUID playerUuid);

	String getName();
}