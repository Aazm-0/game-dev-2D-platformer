package game2Dupdates.entitites.sound;

import javax.sound.midi.*;
import javax.sound.sampled.*;
import java.io.File;
import java.util.HashMap;

public class SoundManager {

    // SFX Logic using clipping
    //Implement singelton pattern only one instance with all clips loaded
    // We will use the same instance everywhere
    private static SoundManager instance;

    // Sound lib - Stores names to file
    private HashMap<String, String> soundLib = new HashMap<>();

    // Actual lib - Stores the preloaded clips with same naming
    private HashMap<String, Clip> clipLib = new HashMap<>();
    // Clip used so its preloaded


    // Background Music logic using MIDI sequencer
    private Sequencer sequencer;
    private String currentTrack = "";

    private static final String BACKGROUND_GAME = "sounds/midi/background_game.mid";
    private static final String BACKGROUND_BOSS = "sounds/midi/background_boss.mid";


    public static SoundManager getInstance() {
        if (instance == null) {
            instance = new SoundManager();
        }
        return instance;
    }

    public SoundManager() {
        // Registering the sfx
        register("jump", "sounds/sfx/jump.wav");
        register("hurt", "sounds/sfx/hurt.wav");
        register("kill", "sounds/sfx/kill.wav");
        register("attack", "sounds/sfx/attack.wav");

        // Opening the sequencer
        try {
            // This is the orchestra that will play instructions of the midi files
            sequencer = MidiSystem.getSequencer();
            sequencer.open();
        } catch (Exception e) {
            System.out.println("Music player failed to load sequencer: " + e.getMessage());
        }
    }

    /**
     * Registers the name to both the libraries soundLib - storing names of file
     * clipLib - Store the actual preloaded audio
     *
     * @param name name to register audio with
     * @param path path to the file
     */
    private void register(String name, String path) {
        soundLib.put(name, path);
        try {
            File file = new File(path);
            AudioInputStream stream = AudioSystem.getAudioInputStream(file);
            AudioFormat format = stream.getFormat();
            DataLine.Info info = new DataLine.Info(Clip.class, format);
            Clip clip = (Clip) AudioSystem.getLine(info);
            clip.open(stream);
            clipLib.put(name, clip);
        } catch (Exception e) {
            System.out.println("Error loading audio file" + e.getMessage());
        }
    }

    // Play method no volume call plays it at full volume call plays it at  volume specified
    // Maybe to pause screen i add audio control

    public void play(String name) {
        play(name, 1.0f);
    }

    /**
     * Second variation called with setVolume stops clip changes volume and starts it again
     *
     * @param name   Name of the sound to play
     * @param volume the volume you want it at 0.0 - 1.0
     */
    public void play(String name, float volume) {
        if (volume < 0 || volume > 1) {
            System.out.println("Volume must be between 0 and 1");
            return;
        }

        Clip clip = clipLib.get(name);
        if (clip == null) {
            System.out.println("The name doesnt exist print out soundLib for available");
            return;
        }

        //Reset clip and applying volume
        clip.stop();
        clip.setFramePosition(0);
        setVolume(clip, volume);
        clip.start();
    }

    public void setVolume(Clip clip, float volume) {
        if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            FloatControl gain = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            // Umm i dont get the physics behind how log works with human ears
            float db = volume > 0 ? (float) (20.0 * Math.log10(volume)) : gain.getMinimum();
            gain.setValue(Math.max(gain.getMinimum(), Math.min(gain.getMaximum(), db)));
        }
    }

    public HashMap<String, String> getSoundLib() {
        return soundLib;
    }



    public void playMidi(String path) {
        if (path.equals(currentTrack)) return; // already playing it

        try {
            Sequence sequence = MidiSystem.getSequence(new File(path));
            sequencer.stop();
            sequencer.setSequence(sequence);
            sequencer.setLoopCount(Sequencer.LOOP_CONTINUOUSLY);
            sequencer.setTempoInBPM(120);
            sequencer.start();
            currentTrack = path;
        } catch (Exception e) {
            System.out.println("The music player failed to play " + path + "-" + e.getMessage());
        }

    }

    public void playBackgroundMidi() {
        playMidi(BACKGROUND_GAME);
    }

    public void playBackgroundBossMidi() {
        playMidi(BACKGROUND_BOSS);
    }

    //Set Volume for midi?
    public void setVolume(float volume) {
        if(isPlaying()) return;

        try {
            MidiChannel[] channels = ((Synthesizer) MidiSystem.getSynthesizer()).getChannels();
            int midiVol = (int) (volume * 127);
            for(MidiChannel channel : channels) {
                channel.controlChange(7,midiVol);
            }
        } catch (Exception e) {
            System.out.println("Error getting synthesizer midi: " + e.getMessage());
        }
    }

    public void setTempo(float bpm) {
        if(!isPlaying()) return;
        sequencer.setTempoInBPM(bpm);
    }

    public void pauseMidi() {
        if (sequencer != null && sequencer.isRunning()) {
            sequencer.stop();
        }
    }

    public void resumeMidi() {
        if (sequencer != null && !sequencer.isRunning() && !currentTrack.isEmpty()) {
            sequencer.start();
        }
    }

    public boolean isPlaying() { return sequencer != null && sequencer.isRunning(); }
    public String getCurrentTrack() { return currentTrack; }
}
