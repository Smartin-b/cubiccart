package smartin.cubiccart.mixin;

import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import smartin.cubiccart.Config;

@Mixin(AbstractMinecartEntity.class)
public abstract class AbstractMinecartEntityMixin {


    @Inject(
            method = "applySlowdown(Lnet/minecraft/util/math/Vec3d;)Lnet/minecraft/util/math/Vec3d;",
            at = @At("HEAD"),
            cancellable = true,
            remap = true,
            require = -1)
    public void cubiccarts_applySlowdown(Vec3d velocity, CallbackInfoReturnable<Vec3d> cir) {
        AbstractMinecartEntity minecartEntity = (AbstractMinecartEntity) (Object) this;
        // Apply the drag force to the velocity
        double newSpeed = Config.getNewSpeed(velocity.length() * 20) / 20.0;
        Vec3d vec3d = velocity.normalize().multiply(newSpeed);
        if (minecartEntity.isTouchingWater()) {
            vec3d = vec3d.multiply(0.949999988079071);
        }
        cir.setReturnValue(vec3d);
    }
}
