package net.citizensnpcs.nms.v1_13_R2.entity;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_13_R2.CraftServer;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftDolphin;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftEntity;
import org.bukkit.entity.Dolphin;
import org.bukkit.util.Vector;

import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.nms.v1_13_R2.util.NMSBoundingBox;
import net.citizensnpcs.nms.v1_13_R2.util.NMSImpl;
import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.ai.NPCHolder;
import net.citizensnpcs.util.NMS;
import net.citizensnpcs.util.Util;
import net.minecraft.server.v1_13_R2.AxisAlignedBB;
import net.minecraft.server.v1_13_R2.BlockPosition;
import net.minecraft.server.v1_13_R2.DamageSource;
import net.minecraft.server.v1_13_R2.Entity;
import net.minecraft.server.v1_13_R2.EntityBoat;
import net.minecraft.server.v1_13_R2.EntityDolphin;
import net.minecraft.server.v1_13_R2.EntityMinecartAbstract;
import net.minecraft.server.v1_13_R2.FluidType;
import net.minecraft.server.v1_13_R2.GenericAttributes;
import net.minecraft.server.v1_13_R2.IBlockData;
import net.minecraft.server.v1_13_R2.NBTTagCompound;
import net.minecraft.server.v1_13_R2.SoundEffect;
import net.minecraft.server.v1_13_R2.Tag;
import net.minecraft.server.v1_13_R2.World;

public class DolphinController extends MobEntityController {
    public DolphinController() {
        super(EntityDolphinNPC.class);
    }

    @Override
    public Dolphin getBukkitEntity() {
        return (Dolphin) super.getBukkitEntity();
    }

    public static class DolphinNPC extends CraftDolphin implements NPCHolder {
        private final CitizensNPC npc;

        public DolphinNPC(EntityDolphinNPC entity) {
            super((CraftServer) Bukkit.getServer(), entity);
            this.npc = entity.npc;
        }

        @Override
        public NPC getNPC() {
            return npc;
        }
    }

    public static class EntityDolphinNPC extends EntityDolphin implements NPCHolder {
        private boolean inProtectedTick;

        private final CitizensNPC npc;

        public EntityDolphinNPC(World world) {
            this(world, null);
        }

        public EntityDolphinNPC(World world, NPC npc) {
            super(world);
            this.npc = (CitizensNPC) npc;
            if (npc != null) {
                this.setNoAI(true);
                this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED)
                        .setValue(this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).getValue() / 10);
            }
        }

        @Override
        public void a(AxisAlignedBB bb) {
            super.a(NMSBoundingBox.makeBB(npc, bb));
        }

        @Override
        protected void a(double d0, boolean flag, IBlockData block, BlockPosition blockposition) {
            if (npc == null || !npc.isFlyable()) {
                super.a(d0, flag, block, blockposition);
            }
        }

        @Override
        public void a(Entity entity, float strength, double dx, double dz) {
            NMS.callKnockbackEvent(npc, strength, dx, dz, (evt) -> super.a(entity, (float) evt.getStrength(),
                    evt.getKnockbackVector().getX(), evt.getKnockbackVector().getZ()));
        }

        @Override
        public void a(float f, float f1, float f2) {
            if (npc == null || !npc.isFlyable()) {
                if (!NMSImpl.moveFish(npc, this, f, f1, f2, cK())) {
                    super.a(f, f1, f2);
                }
            } else {
                NMSImpl.flyingMoveLogic(this, f, f1, f2);
            }
        }

        @Override
        public boolean ap() {
            return inProtectedTick ? true : super.ap();
        }

        @Override
        public boolean b(Tag<FluidType> tag) {
            return NMSImpl.fluidPush(npc, this, () -> super.b(tag));
        }

        @Override
        public int bn() {
            return NMS.getFallDistance(npc, super.bn());
        }

        @Override
        public void c(float f, float f1) {
            if (npc == null || !npc.isFlyable()) {
                super.c(f, f1);
            }
        }

        @Override
        public void collide(net.minecraft.server.v1_13_R2.Entity entity) {
            // this method is called by both the entities involved - cancelling
            // it will not stop the NPC from moving.
            super.collide(entity);
            if (npc != null)
                Util.callCollisionEvent(npc, entity.getBukkitEntity());
        }

        @Override
        protected SoundEffect cs() {
            return NMSImpl.getSoundEffect(npc, super.cs(), NPC.Metadata.DEATH_SOUND);
        }

        @Override
        protected SoundEffect d(DamageSource damagesource) {
            return NMSImpl.getSoundEffect(npc, super.d(damagesource), NPC.Metadata.HURT_SOUND);
        }

        @Override
        public boolean d(NBTTagCompound save) {
            return npc == null ? super.d(save) : false;
        }

        @Override
        protected SoundEffect D() {
            return NMSImpl.getSoundEffect(npc, super.D(), NPC.Metadata.AMBIENT_SOUND);
        }

        

        @Override
        public void f(double x, double y, double z) {
            Vector vector = Util.callPushEvent(npc, x, y, z);
            if (vector != null) {
                super.f(vector.getX(), vector.getY(), vector.getZ());
            }
        }

        @Override
        public CraftEntity getBukkitEntity() {
            if (npc != null && !(bukkitEntity instanceof NPCHolder))
                bukkitEntity = new DolphinNPC(this);
            return super.getBukkitEntity();
        }

        @Override
        public NPC getNPC() {
            return npc;
        }

        @Override
        protected void I() {
            if (npc == null) {
                super.I();
            }
        }

        @Override
        public boolean isLeashed() {
            return NMSImpl.isLeashed(npc, super::isLeashed, this);
        }

        @Override
        protected boolean n(Entity entity) {
            if (npc != null && (entity instanceof EntityBoat || entity instanceof EntityMinecartAbstract)) {
                return !npc.isProtected();
            }
            return super.n(entity);
        }

        @Override
        public void tick() {
            if (npc != null && npc.isProtected()) {
                inProtectedTick = true;
            }
            super.tick();
            inProtectedTick = false;
            if (npc != null) {
                npc.update();
            }
        }

        @Override
        public boolean z_() {
            if (npc == null || !npc.isFlyable()) {
                return super.z_();
            } else {
                return false;
            }
        }
    }
}