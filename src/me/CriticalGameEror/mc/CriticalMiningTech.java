package me.CriticalGameEror.mc;

import org.bukkit.plugin.java.JavaPlugin;

import me.CriticalGameEror.mc.Commands.AddBlock;
import me.CriticalGameEror.mc.filehandler.SpeedConfigFileHandler;

public class CriticalMiningTech extends JavaPlugin{
	
	public SpeedConfigFileHandler filehandler;
	
	public void onEnable() {
		filehandler = new SpeedConfigFileHandler(this);
		new PacketManager(this);
		new AddBlock(this);
	}
	
	public void onDisable() {
		//TODO
	}
	



}
