package basementhost.randomchad.taskmodule;

import java.util.List;
import java.util.Set;

public class TaskDefinition {

	private final String id;
	private final Set<TaskPanelType> panels;
	private final String displayNameLangKey;
	private final String loreLangKey;
	private final List<TaskCondition> conditions;

	public TaskDefinition(
			String id,
			Set<TaskPanelType> panels,
			String displayNameLangKey,
			String loreLangKey,
			List<TaskCondition> conditions
	) {
		this.id = id;
		this.panels = panels;
		this.displayNameLangKey = displayNameLangKey;
		this.loreLangKey = loreLangKey;
		this.conditions = conditions;
	}

	public String getId() {
		return id;
	}

	public Set<TaskPanelType> getPanels() {
		return panels;
	}

	public String getDisplayNameLangKey() {
		return displayNameLangKey;
	}

	public String getLoreLangKey() {
		return loreLangKey;
	}

	public List<TaskCondition> getConditions() {
		return conditions;
	}

	public boolean isVisibleInPanel(TaskPanelType panelType) {
		return panels.contains(panelType);
	}
}