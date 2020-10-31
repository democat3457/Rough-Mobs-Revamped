package com.p1ut0nium.roughmobsrevamped.ai.misc;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;

public class RoughAIAddEffect extends EntityAIBase {
	
	private final EntityLiving entity;
	private final Potion effect;
	private final double range;
	
	public RoughAIAddEffect(EntityLiving entity, Potion effect, double range) {
		this.entity = entity;
		this.effect = effect;
		this.range = range;
	}

	@Override
	public boolean shouldExecute() {
		return !GetEntities().isEmpty();
	}
	
	@Override
	public void updateTask() {
		
		for (EntityLivingBase living : GetEntities())
		{
			living.addPotionEffect(new PotionEffect(effect, 200, 1));
		}
	}
	
	private List<EntityLivingBase> GetEntities() {
		
		List<EntityLivingBase> allLiving = entity.world.getEntitiesWithinAABB(EntityLivingBase.class, entity.getEntityBoundingBox().grow(range));
		List<EntityLivingBase> friendlyLiving = new ArrayList<>();
		
		for (EntityLivingBase living : allLiving)
			if (!(living instanceof EntityMob))
				friendlyLiving.add(living);
		
		return friendlyLiving;
	}
}
