package basementhost.randomchad.manager;

import basementhost.randomchad.gui.ChadPromoterGuiHolder;
import basementhost.randomchad.lang.LangManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class GuiManager {

	private final DataManager dataManager;
	private final PromoteManager promoteManager;
	private final LangManager langManager;

	public GuiManager(DataManager dataManager, PromoteManager promoteManager, LangManager langManager) {
		this.dataManager = dataManager;
		this.promoteManager = promoteManager;
		this.langManager = langManager;
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
}