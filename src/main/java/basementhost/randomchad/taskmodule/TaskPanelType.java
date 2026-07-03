package basementhost.randomchad.taskmodule;

public enum TaskPanelType {

	PROMOTER,
	SELF;

	public static TaskPanelType fromString(String value) {
		if (value == null) {
			return null;
		}

		try {
			return TaskPanelType.valueOf(value.toUpperCase());
		} catch (IllegalArgumentException exception) {
			return null;
		}
	}
}