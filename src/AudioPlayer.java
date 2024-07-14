import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.util.ArrayList;

import javazoom.jl.player.advanced.AdvancedPlayer;
import javazoom.jl.player.advanced.PlaybackEvent;
import javazoom.jl.player.advanced.PlaybackListener;


public class AudioPlayer extends PlaybackListener {
    // this will be used to update the isPaused more  synchronously
    private static final Object playSignal = new Object();

    // need reference so that we can update the gui in this class
    private AudioPlayerGUI audioPlayerGUI;

    // we will need a way to store our audio's details, so we will be creating a audio class
    private Audio currentAudio;
    public Audio getCurrentAudio() {
        if(currentAudio !=null)
            return currentAudio;
        throw new UnsupportedOperationException("Load an audio first");
    }

    private ArrayList<Audio> playlist;

    // we will need to keep track the index we are in the playlist
    private int currentPlaylistIndex;

    // Use JLayer library to create an AdvancedPlayer object which will handle playing the audio
    private AdvancedPlayer advancedPlayer;

    // pause/play boolean flag used to indicate whether the player hase been paused
    private boolean isPaused;
    public boolean isPaused() {
        return isPaused;
    }

    // boolean falg used to tell when audio is finished
    private boolean isAudioFinished;

    private boolean isNextPressed, isPrevPressed;

    // stores in the last frame when the playback is finished (used for pausing and resuming)
    private int currentFrame;
    public void setCurrentFrame(int frame) {
        currentFrame = frame;
    }

    // track how many milliseconds has passed since playing the audio (used for updating the playback slider)
    private int currentTimeInMilli;
    public void setCurrentTimeInMilli(int timeInMilli) {
        currentTimeInMilli = timeInMilli;
    }

    // Constructor
    public AudioPlayer(AudioPlayerGUI audioPlayerGUI) {
        this.audioPlayerGUI = audioPlayerGUI;
    }

    public void loadAudio(Audio audio) {
        currentAudio = audio;
        playlist = null;

        // stop the audio if possible
        if(!isAudioFinished)
            stopAudio();

        // play the current audio if not null
        if (currentAudio != null) {
            // reset frame
            currentFrame = 0;

            // reset current time in milli
            currentTimeInMilli = 0;

            // update gui
            audioPlayerGUI.setPlaybackSliderValue(0);

            playCurrentAudio();
        }
    }

