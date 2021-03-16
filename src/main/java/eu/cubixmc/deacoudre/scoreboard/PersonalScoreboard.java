package eu.cubixmc.deacoudre.scoreboard;

import eu.cubixmc.deacoudre.DeACoudre;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class PersonalScoreboard {
    private final Player player;
    private DeACoudre main;
    private final UUID uuid;
    private final ObjectiveSign objectiveSign;
    final Date date = new Date();
    private String currentDate = new SimpleDateFormat("dd-MM-yyyy").format(date).replace("-", "/");

    PersonalScoreboard(DeACoudre main, Player player){
        this.main = main;
        this.player = player;
        uuid = player.getUniqueId();
        objectiveSign = new ObjectiveSign("sidebar", "DeACoudre");

        reloadData();
        objectiveSign.addReceiver(player);
    }

    public void reloadData(){}

    public void setLines(String ip){
        objectiveSign.setDisplayName(ChatColor.GRAY+"- "+ChatColor.GOLD+"Dé à Coudre"+ChatColor.DARK_GRAY+" -");

        objectiveSign.setLine(0, "§8» §7" + currentDate);
        objectiveSign.setLine(1, "§1");
        objectiveSign.setLine(2, "§8» §7Joueurs: §e"+Bukkit.getOnlinePlayers().size() + "§6/§e"+Bukkit.getMaxPlayers());
        objectiveSign.setLine(3, "§8» §7Attente de joueurs...");
        objectiveSign.setLine(4, "§2");
        objectiveSign.setLine(5, "§8» " + ip);

        objectiveSign.updateLines();
    }

    public void onLogout(){
        objectiveSign.removeReceiver(Bukkit.getServer().getOfflinePlayer(uuid));
    }
}