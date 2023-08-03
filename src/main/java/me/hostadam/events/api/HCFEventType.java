package me.hostadam.events.api;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.ChatColor;

@Getter
@AllArgsConstructor
public enum HCFEventType {

	KOTH("KOTH", ChatColor.BLUE, "§7[§9§lKing of the Hill§7]", "§7[§9§lKOTH§7]"),
	DTC("DTC", ChatColor.DARK_GREEN, "§7[§2§lDestroy the Core§7]", "§7[§2§lDTC§7]");

	private String name;
	private ChatColor defaultColor;
	private String prefix, smallPrefix;
}