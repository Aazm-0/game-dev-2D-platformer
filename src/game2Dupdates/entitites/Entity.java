package game2Dupdates.entitites;

import game2D.Animation;
import game2D.Sprite;
import game2Dupdates.entitites.sound.SoundManager;

public abstract class Entity extends Sprite {
    // Entity Bordering Values
    protected int collisionOffsetX = 0;
    protected int collisionOffsetY = 0;
    protected int collisionWidth;
    protected int collisionHeight;



    //Entity Stats
    protected int health;
    protected int maxHealth;

    //EntityStates
    protected boolean isAlive = true;
    protected boolean isDying = false;


    // Invinicibility frames
    protected long iFrameTimer = 0;
    protected long iFrameDuration = 500;

    public Entity(Animation anim) {
        super(anim);
    }

    public void land() {}

    /**
     * For changing max health on each damage take
     * @param dmg the amount of damage taken
     */
    public void takeDamage(int dmg){
        if(iFrameTimer > 0) return; // Dont take damage in invibility frame

        SoundManager.getInstance().play("hurt");
        health  -= dmg;
        iFrameTimer = iFrameDuration;
        if(health <= 0){
            die();
        }
    }

    /**
     * State change for player to be alive or not
     */
    public void die(){
        isAlive = false;
        isDying = true;
        SoundManager.getInstance().play("kill");
        setVelocity(0,0);
    }

    public int getMaxHealth(){return maxHealth;}
    public int getHealth(){return health;}
    public boolean getIsAlive(){return isAlive;}
    public boolean getisDying(){return isDying;}
    public int getCollisionOffsetX() { return collisionOffsetX; }
    public int getCollisionOffsetY() { return collisionOffsetY; }
    public int getCollisionWidth()   { return collisionWidth; }
    public int getCollisionHeight()  { return collisionHeight; }
}
