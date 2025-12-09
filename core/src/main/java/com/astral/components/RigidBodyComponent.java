package com.astral.components;

import com.astral.ecs.Component;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.collision.btCollisionShape;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;

/**
 * Physics rigid body component
 */
public class RigidBodyComponent implements Component {

    public btRigidBody body;
    public btCollisionShape shape;
    public float mass = 1f;
    public short collisionGroup = 1;
    public short collisionMask = -1;
    public boolean kinematic = false;
    public boolean dynamic = true;
    public boolean syncRotationFromPhysics = true; // Set false for player-controlled ships

    // Cached velocity for networking
    public final Vector3 velocity = new Vector3();
    public final Vector3 angularVelocity = new Vector3();

    public void applyForce(Vector3 force) {
        if (body != null) {
            body.applyCentralForce(force);
        }
    }

    public void applyImpulse(Vector3 impulse) {
        if (body != null) {
            body.applyCentralImpulse(impulse);
        }
    }

    public void applyTorque(Vector3 torque) {
        if (body != null) {
            body.applyTorque(torque);
        }
    }

    public void setLinearVelocity(Vector3 vel) {
        if (body != null) {
            body.setLinearVelocity(vel);
        }
        velocity.set(vel);
    }

    public Vector3 getLinearVelocity() {
        if (body != null) {
            velocity.set(body.getLinearVelocity());
        }
        return velocity;
    }

    public void setAngularVelocity(Vector3 vel) {
        if (body != null) {
            body.setAngularVelocity(vel);
        }
        angularVelocity.set(vel);
    }

    public void setDamping(float linear, float angular) {
        if (body != null) {
            body.setDamping(linear, angular);
        }
    }

    @Override
    public void reset() {
        body = null;
        shape = null;
        mass = 1f;
        collisionGroup = 1;
        collisionMask = -1;
        kinematic = false;
        dynamic = true;
        velocity.setZero();
        angularVelocity.setZero();
    }
}