    public void loadPlaylist(File playlistFile) {
        playlist = new ArrayList<>();

        // store the path from the from the text file to playlist array list
        try {
            FileReader fileReader = new FileReader(playlistFile);
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            // read each line from the text file and store the text into the audioPath variable
            String audioPath;
            while((audioPath = bufferedReader.readLine()) != null) {
                // create audio based on audio path and add to playlist array list
                playlist.add(new Audio(audioPath));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if(playlist.size() > 0) {
            // reset playback slider
            audioPlayerGUI.setPlaybackSliderValue(0);

            // update current audio to the first audio in the playlist
            currentAudio = playlist.get(0);

            // start from the beginning frame
            currentFrame = 0;

            // update gui
            audioPlayerGUI.enablePauseButtonAndDisablePlayButton();
            audioPlayerGUI.updateAudioTitleAndArtist(currentAudio);
            audioPlayerGUI.updatePlaybackSlider(currentAudio);

            //start audio
            playCurrentAudio();

        }

    }

    public void playCurrentAudio() {
        if(currentAudio == null) {
            audioPlayerGUI.enablePlayButtonAndDisablePauseButton();
            return;
        }

        try {
            // read mp3 audio data
            FileInputStream fileInputStream = new FileInputStream(currentAudio.getFilePath());
            BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);

            // create a new advanced player
            advancedPlayer = new AdvancedPlayer(bufferedInputStream);
            advancedPlayer.setPlayBackListener(this);

            // start audio
            startAudioThread();

            // start playback slider thread
            startPlaybackSliderThread();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void pauseAudio() {
        if (currentAudio == null) {
            audioPlayerGUI.enablePlayButtonAndDisablePauseButton();
            return;
        }

        if (advancedPlayer != null) {
            // update isPaused flag
            isPaused = true;

            // then we want to stop the player
            stopAudio();
        }
    }

    public void stopAudio() {
        if(advancedPlayer != null) {
            advancedPlayer.stop();
            advancedPlayer.close();
            advancedPlayer = null;
        }
    }

    public void prevAudio() {
        // no need to go to next audio if there is no playlist
        if(playlist == null) return;

        // check to see if we can go to previous song
        if(currentPlaylistIndex - 1 < 0) return;

        isPrevPressed = true;
        isNextPressed = false;

        // stop the audio if possible
        if(!isAudioFinished)
            stopAudio();

        // decrease current playlist index
        currentPlaylistIndex--;

         // update current audio
        currentAudio = playlist.get(currentPlaylistIndex);        

        // reset frame
        currentFrame = 0;

        // reset current time in milli
        currentTimeInMilli = 0;

        // update gui
        audioPlayerGUI.enablePauseButtonAndDisablePlayButton();
        audioPlayerGUI.updateAudioTitleAndArtist(currentAudio);
        audioPlayerGUI.updatePlaybackSlider(currentAudio);

        // play the audio
        playCurrentAudio();
    }

    public void nextAudio() {
        // no need to go to next audio if there is no playlist
        if(playlist == null) return;

        // check to see if we have reached the end of the playlist 
        if(currentPlaylistIndex + 1 > playlist.size() - 1) return;

        isNextPressed = true;
        isPrevPressed = false;

        // stop the audio if possible
        if(!isAudioFinished)
            stopAudio();

        // increase current playlist index
        currentPlaylistIndex++;

        // update current audio
        currentAudio = playlist.get(currentPlaylistIndex);        

        // reset frame
        currentFrame = 0;

        // reset current time in milli
        currentTimeInMilli = 0;

        // update gui
        audioPlayerGUI.enablePauseButtonAndDisablePlayButton();
        audioPlayerGUI.updateAudioTitleAndArtist(currentAudio);
        audioPlayerGUI.updatePlaybackSlider(currentAudio);

        // play the audio
        playCurrentAudio();
    }

    // create a thread that will handle playing audio
    private void startAudioThread() {
        new Thread(() -> {
            try {
                if (isPaused) {
                    synchronized(playSignal) {
                        // update flag
                        isPaused = false;

                        // notify the other thread to continue (makes sure that isPaused is updated to false properly)
                        playSignal.notify();
                    }
                    // resume audio from last frame
                    advancedPlayer.play(currentFrame, Integer.MAX_VALUE);
                    
                } else {
                    // play audio from the beginning
                    advancedPlayer.play();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    // create a thread that will handle updating the slider
    private void startPlaybackSliderThread() {
        new Thread(() -> {
            if(isPaused) {
                try {
                    // wait till it gets notified by other thread to continue
                    // makes sure that isPaused boolean flag updates to false before continuing
                    synchronized(playSignal) {
                        playSignal.wait();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            while (!isPaused && !isAudioFinished && !isPrevPressed && !isNextPressed) {
                try {
                    // increment current time milli
                    currentTimeInMilli++;

                    // calculate into frame value
                    int calculatedFrame = (int) ((double) currentTimeInMilli * 2.08 * currentAudio.getFrameRatePerMilliseconds());

                    // update gui
                    audioPlayerGUI.setPlaybackSliderValue(calculatedFrame);

                    // mimic 1 millisecond using thread.sleep
                    Thread.sleep(1);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }).start();
    }

    @Override
    public void playbackStarted(PlaybackEvent evt) {
        // this method gets called in the beginning of the audio 
        System.out.println("Playback Started");
        isAudioFinished = false;
        isNextPressed = false;
        isPrevPressed =  false;
    }

    @Override
    public void playbackFinished(PlaybackEvent evt) {
        // this method gets called when the audio finishes or the player gets closed
        System.out.println("Playback Finished");

        if(isPaused)
            currentFrame += (int) ((double) evt.getFrame() * currentAudio.getFrameRatePerMilliseconds());
        else {
            // if user pressed next or prev, we don't need execute the rest of the code
            if (isNextPressed || isPrevPressed) return;

            // when the audio ends
            isAudioFinished = true;
            audioPlayerGUI.setPlaybackSliderValue(0);

            if(playlist == null) {
                // update gui
                audioPlayerGUI.enablePlayButtonAndDisablePauseButton();
            } else {
                // last audio in the playlist
                if(currentPlaylistIndex == playlist.size() - 1) {
                    // update gui
                    audioPlayerGUI.enablePlayButtonAndDisablePauseButton();
                } else {
                    // go to the next audio in the playlist
                    nextAudio();
                }
            }
        }
        // if(audioPlayerGUI.getPlaybackSliderValue() == currentAudio.getMp3File().getFrameCount())
        //     audioPlayerGUI.setPlaybackSliderValue(0);
        
    }
    
}
