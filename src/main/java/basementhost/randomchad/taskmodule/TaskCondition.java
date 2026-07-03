package basementhost.randomchad.taskmodule;

import org.bukkit.Material;
import org.bukkit.World;

import java.util.List;

public class TaskCondition {

	private final String id;
	private final TaskConditionType type;
	private final long amount;
	private final List<Material> materials;
	private final boolean countPlayerPlaced;
	private final boolean hostileOnly;
	private final World.Environment worldEnvironment;
	private final boolean countSpawnerMobs;
	private final String skill;

	public TaskCondition(
			String id,
			TaskConditionType type,
			long amount,
			List<Material> materials,
			boolean countPlayerPlaced,
			boolean hostileOnly,
			World.Environment worldEnvironment,
			boolean countSpawnerMobs,
			String skill
	) {
		this.id = id;
		this.type = type;
		this.amount = amount;
		this.materials = materials;
		this.countPlayerPlaced = countPlayerPlaced;
		this.hostileOnly = hostileOnly;
		this.worldEnvironment = worldEnvironment;
		this.countSpawnerMobs = countSpawnerMobs;
		this.skill = skill;
	}

	public String getId() {
		return id;
	}

	public TaskConditionType getType() {
		return type;
	}

	public long getAmount() {
		return amount;
	}

	public List<Material> getMaterials() {
		return materials;
	}

	public boolean isCountPlayerPlaced() {
		return countPlayerPlaced;
	}

	public boolean isHostileOnly() {
		return hostileOnly;
	}

	public World.Environment getWorldEnvironment() {
		return worldEnvironment;
	}

	public boolean isCountSpawnerMobs() {
		return countSpawnerMobs;
	}

	public String getSkill() {
		return skill;
	}

	public boolean hasMaterialLimit() {
		return materials != null && !materials.isEmpty();
	}
}