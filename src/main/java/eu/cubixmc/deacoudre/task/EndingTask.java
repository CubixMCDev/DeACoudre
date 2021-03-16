package eu.cubixmc.deacoudre.task;

import eu.cubixmc.deacoudre.DeACoudre;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import net.md_5.bungee.api.ChatColor;

public class EndingTask extends BukkitRunnable{

	private DeACoudre main;
	private boolean stop;
	
	public EndingTask(DeACoudre main, boolean stop) {
		this.main = main;
		this.stop = stop;
	}

	@Override
	public void run() {
		if(!stop) {
			for(Player p: Bukkit.getOnlinePlayers()) {
				try {
					main.getGame().movePlayer(p);
				}catch(Exception e) {
					p.kickPlayer(ChatColor.RED+"Partie termin�");
				}
			}
			main.getGame().resetBlock();
			
			stop = true;
		}else
			Bukkit.shutdown();
	}

}
