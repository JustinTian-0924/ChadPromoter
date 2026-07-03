package basementhost.randomchad.taskmodule;

public enum TaskConditionType {

	BLOCK_BREAK,
	MOB_KILL,
	AURASKILLS_TOTAL_LEVEL,
	AURASKILLS_SKILL_LEVEL,
	CHAT,
	CROP_HARVEST,
	SWIM_DISTANCE,
	FISH;

	public static TaskConditionType fromString(String value) {
		if (value == null) {
			return null;
		}

		try {
			return TaskConditionType.valueOf(value.toUpperCase());
		} catch (IllegalArgumentException exception) {
			return null;
		}
	}
}