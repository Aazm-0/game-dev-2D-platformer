package game2Dupdates.entitites;

import game2D.Animation;
import game2Dupdates.entitites.sound.SoundManager;

import java.awt.*;
import java.awt.event.KeyEvent;

public class Player extends Entity {

    // Physics constants + Other player constants
    // wont keep the physics constant as maybe difficulty settings or powerups
    private float gravity = 0.00005f * 2;
    private float movement_velocity_x = 0.1f;
    private float jump_force = -0.08f;
    private int spawnXBlocks = 6;
    private int spawnYBlocks = 0;

    private final int scale;

    // Visual offest of y so it looks like the player is in the tile
    private int visualOffsetY = 20;

    // Player state flags
    private boolean moveRight = false;
    private boolean moveLeft = false;
    private boolean jumping = false;

    // direction face for inversion of image
    private boolean facingLeft = false;
    // we also need is grounded because the jumping animation is physics based but at to y velocity 0 we
    // could check it through if player is in air and then check the velocity
    private boolean isGrounded = false;
    private boolean hasAttackedInAir = false;

    //Damage values
    private boolean isHurt = false; // Toggler after duration
    private float knockBack; // Movemenet after interaction
    private boolean isInvincible = false;

    //Attack values
    private boolean isAttacking = false;
    private long attackTimer = 0;
    private long attackDuration = 400; // Again duration toggles and timers needed for updates
    private int attackReach; // This we will use to make a new attack box


    // Player animations
    private Animation idle;
    private Animation run;
    private Animation jump;
    private Animation hurt;
    private Animation fall;
    private Animation attack;

    /**
     * Creates a new Sprite object with the specified Animation.
     *
     * @param scale The scale multiplier of the player class
     *
     */
    public Player(int scale) {
        super(new Animation());
        this.scale = scale;

        // Physics also scalled
        gravity = 0.00005f * 3;
        movement_velocity_x = 0.1f * scale;
        jump_force = -0.08f * scale;
        knockBack = 0.1f * scale;

        // Setting up the health of the player
        maxHealth = 5;
        health = maxHealth;
        iFrameDuration = 1500; // longer invinicibility than base

        loadAnimations();
        setScale(scale, scale);

        // Thank god i was getting width before loading animations....

        // loading in collision offsets for x the sprite is wider than the hit box
        // i adjusted the height using python script for the sprite sheet but
        // here ill manually offset the width cause it might be bettter for hitbox interactions
        // with enemies etc and also forgiving
        // moves it left and cuts it from both sides
        collisionOffsetX = 25 * scale;
        collisionWidth = getWidth() - (collisionOffsetX * 2);
        collisionOffsetY = 10 * scale;
        // Cut only from tup
        collisionHeight = getHeight() - collisionOffsetY;

        attackReach = 30 * scale;
    }

    /**
     * Load animation from sprite sheet and assign to the one needed
     */
    public void loadAnimations() {
        String playerAnimationFolder = "entities/player/player_uniform.png";
        int spriteSheetCol = 13;
        int spriteSheetRow = 3;

        idle = new Animation();
        idle.loadAnimationSeries(playerAnimationFolder, spriteSheetCol, spriteSheetRow, 100, 20, 3);

        run = new Animation();
        run.loadAnimationSeries(playerAnimationFolder, spriteSheetCol, spriteSheetRow, 100, 30, 5);

        jump = new Animation();
        jump.loadAnimationSeries(playerAnimationFolder, spriteSheetCol, spriteSheetRow, 100, 0, 2);

        fall = new Animation();
        fall.loadAnimationSeries(playerAnimationFolder, spriteSheetCol, spriteSheetRow, 100, 2, 2);

        hurt = new Animation();
        hurt.loadAnimationSeries(playerAnimationFolder, spriteSheetCol, spriteSheetRow, 100, 18, 2);

        // Duration of animation may be more than the attack retracting attack is going to be considered
        attack = new Animation();
        attack.loadAnimationSeries(playerAnimationFolder, spriteSheetCol, spriteSheetRow, 100, 25, 6);

        // Setting the initial animation after loading
        setAnimation(idle);
    }


    /**
     * Update player states,physics and animation
     * <p>
     *
     * @param elapsed time elapsed in the game loop
     */
    public void update(long elapsed) {
//        System.out.println("player health : "+health);
        // Applying gravity so it works on every tick
        setVelocityY(getVelocityY() + (gravity * elapsed));

        // This is the physics check for not being grounded
        if (Math.abs(getVelocityY()) > 0.05f) {
            isGrounded = false;
        }

        // Bug fix if player walks of ledge set jumping to true so he cant jump in air
        if (!isGrounded && !jumping) {
            jumping = true;
        }

        // Handle diminishing of Iframecounter
        if (iFrameTimer > 0) {
            iFrameTimer -= elapsed;
            if (iFrameTimer <= 0) {
                iFrameTimer = 0;
                isHurt = false;  //Not here since jumping animation takes over move it to landing logic timer
//                System.out.println("Update aniamtion turned to false");
            }
        }

        // Handle diminishing of atttack counter
        if (attackTimer > 0) {
            attackTimer -= elapsed;
            if (attackTimer <= 0) {
                attackTimer = 0;
                isAttacking = false;
            }
        }

        if (isHurt) {
        } // This will let the hurt animation play out
        // Handle horizontal movement
        else if (moveRight) {
            setVelocityX(movement_velocity_x);
            facingLeft = false;
            setScale(scale, scale);
        } else if (moveLeft) {
            setVelocityX(-movement_velocity_x);
            setScale(-scale, scale); // This will invert the image
            // The set scale method is set up where the scale will invert but also add the width teleporting the player render
            // We will change the draw logic in game panel
            facingLeft = true;
//            setScale(scale, scale);

        } else {
            setVelocityX(0);
        }

        // Changes animation check every tick
        updateAnimation();

        super.update(elapsed);

    }

