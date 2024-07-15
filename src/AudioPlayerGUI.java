import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Hashtable;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;

import api.JnaFileChooser;
import slider.JGradientSlider;

public class AudioPlayerGUI extends JFrame {

    // color configurations
    public static final Color FRAME_COLOR = new Color(0x1A5319);
    public static final Color TEXT_COLOR = new Color(0xDEF9C4);

    private AudioPlayer audioPlayer;

    // allow us to use file explorer in our app
    private JnaFileChooser jFileChooser;

    private JLabel audioTitle, audioArtist;
    private JPanel playbackBtns;
    private JGradientSlider playbackSlider;
    
    // Constructor
    public AudioPlayerGUI() {        
        // calls JFrame constructor to configure out gui and set the title header to "Audio Player"
        super("Music Player");

        // image icon for app
        setIconImage(new ImageIcon("src\\assests\\images\\icon.png").getImage());

        // set the size and width
        setSize(400, 600);

        // end process when the app is closed
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // launch app at the center of the screen
        setLocationRelativeTo(null);

        // prevent the app from being resized
        setResizable(false);

        // set layout to null which allows us to control the (x, y) coordinates of our components
        // and also set the height and width
        setLayout(null);

        // change the frame color
        getContentPane().setBackground(FRAME_COLOR);

        audioPlayer = new AudioPlayer(this);

        jFileChooser = new JnaFileChooser();

        jFileChooser.setTitle("Select audio");

        // set a default path for file explorer
        jFileChooser.setCurrentDirectory("src\\assests\\audios");

        //filter file to see only mp3 files
        jFileChooser.addFilter("MP3 Files", "mp3");

        addGuiComponents();
        
        addKeyBindngs();

    }

