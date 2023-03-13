package me.CriticalGameEror.mc.blockbreaking;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Set;

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
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;

import me.CriticalGameEror.mc.CriticalMiningTech;

public class PacketManager implements Listener{
	
	private CriticalMiningTech plugin;
	
	private static HashMap<String, PotionEffect> previousFatigueEffects = new HashMap<String, PotionEffect>();
	
	public static HashMap<String, Long> armSwinging = new HashMap<String, Long>();
	
	private ProtocolManager manager;
	
	private BlockDamage damage;
	
	public PacketManager(CriticalMiningTech plugin) {
		this.plugin = plugin;
		manager = ProtocolLibrary.getProtocolManager();
		Bukkit.getPluginManager().registerEvents(this, plugin);
		damage = new BlockDamage(plugin);
		receivedArmAnimation();
		checkArmAnimation();
		
	}
	
	private void receivedArmAnimation() {
		manager.addPacketListener(new PacketAdapter(plugin, ListenerPriority.NORMAL, PacketType.Play.Client.ARM_ANIMATION) {
		    @Override
		    public void onPacketReceiving(PacketEvent event) {
		        armSwinging.put(event.getPlayer().getName(), System.currentTimeMillis());
		    }
		});
	}
	
	@EventHandler
	private void blockStartBreaking(BlockDamageEvent event) {      
		if (event.getBlock().getType().isAir()) {
       		removeMiningFatigue(event.getPlayer());
       		return;
       	}
		
		BlockDamage.cancelTaskWithBlockReset(event.getPlayer());
		
		
		YamlConfiguration config = plugin.filehandler.getConfig();
		
		if (config.getConfigurationSection("Speeds") == null) {
			return;
		}
		
		if (config.getConfigurationSection("Speeds").contains(event.getBlock().getType().toString())) {	
			addMiningFatigue(event.getPlayer());
			damage.configureBreakingPacket(config.getConfigurationSection("Speeds").getDouble(event.getBlock().getType().toString()), event.getPlayer(), event.getBlock());
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
	
	// checks that an arm swing packet was delivered in the last tick (0.15 seconds)
	private void checkArmAnimation() {
		Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {

			@Override
			public void run() {
				Set<String> keySet = armSwinging.keySet();
				long currentTime = System.currentTimeMillis();
				for (String string : keySet) {
					if (armSwinging.get(string) + 150 < currentTime) {
						armSwinging.remove(string);
					}
				}
			}
			
		}, 1L, 1L);
	}
	
	

}
