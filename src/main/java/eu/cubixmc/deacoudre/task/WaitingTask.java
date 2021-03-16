package eu.cubixmc.deacoudre.task;

import eu.cubixmc.deacoudre.Game;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import net.md_5.bungee.api.ChatColor;

public class WaitingTask extends BukkitRunnable{

	private int timeWaiting, minPlayer,time;
	private Game game;
	
	public WaitingTask(Game game, int timeWaiting, int minPlayer) {
		this.game = game;
		this.timeWaiting = timeWaiting;
		this.minPlayer = minPlayer;
		this.time = timeWaiting;
	}
	
	@Override
	public void run() {
		
		if(game.getState() == Game.State.WAITING) {
			if(Bukkit.getOnlinePlayers().size() >= minPlayer) {
				
				if(time <= 0) {
					game.setPlaying();
					this.cancel();
				}else {
					
					if(time <= 10)
						for(Player player: Bukkit.getOnlinePlayers())
							Game.sendTitle(player, ChatColor.RED+""+time, ChatColor.YELLOW+"Préparez vous");
				}
				
				time--;
				
			}else {
				time = timeWaiting;
			}
		}else 
			this.cancel();
		
	}

}
