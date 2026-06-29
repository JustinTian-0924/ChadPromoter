package basementhost.randomchad.listener;

import basementhost.randomchad.gui.ChadPromoterGuiHolder;
import basementhost.randomchad.manager.GuiManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class GuiClickListener implements Listener {

	private final GuiManager guiManager;

	public GuiClickListener(GuiManager guiManager) {
		this.guiManager = guiManager;
	}

	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		if (!(event.getWhoClicked() instanceof Player player)) {
			return;
		}

		if (!(event.getInventory().getHolder() instanceof ChadPromoterGuiHolder holder)) {
			return;
		}

		event.setCancelled(true);

		if (event.getClickedInventory() == null || !event.getClickedInventory().equals(event.getInventory())) {
			return;
		}

		if (holder.getGuiType() == ChadPromoterGuiHolder.GuiType.MAIN) {
			handleMainGuiClick(player, event.getSlot());
			return;
		}

		if (holder.getGuiType() == ChadPromoterGuiHolder.GuiType.PROMOTED_LIST) {
			handlePromotedListClick(player, event.getSlot(), holder.getPage());
			return;
		}

		if (holder.getGuiType() == ChadPromoterGuiHolder.GuiType.REWARD_LIST) {
			handleRewardListClick(player, event.getSlot(), holder);
		}
	}

	private void handleMainGuiClick(Player player, int slot) {
		if (slot == 13) {
			guiManager.openPromotedPlayersGui(player, 0);
		}
	}

	private void handlePromotedListClick(Player player, int slot, int page) {
		if (slot >= 0 && slot < 21) {
			guiManager.openRewardListGuiByPromotedListSlot(player, page, slot);
			return;
		}

		if (slot == 18 && page > 0) {
			guiManager.openPromotedPlayersGui(player, page - 1);
			return;
		}

		if (slot == 22) {
			guiManager.openMainGui(player);
			return;
		}

		if (slot == 26) {
			guiManager.openPromotedPlayersGui(player, page + 1);
		}
	}

	private void handleRewardListClick(Player player, int slot, ChadPromoterGuiHolder holder) {
		if (slot >= 0 && slot < 21) {
			guiManager.claimRewardFromGui(
					player,
					holder.getPromotedPlayerUuid(),
					holder.getPage(),
					holder.getParentPage(),
					slot
			);
			return;
		}

		if (slot == 18 && holder.getPage() > 0) {
			guiManager.openRewardListGui(
					player,
					holder.getPromotedPlayerUuid(),
					holder.getPage() - 1,
					holder.getParentPage()
			);
			return;
		}

		if (slot == 22) {
			guiManager.openPromotedPlayersGui(player, holder.getParentPage());
			return;
		}

		if (slot == 26) {
			guiManager.openRewardListGui(
					player,
					holder.getPromotedPlayerUuid(),
					holder.getPage() + 1,
					holder.getParentPage()
			);
		}
	}
}