package basementhost.randomchad.manager;

import basementhost.randomchad.gui.ChadPromoterGuiHolder;
import basementhost.randomchad.lang.LangManager;
import basementhost.randomchad.playtime.PlaytimeManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class GuiManager {

	private final DataManager dataManager;
	private final PromoteManager promoteManager;
	private final LangManager langManager;
	private final RewardManager rewardManager;
	private final PlaytimeManager playtimeManager;

	public GuiManager(
			DataManager dataManager,
			PromoteManager promoteManager,
			LangManager langManager,
			RewardManager rewardManager,
			PlaytimeManager playtimeManager
	) {
		this.dataManager = dataManager;
		this.promoteManager = promoteManager;
		this.langManager = langManager;
		this.rewardManager = rewardManager;
		this.playtimeManager = playtimeManager;
	}

	public void openMainGui(Player player) {
		String code = promoteManager.getOrCreatePromoteCode(player);
		int invitedCount = dataManager.getPromotedPlayerCount(player.getUniqueId());
		String usedCode = dataManager.getUsedCode(player.getUniqueId());

		ChadPromoterGuiHolder holder = new ChadPromoterGuiHolder(ChadPromoterGuiHolder.GuiType.MAIN, 0);

		Inventory inventory = Bukkit.createInventory(
				holder,
				27,
				langManager.getRawMessage("gui.title")
		);

		holder.setInventory(inventory);

		inventory.setItem(11, createItem(
				Material.NAME_TAG,
				langManager.getRawMessage("gui.my-code-name"),
				langManager.getRawMessageList("gui.my-code-lore", Map.of("%code%", code))
		));

		inventory.setItem(13, createItem(
				Material.PLAYER_HEAD,
				langManager.getRawMessage("gui.invited-count-name"),
				langManager.getRawMessageList("gui.invited-count-lore", Map.of("%count%", String.valueOf(invitedCount)))
		));

		List<String> usedCodeLore = usedCode == null
				? langManager.getRawMessageList("gui.used-code-none-lore", Map.of())
				: langManager.getRawMessageList("gui.used-code-exists-lore", Map.of("%code%", usedCode));

		inventory.setItem(15, createItem(
				Material.BOOK,
				langManager.getRawMessage("gui.used-code-name"),
				usedCodeLore
		));

		inventory.setItem(22, createItem(
				Material.PAPER,
				langManager.getRawMessage("gui.help-name"),
				langManager.getRawMessageList("gui.help-lore", Map.of())
		));

		player.openInventory(inventory);
	}

	public void openPromotedPlayersGui(Player player, int page) {
		List<UUID> promotedPlayerUuids = dataManager.getPromotedPlayerUuids(player.getUniqueId());
		int itemsPerPage = 21;
		int startIndex = page * itemsPerPage;
		int endIndex = Math.min(startIndex + itemsPerPage, promotedPlayerUuids.size());

		ChadPromoterGuiHolder holder = new ChadPromoterGuiHolder(
				ChadPromoterGuiHolder.GuiType.PROMOTED_LIST,
				page
		);

		Inventory inventory = Bukkit.createInventory(
				holder,
				27,
				langManager.getRawMessage("gui.promoted-list-title", Map.of("%page%", String.valueOf(page + 1)))
		);

		holder.setInventory(inventory);

		if (promotedPlayerUuids.isEmpty()) {
			inventory.setItem(13, createItem(
					Material.BARRIER,
					langManager.getRawMessage("gui.no-promoted-players-name"),
					langManager.getRawMessageList("gui.no-promoted-players-lore", Map.of())
			));
		} else {
			for (int index = startIndex; index < endIndex; index++) {
				UUID promotedPlayerUuid = promotedPlayerUuids.get(index);
				String playerName = dataManager.getPlayerName(promotedPlayerUuid);

				inventory.setItem(index - startIndex, createItem(
						Material.PLAYER_HEAD,
						langManager.getRawMessage("gui.promoted-player-name", Map.of("%player%", playerName)),
						langManager.getRawMessageList(
								"gui.promoted-player-lore",
								Map.of(
										"%player%", playerName,
										"%uuid%", promotedPlayerUuid.toString()
								)
						)
				));
			}
		}

		if (page > 0) {
			inventory.setItem(18, createItem(
					Material.ARROW,
					langManager.getRawMessage("gui.previous-page-name"),
					List.of()
			));
		}

		inventory.setItem(22, createItem(
				Material.BARRIER,
				langManager.getRawMessage("gui.back-name"),
				List.of()
		));

		if (endIndex < promotedPlayerUuids.size()) {
			inventory.setItem(26, createItem(
					Material.ARROW,
					langManager.getRawMessage("gui.next-page-name"),
					List.of()
			));
		}

		player.openInventory(inventory);
	}

	public void openRewardListGui(Player promoter, UUID promotedPlayerUuid) {
		String promotedPlayerName = dataManager.getPlayerName(promotedPlayerUuid);
		long promotedPlaytimeSeconds = playtimeManager.getPlaytimeSeconds(promotedPlayerUuid);

		ChadPromoterGuiHolder holder = new ChadPromoterGuiHolder(
				ChadPromoterGuiHolder.GuiType.REWARD_LIST,
				0,
				promotedPlayerUuid
		);

		Inventory inventory = Bukkit.createInventory(
				holder,
				27,
				langManager.getRawMessage(
						"gui.reward-list-title",
						Map.of("%player%", promotedPlayerName)
				)
		);

		holder.setInventory(inventory);

		List<RewardDefinition> rewards = new ArrayList<>(rewardManager.getRewards());

		for (int index = 0; index < rewards.size() && index < 21; index++) {
			RewardDefinition reward = rewards.get(index);

			boolean claimed = dataManager.hasClaimedReward(
					promoter.getUniqueId(),
					promotedPlayerUuid,
					reward.getId()
			);

			boolean unlocked = rewardManager.isUnlocked(
					reward,
					promotedPlayerUuid,
					promotedPlaytimeSeconds
			);

			List<String> lore = new ArrayList<>(
					langManager.getRawMessageList(reward.getLoreLangKey(), Map.of(
							"%current_playtime%", formatSeconds(promotedPlaytimeSeconds),
							"%required_playtime%", formatSeconds(reward.getRequiredPlaytimeSeconds())
					))
			);

			if (claimed) {
				lore.addAll(langManager.getRawMessageList("gui.reward-claimed-lore", Map.of()));
			} else if (unlocked) {
				lore.addAll(langManager.getRawMessageList("gui.reward-available-lore", Map.of()));
			} else {
				lore.addAll(langManager.getRawMessageList("gui.reward-locked-lore", Map.of(
						"%current_playtime%", formatSeconds(promotedPlaytimeSeconds),
						"%required_playtime%", formatSeconds(reward.getRequiredPlaytimeSeconds())
				)));
			}

			Material material = reward.getMaterial();

			if (claimed) {
				material = Material.BARRIER;
			} else if (!unlocked) {
				material = Material.GRAY_DYE;
			}

			inventory.setItem(index, createItem(
					material,
					langManager.getRawMessage(reward.getDisplayNameLangKey()),
					lore
			));
		}

		inventory.setItem(22, createItem(
				Material.ARROW,
				langManager.getRawMessage("gui.reward-back-name"),
				List.of()
		));

		playerOpen(promoter, inventory);
	}

	public void openRewardListGuiByPromotedListSlot(Player promoter, int page, int slot) {
		if (slot < 0 || slot >= 21) {
			return;
		}

		List<UUID> promotedPlayerUuids = dataManager.getPromotedPlayerUuids(promoter.getUniqueId());
		int index = page * 21 + slot;

		if (index < 0 || index >= promotedPlayerUuids.size()) {
			return;
		}

		UUID promotedPlayerUuid = promotedPlayerUuids.get(index);
		openRewardListGui(promoter, promotedPlayerUuid);
	}

	public void claimRewardFromGui(Player promoter, UUID promotedPlayerUuid, int slot) {
		if (promotedPlayerUuid == null) {
			return;
		}

		if (slot < 0 || slot >= 21) {
			return;
		}

		List<RewardDefinition> rewards = new ArrayList<>(rewardManager.getRewards());

		if (slot >= rewards.size()) {
			return;
		}

		RewardDefinition reward = rewards.get(slot);
		long promotedPlaytimeSeconds = playtimeManager.getPlaytimeSeconds(promotedPlayerUuid);

		RewardManager.ClaimResult result = rewardManager.claimReward(
				promoter.getUniqueId(),
				promotedPlayerUuid,
				reward,
				promotedPlaytimeSeconds
		);

		switch (result) {
			case SUCCESS -> langManager.sendMessage(promoter, "reward.claimed");
			case ALREADY_CLAIMED -> langManager.sendMessage(promoter, "reward.already-claimed");
			case NOT_UNLOCKED -> langManager.sendMessage(promoter, "reward.not-unlocked");
			case VAULT_UNAVAILABLE -> langManager.sendMessage(promoter, "reward.vault-unavailable");
		}

		openRewardListGui(promoter, promotedPlayerUuid);
	}

	private void playerOpen(Player player, Inventory inventory) {
		player.openInventory(inventory);
	}

	private ItemStack createItem(Material material, String name, List<String> lore) {
		ItemStack itemStack = new ItemStack(material);
		ItemMeta itemMeta = itemStack.getItemMeta();

		if (itemMeta != null) {
			itemMeta.setDisplayName(name);
			itemMeta.setLore(lore);
			itemStack.setItemMeta(itemMeta);
		}

		return itemStack;
	}

	private String formatSeconds(long seconds) {
		long hours = seconds / 3600;
		long minutes = (seconds % 3600) / 60;
		long remainingSeconds = seconds % 60;

		if (hours > 0) {
			return hours + "h " + minutes + "m";
		}

		if (minutes > 0) {
			return minutes + "m " + remainingSeconds + "s";
		}

		return remainingSeconds + "s";
	}
}