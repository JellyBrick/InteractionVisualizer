package com.loohp.interactionvisualizer.Managers;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.loohp.interactionvisualizer.InteractionVisualizer;
import com.loohp.interactionvisualizer.ObjectHolders.ChunkPosition;
import com.loohp.interactionvisualizer.ObjectHolders.ChunkUpdateQueue;

public class TileEntityManager {
	
	private static Plugin plugin = InteractionVisualizer.plugin;
	private static ChunkUpdateQueue chunks = new ChunkUpdateQueue();
	private static List<BlockState> states = Collections.synchronizedList(new LinkedList<BlockState>());
	private static AtomicInteger stateTaskCount = new AtomicInteger();
	private static AtomicInteger stateDoneCount = new AtomicInteger();
	private static HashMap<TileEntityType, List<Block>> current = new HashMap<TileEntityType, List<Block>>();
	private static HashMap<TileEntityType, List<Block>> upcomming = new HashMap<TileEntityType, List<Block>>();
	
	private static Integer tileEntityChunkPerTick = InteractionVisualizer.tileEntityChunkPerTick;
	
	public static enum TileEntityType {
		BLAST_FURNACE,
		BREWING_STAND,
		FURNACE,
		SMOKER,
		BEACON,
		JUKEBOX,
		BEE_NEST,
		BEEHIVE;
	}
	
	public static List<Block> getTileEntites(TileEntityType type) {
		List<Block> list = current.get(type);
		return list != null ? list : new LinkedList<Block>();
	}
	
