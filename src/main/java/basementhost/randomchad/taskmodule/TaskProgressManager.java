package basementhost.randomchad.taskmodule;

import basementhost.randomchad.manager.DataManager;

import java.util.UUID;

public class TaskProgressManager {

	private final DataManager dataManager;
	private final TaskManager taskManager;

	public TaskProgressManager(DataManager dataManager, TaskManager taskManager) {
		this.dataManager = dataManager;
		this.taskManager = taskManager;
	}

	public long getProgress(UUID playerUuid, String taskId, String conditionId) {
		return dataManager.getTaskConditionProgress(playerUuid, taskId, conditionId);
	}

	public void addProgress(UUID playerUuid, String taskId, String conditionId, long amount) {
		if (amount <= 0) {
			return;
		}

		TaskDefinition taskDefinition = taskManager.getTask(taskId);

		if (taskDefinition == null) {
			return;
		}

		TaskCondition condition = getCondition(taskDefinition, conditionId);

		if (condition == null) {
			return;
		}

		long current = getProgress(playerUuid, taskId, conditionId);

		if (current >= condition.getAmount()) {
			return;
		}

		long newValue = Math.min(current + amount, condition.getAmount());
		dataManager.setTaskConditionProgress(playerUuid, taskId, conditionId, newValue);
	}

	public void setProgress(UUID playerUuid, String taskId, String conditionId, long amount) {
		dataManager.setTaskConditionProgress(playerUuid, taskId, conditionId, Math.max(0, amount));
	}

	public boolean isTaskCompleted(UUID playerUuid, String taskId) {
		TaskDefinition taskDefinition = taskManager.getTask(taskId);

		if (taskDefinition == null) {
			return false;
		}

		for (TaskCondition condition : taskDefinition.getConditions()) {
			long current = getProgress(playerUuid, taskId, condition.getId());

			if (current < condition.getAmount()) {
				return false;
			}
		}

		return true;
	}

	private TaskCondition getCondition(TaskDefinition taskDefinition, String conditionId) {
		for (TaskCondition condition : taskDefinition.getConditions()) {
			if (condition.getId().equalsIgnoreCase(conditionId)) {
				return condition;
			}
		}

		return null;
	}

	public void resetTaskProgress(UUID playerUuid, String taskId) {
		dataManager.resetTaskProgress(playerUuid, taskId);
	}
}