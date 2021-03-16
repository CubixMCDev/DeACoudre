package eu.cubixmc.deacoudre;

import eu.cubixmc.deacoudre.cmd.StartCmd;
import eu.cubixmc.deacoudre.scoreboard.ScoreboardManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.entity.Player;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class DeACoudre extends JavaPlugin{
	
	private Game game;
	private ScoreboardManager scoreboardManager;

	private ScheduledExecutorService executorMonoThread;
	private ScheduledExecutorService scheduledExecutorService;
	
	@Override
	public void onEnable() {
		scheduledExecutorService = Executors.newScheduledThreadPool(16);
		executorMonoThread = Executors.newScheduledThreadPool(1);
		scoreboardManager = new ScoreboardManager(this);

		saveDefaultConfig();
		game = new Game(this);
		getServer().getPluginManager().registerEvents(new PlayerListenner(this), this);
		this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
		
		getCommand("start").setExecutor(new StartCmd(this));

		for(Player player: Bukkit.getOnlinePlayers())
			getScoreboardManager().onLogin(player);
	}
	
	@Override
	public void onDisable() {
		game.resetBlock();
		getScoreboardManager().onDisable();
	}

	public ScoreboardManager getScoreboardManager() {
		return scoreboardManager;
	}

	public ScheduledExecutorService getExecutorMonoThread() {
		return executorMonoThread;
	}

	public ScheduledExecutorService getScheduledExecutorService() {
		return scheduledExecutorService;
	}

	public Game getGame() {
		return game;
	}
	
	
}
