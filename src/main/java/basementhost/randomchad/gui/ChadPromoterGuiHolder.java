package basementhost.randomchad.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class ChadPromoterGuiHolder implements InventoryHolder {

	private final GuiType guiType;
	private final int page;
	private Inventory inventory;

	public ChadPromoterGuiHolder(GuiType guiType, int page) {
		this.guiType = guiType;
		this.page = page;
	}

	public GuiType getGuiType() {
		return guiType;
	}

	public int getPage() {
		return page;
	}

	public void setInventory(Inventory inventory) {
		this.inventory = inventory;
	}

	@Override
	public Inventory getInventory() {
		return inventory;
	}

	public enum GuiType {
		MAIN,
		PROMOTED_LIST
	}
}