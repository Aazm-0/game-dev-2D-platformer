package game2Dupdates.entitites;

import game2D.Animation;

public class Projectile extends Entity {

    public enum MovementType {
        STRAIGHT,
        ARC,
        HOMING
    }

    private MovementType movementType;
    private long expiryTimer;
    private float speed;

    // For homing we will need reference to entity
    private Entity target;


    public Projectile(Animation anim,long maxExpiryTime, float spawnX, float spawnY, boolean facingRight,
                      float speed, MovementType movementType) {
        super(anim);
        this.movementType = movementType;

        this.expiryTimer = maxExpiryTime;
        this.speed = speed;

        // Collision hitbox
        collisionOffsetX = 2;
        collisionOffsetY = 2;
        collisionWidth = getWidth() - (collisionOffsetX * 2);
        collisionHeight = getHeight() - collisionOffsetY;

        setPosition(spawnX, spawnY);

        setVelocityX(facingRight ? speed : -speed);

        show();
    }

    public void update(long elapsed) {
        if (!isAlive) return; // Remove update functionality after hit

        expiryTimer -= elapsed;
        if (expiryTimer <= 0) {
            isAlive = false;
            expiryTimer = 0;
            return;
        }

        switch (movementType) {
            case STRAIGHT:
                break;
            case ARC:
                break;
            case HOMING:
                break;
        }

        super.update(elapsed);
    }

    public void onHit(){
        isAlive = false;
    }

    public void setTarget(Entity target) {
        this.target = target;
    }

    public boolean getIsExpired(){
        return !isAlive;
    }

}