	public static void run() {
		upcomming.put(TileEntityType.BLAST_FURNACE, new LinkedList<Block>());
		upcomming.put(TileEntityType.BREWING_STAND, new LinkedList<Block>());
		upcomming.put(TileEntityType.FURNACE, new LinkedList<Block>());
		upcomming.put(TileEntityType.SMOKER, new LinkedList<Block>());
		upcomming.put(TileEntityType.BEACON, new LinkedList<Block>());
		upcomming.put(TileEntityType.JUKEBOX, new LinkedList<Block>());
		upcomming.put(TileEntityType.BEE_NEST, new LinkedList<Block>());
		upcomming.put(TileEntityType.BEEHIVE, new LinkedList<Block>());
		stateTaskCount.set(0);
		stateDoneCount.set(0);
		Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> getAllChunks());
	}
	
	private static void getAllChunks() {
		for (Player player : Bukkit.getOnlinePlayers()) {
			Location location = player.getLocation().clone();
			World world = location.getWorld();
			int chunkX = (int) Math.floor((double) location.getBlockX() / 16.0);
			int chunkZ = (int) Math.floor((double) location.getBlockZ() / 16.0);
			
			chunks.add(new ChunkPosition(world, chunkX + 1, chunkZ + 1));
			chunks.add(new ChunkPosition(world, chunkX + 1, chunkZ));
			chunks.add(new ChunkPosition(world, chunkX + 1, chunkZ - 1));
			chunks.add(new ChunkPosition(world, chunkX, chunkZ + 1));
			chunks.add(new ChunkPosition(world, chunkX, chunkZ));
			chunks.add(new ChunkPosition(world, chunkX, chunkZ - 1));
			chunks.add(new ChunkPosition(world, chunkX - 1, chunkZ + 1));
			chunks.add(new ChunkPosition(world, chunkX - 1, chunkZ));
			chunks.add(new ChunkPosition(world, chunkX - 1, chunkZ - 1));
		}
		
		if (plugin.isEnabled()) {
			Bukkit.getScheduler().runTaskLater(plugin, () -> loadBlockStates(), 1);
		}
	}
	
	private static void loadBlockStates() {
		int count = 0;
		while (!chunks.isEmpty()) {
			ChunkPosition chunkpos = chunks.poll();
			if (chunkpos.isLoaded()) {
				count++;
				BlockState[] stateArray = chunkpos.getChunk().getTileEntities();
				stateTaskCount.incrementAndGet();
				Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
					for (BlockState state : stateArray) {
						states.add(state);
					}
					stateDoneCount.incrementAndGet();
				});
			}
			if (count >= tileEntityChunkPerTick) {
				break;
			}
		}
		if (chunks.isEmpty()) {
			if (InteractionVisualizer.loadTileEntitiesAsync) {
				Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
					while (stateTaskCount.get() > stateDoneCount.get()) {
						try {TimeUnit.MILLISECONDS.sleep(50);} catch (InterruptedException e) {e.printStackTrace();}
					}
					loadTileEntities();
				});
			} else {
				Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
					while (stateTaskCount.get() > stateDoneCount.get()) {
						try {TimeUnit.MILLISECONDS.sleep(50);} catch (InterruptedException e) {e.printStackTrace();}
					}
					Bukkit.getScheduler().runTask(plugin, () -> loadTileEntitiesSynced());
				});
			}
		} else {
			Bukkit.getScheduler().runTaskLater(plugin, () -> loadBlockStates(), 1);
		}
	}

	private static void loadTileEntities() {
		while (!states.isEmpty()) {
			BlockState state = states.remove(0);
			Block block = state.getBlock();
			Material type = state.getType();
			if (type.toString().toUpperCase().equals("BLAST_FURNACE")) {
				upcomming.get(TileEntityType.BLAST_FURNACE).add(block);
			} else if (type.toString().toUpperCase().equals("BREWING_STAND")) {
				upcomming.get(TileEntityType.BREWING_STAND).add(block);
			} else if (isFurnace(type)) {
				upcomming.get(TileEntityType.FURNACE).add(block);
			} else if (type.toString().toUpperCase().equals("SMOKER")) {
				upcomming.get(TileEntityType.SMOKER).add(block);
			} else if (type.toString().toUpperCase().equals("BEACON")) {
				upcomming.get(TileEntityType.BEACON).add(block);
			} else if (type.toString().toUpperCase().equals("JUKEBOX")) {
				upcomming.get(TileEntityType.JUKEBOX).add(block);
			} else if (type.toString().toUpperCase().equals("BEE_NEST")) {
				upcomming.get(TileEntityType.BEE_NEST).add(block);
			} else if (type.toString().toUpperCase().equals("BEEHIVE")) {
				upcomming.get(TileEntityType.BEEHIVE).add(block);
			}
		}
		if (plugin.isEnabled()) {
			Bukkit.getScheduler().runTaskLater(plugin, () -> {
				current = upcomming;
				upcomming = new HashMap<TileEntityType, List<Block>>();
				Bukkit.getScheduler().runTaskLater(plugin, () -> run(), 1);
			}, 1);
		}
	}
	
	private static void loadTileEntitiesSynced() {
		int count = 0;
		while (!states.isEmpty()) {
			count++;
			BlockState state = states.remove(0);
			Block block = state.getBlock();
			Material type = state.getType();
			if (type.toString().toUpperCase().equals("BLAST_FURNACE")) {
				upcomming.get(TileEntityType.BLAST_FURNACE).add(block);
			} else if (type.toString().toUpperCase().equals("BREWING_STAND")) {
				upcomming.get(TileEntityType.BREWING_STAND).add(block);
			} else if (isFurnace(type)) {
				upcomming.get(TileEntityType.FURNACE).add(block);
			} else if (type.toString().toUpperCase().equals("SMOKER")) {
				upcomming.get(TileEntityType.SMOKER).add(block);
			} else if (type.toString().toUpperCase().equals("BEACON")) {
				upcomming.get(TileEntityType.BEACON).add(block);
			} else if (type.toString().toUpperCase().equals("JUKEBOX")) {
				upcomming.get(TileEntityType.JUKEBOX).add(block);
			} else if (type.toString().toUpperCase().equals("BEE_NEST")) {
				upcomming.get(TileEntityType.BEE_NEST).add(block);
			} else if (type.toString().toUpperCase().equals("BEEHIVE")) {
				upcomming.get(TileEntityType.BEEHIVE).add(block);
			}
			if (count > 10) {
				break;
			}
		}
		if (states.isEmpty()) {
			Bukkit.getScheduler().runTaskLater(plugin, () -> {
				current = upcomming;
				upcomming = new HashMap<TileEntityType, List<Block>>();
				Bukkit.getScheduler().runTaskLater(plugin, () -> run(), 1);
			}, 1);
		} else {
			Bukkit.getScheduler().runTaskLater(plugin, () -> loadTileEntitiesSynced(), 1);
		}
	}
	
	private static boolean isFurnace(String material) {
		if (material.toUpperCase().equals("FURNACE")) {
			return true;
		}
		if (material.toUpperCase().equals("BURNING_FURNACE")) {
			return true;
		}
		return false;
	}
	
	private static boolean isFurnace(Material material) {
		return isFurnace(material.toString());
	}
	
}
