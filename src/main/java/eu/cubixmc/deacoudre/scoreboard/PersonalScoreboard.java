package eu.cubixmc.deacoudre.scoreboard;

import eu.cubixmc.deacoudre.DeACoudre;
import eu.cubixmc.deacoudre.Game;
import eu.cubixmc.deacoudre.Jumper;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class PersonalScoreboard {
    private final Player player;
    private Jumper jumper;
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

        if(main.getGame().getState() == Game.State.WAITING) {
            objectiveSign.setLine(1, "§1");
            objectiveSign.setLine(2, "§8» §7Joueurs: §e" + Bukkit.getOnlinePlayers().size() + "§6/§e" + main.getGame().getMaxPlayer());
            objectiveSign.setLine(3, "§8» §7Attente de joueurs...");
            objectiveSign.setLine(4, "§2");
            objectiveSign.setLine(5, "§8» " + ip);
        }else if(main.getGame().getState() == Game.State.PLAYING){
            if(jumper == null)
                jumper = main.getGame().getJumper(player);


            objectiveSign.setLine(1, "§1");
            objectiveSign.setLine(2, "§8» §7Joueurs: §e" + Bukkit.getOnlinePlayers().size() + "§6/§e" + main.getGame().getJumperList().size());
            objectiveSign.setLine(3, "§8» §7Joueur qui saute: §e" + main.getGame().getCurrentJumper().getPlayer().getName());
            if(jumper != null) {
                objectiveSign.setLine(4, "§c");
                objectiveSign.setLine(5, "§8» §7Vies: §e" + jumper.getLife());
            }
            objectiveSign.setLine(6, "§2");
            objectiveSign.setLine(7, "§8» " + ip);
        }else if(main.getGame().getState() == Game.State.ENDING){
            objectiveSign.setLine(1, "§1");
            objectiveSign.setLine(2, "§8» §7Gagnant: §e" + main.getGame().getJumperList().get(0).getPlayer().getName());
            objectiveSign.setLine(3, "§8» §7Partie terminé...");
            objectiveSign.setLine(4, "§2");
            objectiveSign.setLine(5, "§8» " + ip);
        }

        objectiveSign.updateLines();
    }

    public void onLogout(){
        objectiveSign.removeReceiver(Bukkit.getServer().getOfflinePlayer(uuid));
    }
}