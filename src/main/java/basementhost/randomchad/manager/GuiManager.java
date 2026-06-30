package basementhost.randomchad.manager;

import basementhost.randomchad.gui.ChadPromoterGuiHolder;
import basementhost.randomchad.lang.LangManager;
import basementhost.randomchad.playtime.PlaytimeManager;
import basementhost.randomchad.util.TimeUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;

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
	private final GuiConfigManager guiConfigManager;

	public GuiManager(
			DataManager dataManager,
			PromoteManager promoteManager,
			LangManager langManager,
			RewardManager rewardManager,
			PlaytimeManager playtimeManager,
			GuiConfigManager guiConfigManager
	) {
		this.dataManager = dataManager;
		this.promoteManager = promoteManager;
		this.langManager = langManager;
		this.rewardManager = rewardManager;
		this.playtimeManager = playtimeManager;
		this.guiConfigManager = guiConfigManager;
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

		inventory.setItem(guiConfigManager.getInt("main.my-code.slot", 11), createPlayerHeadItem(
				player.getUniqueId(),
				langManager.getRawMessage("gui.my-code-name"),
				langManager.getRawMessageList("gui.my-code-lore", Map.of("%code%", code))
		));

		inventory.setItem(guiConfigManager.getInt("main.invited-count.slot", 13), createItem(
				Material.PLAYER_HEAD,
				langManager.getRawMessage("gui.invited-count-name"),
				langManager.getRawMessageList("gui.invited-count-lore", Map.of("%count%", String.valueOf(invitedCount)))
		));
		List<String> usedCodeLore = usedCode == null
				? langManager.getRawMessageList("gui.used-code-none-lore", Map.of())
				: langManager.getRawMessageList("gui.used-code-exists-lore", Map.of("%code%", usedCode));
		inventory.setItem(guiConfigManager.getInt("main.used-code.slot", 15), createItem(
				Material.BOOK,
				langManager.getRawMessage("gui.used-code-name"),
				usedCodeLore
		));

		inventory.setItem(guiConfigManager.getInt("main.help.slot", 22), createItem(
				Material.PAPER,
				langManager.getRawMessage("gui.help-name"),
				langManager.getRawMessageList("gui.help-lore", Map.of())
		));

		fillEmptySlots(inventory);
		player.openInventory(inventory);
	}

	public void openPromotedPlayersGui(Player player, int page) {
		List<UUID> promotedPlayerUuids = dataManager.getPromotedPlayerUuids(player.getUniqueId());

		List<Integer> contentSlots = guiConfigManager.getContentSlots();
		int itemsPerPage = contentSlots.size();
		int maxPage = getMaxPage(promotedPlayerUuids.size(), itemsPerPage);
		page = clampPage(page, maxPage);
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

				inventory.setItem(contentSlots.get(index - startIndex), createPlayerHeadItem(
						promotedPlayerUuid,
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
			inventory.setItem(guiConfigManager.getInt("promoted-list.previous-page.slot", 18), createItem(
					Material.ARROW,
					langManager.getRawMessage("gui.previous-page-name"),
					List.of()
			));
		}

		inventory.setItem(guiConfigManager.getInt("promoted-list.back.slot", 22), createItem(
				Material.BARRIER,
				langManager.getRawMessage("gui.back-name"),
				List.of()
		));

		if (endIndex < promotedPlayerUuids.size()) {
			inventory.setItem(guiConfigManager.getInt("promoted-list.next-page.slot", 26), createItem(
					Material.ARROW,
					langManager.getRawMessage("gui.next-page-name"),
					List.of()
			));
		}

		fillEmptySlots(inventory);
		player.openInventory(inventory);
	}

	public void openRewardListGui(Player promoter, UUID promotedPlayerUuid, int rewardPage, int promotedListPage) {
		String promotedPlayerName = dataManager.getPlayerName(promotedPlayerUuid);
		long promotedPlaytimeSeconds = playtimeManager.getPlaytimeSeconds(promotedPlayerUuid);

		List<RewardDefinition> rewards = new ArrayList<>(rewardManager.getRewards());

		List<Integer> contentSlots = guiConfigManager.getContentSlots();
		int itemsPerPage = contentSlots.size();
		int maxPage = getMaxPage(rewards.size(), itemsPerPage);
		rewardPage = clampPage(rewardPage, maxPage);
		int startIndex = rewardPage * itemsPerPage;
		int endIndex = Math.min(startIndex + itemsPerPage, rewards.size());

		ChadPromoterGuiHolder holder = new ChadPromoterGuiHolder(
				ChadPromoterGuiHolder.GuiType.REWARD_LIST,
				rewardPage,
				promotedListPage,
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

		for (int index = startIndex; index < endIndex; index++) {
			int slot = contentSlots.get(index - startIndex);
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
							"%current_playtime%", TimeUtil.formatSeconds(langManager, promotedPlaytimeSeconds),
							"%required_playtime%", TimeUtil.formatSeconds(langManager, reward.getRequiredPlaytimeSeconds())
					))
			);

			if (claimed) {
				lore.addAll(langManager.getRawMessageList("gui.reward-claimed-lore", Map.of()));
			} else if (unlocked) {
				lore.addAll(langManager.getRawMessageList("gui.reward-available-lore", Map.of()));
			} else {
				lore.addAll(langManager.getRawMessageList("gui.reward-locked-lore", Map.of(
						"%current_playtime%", TimeUtil.formatSeconds(langManager, promotedPlaytimeSeconds),
						"%required_playtime%", TimeUtil.formatSeconds(langManager, reward.getRequiredPlaytimeSeconds())
				)));
			}

			Material material = reward.getMaterial();

			if (claimed) {
				material = Material.BARRIER;
			} else if (!unlocked) {
				material = Material.GRAY_DYE;
			}

			inventory.setItem(slot, createItem(
					material,
					langManager.getRawMessage(reward.getDisplayNameLangKey()),
					lore
			));
		}

		if (rewardPage > 0) {
			inventory.setItem(guiConfigManager.getInt("reward-list.previous-page.slot", 18), createItem(
					Material.ARROW,
					langManager.getRawMessage("gui.reward-previous-page-name"),
					List.of()
			));
		}

		inventory.setItem(guiConfigManager.getInt("reward-list.back.slot", 22), createItem(
				Material.ARROW,
				langManager.getRawMessage("gui.reward-back-name"),
				List.of()
		));

		if (endIndex < rewards.size()) {
			inventory.setItem(guiConfigManager.getInt("reward-list.next-page.slot", 26), createItem(
					Material.ARROW,
					langManager.getRawMessage("gui.reward-next-page-name"),
					List.of()
			));
		}

		fillEmptySlots(inventory);
		promoter.openInventory(inventory);
	}

	public void openRewardListGuiByPromotedListSlot(Player promoter, int promotedListPage, int slot) {
		int contentSlotIndex = guiConfigManager.getContentSlotIndex(slot);

		if (contentSlotIndex == -1) {
			return;
		}

		List<Integer> contentSlots = guiConfigManager.getContentSlots();
		int itemsPerPage = contentSlots.size();

		List<UUID> promotedPlayerUuids = dataManager.getPromotedPlayerUuids(promoter.getUniqueId());
		int index = promotedListPage * itemsPerPage + contentSlotIndex;

		if (index < 0 || index >= promotedPlayerUuids.size()) {
			return;
		}

		UUID promotedPlayerUuid = promotedPlayerUuids.get(index);
		openRewardListGui(promoter, promotedPlayerUuid, 0, promotedListPage);
	}

	public void claimRewardFromGui(Player promoter, UUID promotedPlayerUuid, int rewardPage, int promotedListPage, int slot) {
		if (promotedPlayerUuid == null) {
			return;
		}

		int contentSlotIndex = guiConfigManager.getContentSlotIndex(slot);
		if (contentSlotIndex == -1) {
			return;
		}
		List<Integer> contentSlots = guiConfigManager.getContentSlots();
		int itemsPerPage = contentSlots.size();
		List<RewardDefinition> rewards = new ArrayList<>(rewardManager.getRewards());
		int rewardIndex = rewardPage * itemsPerPage + contentSlotIndex;

		if (rewardIndex < 0 || rewardIndex >= rewards.size()) {
			return;
		}

		RewardDefinition reward = rewards.get(rewardIndex);
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

		openRewardListGui(promoter, promotedPlayerUuid, rewardPage, promotedListPage);
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

	private ItemStack createPlayerHeadItem(UUID playerUuid, String name, List<String> lore) {
		ItemStack itemStack = new ItemStack(Material.PLAYER_HEAD);
		ItemMeta itemMeta = itemStack.getItemMeta();

		if (itemMeta instanceof SkullMeta skullMeta) {
			OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerUuid);
			skullMeta.setOwningPlayer(offlinePlayer);
			skullMeta.setDisplayName(name);
			skullMeta.setLore(lore);
			itemStack.setItemMeta(skullMeta);
		}

		return itemStack;
	}

	private void fillEmptySlots(Inventory inventory) {
		if (!guiConfigManager.getBoolean("common.fill-empty-slots", true)) {
			return;
		}

		Material fillerMaterial = guiConfigManager.getMaterial(
				"common.filler-material",
				Material.GRAY_STAINED_GLASS_PANE
		);

		ItemStack filler = createItem(
				fillerMaterial,
				langManager.getRawMessage("gui.filler-name"),
				List.of()
		);

		for (int slot = 0; slot < inventory.getSize(); slot++) {
			if (inventory.getItem(slot) == null) {
				inventory.setItem(slot, filler);
			}
		}
	}

	private int getMaxPage(int itemCount, int itemsPerPage) {
		if (itemCount <= 0 || itemsPerPage <= 0) {
			return 0;
		}
		return (itemCount - 1) / itemsPerPage;
	}

	private int clampPage(int page, int maxPage) {
		if (page < 0) {
			return 0;
		}

		return Math.min(page, maxPage);
	}
}