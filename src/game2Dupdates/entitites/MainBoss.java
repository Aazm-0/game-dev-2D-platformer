package game2Dupdates.entitites;

import game2D.Animation;

import java.util.ArrayList;

public class MainBoss extends Entity {

    private int scale;

    // Its going to be phase based according to hp
    private int currentPhase = 1;

    private Animation flying;


    // Physics variables and movement variables
    private float movementSpeed = 0.03f;
    private float leftFloatBoundryTiles = 3;
    private float rightFloatBoundryTiles = 3;
    private boolean movingRight = true;
    private float floatY;


    //Attack stats
    private long attackCooldown = 0;
    private long attackInterval = 2000;
    private boolean shouldSpawnProjectiles = false;
    // This is for cycling between attacks
    private int attackIndex = 0;

    // For dashing towards the player
    private enum DashState {
        IDLE, WINDING, DASHING, RESET
    }

    private DashState dashState = DashState.IDLE;
    private long dashWindupTimer = 0;
    private long dashWindupDuration = 600;
    private long dashDuration = 0;
    private long maxDashDuration = 400;
    private float dashSpeed = 0.6f;
    private float dashTriggerRange = 300; // pixels player has to be at
    private float orignalX;

    public MainBoss(int scale, int tileWidth, int tileHeight, int spawnTileX, int spawnTileY) {
        super(new Animation());

        this.scale = scale;
        this.maxHealth = 20;
        this.health = maxHealth;

        movementSpeed = movementSpeed * scale;

        loadAnimation();
        setScale(scale, scale);

        collisionOffsetX = 10 * scale;
        collisionOffsetY = 50 * scale;
        collisionWidth = getWidth() - (collisionOffsetX * 2);
        collisionHeight = getHeight() - collisionOffsetY;

        floatY = spawnTileY * tileHeight;
        setPosition(spawnTileX * tileWidth, floatY);


        //Set boundry ranges
        float spawnTileXPixels = spawnTileX * tileWidth;
        leftFloatBoundryTiles = spawnTileXPixels - (tileWidth * leftFloatBoundryTiles);
        rightFloatBoundryTiles = spawnTileXPixels + (tileWidth * rightFloatBoundryTiles);

        show();
    }

    private void loadAnimation() {
        flying = new Animation();
        flying.loadAnimationFromSheet("entities/enemies/angel_uniform.png", 11, 1, 100);

        setAnimation(flying);

    }

    public void update(long elapsedTime, float playerX,float   playerY) {
        if (isDying) {
            super.update(elapsedTime);
            return;
        }

        if (iFrameTimer > 0) {
            iFrameTimer -= elapsedTime;
            if (iFrameTimer < 0) {
                iFrameTimer = 0;
            }
        }

        shouldSpawnProjectiles = false;


        attackCooldown += elapsedTime;
        if (attackCooldown > attackInterval) {
            attackCooldown = 0;
            double rand = Math.random();
            if (rand < 0.7) {
                shouldSpawnProjectiles = true;
                if (rand < 0.4) {
                    attackIndex = 1;
//                    System.out.println("Verttical projectile attack");
                } else {
                    attackIndex = 2;
//                    System.out.println("Splite projectile attack");
                }
            } else {
                triggerDash(playerX);
//                System.out.println("Dash attack");
            }
        }

        updateDash(elapsedTime, playerX,playerY);

        if (dashState == DashState.IDLE) {
            updateFloat(elapsedTime);
        }


        super.update(elapsedTime);
    }

    private void updateFloat(long elapsedTime) {
        if (movingRight) {
            setVelocityX(movementSpeed);
            setScale(scale, scale);
            if (getX() >= rightFloatBoundryTiles) movingRight = false;
        } else {
            setVelocityX(-movementSpeed);
            setScale(-scale, scale);
            if (getX() <= leftFloatBoundryTiles) movingRight = true;
        }

        // Lock y as float position
        setY(floatY);
        setVelocityY(0);
    }


    public Projectile spawnStraightShot(int scale) {
        Animation projAnim = new Animation();
        projAnim.loadAnimationFromSheet("entities/fx/fireball_uniform.png", 3, 1, 100);

        float spawnX = movingRight ? getX() + getWidth() : getX() - 20;
        float spawnY = getY() + getHeight() / 2f;

        return new Projectile(projAnim,
                3000,
                spawnX, spawnY, movingRight, 0.15f * scale, Projectile.MovementType.STRAIGHT);
    }

