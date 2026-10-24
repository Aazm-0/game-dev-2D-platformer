package game2Dupdates.entitites.sound;

public class SoundEffects {

    private static SoundEffects soundEffects;

    //Two sound effects utilized
    // Lowering bpm to increase pith : Cause Low Health
    // Increasing volume : Cause distance to boss

    // The max tiles are 110 and the boss music starts at like 60
    private float maxHearingDistance = 40 * 64;
    private float minDistance = 5 * 64;

    private float normalTempo = 120;
    private float lowTempo = 80;

    private SoundEffects(){}

    public static SoundEffects getInstance(){
        if (soundEffects == null){
            soundEffects = new SoundEffects();
        }
        return soundEffects;
    }

    public void distanceBasedVolumeBoss(float playerX,float bossX,SoundManager soundManager){
        if(!soundManager.isPlaying()) return;
        float distance = Math.abs(bossX - playerX);

        float setDistanceForCalc = Math.max(minDistance,Math.min(distance,maxHearingDistance));

        // Normalizing i dont get the math here pretty rusty
        float volume = 1.0f - ((setDistanceForCalc-minDistance) / (maxHearingDistance -minDistance));
//        System.out.printf("Distance: %.0fpx | Volume: %.2f%n", distance, volume);
        soundManager.setVolume(volume);
    }

    public void healthBasedPitch(int currentHealth,int maxHealth,SoundManager soundManager){
        if(!soundManager.isPlaying()) return;

        float healthRation = (float) currentHealth/maxHealth;

        float targetTempo = lowTempo + (healthRation * (normalTempo - lowTempo));

        soundManager.setTempo(targetTempo);
    }
}
