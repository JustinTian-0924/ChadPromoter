package basementhost.randomchad.manager;

import org.bukkit.Material;

import java.util.List;

public class RewardDefinition {

	private final String id;
	private final String type;
	private final long requiredPlaytimeSeconds;
	private final Material material;
	private final String displayNameLangKey;
	private final String loreLangKey;
	private final double money;
	private final List<String> commands;

	public RewardDefinition(
			String id,
			String type,
			long requiredPlaytimeSeconds,
			Material material,
			String displayNameLangKey,
			String loreLangKey,
			double money,
			List<String> commands
	) {
		this.id = id;
		this.type = type;
		this.requiredPlaytimeSeconds = requiredPlaytimeSeconds;
		this.material = material;
		this.displayNameLangKey = displayNameLangKey;
		this.loreLangKey = loreLangKey;
		this.money = money;
		this.commands = commands;
	}

	public String getId() {
		return id;
	}

	public String getType() {
		return type;
	}

	public long getRequiredPlaytimeSeconds() {
		return requiredPlaytimeSeconds;
	}

	public Material getMaterial() {
		return material;
	}

	public String getDisplayNameLangKey() {
		return displayNameLangKey;
	}

	public String getLoreLangKey() {
		return loreLangKey;
	}

	public double getMoney() {
		return money;
	}

	public List<String> getCommands() {
		return commands;
	}
}