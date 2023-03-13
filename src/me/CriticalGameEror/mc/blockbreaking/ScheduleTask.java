package me.CriticalGameEror.mc.blockbreaking;

import org.bukkit.block.Block;

public class ScheduleTask {
	
	public int taskId;
	public Block block;
	
	public ScheduleTask(int taskId, Block block) {
		this.taskId = taskId;
		this.block = block;
	}

}
