package com.p1ut0nium.roughmobsrevamped.features;

import com.p1ut0nium.roughmobsrevamped.ai.combat.RoughAIDropTNT;
import com.p1ut0nium.roughmobsrevamped.config.RoughConfig;
import com.p1ut0nium.roughmobsrevamped.misc.FeatureHelper;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.entity.monster.EntityGhast;
import net.minecraft.entity.projectile.EntityLargeFireball;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.EnumParticleTypes;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

public class GhastFeatures extends EntityFeatures {

	private boolean projectileImmunity;
	private boolean dropTnt;
	private int explosionRadius;
	
	@SuppressWarnings("unchecked")
	public GhastFeatures() {
		super("ghast", EntityGhast.class);
	}
	
	@Override
	public void initConfig() {
		super.initConfig();
		
		projectileImmunity = RoughConfig.getBoolean(name, "ProjectileImmunity", true, "Set to false to prevent %ss from being immune to projectiles");
		dropTnt = RoughConfig.getBoolean(name, "DropTnt", true, "Set to false to prevent %ss from dropping primed TNT on their targets");
		explosionRadius = RoughConfig.getInteger(name, "ExplosionRadius", 3, 0, MAX, "Ghast fireball explosion radius\nThe vanilla default is 1");
	}
	
	@Override
	public void addAI(EntityJoinWorldEvent event, Entity entity, EntityAITasks tasks, EntityAITasks targetTasks) {
		
		if (entity instanceof EntityGhast)
		{
			if (explosionRadius != 1)
				((EntityGhast)entity).explosionStrength = explosionRadius;
		}
		
		if (dropTnt && entity instanceof EntityLiving)
			tasks.addTask(1, new RoughAIDropTNT((EntityLiving) entity));
	}
	
	@Override
	public void onDefend(Entity target, Entity attacker, Entity immediateAttacker, LivingAttackEvent event) {
		
		if (projectileImmunity && event.getSource().isProjectile() && !(immediateAttacker instanceof EntityLargeFireball))
		{
			event.setCanceled(true);
			FeatureHelper.spawnParticle(target, EnumParticleTypes.SMOKE_LARGE, 5);
			FeatureHelper.playSound(target, SoundEvents.BLOCK_SNOW_BREAK);
		}
	}
}
