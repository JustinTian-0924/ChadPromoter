package basementhost.randomchad.taskmodule;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TaskManager {

	private final JavaPlugin plugin;
	private File tasksFile;
	private FileConfiguration tasksConfig;
	private final Map<String, TaskDefinition> tasks = new LinkedHashMap<>();

	public TaskManager(JavaPlugin plugin) {
		this.plugin = plugin;
		saveDefaultTasksFile();
		loadTasks();
	}

	private void saveDefaultTasksFile() {
		tasksFile = new File(plugin.getDataFolder(), "tasks.yml");

		if (!tasksFile.exists()) {
			plugin.saveResource("tasks.yml", false);
		}
	}

	public void reload() {
		loadTasks();
	}

	private void loadTasks() {
		tasks.clear();
		tasksConfig = YamlConfiguration.loadConfiguration(tasksFile);

		ConfigurationSection tasksSection = tasksConfig.getConfigurationSection("tasks");

		if (tasksSection == null) {
			plugin.getLogger().warning("No tasks section found in tasks.yml.");
			return;
		}

		for (String taskId : tasksSection.getKeys(false)) {
			ConfigurationSection taskSection = tasksSection.getConfigurationSection(taskId);

			if (taskSection == null) {
				continue;
			}

			TaskDefinition taskDefinition = loadTaskDefinition(taskId, taskSection);

			if (taskDefinition != null) {
				tasks.put(taskId, taskDefinition);
			}
		}

		plugin.getLogger().info("Loaded " + tasks.size() + " task(s).");
	}

	private TaskDefinition loadTaskDefinition(String taskId, ConfigurationSection taskSection) {
		Set<TaskPanelType> panels = loadPanels(taskSection.getStringList("panels"));

		String displayNameLangKey = taskSection.getString("display-name-lang-key", "tasks." + taskId + ".name");
		String loreLangKey = taskSection.getString("lore-lang-key", "tasks." + taskId + ".lore");

		ConfigurationSection conditionsSection = taskSection.getConfigurationSection("conditions");

		if (conditionsSection == null) {
			plugin.getLogger().warning("Task " + taskId + " has no conditions.");
			return null;
		}

		List<TaskCondition> conditions = new ArrayList<>();

		for (String conditionId : conditionsSection.getKeys(false)) {
			ConfigurationSection conditionSection = conditionsSection.getConfigurationSection(conditionId);

			if (conditionSection == null) {
				continue;
			}

			TaskCondition condition = loadCondition(taskId, conditionId, conditionSection);

			if (condition != null) {
				conditions.add(condition);
			}
		}

		if (conditions.isEmpty()) {
			plugin.getLogger().warning("Task " + taskId + " has no valid conditions.");
			return null;
		}

		return new TaskDefinition(
				taskId,
				panels,
				displayNameLangKey,
				loreLangKey,
				conditions
		);
	}

	private Set<TaskPanelType> loadPanels(List<String> panelNames) {
		Set<TaskPanelType> panels = new LinkedHashSet<>();

		for (String panelName : panelNames) {
			TaskPanelType panelType = TaskPanelType.fromString(panelName);

			if (panelType != null) {
				panels.add(panelType);
			}
		}

		if (panels.isEmpty()) {
			panels.add(TaskPanelType.SELF);
		}

		return panels;
	}

	private TaskCondition loadCondition(String taskId, String conditionId, ConfigurationSection section) {
		TaskConditionType type = TaskConditionType.fromString(section.getString("type"));

		if (type == null) {
			plugin.getLogger().warning("Task " + taskId + " condition " + conditionId + " has invalid type.");
			return null;
		}

		long amount = section.getLong("amount", 1);
		List<Material> materials = loadMaterials(section.getStringList("materials"));

		boolean countPlayerPlaced = section.getBoolean("count-player-placed", true);
		boolean hostileOnly = section.getBoolean("hostile-only", false);
		boolean countSpawnerMobs = section.getBoolean("count-spawner-mobs", true);

		World.Environment worldEnvironment = null;
		String environmentName = section.getString("world-environment");

		if (environmentName != null && !environmentName.isBlank()) {
			try {
				worldEnvironment = World.Environment.valueOf(environmentName.toUpperCase());
			} catch (IllegalArgumentException exception) {
				plugin.getLogger().warning("Task " + taskId + " condition " + conditionId + " has invalid world-environment.");
			}
		}

		String skill = section.getString("skill");

		return new TaskCondition(
				conditionId,
				type,
				amount,
				materials,
				countPlayerPlaced,
				hostileOnly,
				worldEnvironment,
				countSpawnerMobs,
				skill
		);
	}

	private List<Material> loadMaterials(List<String> materialNames) {
		List<Material> materials = new ArrayList<>();

		for (String materialName : materialNames) {
			Material material = Material.matchMaterial(materialName);

			if (material != null) {
				materials.add(material);
			} else {
				plugin.getLogger().warning("Invalid material in tasks.yml: " + materialName);
			}
		}

		return materials;
	}

	public TaskDefinition getTask(String taskId) {
		return tasks.get(taskId);
	}

	public Collection<TaskDefinition> getTasks() {
		return tasks.values();
	}

	public List<TaskDefinition> getTasksByPanel(TaskPanelType panelType) {
		List<TaskDefinition> result = new ArrayList<>();

		for (TaskDefinition taskDefinition : tasks.values()) {
			if (taskDefinition.isVisibleInPanel(panelType)) {
				result.add(taskDefinition);
			}
		}

		return result;
	}
}