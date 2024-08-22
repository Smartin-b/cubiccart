package smartin.cubiccarts.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.registry.tag.BlockTags;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractMinecartEntity.class)
public abstract class AbstractMinecartEntityMixin {


    @Inject(
            method = "getVelocityMultiplier",
            at = @At("HEAD"),
            cancellable = true,
            remap = true,
            require = -1)
    public void cubiccarts_adjustMinecartPushVelocity(CallbackInfoReturnable<Float> cir) {
        AbstractMinecartEntity entity = (AbstractMinecartEntity) (Object) this;
        BlockState blockState = entity.getWorld().getBlockState(entity.getBlockPos());
        if (blockState.isIn(BlockTags.RAILS)) {
            cir.setReturnValue(1.0F);
        }
    }
}
