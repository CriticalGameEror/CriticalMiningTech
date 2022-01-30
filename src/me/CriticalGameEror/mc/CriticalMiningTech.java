package me.CriticalGameEror.mc;

import org.bukkit.plugin.java.JavaPlugin;

public class CriticalMiningTech extends JavaPlugin{
	
	public void onEnable() {
		new PacketManager(this);
	}
	
	public void onDisable() {
		//TODO
	}

}
