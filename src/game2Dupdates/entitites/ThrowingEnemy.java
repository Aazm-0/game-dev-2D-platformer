package game2Dupdates.entitites;

import game2D.Animation;

public class ThrowingEnemy extends Entity {

    private int scale;

    private float gravity;

    // Animations only three
    private Animation idle;
    private Animation throwing;
    private Animation death;

    // States
    private boolean isThrowing = false;
    private boolean facingRight;
    private boolean isGrounded = false;

    //Throwing data
    private long throwTimer = 0;
    private long throwInterval = 2000;
    private long throwAnimInterval = 900; //9 100 sec frames i know hardcoded
    private long throwAnimTimer = 0;
    private boolean shouldSpawnProjectile = false;


    public ThrowingEnemy(int scale, int tileWidth, int tileHeight, int spawnTileX, int spawnTileY, boolean facingRight) {
        super(new Animation());

        this.scale = scale;

        this.maxHealth = 3;
        this.health = maxHealth;
        this.facingRight = facingRight;
        gravity = 0.00005f;

        loadAnimation();

        setScale(facingRight ? -scale : scale, scale);

        //Hitbox
        collisionOffsetX = 8 * scale;
        collisionOffsetY = 4 * scale;
        collisionWidth = getWidth() - (collisionOffsetX * 2);
        collisionHeight = getHeight() - collisionOffsetY;

        setPosition(spawnTileX * tileWidth, spawnTileY * tileHeight - collisionHeight);

        show();

    }


    public void loadAnimation() {
        idle = new Animation();
        idle.loadAnimationSeries("entities/enemies/wizard_uniform.png", 15, 1, 100, 1, 6);

        throwing = new Animation();
        throwing.loadAnimationSeries("entities/enemies/wizard_uniform.png", 15, 1, 100, 7, 9);


        death = new Animation();
        death.loadAnimationFromSheet("entities/fx/death_anim_uniform.png", 5, 1, 100);

        setAnimation(idle);

    }

    public void update(long elapsed) {
        if (isDying) {
            super.update(elapsed);
            return;
        }

        // Giving gravity
        setVelocityY(getVelocityY() + (gravity * elapsed));
        if (getVelocityY() > 0.05f) isGrounded = false;

        if (iFrameTimer > 0) {
            iFrameTimer -= elapsed;
            if (iFrameTimer < 0) iFrameTimer = 0;
        }

        // Reset shouldfireProjectile each frame so we dont spawn at every frame
        // if throwInterval passed will change state in same frame

        shouldSpawnProjectile = false;

        throwTimer += elapsed;
        if (throwTimer >= throwInterval) {
            throwTimer = 0;
            isThrowing = true;
            throwAnimTimer = throwAnimInterval;
            shouldSpawnProjectile = true;
        }

        if (throwAnimTimer > 0) {
            throwAnimTimer -= elapsed;
            if (throwAnimTimer <= 0) {
                throwAnimTimer = 0;
                isThrowing = false;
            }
        }


        updateAnimation();

        super.update(elapsed);
    }

    private void updateAnimation() {
        if (isThrowing) setAnimation(throwing);
        else setAnimation(idle);
    }

    public void land() {
        isGrounded = true;
        setVelocityY(0);
    }

    public void die() {
        setAnimation(death);
        super.die();
    }

    public void startThrowing() {
        isThrowing = true;
    }

    public void stopThrowing() {
        isThrowing = false;
    }

    public boolean isDeathAnimationFinished() {
        return isDying && getAnimation().hasLooped();
    }

    public boolean isShouldSpawnProjectile(){
        return shouldSpawnProjectile;
    }

    public Projectile spawnProjectile(int scale) {
        Animation projAnim = new Animation();
        projAnim.loadAnimationFromSheet("entities/fx/fireball_uniform.png", 3, 1, 100);
        float spawnX = facingRight ?
                getX() + getWidth() :
                getX() - 20;
        float spawnY = getY() + getHeight() / 2f;

        return new Projectile(
                projAnim,
                1500,
                spawnX, spawnY,
                facingRight,
                0.1f * scale,
                Projectile.MovementType.STRAIGHT
        );
    }
}
