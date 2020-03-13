/*
 * Rough Mobs Revamped for Minecraft Forge 1.14.4
 * 
 * This is a complete revamp of Lellson's Rough Mobs 2
 * 
 * Author: p1ut0nium_94
 * Website: https://www.curseforge.com/minecraft/mc-mods/rough-mobs-revamped
 * Source: https://github.com/p1ut0nium-git/Rough-Mobs-Revamped/tree/1.14.4
 * 
 */
package com.p1ut0nium.roughmobsrevamped.features;

import java.util.List;

import com.p1ut0nium.roughmobsrevamped.config.RoughConfig;
import com.p1ut0nium.roughmobsrevamped.misc.BossHelper.BossApplier;
import com.p1ut0nium.roughmobsrevamped.misc.EquipHelper.EquipmentApplier;
import com.p1ut0nium.roughmobsrevamped.misc.FeatureHelper;
import com.p1ut0nium.roughmobsrevamped.misc.MountHelper;
import com.p1ut0nium.roughmobsrevamped.misc.MountHelper.HorseType;
import com.p1ut0nium.roughmobsrevamped.misc.SpawnHelper;
import com.p1ut0nium.roughmobsrevamped.reference.Constants;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.monster.ZombieEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.Effects;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;

public class ZombieFeatures extends EntityFeatures {
	
	private static final String BOSS_MINION = Constants.unique("bossMinion");
	
	private float leapHeight; 
	private int leapChance;
	
	private int hungerDuration;
	private int hungerChance;
	
	private int horseChance;
	private int horseMinY;
	
	private boolean babyBurn;
	private boolean helmetBurn;
	
	private EquipmentApplier equipApplier;
	private BossApplier bossApplier;

	private List<String> breakBlocks;
	private List<Block> allowedBreakBlocks;
	
	public ZombieFeatures() {
		super("zombie", Constants.ZOMBIES);
	}
	
	@Override
	public void preInit() {
		equipApplier = new EquipmentApplier(
				name, 
				Constants.CHANCE_PER_WEAPON_DEFAULT, 
				Constants.CHANCE_PER_ARMOR_DEFAULT, 
				Constants.CHANCE_PER_ENCHANT_DEFAULT, 
				Constants.ENCHANT_MULTIPLIER_DEFAULT, 
				Constants.DROP_CHANCE_DEFAULT);
		bossApplier = new BossApplier(
				name, 
				RoughConfig.zombieChampionChance, 
				1F, 
				0.2F, 
				RoughConfig.zombieChampionNames.toArray(new String[0])) {

			@Override
			public void addBossFeatures(LivingEntity entity) {
				if (SpawnHelper.disableBabyZombies() == false) {
					for (int i = 0; i < 4; i++) {
						ZombieEntity zombieMinion = new ZombieEntity(entity.getEntityWorld());
						CompoundNBT nbtMinion = zombieMinion.getPersistentData();
						nbtMinion.putBoolean(BOSS_MINION, true);
						zombieMinion.setPosition(entity.posX + Math.random() - Math.random(), entity.posY + Math.random(), entity.posZ + Math.random() - Math.random());
						zombieMinion.onInitialSpawn(zombieMinion.world, entity.getEntityWorld().getDifficultyForLocation(entity.getPosition()), SpawnReason.MOB_SUMMONED, null, nbtMinion);
						zombieMinion.setChild(true);

						entity.world.addEntity(zombieMinion);
					}
				}
			}
		};
	}
	
	@Override
	public void initConfig() {
		super.initConfig();

		leapChance = RoughConfig.zombieLeapChance;
		leapHeight = RoughConfig.zombieLeapHeight;
		
		hungerDuration = RoughConfig.zombieHungerDuration * 20;
		hungerChance = RoughConfig.zombieHungerChance;
		
		horseChance = RoughConfig.zombieHorseChance;
		horseMinY = RoughConfig.zombieHorseMinY;
		
		babyBurn = RoughConfig.zombieBabyBurn;
		helmetBurn = RoughConfig.zombieHelmetBurn;
		
		breakBlocks = RoughConfig.zombieBreakBlocks;

		boolean isBoss = false;
		boolean useDefaultValues = false;
		equipApplier.initConfig(isBoss, useDefaultValues);
		bossApplier.initConfig();
	}

	@Override
	public void postInit() {
		equipApplier.createPools();
		bossApplier.postInit();
		allowedBreakBlocks = FeatureHelper.getBlocksFromNames(breakBlocks);
	}
	
	/* TODO Add AI
	@Override
	public void addAI(EntityJoinWorldEvent event, Entity entity, EntityAITasks tasks, EntityAITasks targetTasks) {
		
		if (!(entity instanceof LivingEntity))
			return;
		
		if (leapChance > 0)
			tasks.addTask(1, new RoughAILeapAtTargetChanced((LivingEntity) entity, leapHeight, leapChance));
		
		if (babyBurn && entity instanceof ZombieEntity && ((ZombieEntity)entity).isChild() && !entity.isImmuneToFire())
			tasks.addTask(0, new RoughAISunlightBurn((LivingEntity) entity, false));
		
		if (helmetBurn)
			tasks.addTask(0, new RoughAISunlightBurn((LivingEntity) entity, true));
		
		if (allowedBreakBlocks.size() > 0)
			tasks.addTask(1, new RoughAIBreakBlocks((LivingEntity) entity, 8, allowedBreakBlocks));
	}
	*/
	
	@Override
	public void addFeatures(EntityJoinWorldEvent event, Entity entity) {
		
		if (!(entity instanceof LivingEntity) || (entity.getPersistentData()).getBoolean(BOSS_MINION))
			return;

		if (super.bossesEnabled(entity)) {
			Entity boss = bossApplier.trySetBoss((LivingEntity) entity);
			if (boss != null) {
				entity = boss;
				event.setCanceled(true);
			}
			else
				equipApplier.equipEntity((LivingEntity) entity);
		}

		// Attempt to spawn zombie on a horse
		MountHelper.tryMountHorse(entity, HorseType.ZOMBIE, horseChance, horseMinY);
	}
	
	@Override
	public void onAttack(Entity attacker, Entity immediateAttacker, Entity target, LivingAttackEvent event) {
		
		if (target instanceof LivingEntity && hungerChance > 0)
			FeatureHelper.addEffect((LivingEntity)target, Effects.HUNGER, hungerDuration, 0, hungerChance, true, 4);
	}
}
