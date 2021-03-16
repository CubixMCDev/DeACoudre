package eu.cubixmc.deacoudre;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;

import eu.cubixmc.deacoudre.task.EndingTask;
import eu.cubixmc.deacoudre.task.PlayingTask;
import eu.cubixmc.deacoudre.task.WaitingTask;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_8_R3.IChatBaseComponent;
import net.minecraft.server.v1_8_R3.IChatBaseComponent.ChatSerializer;
import net.minecraft.server.v1_8_R3.PacketPlayOutTitle.EnumTitleAction;
import net.minecraft.server.v1_8_R3.PacketPlayOutTitle;
import org.bukkit.util.Vector;

public class Game {

	public enum State{
		WAITING, PLAYING, ENDING;
	}
	
	private final DeACoudre main;
	private State state;
	
	private HashMap<Location, Material> blockList;
	
	private HashMap<String, Color> colorJumper;
	private ArrayList<Jumper> jumperList;
	private Jumper currentJumper;
	private int jumperPos;
	
	private HashMap<String, Color> colors;

	private PlayingTask playTask;

	//Config
	private String prefix, map;
	private Location jump;
	private Location watch;
	private Location waiting;
	private int maxPlayer;
	
	public Game(DeACoudre deACoudre) {
		main = deACoudre;
		
		blockList = new HashMap<>();
		
		//load list of colors
		colors = new HashMap<>();
		for(String c: main.getConfig().getConfigurationSection("rule.colors").getKeys(false)) {
			Color color = new Color(main.getConfig().getString("rule.colors."+c+".name"),
					main.getConfig().getString("rule.colors."+c+".type"),
					(byte)main.getConfig().getInt("rule.colors."+c+".data"));
			if(main.getConfig().contains("rule.colors."+c+".permission"))
				color.setPermission(main.getConfig().getString("rule.colors."+c+".permission"));
			if(main.getConfig().contains("rule.colors."+c+".sound"))
				color.setSound(Sound.valueOf(main.getConfig().getString("rule.colors."+c+".sound")));
			colors.put(color.getName(), color);
		}
		colorJumper = new HashMap<>();
		
		//List of jumper
		jumperList = new ArrayList<>();
		jumperPos = 0;
		
		//load Config
		prefix = ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("message.prefix"));
		jump = getLocation("location.jump");
		watch = getLocation("location.watching");
		map = ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("rule.arena-name"));
		maxPlayer = main.getConfig().getInt("rule.max-players");
		waiting = getLocation("location.waiting");
		main.getConfig().getString("rule.lobby-name");
		
		setWaiting();
	}
	
	public void setWaiting() {
		this.state = State.WAITING;
		
		
		
		for(Player player: Bukkit.getOnlinePlayers()) {
			player.teleport(waiting);
			setWaitingInventory(player);
		}
		
		int min = main.getConfig().getInt("rule.min-players");
		int time = main.getConfig().getInt("rule.starting-time");
		
		WaitingTask task = new WaitingTask(this, time, min);
		task.runTaskTimer(main, 20, 20);
	}
	
	public void setPlaying() {
		this.state = State.PLAYING;
		
		ArrayList<Color> color = new ArrayList<>(colors.values());
		for(Color c : colorJumper.values())
			color.remove(c);
		for(Color c : colors.values())
			if(c.hasPermission())
				color.remove(c);
		int life = main.getConfig().getInt("rule.start-life");
		for(Player player: Bukkit.getOnlinePlayers()) {
			if(!colorJumper.containsKey(player.getName())) {
				//Choose random color
				Random rand = new Random();
				Color mat = color.get(rand.nextInt(color.size()));
				color.remove(mat);
				
				//Add Player to game
				colorJumper.put(player.getName(), mat);
			}
			Jumper j = new Jumper(player, life);
			jumperList.add(j);
			
			setPlayingInventory(j);
			player.setGameMode(GameMode.SPECTATOR);
			player.getInventory().setHelmet(colorJumper.get(player.getName()).getBlock());
		}
		
		
		Collections.shuffle(jumperList);
		jump(jumperList.get(0));
		
		int tick = 5;
		playTask = new PlayingTask(this,tick,main.getConfig().getInt("rule.jump-time"));
		playTask.runTaskTimer(main, tick, tick);
		
	}

	
	public void setEnding() {
		this.state = State.ENDING;
		
		for(Player player: Bukkit.getOnlinePlayers())
			setEndingInventory(player);
		
		EndingTask task = new EndingTask(main,false);
		task.runTaskLater(main, 200);
		EndingTask task2 = new EndingTask(main,true);
		task2.runTaskLater(main, 210);
	}
	
	public State getState() {
		return state;
	}
	
	
	public Location getWaiting() {
		return waiting;
	}

	public Jumper getCurrentJumper() {
		return currentJumper;
	}
	
	
	
	public Location getWatch() {
		return watch;
	}

	public int getMaxPlayer() {
		return maxPlayer;
	}

	public Color getJumperColor(Jumper jumper) {
		return colorJumper.get(jumper.getPlayer().getName());
	}

	public void sendMessage(String message) {
		for(Player player: Bukkit.getOnlinePlayers())
			player.sendMessage(prefix+" "+message);
	}
	
	public void jump(Jumper jumper) {
		currentJumper = jumper;
		jumper.getPlayer().setFallDistance(0);
		Game.sendTitle(jumper.getPlayer(),ChatColor.RED+"Saute!");
		if(playTask != null)
			playTask.resetTimer();
		jumper.getPlayer().setGameMode(GameMode.ADVENTURE);
		jumper.getPlayer().teleport(jump);
		this.sendMessage(ChatColor.GRAY+jumper.getPlayer().getName()+" va maintenant sauter!");
	}
	
	@SuppressWarnings("deprecation")
	public void splash(Location loc) {
		boolean dac = false;
		while(new Location(loc.getWorld(),loc.getBlockX(),loc.getBlockY()+1,loc.getBlockZ()).getBlock().isLiquid())
			loc.add(0, 1, 0);
		
		blockList.put(loc, loc.getBlock().getType());
		loc.getBlock().setType(getJumperColor(getCurrentJumper()).getBlock().getType());
		loc.getBlock().setData(getJumperColor(getCurrentJumper()).getBlock().getData().getData());
		
		//Check if player has done a DAC
		if(!new Location(loc.getWorld(),loc.getBlockX()+1,loc.getBlockY(),loc.getBlockZ()).getBlock().isLiquid())
			if(!new Location(loc.getWorld(),loc.getBlockX()-1,loc.getBlockY(),loc.getBlockZ()).getBlock().isLiquid())
				if(!new Location(loc.getWorld(),loc.getBlockX(),loc.getBlockY(),loc.getBlockZ()+1).getBlock().isLiquid())
					if(!new Location(loc.getWorld(),loc.getBlockX(),loc.getBlockY(),loc.getBlockZ()-1).getBlock().isLiquid())
						dac = true;
		Random r = new Random();
		if(dac) {
			currentJumper.addLife();
			this.sendMessage(ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("message.success-dac")).replace("%player%", currentJumper.getPlayer().getName()));
		}else
			this.sendMessage(ChatColor.translateAlternateColorCodes('&',main.getConfig().getStringList("message.success-jump").get(r.nextInt(main.getConfig().getStringList("message.success-jump").size()))).replace("%player%", currentJumper.getPlayer().getName()));
		setPlayingInventory(currentJumper);

		Sound sound = Sound.SPLASH2;
		if(getJumperColor(currentJumper).hasSound())
			sound = getJumperColor(currentJumper).getSound();

		for(Player player: Bukkit.getOnlinePlayers())
			player.playSound(player.getLocation(), sound,100,1);

		currentJumper.getPlayer().setGameMode(GameMode.SPECTATOR);
		jumperPos++;
		if(jumperPos >= jumperList.size()) {
			jumperPos = 0;
			Collections.shuffle(jumperList);
		}
		
		jump(jumperList.get(jumperPos));
		
	}
	
	public void resetBlock() {
		for(Location loc: blockList.keySet())
			loc.getBlock().setType(blockList.get(loc));
	}
	
	public void fail() {
		currentJumper.fail();

		for(Player player: Bukkit.getOnlinePlayers())
			player.playSound(player.getLocation(), Sound.ANVIL_BREAK,100,1);

		if(!currentJumper.isDead()) {
			this.sendMessage(ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("message.fail-jump-not-die")).replace("%player%", currentJumper.getPlayer().getName()));
			setPlayingInventory(currentJumper);
			jump(currentJumper);
		}else {
			currentJumper.getPlayer().setGameMode(GameMode.SPECTATOR);
			jumperList.remove(currentJumper);
			Random r = new Random();
			this.sendMessage(ChatColor.translateAlternateColorCodes('&',main.getConfig().getStringList("message.fail-jump").get(r.nextInt(main.getConfig().getStringList("message.fail-jump").size()))).replace("%player%", currentJumper.getPlayer().getName()));
			getCurrentJumper().getPlayer().playSound(getCurrentJumper().getPlayer().getLocation(),Sound.ENDERDRAGON_GROWL,100,1);
			if(jumperList.size() == 1) {
				this.sendMessage(ChatColor.AQUA+jumperList.get(0).getPlayer().getName()+" est le grand vainqueur !");
				this.setEnding();
				return;
			}
			jumperPos++;
			if(jumperPos >= jumperList.size()) {
				jumperPos = 0;
				Collections.shuffle(jumperList);
				jump(jumperList.get(jumperPos));
			}
		}
	}
	
	public Location getLocation(String path) {
		Location loc = new Location(Bukkit.getWorld(main.getConfig().getString(path+".world")),
				main.getConfig().getDouble(path+".x"),
				main.getConfig().getDouble(path+".y"),
				main.getConfig().getDouble(path+".z"));
		
		if(main.getConfig().contains(path+".yaw"))
			loc.setYaw((float) main.getConfig().getDouble(path+".yaw"));
		
		if(main.getConfig().contains(path+".pitch"))
			loc.setPitch((float) main.getConfig().getDouble(path+".pitch"));
		
		return loc;
	}

	public Jumper getJumper(Player player){
		for(Jumper jumper: getJumperList())
			if(jumper.getPlayer().getName().equals(player.getName()))
				return jumper;

		return null;
	}
	
	public void selectColor(Player player, String color) {
		colorJumper.put(player.getName(), getColorList().get(color));
	}
	
	public void removePlayer(Player player) {
			
		
		colorJumper.remove(player.getName());
		if(this.getState() == State.PLAYING) {
			Jumper jump = null;
			for(Jumper jumper: jumperList)
				if(jumper.getPlayer().getName().equals(player.getName())) {
					jump = jumper;
					break;
				}
			
			jumperList.remove(jump);
			
			if(jumperList.size() == 1) {
				currentJumper = jumperList.get(0);
				this.sendMessage(ChatColor.AQUA+jumperList.get(0).getPlayer().getName()+" est le grand vainqueur !");
				this.setEnding();
				return;
			}
		}
		
		
		
	}
	
	public ArrayList<Jumper> getJumperList(){
		return jumperList;
	}
	
	public String getMapName() {
		return map;
	}
	
	public HashMap<String, Color> getColorList(){
		return colors;
	}
	
	public HashMap<String, Color> getJumperColorList(){
		return colorJumper;
	}
	
	public static void setWaitingInventory(Player player) {
		player.getInventory().clear();
		player.getInventory().setHelmet(null);
		player.setMaxHealth(20);
		player.setHealth(player.getMaxHealth());
		player.setFoodLevel(20);
		player.setGameMode(GameMode.ADVENTURE);
		
		
		//rules
		ItemStack rule = new ItemStack(Material.WRITTEN_BOOK);
		BookMeta ruleM = (BookMeta) rule.getItemMeta();
		ruleM.addPage("Il faut sauter dans l'eau. A chaque saut, un bloc est posé sous ses pieds de façon à combler le bloc d'eau sur lequel il est tombé. Au fur et à mesure que le jeu continue, les cases d'eau sont de moins en moins nombreuses. Il faut donc sauter avec...");
		ruleM.addPage("de plus en plus de précision pour éviter de tomber sur les blocs posés et ne pas mourir, afin de rester le dernier en vie.");
		ruleM.setDisplayName(ChatColor.GOLD+"Règlement");
		ruleM.setLore(Arrays.asList(ChatColor.YELLOW+"Clic droit"));
		ruleM.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		rule.setItemMeta(ruleM);
		player.getInventory().setItem(0, rule);
		
		//color
		ItemStack color = new ItemStack(Material.WOOL);
		ItemMeta colorM = color.getItemMeta();
		colorM.setDisplayName(ChatColor.GOLD+"Choissir une couleur");
		colorM.setLore(Arrays.asList(ChatColor.YELLOW+"Clic droit"));
		color.setItemMeta(colorM);
		player.getInventory().setItem(4, color);
		
		//Quit
		ItemStack quit = new ItemStack(Material.BED);
		ItemMeta quitM = quit.getItemMeta();
		quitM.setDisplayName(ChatColor.RED+"Retour au lobby");
		quitM.setLore(Arrays.asList(ChatColor.YELLOW+"Clic droit"));
		
		quit.setItemMeta(quitM);
		player.getInventory().setItem(8, quit);
		
	}
	
	
	
	public void setPlayingInventory(Jumper jumper) {
		jumper.getPlayer().getInventory().clear();
		jumper.getPlayer().setMaxHealth(jumper.getLife()*2);
		jumper.getPlayer().setHealth(jumper.getPlayer().getMaxHealth());

		ItemStack life = this.getJumperColor(jumper).getBlock().clone();
		life.setAmount(jumper.getLife());
		ItemMeta lifeM = life.getItemMeta();
		
		if(jumper.getLife() == 1)
			lifeM.setDisplayName(ChatColor.WHITE+"Il te reste "+ChatColor.RED+"1"+ChatColor.WHITE+" vie");
		else
			lifeM.setDisplayName(ChatColor.WHITE+"Il te reste "+ChatColor.RED+jumper.getLife()+ChatColor.WHITE+" vies");
		life.setItemMeta(lifeM);
		
		jumper.getPlayer().getInventory().setItem(4, life);
	}
	
	public void setEndingInventory(Player player) {
		player.getInventory().clear();
		player.getInventory().setHelmet(null);
		player.setLevel(0);
		player.setGameMode(GameMode.ADVENTURE);
		
		
		//Quit
		ItemStack quit = new ItemStack(Material.BED);
		ItemMeta quitM = quit.getItemMeta();
		quitM.setDisplayName(ChatColor.RED+"Retour au lobby");
		quitM.setLore(Arrays.asList(ChatColor.YELLOW+"Clic droit"));
		quit.setItemMeta(quitM);
		player.getInventory().setItem(8, quit);
		
	}
	
	public void movePlayer(Player player) {
		ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Connect");
        out.writeUTF("hub1");
        player.sendPluginMessage(main, "BungeeCord", out.toByteArray());
	}
	
	public static void sendTitle(Player player, String title) {
		IChatBaseComponent chatTitle = ChatSerializer.a("{\"text\":\""+title+"\"}");
		
		PacketPlayOutTitle t = new PacketPlayOutTitle(EnumTitleAction.TITLE, chatTitle);
		PacketPlayOutTitle length = new PacketPlayOutTitle(5,20,5);
		
		((CraftPlayer) player).getHandle().playerConnection.sendPacket(t);
		((CraftPlayer) player).getHandle().playerConnection.sendPacket(length);
	}
	
	public static void sendTitle(Player player, String title, String subtitle) {
		IChatBaseComponent chatTitle = ChatSerializer.a("{\"text\":\""+title+"\"}");
		IChatBaseComponent chatSubtitle = ChatSerializer.a("{\"text\":\""+subtitle+"\"}");
		
		PacketPlayOutTitle t = new PacketPlayOutTitle(EnumTitleAction.TITLE, chatTitle);
		PacketPlayOutTitle sub = new PacketPlayOutTitle(EnumTitleAction.SUBTITLE, chatSubtitle);
		PacketPlayOutTitle length = new PacketPlayOutTitle(0,25,5);
		
		((CraftPlayer) player).getHandle().playerConnection.sendPacket(t);
		((CraftPlayer) player).getHandle().playerConnection.sendPacket(sub);
		((CraftPlayer) player).getHandle().playerConnection.sendPacket(length);
	}
}