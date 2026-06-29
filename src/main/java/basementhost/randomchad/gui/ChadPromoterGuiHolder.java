package basementhost.randomchad.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.UUID;

public class ChadPromoterGuiHolder implements InventoryHolder {

	private final GuiType guiType;
	private final int page;
	private final UUID promotedPlayerUuid;
	private Inventory inventory;

	public ChadPromoterGuiHolder(GuiType guiType, int page) {
		this(guiType, page, null);
	}

	public ChadPromoterGuiHolder(GuiType guiType, int page, UUID promotedPlayerUuid) {
		this.guiType = guiType;
		this.page = page;
		this.promotedPlayerUuid = promotedPlayerUuid;
	}

	public GuiType getGuiType() {
		return guiType;
	}

	public int getPage() {
		return page;
	}

	public UUID getPromotedPlayerUuid() {
		return promotedPlayerUuid;
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
		PROMOTED_LIST,
		REWARD_LIST
	}
}