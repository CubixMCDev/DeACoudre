package eu.cubixmc.deacoudre;

import java.util.ArrayList;

import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.md_5.bungee.api.ChatColor;


public class PlayerListenner implements Listener {

	private DeACoudre main;
	
	public PlayerListenner(DeACoudre deACoudre) {
		this.main = deACoudre;
	}
	
	@EventHandler
	public void damage(EntityDamageEvent event) {
		if(!(event.getEntity() instanceof Player)) return;
		Player player = (Player) event.getEntity();
		
		if(main.getGame().getState() == Game.State.PLAYING)
			if(event.getCause() == DamageCause.FALL)
				if(main.getGame().getCurrentJumper().getPlayer().getName().equals(player.getName()))
					main.getGame().fail();
		
		event.setCancelled(true);
	}
	
	@EventHandler
	public void food(FoodLevelChangeEvent event) {
		if(!(event.getEntity() instanceof Player)) return;
		Player player = (Player) event.getEntity();
		event.setCancelled(true);
		player.setFoodLevel(20);
	}
	
	@EventHandler
	public void drop(PlayerDropItemEvent event) {
		event.setCancelled(true);
	}
	
	@EventHandler
	public void quit(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		main.getScoreboardManager().onLogout(player);
		if(main.getGame().getState() != Game.State.ENDING)
			event.setQuitMessage(ChatColor.RED+event.getPlayer().getName()+" a quitté la partie.");
		else
			event.setQuitMessage(null);
		main.getGame().removePlayer(event.getPlayer());
		
	}
	
	@EventHandler
	public void join(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		main.getScoreboardManager().onLogin(player);
		if(main.getGame().getState() == Game.State.WAITING) {
			event.setJoinMessage(ChatColor.YELLOW+event.getPlayer().getName()+" a rejoint la partie ("+Bukkit.getOnlinePlayers().size()+"/"+main.getGame().getMaxPlayer()+")");
			Game.setWaitingInventory(event.getPlayer());
			event.getPlayer().teleport(main.getGame().getWaiting());
		}else {
			event.setJoinMessage(ChatColor.YELLOW+event.getPlayer().getName()+" a rejoint la partie en tant que spectateur !");
			main.getGame().setEndingInventory(event.getPlayer());
			event.getPlayer().teleport(main.getGame().getWatch());
		}
	}
	
	@EventHandler
	public void interact(PlayerInteractEvent event) {
		if(event.getItem() == null) return;
		if(event.getAction() == Action.RIGHT_CLICK_AIR)
			if(event.hasItem())
				if(event.getItem().hasItemMeta())
					if(event.getItem().getItemMeta().hasDisplayName()) {
						if(event.getItem().getItemMeta().getDisplayName().equals(ChatColor.GOLD+"Choissir une couleur")) {
							int size = main.getGame().getColorList().size()/9;
							if(main.getGame().getColorList().size()%9!=0)
								size++;
							size++;
							ArrayList<String> colorVIP = new ArrayList<>();
							Inventory select = Bukkit.createInventory(null, size*9,ChatColor.AQUA+"Choissir une couleur");
							int slot = 0;
							for(String colorName: main.getGame().getColorList().keySet()) {
								Color c = main.getGame().getColorList().get(colorName);
								if(!c.hasPermission()) {
									ItemStack color = main.getGame().getColorList().get(colorName).getBlock().clone();
									ItemMeta colorM = color.getItemMeta();
									colorM.setDisplayName(colorName);
										
									if(main.getGame().getJumperColorList().containsValue(main.getGame().getColorList().get(colorName)))
										colorM.addEnchant(Enchantment.DURABILITY, 1, false);
									colorM.addItemFlags(ItemFlag.HIDE_ENCHANTS);
									color.setItemMeta(colorM);
									select.setItem(slot, color);
									slot++;
								}else {
									colorVIP.add(colorName);
								}
								
							}
							
							while(slot%9!=0)
								slot++;
							
							for(String colorName: colorVIP) {
								Color c = main.getGame().getColorList().get(colorName);
								ItemStack color = main.getGame().getColorList().get(colorName).getBlock().clone();
								ItemMeta colorM = color.getItemMeta();
								colorM.setDisplayName(colorName);
								
								if(!event.getPlayer().hasPermission(c.getPermission()))
									colorM.setLore(Arrays.asList(ChatColor.RED+"Vous n'avez pas la permission de choissir cette couleur."));
								if(main.getGame().getJumperColorList().containsValue(main.getGame().getColorList().get(colorName)))
									colorM.addEnchant(Enchantment.DURABILITY, 1, false);
								colorM.addItemFlags(ItemFlag.HIDE_ENCHANTS);
								color.setItemMeta(colorM);
								select.setItem(slot, color);
								slot++;
							}
							
							event.getPlayer().openInventory(select);
						}
						
						if(event.getItem().getItemMeta().getDisplayName().equals(ChatColor.RED+"Retour au lobby")) {
							event.setCancelled(true);
							main.getGame().movePlayer(event.getPlayer());
							return;
						}
					}
	
		if(event.getItem().getType() != Material.WRITTEN_BOOK)
			event.setCancelled(true);
	}
	
	@EventHandler
	public void inventory(InventoryClickEvent event) {
		event.setCancelled(true);
		if(event.getClickedInventory() == null) return;
		if(event.getCurrentItem() == null) return;
		if(event.getWhoClicked() == null) return;
		if(!(event.getWhoClicked() instanceof Player)) return;
		if(event.getClickedInventory().getName() == null) return;
		if(event.getClickedInventory().getName().equals(ChatColor.AQUA+"Choissir une couleur")) {
			if(event.getCurrentItem().hasItemMeta())
				if(event.getCurrentItem().getItemMeta().hasDisplayName())
					if(main.getGame().getColorList().containsKey(event.getCurrentItem().getItemMeta().getDisplayName())) {
						if(main.getGame().getJumperColorList().containsValue(main.getGame().getColorList().get(event.getCurrentItem().getItemMeta().getDisplayName()))) {
							event.getWhoClicked().sendMessage(ChatColor.RED+"Un joueur a déjà selectionner cette couleur.");
							event.getWhoClicked().closeInventory();
						}else {
							if(main.getGame().getColorList().get(event.getCurrentItem().getItemMeta().getDisplayName()).hasPermission())
								if(!event.getWhoClicked().hasPermission(main.getGame().getColorList().get(event.getCurrentItem().getItemMeta().getDisplayName()).getPermission())) {
									event.getWhoClicked().closeInventory();
									event.getWhoClicked().sendMessage(ChatColor.RED+"Vous n'avez pas la permission pour sélectionner cette couleur.");
									return;
								}
							main.getGame().selectColor((Player)event.getWhoClicked(), event.getCurrentItem().getItemMeta().getDisplayName());
							event.getWhoClicked().closeInventory();
							event.getWhoClicked().sendMessage(ChatColor.AQUA+"Vous avez sélectionner la couleur "+event.getCurrentItem().getItemMeta().getDisplayName());
						}
							
					}
			
		}
	}
	

}
