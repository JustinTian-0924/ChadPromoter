package basementhost.randomchad.manager;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class GuiConfigManager {

	private final JavaPlugin plugin;
	private File guiFile;
	private FileConfiguration guiConfig;

	public GuiConfigManager(JavaPlugin plugin) {
		this.plugin = plugin;
		saveDefaultGuiFile();
		loadGuiConfig();
	}

	private void saveDefaultGuiFile() {
		guiFile = new File(plugin.getDataFolder(), "gui.yml");

		if (!guiFile.exists()) {
			plugin.saveResource("gui.yml", false);
		}
	}

	private void loadGuiConfig() {
		guiConfig = YamlConfiguration.loadConfiguration(guiFile);
	}

	public void reload() {
		loadGuiConfig();
	}

	public boolean getBoolean(String path, boolean defaultValue) {
		return guiConfig.getBoolean(path, defaultValue);
	}

	public int getInt(String path, int defaultValue) {
		return guiConfig.getInt(path, defaultValue);
	}

	public Material getMaterial(String path, Material defaultMaterial) {
		String materialName = guiConfig.getString(path, defaultMaterial.name());
		Material material = Material.matchMaterial(materialName);

		return material == null ? defaultMaterial : material;
	}

	public List<Integer> getContentSlots() {
		List<Integer> slots = guiConfig.getIntegerList("common.content-slots");

		if (slots.isEmpty()) {
			slots = new ArrayList<>();

			for (int slot = 0; slot <= 17; slot++) {
				slots.add(slot);
			}
		}

		return slots;
	}

	public int getContentSlotIndex(int clickedSlot) {
		List<Integer> contentSlots = getContentSlots();

		for (int index = 0; index < contentSlots.size(); index++) {
			if (contentSlots.get(index) == clickedSlot) {
				return index;
			}
		}

		return -1;
	}
}