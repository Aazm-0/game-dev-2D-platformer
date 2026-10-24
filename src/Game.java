
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;


import game2D.*;
import game2Dupdates.entitites.*;
import game2Dupdates.entitites.sound.SoundEffects;
import game2Dupdates.entitites.sound.SoundManager;

// Game demonstrates how we can override the GameCore class
// to create our own 'game'. We usually need to implement at
// least 'draw' and 'update' (not including any local event handling)
// to begin the process. You should also add code to the 'init'
// method that will initialise event handlers etc. ddwddd

// Student ID: ???????


@SuppressWarnings("serial")


public class Game extends GameCore implements MouseListener, MouseMotionListener {
    // I wanted to load Start and instructions in a different Panel but this is a shortcut workaround

    // For screen division for this games
    private enum SCREEN {
        PLAYING,
        PAUSED,
        DEATH,
        START,
        INSTRUCTIONS,
        TRANSITION
    }

    SCREEN screenPhase = SCREEN.START;

    // Useful game constants
    static int scale = 2;


    static int screenWidth = 300 * scale;
    static int screenHeight = 224 * scale;


//    ArrayList<Sprite> 	clouds = new ArrayList<Sprite>();

    // Game state flags
    boolean debug = false;
    // I am going to have a singular sized level for that vertical scroll will be false
    boolean verticalScroll = false;
    Player player;
    TileMap dmap = new TileMap();
    TileMap tmap = new TileMap();    // Our tile map, note that we load it in init()
    ArrayList<Tile> collidedTiles = new ArrayList<Tile>();
    Image farBackground, midBackground;

    long total;                    // The score will be the total time elapsed since a crash

    ArrayList<PatrolEnemy> enemies = new ArrayList<>();
    ArrayList<ThrowingEnemy> throwEnemies = new ArrayList<>();
    ArrayList<MainBoss> bosses = new ArrayList<>();
    ArrayList<Projectile> projectiles = new ArrayList<>();


    // UI resources
    Image healthBarEmpty, healthBarFull;
    float displayedHealth; // We will use interploatation to reduce it slowly
    Image bossHealthBarEmpty, bossHealthBarFull;
    float displayBossHealth;
    Image pauseScreenOverlay;
    Image deathScreenOverlay;
    Image startScreen;
    Image instructionScreen;
    GameButton respawnButton, quitButton, startButton, backButton, instructionsButton;

    // Changing map
    private int currentMap = 1;

    // Transition transparency change attrivutes
    private float transitionAlpha = 0f;
    private boolean fadingOut = true;
    private boolean transitionTriggered = false;
    private long transitionSpeed = 3;

    /**
     * The obligatory main method that creates
     * an instance of our class and starts it running
     *
     * @param args The list of parameters this program might use (ignored)
     */
    public static void main(String[] args) {

        Game gct = new Game();
        gct.init();
        // Start in windowed mode with the given screen height and width
        gct.run(false, screenWidth, screenHeight);
    }

