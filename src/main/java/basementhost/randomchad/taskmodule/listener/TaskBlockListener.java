package basementhost.randomchad.taskmodule.listener;

import basementhost.randomchad.taskmodule.PlacedBlockManager;
import basementhost.randomchad.taskmodule.TaskCondition;
import basementhost.randomchad.taskmodule.TaskConditionType;
import basementhost.randomchad.taskmodule.TaskDefinition;
import basementhost.randomchad.taskmodule.TaskManager;
import basementhost.randomchad.taskmodule.TaskProgressManager;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public class TaskBlockListener implements Listener {

	private final TaskManager taskManager;
	private final TaskProgressManager taskProgressManager;
	private final PlacedBlockManager placedBlockManager;

	public TaskBlockListener(
			TaskManager taskManager,
			TaskProgressManager taskProgressManager,
			PlacedBlockManager placedBlockManager
	) {
		this.taskManager = taskManager;
		this.taskProgressManager = taskProgressManager;
		this.placedBlockManager = placedBlockManager;
	}

	@EventHandler(ignoreCancelled = true)
	public void onBlockPlace(BlockPlaceEvent event) {
		placedBlockManager.markPlaced(event.getBlockPlaced());
	}

	@EventHandler(ignoreCancelled = true)
	public void onBlockBreak(BlockBreakEvent event) {
		Player player = event.getPlayer();
		Block block = event.getBlock();
		Material brokenMaterial = block.getType();

		boolean playerPlaced = placedBlockManager.isPlayerPlaced(block);

		for (TaskDefinition taskDefinition : taskManager.getTasks()) {
			for (TaskCondition condition : taskDefinition.getConditions()) {
				if (condition.getType() != TaskConditionType.BLOCK_BREAK) {
					continue;
				}

				if (!matchesMaterial(condition, brokenMaterial)) {
					continue;
				}

				if (!condition.isCountPlayerPlaced() && playerPlaced) {
					continue;
				}

				taskProgressManager.addProgress(
						player.getUniqueId(),
						taskDefinition.getId(),
						condition.getId(),
						1
				);
			}
		}

		placedBlockManager.remove(block);
	}

	private boolean matchesMaterial(TaskCondition condition, Material material) {
		if (!condition.hasMaterialLimit()) {
			return true;
		}

		return condition.getMaterials().contains(material);
	}
}