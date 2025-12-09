package com.astral.combat;

import com.astral.components.*;
import com.astral.ecs.Entity;
import com.astral.ecs.GameSystem;
import com.astral.ecs.World;
import com.astral.systems.PhysicsSystem;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.Pool;

/**
 * Space combat system - handles weapons, projectiles, and damage
 */
public class SpaceCombatSystem extends GameSystem {

    private final ObjectMap<Integer, Projectile> activeProjectiles = new ObjectMap<>();
    private final Pool<Projectile> projectilePool;
    private PhysicsSystem physicsSystem;

    private int nextProjectileId = 0;

    public SpaceCombatSystem(World world) {
        super(world);
        setPriority(60);

        projectilePool = new Pool<Projectile>(500) {
            @Override
            protected Projectile newObject() {
                return new Projectile();
            }
        };
    }

    public void setPhysicsSystem(PhysicsSystem physicsSystem) {
        this.physicsSystem = physicsSystem;
    }

    @Override
    public void update(float deltaTime) {
        // Process weapon fire requests
        processWeaponFire();

        // Update projectiles
        updateProjectiles(deltaTime);
    }

    private void processWeaponFire() {
        Array<Entity> ships = getEntitiesWith(ShipComponent.class, WeaponComponent.class, TransformComponent.class);

        for (Entity entity : ships) {
            WeaponComponent weapons = entity.get(WeaponComponent.class);
            TransformComponent transform = entity.get(TransformComponent.class);

            for (int i = 0; i < weapons.weaponCount; i++) {
                WeaponMount mount = weapons.mounts[i];
                if (mount == null) continue;

                if (mount.isFiring && mount.canFire()) {
                    fireWeapon(entity, mount, transform);
                    mount.lastFireTime = System.currentTimeMillis();
                }
            }
        }
    }

    private void fireWeapon(Entity owner, WeaponMount mount, TransformComponent transform) {
        WeaponData weapon = mount.weaponData;
        if (weapon == null) return;

        Projectile proj = projectilePool.obtain();
        proj.id = nextProjectileId++;
        proj.ownerId = owner.getId();
        proj.damage = weapon.damage;
        proj.speed = weapon.projectileSpeed;
        proj.lifetime = weapon.range / weapon.projectileSpeed;
        proj.damageType = weapon.damageType;

        // Calculate muzzle position
        Vector3 muzzleOffset = mount.localPosition.cpy().mul(transform.rotation);
        proj.position.set(transform.position).add(muzzleOffset);
        proj.previousPosition.set(proj.position);

        // Calculate direction
        Vector3 direction = new Vector3(0, 0, -1).mul(transform.rotation);

        // Add spread if applicable
        if (weapon.spread > 0) {
            float spreadX = (float) (Math.random() - 0.5) * weapon.spread * 2;
            float spreadY = (float) (Math.random() - 0.5) * weapon.spread * 2;
            direction.rotate(Vector3.X, spreadX);
            direction.rotate(Vector3.Y, spreadY);
        }

        proj.velocity.set(direction).scl(proj.speed);

        // Add owner's velocity for realistic projectile physics
        RigidBodyComponent rb = owner.get(RigidBodyComponent.class);
        if (rb != null) {
            proj.velocity.add(rb.getLinearVelocity());
        }

        activeProjectiles.put(proj.id, proj);

        // TODO: Spawn muzzle flash effect
        // TODO: Play weapon sound
    }

    private void updateProjectiles(float deltaTime) {
        Array<Integer> toRemove = new Array<>();

        for (ObjectMap.Entry<Integer, Projectile> entry : activeProjectiles) {
            Projectile proj = entry.value;

            // Store previous position for raycast
            proj.previousPosition.set(proj.position);

            // Update position
            proj.position.add(proj.velocity.x * deltaTime, proj.velocity.y * deltaTime, proj.velocity.z * deltaTime);

            // Update lifetime
            proj.lifetime -= deltaTime;
            if (proj.lifetime <= 0) {
                toRemove.add(entry.key);
                continue;
            }

            // Raycast for collision
            if (physicsSystem != null) {
                PhysicsSystem.RaycastResult hit = physicsSystem.raycast(
                        proj.previousPosition,
                        proj.position,
                        (short) 0xFFFF // All collision groups
                );

                if (hit != null) {
                    // Find entity from collision object
                    Entity hitEntity = findEntityFromCollision(hit);

                    if (hitEntity != null && hitEntity.getId() != proj.ownerId) {
                        applyDamage(hitEntity, proj, hit.point);
                        toRemove.add(entry.key);
                        // TODO: Spawn impact effect
                    }
                }
            }
        }

        // Clean up expired/hit projectiles
        for (int id : toRemove) {
            Projectile proj = activeProjectiles.remove(id);
            if (proj != null) {
                projectilePool.free(proj);
            }
        }
    }

    private Entity findEntityFromCollision(PhysicsSystem.RaycastResult hit) {
        // TODO: Implement entity lookup from collision object
        // This requires storing entity reference in collision object user data
        return null;
    }

    private void applyDamage(Entity target, Projectile proj, Vector3 hitPoint) {
        ShipComponent ship = target.get(ShipComponent.class);
        if (ship != null) {
            ship.damage(proj.damage);

            if (ship.isDestroyed()) {
                onShipDestroyed(target, proj.ownerId);
            }

            // Post damage event
            world.getEventBus().post(new DamageEvent(target.getId(), proj.ownerId, proj.damage, hitPoint));
        }

        PlayerComponent player = target.get(PlayerComponent.class);
        if (player != null && ship == null) {
            player.damage(proj.damage);

            if (player.isDead()) {
                onPlayerDead(target);
            }
        }
    }

    private void onShipDestroyed(Entity ship, int killerId) {
        // TODO: Spawn explosion
        // TODO: Drop loot
        // TODO: Award XP to killer
        ship.setActive(false);
        world.destroyEntity(ship);
    }

    private void onPlayerDead(Entity player) {
        PlayerComponent pc = player.get(PlayerComponent.class);
        pc.state = PlayerComponent.PlayerState.DEAD;
        // TODO: Respawn logic
    }

    public int getActiveProjectileCount() {
        return activeProjectiles.size;
    }

    // Events
    public static class DamageEvent {
        public final int targetId;
        public final int sourceId;
        public final float damage;
        public final Vector3 hitPoint;

        public DamageEvent(int targetId, int sourceId, float damage, Vector3 hitPoint) {
            this.targetId = targetId;
            this.sourceId = sourceId;
            this.damage = damage;
            this.hitPoint = hitPoint;
        }
    }
}
