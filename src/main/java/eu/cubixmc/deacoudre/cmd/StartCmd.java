package eu.cubixmc.deacoudre.cmd;

import eu.cubixmc.deacoudre.DeACoudre;
import eu.cubixmc.deacoudre.Game;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import net.md_5.bungee.api.ChatColor;

public class StartCmd implements CommandExecutor {

	private DeACoudre main;
	
	public StartCmd(DeACoudre deACoudre) {
		this.main = deACoudre;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String msg, String[] args) {
		if(main.getGame().getState() == Game.State.WAITING) {
			main.getGame().setPlaying();
			sender.sendMessage(ChatColor.GREEN+"La partie est commencé");
		}else
			sender.sendMessage(ChatColor.RED+"La partie est déjà commencé!");
		return true;
	}

}
