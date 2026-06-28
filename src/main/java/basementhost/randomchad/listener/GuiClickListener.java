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
		}
	}

	private void handleMainGuiClick(Player player, int slot) {
		if (slot == 13) {
			guiManager.openPromotedPlayersGui(player, 0);
		}
	}

	private void handlePromotedListClick(Player player, int slot, int page) {
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
}