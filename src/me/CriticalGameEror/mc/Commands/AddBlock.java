package me.CriticalGameEror.mc.Commands;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import me.CriticalGameEror.mc.CriticalMiningTech;

public class AddBlock implements TabExecutor{

	CriticalMiningTech plugin;
	
	public AddBlock(CriticalMiningTech plugin) {
		plugin.getCommand("addblockhardness").setExecutor(this);
		this.plugin = plugin;
	}
	
	@Override
	public List<String> onTabComplete(CommandSender arg0, Command arg1, String arg2, String[] arg3) {
		return null;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("The console cannot send this command!");
			return true;
		}
		
		if (args.length != 1) {
			return false;
		}
		
		Player player = (Player) sender;
		
		Block block = player.getTargetBlockExact(5);
		
		if (block == null) {
			player.sendMessage("You cannot target air!");
			return true;
		}
		
		else if (block.getType().isAir()) {
			player.sendMessage("You cannot target air!");
			return true;
		}
		
		File blockSaveFile = new File(plugin.getDataFolder().toString(), "SpeedConfig.yml");
		YamlConfiguration config = new YamlConfiguration();
		
		if (!blockSaveFile.exists()) {
			blockSaveFile.getParentFile().mkdirs();
			try {
				blockSaveFile.createNewFile();
			} catch (IOException e) {
				player.sendMessage("An error creating the file occured!");
				return true;
			}
		}
		
		try {
			config.load(blockSaveFile);
		} catch (IOException | InvalidConfigurationException e1) {
			player.sendMessage("An error occured when trying to load the file");
			return true;
		}
		
		if (config.getConfigurationSection("Speeds") == null) {
			config.createSection("Speeds");
		}
	
		
		config.set("Speeds." + block.getType().toString(), args[0]);
		
		try {
			config.save(blockSaveFile);
		} catch (IOException e) {
			player.sendMessage("An error occured while saving the file!");
			return true;
		}
		
		player.sendMessage("Block successfully saved to config!");
		return true;
			
			
	}
}
