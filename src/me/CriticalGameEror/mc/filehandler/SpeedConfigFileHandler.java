package me.CriticalGameEror.mc.filehandler;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import me.CriticalGameEror.mc.CriticalMiningTech;

public class SpeedConfigFileHandler {
	
	private File blockSaveFile;
	private YamlConfiguration config = new YamlConfiguration();
	private CriticalMiningTech plugin;
	
	public SpeedConfigFileHandler(CriticalMiningTech plugin) {
		this.plugin = plugin;
		initialiseConfig();
	}
	
	private void initialiseConfig() {
		blockSaveFile = new File(plugin.getDataFolder().toString(), "SpeedConfig.yml");
		
		if (!blockSaveFile.exists()) {
			blockSaveFile.getParentFile().mkdirs();
			try {
				blockSaveFile.createNewFile();
			} catch (IOException e) {
				Bukkit.getLogger().log(Level.SEVERE, "[CriticalMiningTech] An error occured while creating SpeedConfig.yml!");
				return;
			}
		}
		
		try {
			config.load(blockSaveFile);
		} catch (IOException | InvalidConfigurationException e1) {
			Bukkit.getLogger().log(Level.SEVERE, "[CriticalMiningTech] An error occured while loading SpeedConfig.yml!");
			return;
		}
		
		System.out.println("[CriticalMiningTech] SpeedConfig.yml loaded successfully");
	}
	
	
	public YamlConfiguration getBlockSaveFile() {
		return this.config;
	}
	
	public boolean addBlockToConfig(Block block, double amount) {
		if (config.getConfigurationSection("Speeds") == null) {
			config.createSection("Speeds");
		}

		
		config.set("Speeds." + block.getType().toString(), amount);
		
		try {
			config.save(blockSaveFile);
		} catch (IOException e) {
			Bukkit.getLogger().log(Level.SEVERE, "[CriticalMiningTech] An error occured while saving SpeedConfig.yml!");
			return false;
		}
		
		System.out.println("[CriticalMiningTech] SpeedConfig.yml saved successfully");
		return true;
	}
	
	public boolean removeBlockFromConfig(Block block) {
		if (config.getConfigurationSection("Speeds") == null) {
			return true;
		}
		
		config.set("Speeds." + block.getType().toString(), null);
		
		try {
			config.save(blockSaveFile);
		} catch (IOException e) {
			Bukkit.getLogger().log(Level.SEVERE, "[CriticalMiningTech] An error occured while saving SpeedConfig.yml!");
			return false;
		}
		
		System.out.println("[CriticalMiningTech] SpeedConfig.yml saved successfully");
		return true;
		
	}
	
	public YamlConfiguration getConfig() {
		return this.config;
	}
	


}
