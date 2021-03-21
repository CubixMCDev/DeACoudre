package eu.cubixmc.deacoudre.task;

import eu.cubixmc.deacoudre.DeACoudre;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import net.md_5.bungee.api.ChatColor;

public class EndingTask extends BukkitRunnable{

	private DeACoudre main;
	private boolean stop;
	private int time;
	
	public EndingTask(DeACoudre main, boolean stop, int time) {
		this.main = main;
		this.stop = stop;
		this.time = time;
	}

	@Override
	public void run() {
		if(time > 0)
			main.getGame().sendMessage(org.bukkit.ChatColor.GRAY+"Le serveur ferme dans "+time+" secondes");
		else if(time == 0) {
			for (Player p : Bukkit.getOnlinePlayers()) {
					try {
						main.getGame().movePlayer(p);
					} catch (Exception e) {
						p.kickPlayer(ChatColor.RED + "Partie terminé");
					}
				}
				main.getGame().resetBlock();

				stop = true;
		}else if(time < 0)
			Bukkit.shutdown();

		time--;
	}

}
