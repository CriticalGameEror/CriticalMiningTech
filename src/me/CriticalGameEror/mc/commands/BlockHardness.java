package me.CriticalGameEror.mc.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import me.CriticalGameEror.mc.CriticalMiningTech;

public class BlockHardness implements TabExecutor{

	CriticalMiningTech plugin;
	
	public BlockHardness(CriticalMiningTech plugin) {
		plugin.getCommand("blockhardness").setExecutor(this);
		plugin.getCommand("blockhardness").setTabCompleter(this);
		this.plugin = plugin;
	}
	
	@Override
	public List<String> onTabComplete(CommandSender arg0, Command arg1, String arg2, String[] arg3) {
		if (arg3.length == 1) {
			List<String> temp = new ArrayList<String>(Arrays.asList(
					"set",
					"remove"));
			return temp;
		}
		return null;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("The console cannot send this command!");
			return true;
		}
		
		if (args.length == 0) {
			return false;
		}
		
		if (!(args[0].equals("set") || args[0].equals("remove"))) {
			return false;
		}
		if (args[0].equals("set") && args.length != 2) {
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
		
		if (args[0].equals("remove")) {
			if(plugin.filehandler.removeBlockFromConfig(block)) {
				player.sendMessage("Block removed successfully");
				return true;
			} else {
				player.sendMessage("There was an error adding this block");
				return false;
			}
		}
		
		double hardness;
		try {	
			hardness = Double.parseDouble(args[1]);
		} catch (NumberFormatException e) {
			sender.sendMessage("You need to input a number for hardness!");
			return true;
		}
		
		if (plugin.filehandler.addBlockToConfig(block, hardness)) {
			player.sendMessage("Block added successfully");
		} else {
			player.sendMessage("There was an error adding this block");
		}
		
		return true;
	}
}