    private void addGuiComponents() {
        // add toolbar
        addToolbar();

        // load record image
        JLabel audioImage = new JLabel(loadImage("src\\assests\\images\\AlbumCover.png"));
        audioImage.setBounds(0, 50, getWidth()-20, 225);
        add(audioImage);

        // audio title
        audioTitle = new JLabel("Song Title");
        audioTitle.setBounds(0, 285, getWidth()-10, 30);
        audioTitle.setFont(new Font("Dialog", Font.BOLD, 24));
        audioTitle.setForeground(TEXT_COLOR);
        audioTitle.setHorizontalAlignment(SwingConstants.CENTER);
        add(audioTitle);

        // audio artist
        audioArtist = new JLabel("Artist");
        audioArtist.setBounds(0, 315, getWidth()-10, 30);
        audioArtist.setFont(new Font("Dialog", Font.PLAIN, 24));
        audioArtist.setForeground(TEXT_COLOR);
        audioArtist.setHorizontalAlignment(SwingConstants.CENTER);
        add(audioArtist);

        // playback slider
        playbackSlider = new JGradientSlider(JGradientSlider.HORIZONTAL, 0, 100, 0);
        playbackSlider.setBounds(getWidth()/2 - 300/2, 365, 300, 50);
        playbackSlider.setBackground(null);
        playbackSlider.setColors(new Color(0x81A263), new Color(0x365E32));
        playbackSlider.setTrackSize(2);
        playbackSlider.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                // when the user is holding the tick, we want to pause the audio
                audioPlayer.pauseAudio();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                // when the user drops the tick
                JGradientSlider source = (JGradientSlider) e.getSource();

                // get the frame value from where the user wants to playback to
                int frame = source.getValue();

                // update the current frame in the audio player to this frame
                audioPlayer.setCurrentFrame(frame);

                try {
                    // update current time in milli as well
                    audioPlayer.setCurrentTimeInMilli((int) (frame / (2.08 * audioPlayer.getCurrentAudio().getFrameRatePerMilliseconds())));                   
                } catch (Exception e1) {
                    playbackSlider.setValue(0);
                    return;
                }

                // resume the audio
                audioPlayer.playCurrentAudio();

                // toggle on pause button and toggle off play button
                enablePauseButtonAndDisablePlayButton();
            }
            
        });
        add(playbackSlider);

        // playback buttons (i.e. previous, play, next)
        addPlaybackBtns();
    }

    private void addToolbar() {
        JToolBar toolBar = new JToolBar();
        toolBar.setBounds(0, 0, getWidth(), 20);

        // prevent toolbar from being moved
        toolBar.setFloatable(false);

        // add drop down menu
        JMenuBar menuBar = new JMenuBar();
        toolBar.add(menuBar);

        // now we will add a audio menu where we will place the loading audio option
        JMenu audioMenu = new JMenu("Song");
        menuBar.add(audioMenu);

        // add the "load audio" item in the audioMenu
        JMenuItem loadAudio = new JMenuItem("load song");
        loadAudio.addActionListener(e -> {
            jFileChooser.setCurrentDirectory(getMusicPath());
            jFileChooser.showOpenDialog(AudioPlayerGUI.this);

            File selectedFile = jFileChooser.getSelectedFile();

            if(selectedFile != null) {
                // create a audio object based on selected file
                Audio audio = new Audio(selectedFile.getPath());

                // load audio in audio player
                audioPlayer.loadAudio(audio);

                // update audio title and artist
                updateAudioTitleAndArtist(audio);

                // update playback slider
                updatePlaybackSlider(audio);

                // toggle on pause button and toggle off play button
                enablePauseButtonAndDisablePlayButton();
                requestFocus();
            }
        });
        audioMenu.add(loadAudio);

        // now we will add the playlist menu
        JMenu playlistMenu = new JMenu("Playlist");
        menuBar.add(playlistMenu);

        // then add the items to the playlist menu
        JMenuItem createPlaylist = new JMenuItem("Create Playlist");
        createPlaylist.addActionListener(e -> {
            // load audio playlist dialog
            new AudioPlaylistDialog(AudioPlayerGUI.this).setVisible(true);
            requestFocus();
        });
        playlistMenu.add(createPlaylist);

        JMenuItem loadPlaylist = new JMenuItem("Load Playlist");
        loadPlaylist.addActionListener(e -> {
            JnaFileChooser jFileChooser = new JnaFileChooser();
            jFileChooser.setTitle("Select playlist");
            jFileChooser.addFilter("Playlist", "txt");
            jFileChooser.setCurrentDirectory("src\\assests\\playlists");

            jFileChooser.showOpenDialog(AudioPlayerGUI.this);
            File selectedFile = jFileChooser.getSelectedFile();

            if(selectedFile != null) {
                // stop the audio
                audioPlayer.stopAudio();

                // load the playlist
                audioPlayer.loadPlaylist(selectedFile);
                requestFocus();

            }
        });
        playlistMenu.add(loadPlaylist);

        add(toolBar);
    }

    private void addPlaybackBtns() {
        playbackBtns = new JPanel();
        playbackBtns.setBounds(0, 435, getWidth()-10, 80);
        playbackBtns.setBackground(null);

        // previous button
        JButton prevButton = new JButton(loadImage("src\\assests\\images\\previous.png"));
        prevButton.setBorderPainted(false);
        prevButton.setBackground(null);
        prevButton.setFocusable(false);
        prevButton.doClick();
        prevButton.addActionListener(e -> {
            // go to the previous audio
            audioPlayer.prevAudio();
        });
        playbackBtns.add(prevButton);

        // play button
        JButton playButton = new JButton(loadImage("src\\assests\\images\\play.png"));
        playButton.setBorderPainted(false);
        playButton.setBackground(null);
        playButton.setFocusable(false);
        playButton.addActionListener(e -> {
            // toggle off play button and toggle on pause button
            enablePauseButtonAndDisablePlayButton();

            // play or resume audio
            audioPlayer.playCurrentAudio();
        });
        playbackBtns.add(playButton);

        // pause button
        JButton pauseButton = new JButton(loadImage("src\\assests\\images\\pause.png"));
        pauseButton.setBorderPainted(false);
        pauseButton.setBackground(null);
        pauseButton.setFocusable(false);
        pauseButton.setVisible(false);
        pauseButton.addActionListener(e -> {
            // toggle off pause button and toggle on play button
            enablePlayButtonAndDisablePauseButton();

            // pause the audio
            audioPlayer.pauseAudio();
        });
        playbackBtns.add(pauseButton);

        // next button
        JButton nextButton = new JButton(loadImage("src\\assests\\images\\next.png"));
        nextButton.setBorderPainted(false);
        nextButton.setBackground(null);
        nextButton.setFocusable(false);
        nextButton.doClick();
        nextButton.addActionListener(e -> {
            // go to the next audio
            audioPlayer.nextAudio();
        });
        playbackBtns.add(nextButton);

        add(playbackBtns);
    }

    private void addKeyBindngs() {
        InputMap inputMap = getRootPane().getInputMap(JPanel.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = getRootPane().getActionMap();

        inputMap.put(KeyStroke.getKeyStroke("LEFT"), "prev");
        actionMap.put("prev", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                audioPlayer.prevAudio();
            }
        });

        inputMap.put(KeyStroke.getKeyStroke("SPACE"), "playpause");
        actionMap.put("playpause", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(!audioPlayer.isPaused()) {
                    // toggle off pause button and toggle on play button
                    enablePlayButtonAndDisablePauseButton();

                    // pause the audio
                    audioPlayer.pauseAudio();
                } else {
                    // toggle off play button and toggle on pause button
                    enablePauseButtonAndDisablePlayButton();

                    // play or resume audio
                    audioPlayer.playCurrentAudio();
                }
            }
        });

        inputMap.put(KeyStroke.getKeyStroke("RIGHT"), "next");
        actionMap.put("next", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                audioPlayer.nextAudio();
            }
        });
        
    }

    // this will be used to update the slider from the AudioPlayer class
    public void setPlaybackSliderValue(int frame) {
        playbackSlider.setValue(frame);
    }

    public int getPlaybackSliderValue() {
        return playbackSlider.getValue();
    }

    public void updateAudioTitleAndArtist(Audio audio) {
        audioTitle.setText(audio.getAudioTitle());
        audioArtist.setText(audio.getAudioArtist());
    }

    public void updatePlaybackSlider(Audio audio) {
        try {
            // update max count for slider
            playbackSlider.setMaximum(audio.getMp3File().getFrameCount());

            // create the audio length label
            Hashtable<Integer, JLabel> labelTable = new Hashtable<>();

            // beginning will be 00:00
            JLabel labelBeginning = new JLabel("00:00");
            labelBeginning.setFont(new Font("Dialog", Font.BOLD, 18));
            labelBeginning.setForeground(TEXT_COLOR);

            // end will vary depending on the audio 
            JLabel labelEnd = new JLabel(audio.getAudioLength());
            labelEnd.setFont(new Font("Dialog", Font.BOLD, 18));
            labelEnd.setForeground(TEXT_COLOR);
                
            labelTable.put(0, labelBeginning);
            labelTable.put(audio.getMp3File().getFrameCount(), labelEnd);
            
            playbackSlider.setLabelTable(labelTable);
            playbackSlider.setPaintLabels(true);
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "can't play this audio");
        }    
    }

    public void enablePauseButtonAndDisablePlayButton() {
        // retrieve reference to play and pause buttons from playbackBtns panel
        JButton playButton = (JButton) playbackBtns.getComponent(1);
        JButton pauseButton = (JButton) playbackBtns.getComponent(2);

        // turn off play button
        playButton.setVisible(false);
        playButton.setEnabled(false);

        // turn on pause button
        pauseButton.setVisible(true);
        pauseButton.setEnabled(true);

    }

    public void enablePlayButtonAndDisablePauseButton() {
        // retrieve reference to play and pause buttons buttons from playbackBtns panel
        JButton playButton = (JButton) playbackBtns.getComponent(1);
        JButton pauseButton = (JButton) playbackBtns.getComponent(2);

        // turn on play button
        playButton.setVisible(true);
        playButton.setEnabled(true);

        // turn off pause button
        pauseButton.setVisible(false);
        pauseButton.setEnabled(false);

    }

    public String getMusicPath() {
        // Get the user's home directory
        String userHome = System.getProperty("user.home");

        // Determine the Music directory based on the operating system
        String musicPath;

        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("win")) 
            musicPath = userHome + "\\Music";
        else if (osName.contains("mac") || osName.contains("nix") || osName.contains("nux") || osName.contains("aix"))
            musicPath = userHome + "/Music";
        else
            musicPath = userHome + "/Music"; // Default path for unknown OS

        return musicPath;
    }

    private ImageIcon loadImage(String imagePath) {
        try {
            // read the image file from the given path
            BufferedImage image = ImageIO.read(new File(imagePath));

            // returns an image icon so that our component can render the image
            return new ImageIcon(image);
        } catch(Exception e) {
            e.printStackTrace();
        }

        // could not find resource
        return null;
    }

}
