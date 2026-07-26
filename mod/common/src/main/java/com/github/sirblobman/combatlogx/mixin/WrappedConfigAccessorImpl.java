package com.github.sirblobman.combatlogx.mixin;

import com.github.rcubedev.example.config.WrappedConfigAccessor;
import folk.sisby.kaleido.lib.quiltconfig.api.Config;
import folk.sisby.kaleido.lib.quiltconfig.api.WrappedConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@SuppressWarnings("deprecation")
@Mixin(WrappedConfig.class)
public interface WrappedConfigAccessorImpl extends WrappedConfigAccessor {

    @Override
    @Accessor("wrapped")
    Config test$getWrapped();
}