    /**
     * Initialise the class, e.g. set up variables, load images,
     * create animations, register event handlers.
     * <p>
     * This shows you the general principles but you should create specific
     * methods for setting up your game that can be called again when you wish to
     * restart the game (for example you may only want to load animations once
     * but you could reset the positions of sprites each time you restart the game).
     */
    public void init() {


        // Load the tile map and print it out so we can check it is valid
//        tmap.loadMap("maps/map", "basemapbeta1.txt");
        tmap.loadMap("maps/map", "basemapaplha2.txt");

        // Load the decorations for map
//        dmap.loadMap("maps/decor", "decorbasemapbeta.txt");
        dmap.loadMap("maps/decor", "decorbasemapalpha.txt");

        // Spawn enemies
        spawnEnemiesMap1();

        // Create a set of background sprites that we can
        // rearrange to give the illusion of motion
        // Why do we handle background images as sprites why not just load them as offsets
        // The far background is big in width so not fully utilized if i tweak the scrolling faster i dont want it faster than mid backgrounbd
        farBackground = loadImage("background/background.png");
        midBackground = loadImage("background/pillar_mid_background.png");

        // Load ui
        healthBarEmpty = loadImage("ui/healthbarempty.png");
        healthBarFull = loadImage("ui/healthbarfull.png");
        bossHealthBarEmpty = loadImage("ui/bosshealthempty.png");
        bossHealthBarFull = loadImage("ui/bosshealthfull.png");
        pauseScreenOverlay = loadImage("ui/PauseOverlay.png");
        deathScreenOverlay = loadImage("ui/DeathOverlay.png");

        //Screens
        instructionScreen = loadImage("background/instructions_background.png");
        startScreen = loadImage("background/title_screen_background.png");

        respawnButton = new GameButton("Respawn",
                screenWidth / 2 - 60,
                screenHeight / 2 + 60,
                120, 35, new Color(0, 0, 0), new Color(255, 255, 255), new Color(100, 16, 16));


        Color baseColor = new Color(176, 29, 59);
        Color howerColor = new Color(39, 38, 56);
        Color textColor = new Color(255, 187, 49);
        startButton = new GameButton("Start", screenWidth / 4 - 60, screenHeight / 2 + 110, 120, 35, baseColor, howerColor, textColor);
        quitButton = new GameButton("Quit", (screenWidth * 3) / 4 - 60, screenHeight / 2 + 110, 120, 35, baseColor, howerColor, textColor);
        instructionsButton = new GameButton("Instructions", screenWidth / 2 - 60, screenHeight / 2 + 110, 120, 35, baseColor, howerColor, textColor);
        backButton = new GameButton("Back", 20, 40, 80, 30, baseColor, howerColor, textColor);

        // Initializes all animations
        player = new Player(scale);

        displayedHealth = player.getMaxHealth();

        // Start playing the background music
        SoundManager.getInstance().playBackgroundMidi();

        addMouseListener(this);
        addMouseMotionListener(this);
        initialiseGame();

        // Setting up the Frame default configurations
        setSize(screenWidth, screenHeight);
        setResizable(false);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    /**
     * You will probably want to put code to restart a game in
     * a separate method so that you can call it when restarting
     * the game when the player loses.
     */
    public void initialiseGame() {
        total = 0;
        player.initializePlayer(tmap.getTileWidth(), tmap.getMapHeight(), tmap.getTileHeight());
    }

    /**
     * Draw the current state of the game. Note the sample use of
     * debugging output that is drawn directly to the game screen.
     */
    public void drawGame(Graphics2D g) {
        // Be careful about the order in which you draw objects - you
        // should draw the background first, then work your way 'forward'

        // First work out how much we need to shift the view in order to
        // see where the player is. To do this, we adjust the offset so that
        // it is relative to the player's position along with a shift
        int xo = -(int) player.getX() + screenWidth / 2;
        int yo = verticalScroll ? -(int) player.getY() + screenHeight / 2 : 0;

        // Setting the whole background to required color
        g.setColor(new Color(39, 38, 56));
        g.fillRect(0, 0, getWidth(), getHeight());

        // Adding the far background with a large offset
        drawParallaxLayer(g, farBackground, xo, 0.2f, 80);
        // Adding mid background with smaller speed of offset change
        drawParallaxLayer(g, midBackground, xo, 0.6f, 150);

        // draw the decor map
        dmap.draw(g, xo, yo);

        // Apply offsets to tile map and draw  it
        tmap.draw(g, xo, yo);


        // Draw patrol enemies
        for (PatrolEnemy e : enemies) {
            e.setOffsets(xo, yo);
            e.drawTransformed(g);

            if (debug) {
                g.setColor(Color.PINK);
                g.drawRect(
                        (int) e.getX() + xo + e.getCollisionOffsetX(),
                        (int) e.getY() + yo + e.getCollisionOffsetY(),
                        e.getCollisionWidth(),
                        e.getCollisionHeight()
                );
            }
        }

        for (ThrowingEnemy e : throwEnemies) {
            e.setOffsets(xo, yo);
            e.drawTransformed(g);

            if (debug) {
                g.setColor(Color.YELLOW);
                g.drawRect(
                        (int) e.getX() + xo + e.getCollisionOffsetX(),
                        (int) e.getY() + yo + e.getCollisionOffsetY(),
                        e.getCollisionWidth(),
                        e.getCollisionHeight()
                );
            }

        }

        for (Projectile e : projectiles) {
            e.setOffsets(xo, yo);
            e.drawTransformed(g);
            if (debug) {
                g.setColor(Color.PINK);
                g.drawRect(
                        (int) e.getX() + xo + e.getCollisionOffsetX(),
                        (int) e.getY() + yo + e.getCollisionOffsetY(),
                        e.getCollisionWidth(),
                        e.getCollisionHeight()
                );
            }
        }

        for (MainBoss b : bosses) {
            b.setOffsets(xo, yo);
            b.drawTransformed(g);

            if (debug) {
                g.setColor(Color.PINK);
                g.drawRect(
                        (int) b.getX() + xo + b.getCollisionOffsetX(),
                        (int) b.getY() + yo + b.getCollisionOffsetY(),
                        b.getCollisionWidth(),
                        b.getCollisionHeight()
                );
            }
        }

        // Apply offsets to player and draw
        player.setOffsets(xo, yo + player.getVisualOffsetY());


        player.drawTransformed(g);

        // Draw health bar ui
        drawHealthBar(g, 20, 40, healthBarEmpty, healthBarFull, player, displayedHealth);

//        System.out.println(bosses.getFirst().getX() - player.getX());
        float distance = !bosses.isEmpty() ? Math.abs(bosses.getFirst().getX() - player.getX()) : 9 * tmap.getTileWidth();
        if (distance <= 8 * tmap.getTileWidth()) {
//            System.out.println("Drew boss healthbar");
            drawHealthBar(g,
                    (screenWidth - bossHealthBarEmpty.getWidth(null)) / 2,
                    screenHeight - bossHealthBarEmpty.getHeight(null) - 20,
                    bossHealthBarEmpty,
                    bossHealthBarFull,
                    bosses.getFirst(), displayBossHealth

            );
        }

        // Show score and status information
        String msg = String.format("Score: %d", total / 100);
        g.setColor(Color.darkGray);
        g.drawString(msg, getWidth() - 100, 50);

        if (debug) {

            // When in debug mode, you could draw borders around objects
            // and write messages to the screen with useful information.
            // Try to avoid printing to the console since it will produce
            // a lot of output and slow down your game.
            tmap.drawBorder(g, xo, yo, Color.black);

            g.setColor(Color.red);
            g.drawRect((int) player.getX() + xo + player.getCollisionOffsetX(), (int) player.getY() + yo + player.getCollisionOffsetY(), player.getCollisionWidth(), player.getCollisionHeight());
            // The scaling of the image is not accounted for
//        	player.drawBoundingBox(g);


            g.drawString(String.format("Player: %.0f,%.0f", player.getX(), player.getY()),
                    getWidth() - 100, 70);

            drawCollidedTiles(g, tmap, xo, yo);

            // Drawing the attack box
            Rectangle attackBox = player.getAttackBox();
            if (attackBox != null) {
                g.setColor(Color.GREEN);
                g.drawRect(
                        attackBox.x + xo,
                        attackBox.y + yo,
                        attackBox.width,
                        attackBox.height
                );
            }
        }

    }

    public void drawCollidedTiles(Graphics2D g, TileMap map, int xOffset, int yOffset) {
        if (collidedTiles.size() > 0) {
            int tileWidth = map.getTileWidth();
            int tileHeight = map.getTileHeight();

            g.setColor(Color.blue);
            for (Tile t : collidedTiles) {
                g.drawRect(t.getXC() + xOffset, t.getYC() + yOffset, tileWidth, tileHeight);
            }
        }
    }

    public void draw(Graphics2D g) {
        switch (screenPhase) {
            case PLAYING:
                drawGame(g);
                break;
            case PAUSED:
                drawGame(g);
                drawPauseOverlay(g);
                break;
            case DEATH:
                drawGame(g);
                drawDeathOverlay(g);
                break;
            case START:
                drawStartScreen(g);
                break;
            case INSTRUCTIONS:
                drawInstructionsScreen(g);
                break;
            case TRANSITION:
                drawTransition(g);
                break;
        }
    }

    private void drawStartScreen(Graphics2D g) {
        g.drawImage(startScreen, 0, 0, screenWidth, screenHeight, null);
        startButton.draw(g);
        instructionsButton.draw(g);
        quitButton.draw(g);
    }

    private void drawInstructionsScreen(Graphics2D g) {
        g.drawImage(instructionScreen, 0, 0, screenWidth, screenHeight, null);
        backButton.draw(g);
    }

    public void drawPauseOverlay(Graphics2D g) {
        // A blur on existing drawn game
        g.setColor(new Color(0, 0, 0, 150));
        g.fillRect(0, 0, getWidth(), getHeight());

        // Draw the image
        int imgW = pauseScreenOverlay.getWidth(null);
        int imgH = pauseScreenOverlay.getHeight(null);
        int drawX = (getWidth() - imgW) / 2;
        int drawY = (getHeight() - imgH) / 2;
        g.drawImage(pauseScreenOverlay, drawX, drawY, imgW, imgH, null);

    }

    private void drawTransition(Graphics2D g) {
        // We draw the game but behind the black transition
        drawGame(g);
        g.setColor(new Color(0, 0, 0, (int) Math.min(255, transitionAlpha)));
        g.fillRect(0, 0, getWidth(), getHeight());
    }

    public void drawDeathOverlay(Graphics2D g) {
        // A blur on existing drawn game
        g.setColor(new Color(0, 0, 0, 180));
        g.fillRect(0, 0, getWidth(), getHeight());

        // Draw the image
        int imgW = deathScreenOverlay.getWidth(null);
        int imgH = deathScreenOverlay.getHeight(null);
        int drawX = (getWidth() - imgW) / 2;
        int drawY = (getHeight() - imgH) / 2;
        g.drawImage(deathScreenOverlay, drawX, drawY, imgW, imgH, null);

        respawnButton.draw(g);
    }

    /**
     * Update any sprites and check for collisions
     *
     * @param elapsed The elapsed time between this call and the previous call of elapsed
     */
    public void updateGame(long elapsed) {

        // Now update the sprites animation and position
        player.update(elapsed);

        // Then check for any collisions that may have occurred
//        handleScreenEdge(player, tmap, elapsed);
        checkTileCollision(player, tmap);

        //Damage from tiles
        for (Tile t : collidedTiles) {
            if (t.getCharacter() == 'a') {
                player.takeHit(false, 5);
            }
        }

        checkAttackCollision(enemies);
        checkAttackCollision(throwEnemies);
        checkAttackCollision(bosses);

        // Slowly reduce health
        float interpSpeed = 0.005f;
        displayedHealth += (player.getHealth() - displayedHealth) * interpSpeed * elapsed;

        // Check for player being alive if not call respawn
        if (!player.getIsAlive() && screenPhase == SCREEN.PLAYING) {
            screenPhase = SCREEN.DEATH;
            SoundManager.getInstance().pauseMidi();
            // player.respawn(tmap.getTileWidth(), tmap.getMapHeight(), tmap.getTileHeight());
        }

        //Checks for transition tile hit
        checkMapTransition();

        // Changing of music for boss
        if (!bosses.isEmpty() && player.getX() > (60 * tmap.getTileWidth())) {
            SoundManager.getInstance().playBackgroundBossMidi();
            // This will increase and decrease volume based of distance
            SoundEffects.getInstance().distanceBasedVolumeBoss(
                    player.getX(),
                    bosses.getFirst().getX(),
                    SoundManager.getInstance()
            );
        } else {
            SoundManager.getInstance().playBackgroundMidi();
        }

        //Pitch change from health
        SoundEffects.getInstance().healthBasedPitch(player.getHealth(), player.getMaxHealth(), SoundManager.getInstance());

        for (PatrolEnemy e : enemies) {
            if (boundingBoxCollision(player, e)) {
//                System.out.println("Collision");
                boolean hitFromRight = e.getX() > player.getX();
                player.takeHit(hitFromRight, 1);
            }

            e.update(elapsed);
//            handleScreenEdge(e, tmap, elapsed);
            checkTileCollision(e, tmap);
        }

        enemies.removeIf(PatrolEnemy::isDeathAnimationFinished);

        for (ThrowingEnemy e : throwEnemies) {
            if (boundingBoxCollision(player, e)) {
//                System.out.println("Collision");
                boolean hitFromRight = e.getX() > player.getX();
                player.takeHit(hitFromRight, 1);
            }
            e.update(elapsed);
            checkTileCollision(e, tmap);

            if (e.isShouldSpawnProjectile()) {
                // Projectile spawn for each projectile enemy
                projectiles.add(e.spawnProjectile(scale));
            }
        }

        throwEnemies.removeIf(ThrowingEnemy::isDeathAnimationFinished);

        for (MainBoss b : bosses) {
            displayBossHealth += (b.getHealth() - displayBossHealth) * interpSpeed * elapsed;
            if (boundingBoxCollision(player, b)) {
                boolean hitFromRight = b.getX() > player.getX();
                player.takeHit(hitFromRight, 1);
            }
            b.update(elapsed, player.getX(), player.getY());
            // i will be moving it in air so for now i dont want it to collide with tiles

            if (b.isShouldSpawnProjectiles()) {

                if (b.getAttackIndex() == 1) {
                    projectiles.add(b.spawnStraightShot(scale));
                } else {
                    projectiles.addAll(b.spawnScatterShot(scale));
                }
            }
        }

        bosses.removeIf(MainBoss::isDeathAnimationFinished);

        for (Projectile p : projectiles) {
            p.update(elapsed);

            if (boundingBoxCollision(player, p)) {
                boolean hitFromRight = p.getX() > player.getX();
                player.takeHit(hitFromRight, 1);
                p.onHit();
            }
        }

        projectiles.removeIf(Projectile::getIsExpired);
        System.out.println("Player block " + player.getX()/tmap.getTileWidth());
    }

    /**
     * Clean transition from one map to another
     * @param elapsed
     */
    private void updateTransition(long elapsed) {
        if (fadingOut) {
            transitionAlpha += transitionSpeed * elapsed;
            if (transitionAlpha >= 255) {
                transitionAlpha = 255;
                fadingOut = false;
                currentMap = currentMap == 1 ? 2 : 1;
                loadMapLevel(currentMap);
            }
        } else {
            transitionAlpha -= transitionSpeed * elapsed;
            if (transitionAlpha <= 0) {
                transitionAlpha = 0;
                fadingOut = true;
                transitionTriggered = false;
                screenPhase = SCREEN.PLAYING;
                SoundManager.getInstance().resumeMidi();
            }
        }
    }

    public void update(long elapsed) {
        switch (screenPhase) {
            case PLAYING -> updateGame(elapsed);
            case TRANSITION -> updateTransition(elapsed);
            case PAUSED, DEATH, INSTRUCTIONS, START -> {
            }
        }
    }

    /**
     * Checks and handles collisions with the edge of the screen. You should generally
     * use tile map collisions to prevent the player leaving the game area. This method
     * is only included as a temporary measure until you have properly developed your
     * tile maps.
     *
     * @param s       The Sprite to check collisions for
     * @param tmap    The tile map to check
     * @param elapsed How much time has gone by since the last call
     */
    public void handleScreenEdge(Sprite s, TileMap tmap, long elapsed) {
        // This method just checks if the sprite has gone off the bottom screen.
        // Ideally you should use tile collision instead of this approach

        float difference = s.getY() + s.getHeight() - tmap.getPixelHeight();
        if (difference > 0) {
            // Put the player back on the map according to how far over they were
            s.setY(tmap.getPixelHeight() - s.getHeight() - (int) (difference));

            // and make them bounce
            s.setVelocityY(-s.getVelocityY() * 0.75f);
        }
    }


    /**
     * Override of the keyPressed event defined in GameCore to catch our
     * own events
     *
     * @param e The event that has been generated
     */

    public void keyPressed(KeyEvent e)
    // Since key events are handled in GameCore cant make changes to the library
    // Ill just pass it along to the new Player class
    {
        int key = e.getKeyCode();

        // give key code to player
        player.keyPressed(key);

        // Game level keys can be handled here
        switch (key) {
//			case KeyEvent.VK_UP     : flap = true; break;
//			case KeyEvent.VK_RIGHT  : moveRight = true; break;
//			case KeyEvent.VK_S 		: Sound s = new Sound("sounds/caw.wav");
//									  s.start();
//									  break;
            case KeyEvent.VK_ESCAPE:
                if (screenPhase == SCREEN.PLAYING) {
                    screenPhase = SCREEN.PAUSED;
                    SoundManager.getInstance().pauseMidi();
                } else if (screenPhase == SCREEN.PAUSED) {
                    screenPhase = SCREEN.PLAYING;
                    SoundManager.getInstance().resumeMidi();
                }
                break;
            case KeyEvent.VK_B:
                debug = !debug;
                break; // Flip the debug state
//			case KeyEvent.VK_A		: player.setAnimation(walk);
            default:
                break;
        }

    }

    /**
     * Use the sample code in the lecture notes to properly detect
     * a bounding box collision between sprites s1 and s2.
     *
     * @return true if a collision may have occurred, false if it has not.
     */
    public boolean boundingBoxCollision(Entity e1, Entity e2) {
        // The first rectangle should i have imported rectangle and
        // appended all of these values to it remebering that as hit box would be so much easier
        // ughh
        float e1x = e1.getX() + e1.getCollisionOffsetX();
        float e1y = e1.getY() + e1.getCollisionOffsetY();
        float e1w = e1.getCollisionWidth();
        float e1h = e1.getCollisionHeight();

        float e2x = e2.getX() + e2.getCollisionOffsetX();
        float e2y = e2.getY() + e2.getCollisionOffsetY();
        float e2w = e2.getCollisionWidth();
        float e2h = e2.getCollisionHeight();

        return e1x < e2x + e2w &&
                e1x + e1w > e2x &&
                e1y < e2y + e2h &&
                e1y + e1h > e2y;

    }

    /**
     * Check and handles collisions with a tile map for the
     * given sprite 's'. Initial functionality is limited...
     *
     * @param s    The Sprite to check collisions for
     * @param tmap The tile map to check
     */

    public void checkTileCollision(Entity s, TileMap tmap) {
        // Empty out our current set of collided tiles
        collidedTiles.clear();

        // Take a note of a sprite's current position
        float sx = s.getX() + s.getCollisionOffsetX();
        float sy = s.getY() + s.getCollisionOffsetY();

        // Find out how wide and how tall a tile is
        float tileWidth = tmap.getTileWidth();
        float tileHeight = tmap.getTileHeight();

        // The width and height of the sprite
        float sw = s.getCollisionWidth();
        float sh = s.getCollisionHeight();

        // Collision will be seperated into two parts
        // Umm should we even use corners the logic should be x axis wall checks and y axis bottom and top checks why is corner?
        // Part 1: Corner check for player landing or hitting a top block while jumping
        // Part 2: Wall checks if player touches an oncoming wall

        //--- Wall Checks----

        //still buggy so i am goinf to take 3 points for wall checks
        int xtile = (int) (sx / tileWidth);
        int ytileTop = (int) ((sy + sh * 0.1f) / tileHeight); // collision checked from middle of the sprite
        int ytileMiddle = (int) ((sy + sh * 0.5f) / tileHeight);
        int ytileBot = (int) ((sy + sh * 0.9f) / tileHeight);
        Tile lwT = tmap.getTile(xtile, ytileTop); // Should not be checked from middle it should be concrete both top and bottom tiles
        Tile lwM = tmap.getTile(xtile, ytileMiddle);
        Tile lwB = tmap.getTile(xtile, ytileBot); // Should not be checked from middle it should be concrete both top and bottom tiles
        // Check if top tile solid if yes returns that if not checks bottom and returns that
        Tile lw = (lwT != null && lwT.getCharacter() != '.') ? lwT :
                (lwM != null && lwM.getCharacter() != '.') ? lwM :
                        (lwB != null && lwB.getCharacter() != '.') ? lwB : null;
        if (lw != null) {
            // If wall encountered to left move it back and set velocity to 0
            s.setX(lw.getXC() + tileWidth - s.getCollisionOffsetX());
            s.setVelocityX(0);
            collidedTiles.add(lw);
        }

        //Recalculate
        sx = s.getX() + s.getCollisionOffsetX();
        sy = s.getY() + s.getCollisionOffsetY();

        // right wall
        xtile = (int) ((sx + sw) / tileWidth);
        ytileTop = (int) ((sy + sh * 0.1f) / tileHeight);
        ytileMiddle = (int) ((sy + sh * 0.5f) / tileHeight);
        ytileBot = (int) ((sy + sh * 0.9f) / tileHeight);
        Tile rwT = tmap.getTile(xtile, ytileTop);
        Tile rwM = tmap.getTile(xtile, ytileMiddle);
        Tile rwB = tmap.getTile(xtile, ytileBot);
        Tile rw = (rwT != null && rwT.getCharacter() != '.') ? rwT :
                (rwM != null && rwM.getCharacter() != '.') ? rwM :
                        (rwB != null && rwB.getCharacter() != '.') ? rwB : null;
        if (rw != null) {
            s.setX(rw.getXC() - sw - s.getCollisionOffsetX());
            s.setVelocityX(0);
            collidedTiles.add(rw);
        }


        // Since the corner collision change the positioning recalculation?
        sx = s.getX() + s.getCollisionOffsetX();
        sy = s.getY() + s.getCollisionOffsetY();


        //---Ceiling Checks---

        int xtileLeft = (int) (sx / tileWidth);
        int xtileRight = (int) ((sx + sw) / tileWidth);
        int ytile = (int) (sy / tileHeight);
        Tile tl = tmap.getTile(xtileLeft, ytile);
        Tile tr = tmap.getTile(xtileRight, ytile);
        // Moving upwards and we will make both checks here
        if (s.getVelocityY() < 0) {
            float topLeftOverlap = tl != null ? tl.getYC() + tileHeight - sy : 0;
            float topRightOverlap = tr != null ? tr.getYC() + tileHeight - sy : 0;
            if (tl != null && tl.getCharacter() != '.' && topLeftOverlap > 6) {
                s.setY(tl.getYC() + tileHeight - s.getCollisionOffsetY());
                s.setVelocityY(0);
                collidedTiles.add(tl);
            } else if (tr != null && tr.getCharacter() != '.' && topRightOverlap > 6) {
                s.setY(tr.getYC() + tileHeight - s.getCollisionOffsetY());
                s.setVelocityY(0);
                collidedTiles.add(tr);
            }
        }

        // Recalculation
        sx = s.getX() + s.getCollisionOffsetX();
        sy = s.getY() + s.getCollisionOffsetY();

        //---Ground Checks---

        // Bottom Left corner
        xtileLeft = (int) (sx / tileWidth);
        xtileRight = (int) ((sx + sw) / tileWidth);
        ytile = (int) ((sy + sh) / tileHeight);
        Tile bl = tmap.getTile(xtileLeft, ytile);
        Tile br = tmap.getTile(xtileRight, ytile);
        boolean blSolid = bl != null && bl.getCharacter() != '.';
        boolean brSolid = br != null && br.getCharacter() != '.';
        // There is bug while jumping towards a wall small clip teleports s
        // Make a clip overlap difference if there is minimal clip dont teleport
        float bottomLeftOverLap = blSolid ? (sy + sh) - (bl.getYC()) : 0;
        float bottomRightOverlap = brSolid ? (sy + sh) - (br.getYC()) : 0;
        boolean overlapLeft = bottomLeftOverLap > 6;
        boolean overlapRight = bottomRightOverlap > 6;

        if (s.getVelocityY() >= 0) {
            if (blSolid && brSolid && overlapLeft && overlapRight) {
                // Both corners hit then we can snap to the higher surface
                float targetY = Math.min(bl.getYC(), br.getYC()) - sh - s.getCollisionOffsetY();
                s.setY(targetY);
                s.setVelocityY(0);
                collidedTiles.add(bl);
                collidedTiles.add(br);
                s.land();
            } else if (blSolid && overlapLeft) {
                s.setY(bl.getYC() - sh - s.getCollisionOffsetY());
                s.setVelocityY(0);
                collidedTiles.add(bl);
                s.land();
            } else if (brSolid && overlapRight) {
                s.setY(br.getYC() - sh - s.getCollisionOffsetY());
                s.setVelocityY(0);
                collidedTiles.add(br);
                s.land();
            }
        }
    }


    public void keyReleased(KeyEvent e) {

        int key = e.getKeyCode();

        //player keys
        player.keyReleased(key);

        // game keys
        switch (key) {
            case KeyEvent.VK_ESCAPE:
//                stop();
                break;
            case KeyEvent.VK_R:
                if (debug) player.respawn(tmap.getTileWidth(), tmap.getMapHeight(), tmap.getTileHeight());
                break;
            case KeyEvent.VK_I:
                if (debug) player.toggleInvincible();
            default:
                break;
        }
    }

    /**
     * This implements parallax rendering of images using the cameras position
     * It is scalled proportionally
     *
     * @param cameraX The camera position
     * @param speed   The amount of offset/movement we want
     * @param gap     The gap between each render of image
     */

    private void drawParallaxLayer(Graphics2D g, Image img, int cameraX, float speed, int gap) {
        int imgWidth = img.getWidth(null);
        int imgHeight = img.getHeight(null);
        gap = gap * scale;

        int startX = (int) (cameraX * speed) % (imgWidth + gap);
        if (startX > 0) startX -= (imgWidth + gap);

        for (int x = startX; x < getWidth(); x += imgWidth + gap) {
            g.drawImage(img, x, 0, imgWidth * scale, imgHeight * scale, null);
        }
    }

    /**
     * Spawns/ Loads in enemies form a a list off enemies objects
     * Fix later to take in params to clean it up for spawn blocks etc
     */
    private void spawnEnemiesMap1() {
        int tw = tmap.getTileWidth();
        int th = tmap.getTileHeight();
        // Hardcode this for now
        int groundRow = tmap.getMapHeight() - 1;

        bosses.add(new MainBoss(scale, tw, th, 80, groundRow - 6));
//        bosses.add(new MainBoss(scale, tw, th, 18, groundRow - 6));
        enemies.add(new PatrolEnemy(scale, tw, th, 13, groundRow));
        enemies.add(new PatrolEnemy(scale, tw, th, 18, groundRow));
        throwEnemies.add(new ThrowingEnemy(scale, tw, th, 31, groundRow - 3, false));
    }

    private void spawnEnemiesMap2() {
        int tw = tmap.getTileWidth();
        int th = tmap.getTileHeight();
        // Hardcode this for now
        int groundRow = tmap.getMapHeight() - 1;

//        bosses.add(new MainBoss(scale, tw, th, 80, groundRow - 6));
        enemies.add(new PatrolEnemy(scale, tw, th, 14, groundRow-3));
        enemies.add(new PatrolEnemy(scale, tw, th, 42, groundRow));
        enemies.add(new PatrolEnemy(scale, tw, th, 34, groundRow));
        enemies.add(new PatrolEnemy(scale, tw, th, 105, groundRow-5));
        throwEnemies.add(new ThrowingEnemy(scale, tw, th, 40, groundRow - 4, false));
        throwEnemies.add(new ThrowingEnemy(scale, tw, th, 54, groundRow - 3, false));
        throwEnemies.add(new ThrowingEnemy(scale, tw, th, 62, groundRow - 3, false));

        bosses.add(new MainBoss(scale, tw, th, 147, groundRow - 6));
    }

    /**
     * Check the collision of the attack box of a sprite with the collision box of another
     * For now its just players attack box but if there are entites that will generate attacks then we can fix
     */
    public void checkAttackCollision(ArrayList<? extends Entity> targets) { // Is the typecasting liek in Typescript?
        Rectangle attackBox = player.getAttackBox();
        if (attackBox == null) return;

        for (Entity e : targets) {
            Rectangle enemyBox = new Rectangle(
                    (int) e.getX() + e.getCollisionOffsetX(),
                    (int) e.getY() + e.getCollisionOffsetY(),
                    e.getCollisionWidth(),
                    e.getCollisionHeight()
            );

            if (attackBox.intersects(enemyBox)) {
//                System.out.println("The enemy box was hit");
                e.takeDamage(1); // This will reduce the max health
            }
        }
    }

    public void drawHealthBar(Graphics g, int padX, int padY, Image healthEmpty, Image healthFull, Entity e, float dispHealth) {
        Graphics2D g2d = (Graphics2D) g;
        int barWidth = healthEmpty.getWidth(null);
        int barHeight = healthEmpty.getHeight(null);

        // Background empty
        g2d.drawImage(healthEmpty, padX, padY, barWidth, barHeight, null);

        // reduction of full bar
        float healthPercent = dispHealth / e.getMaxHealth();
        int filledWidth = (int) (barWidth * healthPercent);

        g2d.drawImage(
                healthFull,
                padX, padY,
                padX + filledWidth, padY + barHeight,
                0, 0,
                filledWidth, barHeight,
                null
        );
    }

    private void checkMapTransition(){
        if (transitionTriggered) return;

        // When stopping on this tile we get the place where we should transition from
        int triggerTileX = currentMap == 1 ? 90 : 190; // end of map 1, start of map 2
        float triggerPixelX = triggerTileX * tmap.getTileWidth();
//        System.out.println("Difference" + (player.getX() - triggerPixelX));
        if (Math.abs(player.getX() - triggerPixelX) < tmap.getTileWidth()) {
            // Only triggers if boss is defeated
            if (bosses.isEmpty()) {
                transitionTriggered = true;
                screenPhase = SCREEN.TRANSITION;
                SoundManager.getInstance().pauseMidi();
            }
        }
    }


    public void mouseClicked(MouseEvent e) {
        int mx = e.getX();
        int my = e.getY();

        switch (screenPhase) {
            case START:
                if (startButton.isClicked(mx, my)) {
                    screenPhase = SCREEN.PLAYING;
                    SoundManager.getInstance().playBackgroundMidi();
                }
                if (instructionsButton.isClicked(mx, my)) {
                    screenPhase = SCREEN.INSTRUCTIONS;
                }
                if (quitButton.isClicked(mx, my)) {
                    stop();
                }
                break;
            case INSTRUCTIONS:
                if (backButton.isClicked(mx, my)) {
                    screenPhase = SCREEN.START;
                }
                break;
            case DEATH:
                if (respawnButton.isClicked(mx, my)) {
                    player.respawn(tmap.getTileWidth(), tmap.getMapHeight(), tmap.getTileHeight());
                    displayedHealth = player.getMaxHealth();
                    screenPhase = SCREEN.PLAYING;
                    SoundManager.getInstance().resumeMidi();
                }
                break;
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    @Override
    public void mouseDragged(MouseEvent e) {

    }

    @Override
    public void mouseMoved(MouseEvent e) {
        int mx = e.getX();
        int my = e.getY();

//        if (screenPhase == SCREEN.DEATH) {
//            respawnButton.setHovered(mx, my);
//        }

        switch (screenPhase) {
            case START:
                startButton.setHovered(mx, my);
                instructionsButton.setHovered(mx, my);
                quitButton.setHovered(mx, my);
                break;
            case INSTRUCTIONS:
                backButton.setHovered(mx, my);
                break;
            case DEATH:
                respawnButton.setHovered(mx, my);
                break;
        }
    }

    private void loadMapLevel(int mapNumber) {
        enemies.clear();
        throwEnemies.clear();
        bosses.clear();
        projectiles.clear();

        if (mapNumber == 1) {
            tmap.loadMap("maps/map", "basemapaplha2.txt");
            dmap.loadMap("maps/decor", "decorbasemapalpha.txt");
            spawnEnemiesMap1();
        } else if (mapNumber == 2) {
            tmap.loadMap("maps/map", "basemapalpha3.txt");
            dmap.loadMap("maps/decor", "decorbasemapalpha2.txt");
            spawnEnemiesMap2();
        }

        player.initializePlayer(tmap.getTileWidth(), tmap.getMapHeight(), tmap.getTileHeight());

    }
}
