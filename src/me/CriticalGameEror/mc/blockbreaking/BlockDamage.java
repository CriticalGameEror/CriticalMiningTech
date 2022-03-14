package me.CriticalGameEror.mc.blockbreaking;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffectType;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.BlockPosition;

import me.CriticalGameEror.mc.CriticalMiningTech;

public class BlockDamage {
    
    private CriticalMiningTech plugin;
    
    private static HashMap<String, ScheduleTask> scheduleId = new HashMap<String, ScheduleTask>();
	
	private static ProtocolManager manager;
	
	public BlockDamage(CriticalMiningTech plugin) {
		this.plugin = plugin;
		manager = ProtocolLibrary.getProtocolManager();
	}
	

    protected void configureBreakingPacket(double hardness, Player player, Block block) {
		PacketContainer breakingAnimation = manager.createPacket(PacketType.Play.Server.BLOCK_BREAK_ANIMATION);
		
		// this enusres that the player wont conflict with another player's breaking animation
		int entityId = player.getEntityId() + 1;
		entityId = entityId * 1000;
		
		
		breakingAnimation.getIntegers().write(0, entityId);
        breakingAnimation.getBlockPositionModifier().write(0, new BlockPosition(block.getX(), block.getY(), block.getZ()));
        
        breakingTimeCheck(hardness, player, block, breakingAnimation);

	}

    private void breakingTimeCheck(double hardness, Player player, Block block, PacketContainer breakingAnimation) {	
    	double breakingTimeTicks = getBreakingTime(hardness, player, block);

        // check if the breakingTime is instant
        if (breakingTimeTicks == 0) {
        	playerBreakBlock(player, block);
        	return;
        }

        startBreaking(player, breakingAnimation, breakingTimeTicks, block);
    
    }
    