    /**
     * Update animation based on current physics better than change using key movements
     */
    private void updateAnimation() {
//        System.out.println("isHurt: " + isHurt + " iFrameTimer: " + iFrameTimer);
        if (isHurt) setAnimation(hurt); // Highest prio
        else if (isAttacking) setAnimation(attack); // Second prio
            // If i dont cover 0 in jumping animation it will play idle at peak
        else if (jumping && getVelocityY() <= 0) setAnimation(jump); // -ve velocity moving upwards
        else if (jumping && getVelocityY() > 0.01f) setAnimation(fall); // +ve velocity moving downwards
        else if (isGrounded && (moveRight || moveLeft)) setAnimation(run); // Umm the flags
        else setAnimation(idle);

    }

    /**
     * Handling key presses passed over from Game class
     * for player
     *
     * @param key Passed over key code value
     */
    public void keyPressed(int key) {
        switch (key) {
            case KeyEvent.VK_A:
                moveLeft = true;
                break;
            case KeyEvent.VK_D:
                moveRight = true;
                break;
            case KeyEvent.VK_W:
                if (!jumping) { // prevents double jump
                    setVelocityY(jump_force);
                    SoundManager.getInstance().play("jump");
                    jumping = true;
                    isGrounded = false;
                }
                break;
            case KeyEvent.VK_J:
//                if (!isAttacking && isGrounded) {
                    if(!isAttacking && !hasAttackedInAir){
//                        System.out.println(SoundManager.getInstance().getSoundLib());
                        SoundManager.getInstance().play("attack",0.1f);
                    isAttacking = true;
                    attackTimer = attackDuration;
                    if(!isGrounded) hasAttackedInAir = true;
                }
                break;

        }
    }

    /**
     * Handle Key releases
     *
     * @param key key passed over
     */
    public void keyReleased(int key) {
        switch (key) {
            case KeyEvent.VK_A:
                moveLeft = false;
                break;
            case KeyEvent.VK_D:
                moveRight = false;
                break;
        }
    }

    /**
     * To set velocityY to 0 when player lands to prevent sinking
     */
    @Override
    public void land() {
        jumping = false;
        isGrounded = true;
//        isHurt = false;
        setVelocityY(0);
        hasAttackedInAir = false;
    }

    /**
     * Initializes player variables for its spawn
     * The spawn x and y are not passed here because i didnt want to import
     * getTile Size here wait i could just pass it through the methods and then
     * pass spawns here would be better
     * //     * @param spawnX The starting x position of player
     * //     * @param spawnY The starting y position of player
     *
     * @param tileWidth  The tile width
     * @param tileHeight The tile height
     * @param mapHeight  The whole maps height
     */
    public void initializePlayer(int tileWidth, int mapHeight, int tileHeight) {

        int groundY = (mapHeight - spawnYBlocks) * tileHeight;
        setPosition(spawnXBlocks * tileWidth, groundY - getHeight() - tileHeight);
        setVelocity(0, 0);
        setScale(scale, scale);
        show();

    }

    /**
     * Take hit is the method called when player collides with a sprite
     *
     * @param hitFromRight Direction collision happens needed for proper knockback
     * @param damage amount of damage taken do 1-5 cause player health 1-5 metrick
     */
    public void takeHit(boolean hitFromRight,int damage) {
        if (iFrameTimer > 0) return; // invincible
        if(isInvincible) return;

        SoundManager.getInstance().play("hurt");
        takeDamage(damage);
        iFrameTimer = iFrameDuration;
        isHurt = true;

        // Knock back
        float direction = hitFromRight ? -1 : 1;
        setVelocityX(knockBack * direction);
        setVelocityY(-0.05f * scale); // abit of movement upwards
    }

    /**
     * Creates an attack box using rectangle to the existing player to help with the
     * collision checks of the attack box with the other sprites boundry
     *
     * @return The attack box appended to hitbox of the player
     */

    public Rectangle getAttackBox() {
        if (!isAttacking) return null;

        int ax = facingLeft ? (int) getX() + collisionOffsetX - attackReach :
                (int) getX() + collisionOffsetX + collisionWidth;
        int ay = (int) getY() + collisionOffsetY;

        return new Rectangle(ax, ay, attackReach, collisionHeight);
    }

    /**
     * After health is reduced to 0 this method respawns player at spawn point
     * uses the initializePlayer inside it
     *
     * @param tileWidth  The tile width
     * @param tileHeight The tile height
     * @param mapHeight  The whole maps height
     */
    public void respawn(int tileWidth, int mapHeight, int tileHeight) {
        // Set states to default
        health = maxHealth;
        isAlive = true;
        isDying = false;
        isHurt = false;
        iFrameTimer = 0;
        jumping = false;
        isGrounded = false;
        setVelocity(0,0);
        initializePlayer(tileWidth, mapHeight, tileHeight);

    }

    public int getVisualOffsetY() {
        return visualOffsetY;
    }

    public void toggleInvincible(){
        this.isInvincible = !isInvincible;
    }
}
