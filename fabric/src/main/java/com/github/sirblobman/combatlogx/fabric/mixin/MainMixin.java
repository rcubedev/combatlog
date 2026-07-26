/*package com.github.sirblobman.combatlogx.fabric.mixin;

import net.minecraft.server.Main;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Main.class)
public class MainMixin {

    @Inject(
            method = "main",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/fabricmc/loader/impl/game/minecraft/Hooks;startServer(Ljava/io/File;Ljava/lang/Object;)V"
            )
    )
//    @Inject(method = "main", at = @At(value = ""))
    private static void preInit(CallbackInfo ci) {
        System.out.println("Preinit inject");
    }

    @Inject(
            method = "main",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/fabricmc/loader/impl/game/minecraft/Hooks;startServer(Ljava/io/File;Ljava/lang/Object;)V",
                    shift = At.Shift.AFTER
            )
    )
    private static void postInit(CallbackInfo ci) {
        System.out.println("Postinit inject");
    }

    @Inject(method = "main", at = @At(value = "INVOKE", target = "Lnet/minecraft/Util;startTimerHackThread()V"))
    private static void otherPostInit(CallbackInfo ci) {
        System.out.println("Postinit inject default priority on timerhackthread");
    }

    @Inject(method = "main", at = @At(value = "INVOKE", target = "Lnet/minecraft/Util;startTimerHackThread()V"), order = 950)
    private static void otherPostInit950Prio(CallbackInfo ci) {
        System.out.println("Postinit inject 950 priority on timerhackthread");
    }
}
*/