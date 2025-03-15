package com.antoninbeliard.mixin;

import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.AllayEntity;
import net.minecraft.entity.ai.goal.GoalSelector;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MobEntity.class)
public abstract class MixinMobEntity {
    @Shadow protected GoalSelector goalSelector;
    @Shadow protected GoalSelector targetSelector;

    // Inject at the end of MobEntity's constructor
    @Inject(method = "<init>", at = @At("TAIL"))
    private void onConstruct(CallbackInfo ci) {
        // Only clear AI if this mob is an Allay with custom name "bot"
        if ((Object)this instanceof AllayEntity) {
            AllayEntity allay = (AllayEntity)(Object)this;
            if (allay.hasCustomName() && "bot".equals(allay.getCustomName().getString())) {
                goalSelector.getGoals().clear();
                targetSelector.getGoals().clear();
            }
        }
    }
}
