package eu.cubixmc.deacoudre;

import org.bukkit.entity.Player;

public class Jumper {
	
	private Player player;
	private int life;
	public Jumper(Player player) {
		this.player = player;
		life = 1;
	}
	
	public Jumper(Player player, int life) {
		this.player = player;
		this.life = life;
	}
	
	public void addLife() {
		life++;
	}
	
	public void fail() {
		life--;
	}
	
	
	public boolean isDead() {
		return life <= 0;
	}
	
	public int getLife() {
		return life;
	}
	
	public Player getPlayer() {
		return player;
	}
}
