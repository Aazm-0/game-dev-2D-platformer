package game2Dupdates.entitites;

import game2D.Animation;

public class PatrolEnemy extends Entity {


    // Enemy drawing statistics
    private int visualOffsetY;



    // Enemy physics constants
    // Individual physics will make enemies more flexible
    private float patrolSpeed;
    private float gravity;

    private int scale;

    // Animations
    private Animation run;
    private Animation death;



    private boolean facingRight = false;
    private boolean isGrounded = false;

    /**
     * Creates a patrol enemy laods and loads it in
     * Yeah i should probably seperate spawn logic from constructor make it in abstract class
     * but ehh
     *
     * @param scale      scale to show position of player
     * @param tileWidth  width of tile in game
     * @param tileHeight height of tile in game
     */
    public PatrolEnemy(int scale, int tileWidth, int tileHeight, int spawnTileX, int spawnTileY) {
        super(new Animation());

        this.scale = scale;

        // Health Stats
        this.maxHealth = 2;
        this.health = maxHealth;

        gravity = 0.00005f;
        patrolSpeed = 0.05f * scale;

        loadAnimation();
        setScale(-scale, scale);

        // boxing
        collisionOffsetX = 8 * scale;
        collisionOffsetY = 0;
        collisionWidth = getWidth() - (collisionOffsetX * 2);
        collisionHeight = getHeight() - collisionOffsetY;
        visualOffsetY = 10 * scale;

        //  Spawn
        setPosition(
                spawnTileX * tileWidth,
                spawnTileY * tileHeight - getHeight()
        );

        show();
    }

    public void loadAnimation() {
        run = new Animation();
        run.loadAnimationSeries("entities/enemies/burning-ghoul_uniform.png", 16, 1, 100, 1, 12);

        death = new Animation();
        death.loadAnimationFromSheet("entities/fx/death_anim_uniform.png",5,1,100);

        setAnimation(run);
    }

    public void update(long elapsedTime) {

        // Just completes the death animation
        if(isDying){
            super.update(elapsedTime);
            return;
        }


        // Apply gravity
        setVelocityY(getVelocityY() + (gravity * elapsedTime));

        //For falling mark as not grounded
        if (getVelocityY() > 0.05f) {
            isGrounded = false;
        }

        if(iFrameTimer > 0) {
//            System.out.println("The invinicibility frames are" + iFrameTimer);
            iFrameTimer -= elapsedTime;
            if(iFrameTimer <= 0) iFrameTimer = 0;
        };


        // Logic for patrolling is not much of an issue when velocity set
        // to 0 by collision detection just reverse direction
        if (getVelocityX() == 0 && isGrounded){
            facingRight = !facingRight;
        }

        // Yeah i know this is a cheat fix i dont want ot add another flag to
        // account for first frame touching ground and changing direction of player
        if (facingRight){
            setVelocityX(patrolSpeed);
            setScale(-scale,scale);
        } else {
            setVelocityX(-patrolSpeed);
            setScale(scale,scale);
        }

        super.update(elapsedTime);
    }

    public void land() {
        isGrounded = true;
        setVelocityY(0);
    }

    public void die(){
        super.die();
        setAnimation(death);
    }

    public boolean isDeathAnimationFinished(){
        return isDying && getAnimation().hasLooped();
    }

}
