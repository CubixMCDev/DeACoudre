package eu.cubixmc.deacoudre.task;

import eu.cubixmc.deacoudre.Game;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayingTask extends BukkitRunnable{
 
	private Game game;
	private double timer, step;
	private int maxTime;
	
	public PlayingTask(Game game, int tick, int maxTime) {
		this.game = game;
		timer = 0;
		step = Double.parseDouble(tick+"")/20;
		System.out.println("step: "+step);
		this.maxTime = maxTime;
	}
	
	@Override
	public void run() {
		if(game.getState() != Game.State.PLAYING) {
			this.cancel();
			return;
		}
		Location loc = game.getCurrentJumper().getPlayer().getLocation().getBlock().getLocation();
		if(loc.getBlock().isLiquid()) {
			
			game.splash(loc);
		}

		int timeRestant = (int) (maxTime-timer);
		if(timer >= maxTime)
			game.fail();
		else if((maxTime-timer) <= 3)
			if(timer%1==0)
				Game.sendTitle(game.getCurrentJumper().getPlayer(), ChatColor.RED+""+timeRestant);
		
		timer+=step;
	}
	
	public void resetTimer() {
		timer = 0;
	}

	public double getTime(){
		return timer;
	}

}
