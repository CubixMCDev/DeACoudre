package eu.cubixmc.deacoudre;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.inventory.ItemStack;

public class Color {
	private String name, permission;
	private ItemStack block;
	private Sound sound;
	
	public Color(String name, String item, byte data) {
		this.block = new ItemStack(Material.valueOf(item),1,data);
		this.name = ChatColor.translateAlternateColorCodes('&', name);
	}
	
	public Color(String name, String item, byte data, String permission) {
		this.block = new ItemStack(Material.valueOf(item),1,data);
		this.name = ChatColor.translateAlternateColorCodes('&', name);
		this.permission = permission;
	}
	
	public boolean hasPermission() {
		return permission != null;
	}

	public String getName() {
		return name;
	}

	public String getPermission() {
		return permission;
	}

	public ItemStack getBlock() {
		return block;
	}

	public void setName(String name) {
		this.name = ChatColor.translateAlternateColorCodes('&', name);
	}

	public void setPermission(String permission) {
		this.permission = permission;
	}

	public void setBlock(ItemStack block) {
		this.block = block;
	}

	public void setSound(Sound sound){
		this.sound = sound;
	}

	public Sound getSound(){
		return sound;
	}

	public boolean hasSound(){
		return sound != null;
	}
	
	
}