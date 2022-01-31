package me.CriticalGameEror.mc;

import org.bukkit.plugin.java.JavaPlugin;

import me.CriticalGameEror.mc.Commands.AddBlock;

public class CriticalMiningTech extends JavaPlugin{
	
	public void onEnable() {
		new PacketManager(this);
		new AddBlock(this);
	}
	
	public void onDisable() {
		//TODO
	}

}
