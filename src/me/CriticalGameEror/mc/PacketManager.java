package me.CriticalGameEror.mc;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionEffect;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;

public class PacketManager implements Listener{
	
	private CriticalMiningTech plugin;
	
	private static HashMap<String, PotionEffect> previousFatigueEffects = new HashMap<String, PotionEffect>();
	
	private ProtocolManager manager;
	
	private BlockDamage damage;
	
	public PacketManager(CriticalMiningTech plugin) {
		this.plugin = plugin;
		manager = ProtocolLibrary.getProtocolManager();
		Bukkit.getPluginManager().registerEvents(this, plugin);
		damage = new BlockDamage(plugin);
		
	}
	
	@EventHandler
	private void StopBreakingBlocks(BlockDamageEvent event) {      
		if (event.getBlock().getType().isAir()) {
       		removeMiningFatigue(event.getPlayer());
       		return;
       	}
		
		
		YamlConfiguration config = plugin.filehandler.getConfig();
		
		if (config.getConfigurationSection("Speeds") == null) {
			return;
		}
		
		if (config.getConfigurationSection("Speeds").contains(event.getBlock().getType().toString())) {		
			addMiningFatigue(event.getPlayer());
			damage.startBreaking(config.getConfigurationSection("Speeds").getInt(event.getBlock().getType().toString()), event.getPlayer(), event.getBlock());
		} else {
			removeMiningFatigue(event.getPlayer());
		}
        
	}
	
	private void addMiningFatigue(Player player) {
		
		// removes existing fatigue effects and stores them
		if (player.hasPotionEffect(PotionEffectType.SLOW_DIGGING)) {
			previousFatigueEffects.put(player.getName(), player.getPotionEffect(PotionEffectType.SLOW_DIGGING));
			player.removePotionEffect(PotionEffectType.SLOW_DIGGING);
		}
		
		PacketContainer effectAdd = manager.createPacket(PacketType.Play.Server.ENTITY_EFFECT);
        
		effectAdd.getIntegers().write(0, player.getEntityId());
		effectAdd.getBytes().write(0, (byte) (4 & 255));
		effectAdd.getBytes().write(1, (byte) (255 & 255));
		effectAdd.getIntegers().write(1, 1);
		effectAdd.getBytes().write(2, (byte) (1));
        
        try {
			manager.sendServerPacket(player, effectAdd);
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
        
	}
	
	
	private void removeMiningFatigue(Player player) {
		
		if (player.hasPotionEffect(PotionEffectType.SLOW_DIGGING)) {
			previousFatigueEffects.put(player.getName(), player.getPotionEffect(PotionEffectType.SLOW_DIGGING));
			player.removePotionEffect(PotionEffectType.SLOW_DIGGING);
		}
		
		PacketContainer effectRemove = manager.createPacket(PacketType.Play.Server.REMOVE_ENTITY_EFFECT);
        
		effectRemove.getIntegers().write(0, player.getEntityId());
		effectRemove.getEffectTypes().write(0, PotionEffectType.SLOW_DIGGING);
        
        try {
			manager.sendServerPacket(player, effectRemove);
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
        
        // adds back the previous fatigue effect
		if (previousFatigueEffects.containsKey(player.getName())) {
			player.addPotionEffect(previousFatigueEffects.get(player.getName()));
			previousFatigueEffects.remove(player.getName());
		}
	}
	
	

}