    // Three different y velocites x constant so increases y position linearly
    public ArrayList<Projectile> spawnScatterShot(int scale) {

        float spawnX = movingRight ? getX() + getWidth() : getX() - 20;
        float spawnY = getY() + getHeight() / 2f;

        float[] yVelocities = {0f, -0.08f * scale, 0.08f * scale};

        ArrayList<Projectile> projectiles = new ArrayList<>();
        for (float yVelocity : yVelocities) {
            Animation projAnim = new Animation();
            projAnim.loadAnimationFromSheet("entities/fx/fireball_uniform.png", 3, 1, 100);
            Projectile p = new Projectile(
                    projAnim,
                    3000,
                    spawnX, spawnY,
                    movingRight,
                    0.15f * scale, // X axis
                    Projectile.MovementType.STRAIGHT
            );

            p.setVelocityY(yVelocity);
            projectiles.add(p);
        }
        return projectiles;
    }

    private void triggerDash(float playerX) {
        if (dashState != DashState.IDLE) return;

        float distToPlayer = Math.abs(getX() - playerX);
        if (distToPlayer < dashTriggerRange && distToPlayer > 64) {
            dashState = DashState.WINDING;
            dashWindupTimer = dashWindupDuration; // Slow pause
            orignalX = getX();
            setVelocityX(0); // stop floating for abit
        }
    }

    private void updateDash(long elapsedTime, float playerX,float playerY) {
        switch (dashState) {
            case IDLE:
                break;
            case WINDING:
                dashWindupTimer -= elapsedTime;
                setVelocityX(0);
                if (dashWindupTimer <= 0) {
                    dashWindupTimer = 0;
                    dashState = DashState.DASHING;
                    dashDuration = maxDashDuration;

                    // Going to change from just x movement to bi directional movement
//                    boolean dashRight = playerX > getX();
//                    setVelocityX(dashRight ? +dashSpeed : -dashSpeed);

                    float dx = playerX - getX();
                    float dy = playerY - getY();

                    float length = (float)  Math.sqrt(dx * dx + dy * dy);

                    // Normalizing so speed depends more on our movement var
                    dx /= length;
                    dy /= length;

                    setVelocityX(dx * dashSpeed);
                    setVelocityY(dy * dashSpeed);
                }
                break;
            case DASHING:
                dashDuration -= elapsedTime;
                if (dashDuration <= 0) {
                    dashDuration = 0;
                    dashState = DashState.RESET;
                    setVelocityX(0);
                }
                break;
            case RESET:
                float returnSpeed = dashSpeed ;

                float dx = orignalX - getX();
                float dy = floatY - getY();

                float distance = (float) Math.sqrt(dx * dx + dy * dy);

                // when close snaps abit we prob wont see and stops
                if (Math.abs(distance) < 5.0f) {
                    setX(orignalX);
                    setVelocityX(0);
                    setY(floatY);
                    setVelocityY(0);
                    dashState = DashState.IDLE;
                    break;
                }

//                // Moves back until close to orignal
//                if (distance > 0) {
//                    setVelocityX(returnSpeed);
//                } else {
//                    setVelocityX(-returnSpeed);
//                }

                dx /= distance;
                dy /= distance;

                float moveX = dx * returnSpeed;
                float moveY = dy * returnSpeed;

                if (Math.abs(moveX) > Math.abs(orignalX - getX())) {
                    moveX = orignalX - getX();
                }

                if (Math.abs(moveY) > Math.abs(floatY - getY())) {
                    moveY = floatY - getY();
                }

                setVelocityX(moveX);
                setVelocityY(moveY);
                break;

        }
    }

    private void updatePhase() {
        float healthPercent = (float) health / maxHealth;
        if (healthPercent < 0.35f) currentPhase = 2;
    }


    public void die() {
        super.die();
    }

    public boolean isDeathAnimationFinished() {
        return isDying && getAnimation().hasLooped();
    }

    public boolean isShouldSpawnProjectiles() {
        return shouldSpawnProjectiles;
    }

    public int getAttackIndex() {
        return attackIndex;
    }

    public int getCurrentPhase() {
        return currentPhase;
    }

    public float getFloatY() {
        return floatY;
    }
}
