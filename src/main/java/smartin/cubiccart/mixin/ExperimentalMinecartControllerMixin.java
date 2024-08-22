package smartin.cubiccarts.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.PoweredRailBlock;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.entity.vehicle.ExperimentalMinecartController;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ExperimentalMinecartController.class)
public abstract class ExperimentalMinecartControllerMixin {


    @Inject(
            method = "decelerateFromPoweredRail(Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/block/BlockState;)Lnet/minecraft/util/math/Vec3d;",
            at = @At("HEAD"),
            cancellable = true)
    public void cubiccarts_adjustMinecartDeceleration(Vec3d velocity, BlockState railState, CallbackInfoReturnable<Float> cir) {

    }

    @Inject(
            method = "accelerateFromPoweredRail",
            at = @At("HEAD"),
            cancellable = true)
    public void cubiccarts_adjustMinecartAcceleration(Vec3d velocity, BlockPos railPos, BlockState railState, CallbackInfoReturnable<Float> cir) {
    }
}
