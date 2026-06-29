package basementhost.randomchad.util;

import basementhost.randomchad.lang.LangManager;

import java.util.Map;

public class TimeUtil {

	private TimeUtil() {
	}

	public static String formatSeconds(LangManager langManager, long seconds) {
		long hours = seconds / 3600;
		long minutes = (seconds % 3600) / 60;
		long remainingSeconds = seconds % 60;

		if (hours > 0) {
			return langManager.getRawMessage("time.hours-minutes", Map.of(
					"%hours%", String.valueOf(hours),
					"%minutes%", String.valueOf(minutes)
			));
		}

		if (minutes > 0) {
			return langManager.getRawMessage("time.minutes-seconds", Map.of(
					"%minutes%", String.valueOf(minutes),
					"%seconds%", String.valueOf(remainingSeconds)
			));
		}

		return langManager.getRawMessage("time.seconds", Map.of(
				"%seconds%", String.valueOf(remainingSeconds)
		));
	}
}