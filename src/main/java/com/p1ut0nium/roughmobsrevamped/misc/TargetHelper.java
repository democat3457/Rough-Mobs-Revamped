package com.p1ut0nium.roughmobsrevamped.misc;

import java.util.ArrayList;
import java.util.List;

import com.p1ut0nium.roughmobsrevamped.RoughMobs;
import com.p1ut0nium.roughmobsrevamped.config.RoughConfig;
import com.p1ut0nium.roughmobsrevamped.util.Constants;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.util.ResourceLocation;

public class TargetHelper {
	
	public static final String BLOCK_CATEGORY = "targetBlocker";
    private static boolean enableTargetBlock;
    public static boolean enableBlockDebug;
	private static final List<TargetEntry> BlockerList = new ArrayList<TargetEntry>();
    
    public static final String ATTACK_CATEGORY = "targetAttacker";
	public static boolean enableTargetAttack;
	public static boolean ignoreSpawnConditions;
	private static final List<TargetEntry> AttackerList = new ArrayList<TargetEntry>();
	
	public static void init() {
		
		RoughConfig.getConfig().addCustomCategoryComment(BLOCK_CATEGORY, "Entities which can't be targeted by other entities."
																+ "\ne.g. Skeletons can't target other Skeletons by shooting them accidentally"
																+ "\nTakes 2 arguments divided by a semicolon per entry. victim;attacker"
																+ "\nvictim: The entity which should not be targeted if attacked by the attacker (entity name)"
																+ "\nattacker: the attacker entity which can't target the victim (entity name)"
																+ "\nUse \"*\" instead of the victim or attacker if you want this for all entities except players");
		String[] blockers = RoughConfig.getStringArray(BLOCK_CATEGORY, "_List", Constants.DEFAULT_TARGET_BLOCKER, "");
		enableTargetBlock = RoughConfig.getBoolean(BLOCK_CATEGORY, "_Enabled", false, "Set to true to enable the target blocker feature.");
        enableBlockDebug = RoughConfig.getBoolean(BLOCK_CATEGORY, "_Debug", false, "Enable debug prints for target blocking.");
        
		fillList(blockers, ListType.TARGET_BLOCKER);
		
		RoughConfig.getConfig().addCustomCategoryComment(ATTACK_CATEGORY, "Entities always attack these targets."
																+ "\ne.g. Zombies always attack pigs."
																+ "\nTakes 2 arguments divided by a semicolon per entry. victim;attacker"
																+ "\nvictim: The entity which should be attacked (entity name)"
																+ "\nattacker: the attacker entity (entity name)"
																+ "\nMultiple entries for the same victim or attacker are allowed"
																+ "\nUse \"*\" instead of the victim or attacker if you want this for all entities except players");
		
		String[] attackers = RoughConfig.getStringArray(ATTACK_CATEGORY, "_List", Constants.DEFAULT_TARGETS, "");
		enableTargetAttack = RoughConfig.getBoolean(ATTACK_CATEGORY, "_Enabled", false, "Set to true to enable the target attacker feature.");
		ignoreSpawnConditions = RoughConfig.getBoolean(ATTACK_CATEGORY, "_IgnoreSpawnConditions", true, "Disable to require spawn conditions be met in order for target attacker feature to work.");
		
		fillList(attackers, ListType.TARGET_ATTACKER);
	}
	
	@SuppressWarnings("unchecked")
	private static void fillList(String[] options, ListType listType) {
		top:
		for (String option : options) 
		{
			String[] split = option.split(";");
			if (split.length >= 2) 
			{
				Class<? extends Entity>[] entities = new Class[2];
				for (int i = 0; i < 2; i++)
				{
					if (split[i].trim().equals("*"))
					{
						entities[i] = Entity.class;
					}
					else
					{
						Class<? extends Entity> clazz = EntityList.getClass(new ResourceLocation(split[i].trim()));
						if (clazz == null)
						{
							RoughMobs.logger.error(listType + ": \"" + split[1] + "\" is not a valid entity!");
							continue top;
						}
						entities[i] = clazz;
					}
				}
				
                if (listType == ListType.TARGET_BLOCKER)
                    BlockerList.add(new TargetEntry(entities[1], entities[0]));
                else if (listType == ListType.TARGET_ATTACKER)
                    AttackerList.add(new TargetEntry(entities[1], entities[0]));
			}
			else
				RoughMobs.logger.error(listType + ": each option needs at least 2 arguments! (" + option + ")");
		}
	}

	public static Class<? extends Entity> getBlockerEntityForTarget(Entity targetedEntity) {

		for (TargetEntry entry : BlockerList) 
		{
			if (targetedEntity.getClass().equals(entry.getVictimClass()))
				return entry.getAttackerClass();
		}
		
		return null;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void setTargets(Entity attacker) {
		for (TargetEntry entry : AttackerList) {
			if (attacker.getClass().equals(entry.getAttackerClass()))
				((EntityLiving)attacker).targetTasks.addTask(1, new EntityAINearestAttackableTarget((EntityCreature) attacker, entry.getVictimClass(), true));
		}
	}
	
	public static boolean targetBlockerEnabled() {
		return enableTargetBlock;
	}
	
	public static boolean targetAttackerEnabled() {
		return enableTargetAttack;
    }
    
    private static enum ListType {
        TARGET_ATTACKER,
        TARGET_BLOCKER
    }
	
	static class TargetEntry {
		
		private final Class<? extends Entity> attackerClass;
		private final Class<? extends Entity> victimClass;
		
		public TargetEntry(Class<? extends Entity> attackerClass, Class<? extends Entity> victimClass) {
			this.attackerClass = attackerClass;
			this.victimClass = victimClass;
		}
		
		public Class<? extends Entity> getAttackerClass() {
			return attackerClass;
		}
		
		public Class<? extends Entity> getVictimClass() {
			return victimClass;
		}
	}
}
