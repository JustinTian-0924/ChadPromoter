package basementhost.randomchad.lang;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.List;
import java.util.Map;

public class LangManager {

	private final JavaPlugin plugin;
	private FileConfiguration lang;

	public LangManager(JavaPlugin plugin) {
		this.plugin = plugin;
		saveDefaultLangFiles();
		loadLang();
	}

	private void saveDefaultLangFiles() {
		plugin.saveResource("lang/en_us.yml", false);
		plugin.saveResource("lang/zh_cn.yml", false);
	}

	private void loadLang() {
		String languageName = plugin.getConfig().getString("language", "zh_cn");
		File langFile = new File(plugin.getDataFolder(), "lang/" + languageName + ".yml");

		if (!langFile.exists()) {
			plugin.getLogger().warning("Language file not found: " + languageName + ".yml, using zh_cn.yml");
			langFile = new File(plugin.getDataFolder(), "lang/zh_cn.yml");
		}

		this.lang = YamlConfiguration.loadConfiguration(langFile);
	}

	public String getMessage(String path) {
		String prefix = lang.getString("prefix", "");
		String message = lang.getString(path, path);

		return color(prefix + message);
	}

	public String getMessage(String path, Map<String, String> placeholders) {
		String message = getMessage(path);

		for (Map.Entry<String, String> entry : placeholders.entrySet()) {
			message = message.replace(entry.getKey(), entry.getValue());
		}

		return message;
	}

	public void sendMessage(CommandSender sender, String path) {
		sender.sendMessage(getMessage(path));
	}

	public void sendMessage(CommandSender sender, String path, Map<String, String> placeholders) {
		sender.sendMessage(getMessage(path, placeholders));
	}

	public void sendMessageList(CommandSender sender, String path) {
		List<String> messages = lang.getStringList(path);

		for (String message : messages) {
			sender.sendMessage(color(message));
		}
	}

	private String color(String message) {
		return ChatColor.translateAlternateColorCodes('&', message);
	}

	public String getRawMessage(String path) {
		return color(lang.getString(path, path));
	}

	public String getRawMessage(String path, Map<String, String> placeholders) {
		String message = getRawMessage(path);
		for (Map.Entry<String, String> entry : placeholders.entrySet()) {
			message = message.replace(entry.getKey(), entry.getValue());
		}
		return message;
	}

	public List<String> getRawMessageList(String path, Map<String, String> placeholders) {
		List<String> messages = lang.getStringList(path);
		return messages.stream()
				.map(message -> {
					for (Map.Entry<String, String> entry : placeholders.entrySet()) {
						message = message.replace(entry.getKey(), entry.getValue());
					}
					return color(message);
				})
				.toList();
	}
}