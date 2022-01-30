package me.CriticalGameEror.mc;

import java.lang.reflect.InvocationTargetException;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.potion.PotionEffectType;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;

public class PacketManager implements Listener{
	
	private CriticalMiningTech plugin;
	
	private ProtocolManager manager;
	
	public PacketManager(CriticalMiningTech plugin) {
		this.plugin = plugin;
		manager = ProtocolLibrary.getProtocolManager();
		Bukkit.getPluginManager().registerEvents(this, plugin);
		//StopBreakingBlocks();

	}
	
//	private void StopBreakingBlocks(ProtocolManager manager) {
//		manager.addPacketListener(new PacketAdapter(plugin, 
//				ListenerPriority.HIGHEST, 
//				PacketType.Play.Client.BLOCK_DIG) {
//			@Override
//			public void onPacketReceiving(PacketEvent event) {
//				event.getPlayer().sendMessage("A packet was cancelled!");
//				event.setCancelled(true);
//			}
//			
//		});
//	}
	
	@EventHandler
	private void StopBreakingBlocks(BlockDamageEvent event) {      
       	if (event.getBlock().getType().equals(Material.OAK_DOOR)) {
			PacketContainer effectAdd = manager.createPacket(PacketType.Play.Server.ENTITY_EFFECT);
	        
			effectAdd.getIntegers().write(0, event.getPlayer().getEntityId());
			effectAdd.getBytes().write(0, (byte) (4 & 255));
			effectAdd.getBytes().write(1, (byte) (255 & 255));
			effectAdd.getIntegers().write(1, 1);
			effectAdd.getBytes().write(2, (byte) (1));
	        
	        try {
				manager.sendServerPacket(event.getPlayer(), effectAdd);
				event.getPlayer().sendMessage("Packet receibed - added");
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
       	} else {
			PacketContainer effectRemove = manager.createPacket(PacketType.Play.Server.REMOVE_ENTITY_EFFECT);
	        
			effectRemove.getIntegers().write(0, event.getPlayer().getEntityId());
			effectRemove.getEffectTypes().write(0, PotionEffectType.SLOW_DIGGING);
	        
	        try {
				manager.sendServerPacket(event.getPlayer(), effectRemove);
				event.getPlayer().sendMessage("Packet receibed - removed");
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
       	}
        
	}
	
//	private void checkStillBreaking() {
//		
//	}

}
