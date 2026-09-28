package dev.airun.golemysmlink.client;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import dev.airun.golemysmlink.common.ModelSelection;
import dev.xkmc.modulargolems.content.entity.humanoid.HumanoidGolemEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class MaidRenderProxy extends EntityMaid {
    private final SwingClock swingClock = new SwingClock();
    private ModelSelection selection = ModelSelection.NONE;
    private boolean using, blocking;
    private InteractionHand useHand = InteractionHand.MAIN_HAND;
    private ItemStack useStack = ItemStack.EMPTY;
    private int useRemaining, useElapsed;

    public MaidRenderProxy(Level level) {
        super(level);
        setNoAi(true);
        setSilent(true);
        setIsYsmModel(true);
    }

    public void project(HumanoidGolemEntity golem, ModelSelection selected) {
        if (!selection.equals(selected)) {
            selection = selected;
            setYsmModel(selected.modelId(), selected.textureId(), Component.literal(selected.modelId()));
        }
        setIsYsmModel(true);
        setPos(golem.position());
        xo = golem.xo; yo = golem.yo; zo = golem.zo;
        xOld = golem.xOld; yOld = golem.yOld; zOld = golem.zOld;
        setYRot(golem.getYRot()); setXRot(golem.getXRot());
        yRotO = golem.yRotO; xRotO = golem.xRotO;
        yHeadRot = golem.yHeadRot; yHeadRotO = golem.yHeadRotO;
        yBodyRot = golem.yBodyRot; yBodyRotO = golem.yBodyRotO;
        tickCount = golem.tickCount;
        setDeltaMovement(golem.getDeltaMovement());
        setOnGround(golem.onGround());
        setPose(golem.getPose());
        setSprinting(golem.isSprinting());
        setShiftKeyDown(golem.isShiftKeyDown());
        setSwimming(golem.isSwimming());
        setInvisible(golem.isInvisible());
        setGlowingTag(golem.isCurrentlyGlowing());
        setCustomName(golem.getCustomName());
        setCustomNameVisible(golem.isCustomNameVisible());
        setHealth(getMaxHealth() * golem.getHealth() / Math.max(1, golem.getMaxHealth()));
        hurtTime = golem.hurtTime; deathTime = golem.deathTime;
        setAggressive(golem.isAggressive());
        setSwingingArms(golem.isAggressive());
        setLeftHanded(golem.getMainArm() == HumanoidArm.LEFT);
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack item = golem.getItemBySlot(slot);
            if (!ItemStack.matches(getItemBySlot(slot), item)) setItemSlot(slot, item.copy());
        }
        handItemsForAnimation[0] = getMainHandItem();
        handItemsForAnimation[1] = getOffhandItem();
        swinging = golem.swinging;
        swingingArm = golem.swingingArm;
        swingTime = swingClock.sample(golem.swinging, golem.swingTime, golem.tickCount,
                golem.swingingArm == InteractionHand.OFF_HAND);
        attackAnim = golem.attackAnim; oAttackAnim = golem.oAttackAnim;
        walkAnimation.setSpeed(golem.walkAnimation.speed(0));
        walkAnimation.update(golem.walkAnimation.position() - walkAnimation.position(), 1);
        walkAnimation.setSpeed(golem.walkAnimation.speed());
        blocking = golem.isBlocking() && !swinging && attackAnim <= 0 && oAttackAnim <= 0;
        using = golem.isUsingItem() || blocking;
        useHand = blocking && golem.shieldSlot() != null ? golem.shieldSlot() : golem.getUsedItemHand();
        useStack = using ? getItemInHand(useHand) : ItemStack.EMPTY;
        useElapsed = blocking ? Math.max(5, golem.getTicksUsingItem()) : golem.getTicksUsingItem();
        useRemaining = blocking ? Math.max(1, useStack.getUseDuration() - useElapsed) : golem.getUseItemRemainingTicks();
        setAiming(using && !blocking);
    }

    public void stabilizePreviewRotation() {
        yRotO = getYRot(); xRotO = getXRot();
        yHeadRotO = yHeadRot; yBodyRotO = yBodyRot;
    }

    @Override public boolean isUsingItem() { return using; }
    @Override public boolean isBlocking() { return blocking; }
    @Override public InteractionHand getUsedItemHand() { return useHand == null ? InteractionHand.MAIN_HAND : useHand; }
    @Override public ItemStack getUseItem() { return useStack == null ? ItemStack.EMPTY : useStack; }
    @Override public int getUseItemRemainingTicks() { return useRemaining; }
    @Override public int getTicksUsingItem() { return useElapsed; }
    @Override public void tick() {}

    public void release() {
        invalidateCaps();
        setRemoved(RemovalReason.DISCARDED);
    }
}
