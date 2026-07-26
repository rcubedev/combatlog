package com.github.sirblobman.combatlogx.fabric.mixin;

import com.github.sirblobman.combatlogx.CombatLogX;
import com.github.sirblobman.combatlogx.api.ICombatLogX;
import com.github.sirblobman.combatlogx.api.expansion.ExpansionManager;
import com.github.sirblobman.combatlogx.expansion.ExpansionRegistryImpl;
import net.minecraft.core.registries.BuiltInRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BuiltInRegistries.class)
public class BuiltInRegistriesMixin {

    @Inject(method = "freeze", at = @At("HEAD"))
    private static void loadExpansions(CallbackInfo ci) {
        ICombatLogX combatLogX = CombatLogX.INSTANCE;
        if (combatLogX == null) throw new IllegalStateException("CombatLogX not initialized");

        ExpansionRegistryImpl registry = ExpansionRegistryImpl.getInstance();
        ExpansionManager expansionManager = combatLogX.getExpansionManager();
        expansionManager.loadExpansions(registry);
    }
}
