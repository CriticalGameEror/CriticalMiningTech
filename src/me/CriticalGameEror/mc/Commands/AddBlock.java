package me.CriticalGameEror.mc.Commands;

import java.util.List;

import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
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
		
		double hardness;
		try {	
			hardness = Double.parseDouble(args[0]);
		} catch (NumberFormatException e) {
			sender.sendMessage("You need to input a number for hardness!");
			return true;
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
		
		if (plugin.filehandler.addBlockToConfig(block, hardness)) {
			player.sendMessage("Block added successfully");
		} else {
			player.sendMessage("There was an error adding this block");
		}
		
		return true;
	}
}
