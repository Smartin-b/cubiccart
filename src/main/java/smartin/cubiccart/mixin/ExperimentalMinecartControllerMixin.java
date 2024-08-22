package smartin.cubiccart.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.entity.vehicle.ExperimentalMinecartController;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import smartin.cubiccart.Config;
import smartin.cubiccart.Cubiccarts;
import smartin.cubiccart.block.CopperRail;

@Mixin(ExperimentalMinecartController.class)
public abstract class ExperimentalMinecartControllerMixin {


    @Inject(
            method = "decelerateFromPoweredRail(Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/block/BlockState;)Lnet/minecraft/util/math/Vec3d;",
            at = @At("HEAD"),
            cancellable = true)
    public void cubiccarts_adjustMinecartDeceleration(Vec3d velocity, BlockState railState, CallbackInfoReturnable<Vec3d> cir) {
        if (railState.isOf(Cubiccarts.COPPER_RAIL)) {
            if (railState.isOf(Cubiccarts.COPPER_RAIL)) {
                int level = railState.get(CopperRail.POWER_LEVEL);
                double speedTarget = (double) level * 2.5f;
                double currentSpeed = velocity.length();

                if (currentSpeed > speedTarget) {
                    // Calculate the amount to reduce the speed
                    double reduction = (currentSpeed - speedTarget) / 2.0f;

                    // Calculate the new speed, which is halfway to the speed_target
                    double newSpeed = currentSpeed - reduction;

                    // Adjust the velocity to this new speed
                    // Assuming 'velocity' can be scaled by a scalar multiplier
                    cir.setReturnValue(velocity.normalize().multiply(newSpeed));
                }
            }
        }
    }

    @Inject(
            method = "accelerateFromPoweredRail",
            at = @At("HEAD"),
            cancellable = true)
    public void cubiccarts_adjustMinecartAcceleration(Vec3d velocity, BlockPos railPos, BlockState railState, CallbackInfoReturnable<Vec3d> cir) {
        ExperimentalMinecartController controller = (ExperimentalMinecartController) (Object) this;
        if (railState.isOf(Cubiccarts.COPPER_RAIL)) {
            if (railState.isOf(Cubiccarts.COPPER_RAIL)) {
                int level = railState.get(CopperRail.POWER_LEVEL);
                double speedTarget = (double) level * Config.instance.copper_speed_per_tick_per_level;
                double currentSpeed = velocity.length();

                if (currentSpeed < speedTarget) {
                    if (velocity.length() > 0.01) {
                        cir.setReturnValue(velocity.normalize().multiply(velocity.length() + 0.06));
                    } else {
                        Vec3d vec3d = ((MinecartControllerAccessor) controller).getMinecart().getLaunchDirection(railPos);
                        cir.setReturnValue(vec3d.lengthSquared() <= 0.0 ? velocity : vec3d.multiply(velocity.length() + 0.2));
                    }
                }
            }
        }
    }

    @Inject(
            method = "Lnet/minecraft/entity/vehicle/ExperimentalMinecartController;getMaxSpeed()D",
            at = @At("HEAD"),
            cancellable = true)
    public void cubiccarts_adjustMinecartAcceleration(CallbackInfoReturnable<Double> cir) {
        ExperimentalMinecartController controller = (ExperimentalMinecartController) (Object) this;
        var minecart = ((MinecartControllerAccessor) controller).getMinecart();
        cir.setReturnValue(10000000.0);
    }
}