    private void startBreaking(Player player, PacketContainer breakingAnimation, double breakingTimeTicks, Block originalBlock) {

    	int taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
        	double currentTicks = 0d;
        	
        	@Override
            public void run() {
                
        		// stops breaking if player isn't actively breaking the block
        		if (!(PacketManager.armSwinging.containsKey(player.getName()))) {
                	Bukkit.getScheduler().cancelTask(scheduleId.get(player.getName()).taskId);
                	scheduleId.remove(player.getName());      	
                    
                    // returns the breaking animation back to none
                	breakingAnimation.getIntegers().write(1, -1);
                    try {
        				manager.sendServerPacket(player, breakingAnimation);
        			} catch (InvocationTargetException e) {
        				e.printStackTrace();
        			}
                    return;
        		}
        		
        		Block currentTarget = player.getTargetBlockExact(5);
        		
        		// removes any progress if mining from block onto air and cancels this task
                if (currentTarget == null) {
                	Bukkit.getScheduler().cancelTask(scheduleId.get(player.getName()).taskId);
                	scheduleId.remove(player.getName());      	
                    
                    // returns the breaking animation back to none
                	breakingAnimation.getIntegers().write(1, -1);
                    try {
        				manager.sendServerPacket(player, breakingAnimation);
        			} catch (InvocationTargetException e) {
        				e.printStackTrace();
        			}
                    return;
                }
                
                // breaks the block if it has been mined for a succificnet amount of time
                if(currentTicks >= breakingTimeTicks) {
                	// sets the final breaking animation
                	breakingAnimation.getIntegers().write(1, 9);
                    try {
						manager.sendServerPacket(player, breakingAnimation);
					} catch (InvocationTargetException e) {
						e.printStackTrace();
					}
                	
                    playerBreakBlock(player, originalBlock);
                    Bukkit.getScheduler().cancelTask(scheduleId.get(player.getName()).taskId);
                    scheduleId.remove(player.getName());
                    return;
                } else {
                	double multiplier = 0.1;
                	for (int x=0; x <= 9; x++) {
                		if (currentTicks <= (breakingTimeTicks * multiplier)) {
                        	breakingAnimation.getIntegers().write(1, x-1);
                            try {
        						manager.sendServerPacket(player, breakingAnimation);
        					} catch (InvocationTargetException e) {
        						e.printStackTrace();
        					}
                            break;
                		}
                		multiplier += 0.1;
                	}
                }
                
                currentTicks = currentTicks + 1;
            }
    	},0L, 1L);
    	
    	scheduleId.put(player.getName(), new ScheduleTask(taskId, originalBlock));
    }

    public static void cancelTaskWithBlockReset(Player player) {
    	if (scheduleId.containsKey(player.getName())) {
        	Block block = scheduleId.get(player.getName()).block;
    		
    		Bukkit.getScheduler().cancelTask(scheduleId.get(player.getName()).taskId);
        	scheduleId.remove(player.getName());
        	
    		
        	// this enusres that the player wont conflict with another player's breaking animation
    		int entityId = player.getEntityId() + 1;
    		entityId = entityId * 1000;
    		PacketContainer breakingAnimation = manager.createPacket(PacketType.Play.Server.BLOCK_BREAK_ANIMATION);
    		
    		breakingAnimation.getIntegers().write(0, entityId);
            breakingAnimation.getBlockPositionModifier().write(0, new BlockPosition(block.getX(), block.getY(), block.getZ()));
        	
            // returns the breaking animation back to none
        	breakingAnimation.getIntegers().write(1, -1);
            try {
				manager.sendServerPacket(player, breakingAnimation);
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
            return;
        }
    }
    
    @SuppressWarnings("deprecation")
	private double getBreakingTime(double hardness, Player player, Block block) {
    	double speedMultiplier = 1d;
    	
    	
    	ItemStack item = player.getEquipment().getItemInMainHand();	
    	
    	if (block.isPreferredTool(player.getEquipment().getItemInMainHand())) {
    		
    		if (item.getType().equals(Material.AIR)) speedMultiplier = 1d; 
    		
    		else if (item.getType().equals(Material.WOODEN_PICKAXE) || 
    				item.getType().equals(Material.WOODEN_SHOVEL) || 
    				item.getType().equals(Material.WOODEN_AXE) ||
    				item.getType().equals(Material.WOODEN_HOE)) speedMultiplier = 2d; 
    		
    		else if (item.getType().equals(Material.STONE_PICKAXE) ||
    				item.getType().equals(Material.STONE_SHOVEL) ||
    				item.getType().equals(Material.STONE_AXE) ||
    				item.getType().equals(Material.STONE_HOE)) speedMultiplier = 4d;
    		
    		else if (item.getType().equals(Material.IRON_PICKAXE) ||
    				item.getType().equals(Material.IRON_SHOVEL) ||
    				item.getType().equals(Material.IRON_AXE) ||
    				item.getType().equals(Material.IRON_HOE)) speedMultiplier = 6d;
    		
    		else if (item.getType().equals(Material.DIAMOND_PICKAXE) ||
    				item.getType().equals(Material.DIAMOND_SHOVEL) ||
    				item.getType().equals(Material.DIAMOND_AXE) ||
    				item.getType().equals(Material.DIAMOND_HOE)) speedMultiplier = 8d;
    		
    		else if (item.getType().equals(Material.NETHERITE_PICKAXE) ||
    				item.getType().equals(Material.NETHERITE_SHOVEL) ||
    				item.getType().equals(Material.NETHERITE_AXE) ||
    				item.getType().equals(Material.NETHERITE_HOE)) speedMultiplier = 9d;
    		
    		else if (item.getType().equals(Material.GOLDEN_PICKAXE) ||
    				item.getType().equals(Material.GOLDEN_SHOVEL) ||
    				item.getType().equals(Material.GOLDEN_AXE) ||
    				item.getType().equals(Material.GOLDEN_HOE)) speedMultiplier = 12d;

    		if (item.hasItemMeta()) {
				if (item.getItemMeta().hasEnchant(Enchantment.DIG_SPEED)) {
					speedMultiplier += Math.pow(item.getEnchantmentLevel(Enchantment.DIG_SPEED), 2) + 1d;
				}
    		}
    		    
    	}


		if (player.hasPotionEffect(PotionEffectType.FAST_DIGGING)) {
			speedMultiplier *= 1 + (0.2 * player.getPotionEffect(PotionEffectType.FAST_DIGGING).getAmplifier());
		}

		if (player.isInWater()) {
		  speedMultiplier /= 5;
		}

		if (!player.isOnGround()) {
		  speedMultiplier /= 5;
		}

		double damage;
		
		// checks for a custom hardness
		if (plugin.filehandler.getConfig().getConfigurationSection("Speeds") != null) {
			damage = speedMultiplier / plugin.filehandler.getConfig().getDouble("Speeds." + block.getType().toString());
		} else {
			damage = speedMultiplier / block.getType().getHardness();
		}

		damage /= 30;

		// Instant breaking
		if (damage > 1) {
		  return 0d;
		}

		return  Math.round(1 / damage);
    }
    
    private void playerBreakBlock(Player player, Block block) {
//    	Collection<ItemStack> blockDrops = block.getDrops(player.getEquipment().getItemInMainHand());
//    	
//    	block.getLocation().getBlock().setType(Material.AIR);
//    	block.getWorld().playSound(block.getLocation(), Sound.BLOCK_STONE_BREAK, 1.0f, 1.0f);    	
//    	for (ItemStack drop : blockDrops) {
//    		block.getWorld().dropItem(block.getLocation(), drop);	
//    	}
    	
    	block.breakNaturally(player.getEquipment().getItemInMainHand());
    	block.getWorld().playSound(block.getLocation(), block.getBlockData().getSoundGroup().getBreakSound(), 1.0f, 1.0f);
    	
    	ItemStack item = player.getEquipment().getItemInMainHand();
    	
    	if (item == null) {
    		return;
    	}
    	
    	ItemMeta meta = item.getItemMeta();
    	
    	if (meta.isUnbreakable()) {
    		return;
    	}
    	
    	if (meta.hasEnchant(Enchantment.DURABILITY)) {
    		Random random = new Random();
    		
    		if (random.nextInt(Math.round(100 / (meta.getEnchantLevel(Enchantment.DURABILITY) + 1))) == 0) {
    			return;
    		}
    	}
    	
    	if (meta instanceof Damageable) {
    		Damageable damage = (Damageable) meta;
    		
    		if (!damage.hasDamage()) {
    			damage.setDamage(1);
    		} else if (damage.getDamage() >= item.getType().getMaxDurability()) {
    			player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0F, 1.0F);
    			player.getInventory().setItemInMainHand(null);
    			return;
    		} else {
        		damage.setDamage(damage.getDamage() + 1);  
    		} 		
    		item.setItemMeta((ItemMeta) damage);
    	}
    }
}