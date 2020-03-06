package com.p1ut0nium.roughmobsrevamped.entity.boss;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.annotation.Nullable;

import com.p1ut0nium.roughmobsrevamped.client.FogEventHandler;
import com.p1ut0nium.roughmobsrevamped.misc.BossHelper;
import com.p1ut0nium.roughmobsrevamped.util.DamageSourceFog;
import com.p1ut0nium.roughmobsrevamped.util.Utilities;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.effect.LightningBoltEntity;
import net.minecraft.entity.monster.SkeletonEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

public class SkeletonChampionEntity extends SkeletonEntity implements IChampion {

	// Fog variables
    private static boolean FOG_DOT_ENABLED = BossHelper.bossFogDoTEnabled;
    private static boolean FOG_WARNING_ENABLED = BossHelper.bossFogDoTWarning;
	private static int FOG_MAX_DISTANCE = BossHelper.bossFogMaxDistance;
    private static int FOG_DOT_DELAY = BossHelper.bossFogDoTDelay * 20;
    private static int FOG_WARNING_TIME = BossHelper.bossFogDoTWarningTime * 20;
    private HashMap<String, Long> playersWarned = new HashMap<>();
    private List<PlayerEntity> playersInFog = new ArrayList<PlayerEntity>();
    private StringTextComponent fogWarningMsg;
	private static int fog_dot_tick;

	//TODO private double[] bossColorTheme = {1.0, 0.0, 0.0};

	public SkeletonChampionEntity(EntityType<? extends SkeletonChampionEntity> type, World worldIn) {
		super(type, worldIn);
        this.experienceValue = 100;
        
        fog_dot_tick = 0;
        
		fogWarningMsg = new StringTextComponent("The thick fog reaches out for you... You begin to choke as you move through it.\nPerhaps you should find the source of the poisonous mist, or flee to safety.");
		fogWarningMsg.getStyle().setColor(TextFormatting.DARK_GREEN);
	}
	
	/*
	public SkeletonChampionEntity(World worldIn) {
		this(EntityType.SKELETON, worldIn);
	}
	*/
	
    public void onAddedToWorld() {
    	super.onAddedToWorld();
    	
        if (this.world.isRemote && this.posY >= world.getSeaLevel() && this.world.canBlockSeeSky(this.getPosition())) {
			this.world.addWeatherEffect(new LightningBoltEntity(this.world, this.posX, this.posY, this.posZ, true));
			SoundEvent soundEvent = new SoundEvent(new ResourceLocation("entity.lightning.thunder"));
			this.world.playSound(this.posX, this.posY, this.posZ, soundEvent, SoundCategory.AMBIENT, 100.0F, 1.0F, true);
        }
    }
	
    public void onLivingUpdate() {
        if (this.world.isRemote) {
            for (int i = 0; i < 2; ++i) {
                this.world.spawnParticle(ParticleTypes.FLAME, this.posX + (this.rand.nextDouble() - 0.5D) * (double)this.getWidth(), this.posY + this.rand.nextDouble() * (double)this.getHeight(), this.posZ + (this.rand.nextDouble() - 0.5D) * (double)this.getWidth(), 0.0D, 0.0D, 0.0D);
            }
        }
        
        if (!this.world.isRemote) {
	        
	        if (FOG_DOT_ENABLED) {
	        	List<PlayerEntity> playersInRange = this.world.getEntitiesWithinAABB(PlayerEntity.class, this.getBoundingBox().grow(FOG_MAX_DISTANCE));
	        	
	    		// Test to see if playersInRange actually contains players
	    		if (Utilities.containsInstance(playersInRange, PlayerEntity.class)) {

					// For each player in range
					for (PlayerEntity player : playersInRange) {
						
						// Add new player to list of players in the fog
						if (!playersInFog.contains(player)) {
							playersInFog.add(player);
							
							// If fog warning is enabled...
							if (FOG_WARNING_ENABLED) {
								// Warn players on first entering the fog and add them to the players warned list
								if (!playersWarned.containsKey(player.getName())) {
									playersWarned.put(player.getName(), world.getGameTime() + FOG_WARNING_TIME);
									player.sendMessage(fogWarningMsg);
								}
								// If warned player hasn't been warned in a while, warn them again
								else if (playersWarned.containsKey(player.getName()) && world.getGameTime() >= playersWarned.get(player.getName())) {
									playersWarned.replace(player.getName(), world.getGameTime() + FOG_WARNING_TIME);
									player.sendMessage(fogWarningMsg);
								}
							}
						}
					}
					
					// For each player marked as "in the fog"
					for (PlayerEntity playerInFog : playersInFog) {
						
						// Remove them from the "in fog" list if they are no longer in range
						if (!Utilities.containsPlayer(playersInRange, playerInFog)) {
							playersInFog.remove(playerInFog);
						}
					}

					// Reset fog DoT delay
					if (fog_dot_tick == FOG_DOT_DELAY) {
						fog_dot_tick = 0;
					}
					
					// Damage player while inside fog.
					if (fog_dot_tick == 0) {
						for (PlayerEntity player : playersInRange) {
							player.attackEntityFrom(DamageSourceFog.POISONOUS_FOG, 0);
						}
					}

					fog_dot_tick += 1;
				}
	    		
	    		// If no players are in range, then clear the list that keeps track of all players in the fog
	    		else if (!Utilities.containsInstance(playersInRange, PlayerEntity.class)) {
	    			playersInFog.clear();
	    		}
	        }
        }

        super.onLivingUpdate();
    }

    public void onDeath(DamageSource cause)
    {
        super.onDeath(cause);
        
        //TODO Add custom death effects
        
        FogEventHandler.bossDied = true;
    }
    
	protected boolean canDespawn() {
		return false;
	}
    
    //TODO Add custom ambient sound
    protected SoundEvent getAmbientSound() {
        return SoundHandler.ENTITY_BOSS_IDLE;
    }
    
	//TODO Add custom death sound
    protected SoundEvent getDeathSound() {
        return SoundHandler.ENTITY_BOSS_DEATH;
    }
}

