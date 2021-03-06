package com.p1ut0nium.roughmobsrevamped.util.handlers;

import com.p1ut0nium.roughmobsrevamped.util.Constants;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class SoundHandler {

	public static SoundEvent ENTITY_BOSS_SPAWN;
	public static SoundEvent ENTITY_BOSS_IDLE;
	public static SoundEvent ENTITY_BOSS_DEATH;
	public static SoundEvent ENTITY_BOSS_BATSWARM;
	public static SoundEvent ENTITY_PLAYER_COUGH;
	
	public static void registerSounds() {
		ENTITY_BOSS_SPAWN = registerSound("entity.boss.boss_spawn");
		ENTITY_BOSS_IDLE = registerSound("entity.boss.boss_idle");
		ENTITY_BOSS_DEATH = registerSound("entity.boss.boss_death");
		ENTITY_BOSS_BATSWARM = registerSound("entity.boss.boss_batswarm");
		ENTITY_PLAYER_COUGH = registerSound("entity.player.player_cough");
	}
	
	private static SoundEvent registerSound(String name) {
		ResourceLocation location = new ResourceLocation(Constants.MODID, name);
		SoundEvent event = new SoundEvent(location);
		event.setRegistryName(name);
		ForgeRegistries.SOUND_EVENTS.register(event);
		return event;
	}
}
