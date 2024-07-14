import java.awt.Color;
import java.awt.Font;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import button.JCustomButton;
import api.JnaFileChooser;

public class AudioPlaylistDialog extends JDialog {
    private AudioPlayerGUI audioPlayerGUI;

    // store all of the paths to be written to a txt file (when we load a playlist)
    private ArrayList<String> audioPaths;

    public AudioPlaylistDialog(AudioPlayerGUI audioPlayerGUI) {
        this.audioPlayerGUI = audioPlayerGUI;
        audioPaths = new ArrayList<>();

        // configure dialog
        setTitle("Create Playlist");
        setSize(400, 400);
        setResizable(false);
        getContentPane().setBackground(AudioPlayerGUI.FRAME_COLOR);
        setLayout(null);
        setModal(true); // this property makes it so that the dialog has to be closed to give focus
        setLocationRelativeTo(audioPlayerGUI);

        addDialogComponents();
        
    }

    private void addDialogComponents() {
        // container to hold each audio path
        JPanel audioContainer = new JPanel();
        audioContainer.setBackground(new Color(0xA1DD70));
        audioContainer.setLayout(new BoxLayout(audioContainer, BoxLayout.Y_AXIS));
        audioContainer.setBounds((int) (getWidth() * 0.025), 10, (int) (getWidth() * 0.90), (int) (getHeight() * 0.75));
        add(audioContainer);

        // add audio button
        JCustomButton addAudioButton = new JCustomButton("Add");
        addAudioButton.setBounds(60, (int) (getHeight() * 0.80), 100, 25);
        addAudioButton.setFont(new Font("Dialog", Font.BOLD, 14));
        addAudioButton.setRadius(15);
        addAudioButton.setBorder(null);
        addAudioButton.setColors(new Color(0xDEF9C4), null, Color.GRAY, Color.LIGHT_GRAY);
        addAudioButton.setForeground(new Color(0x1A5319));
        addAudioButton.setFocusable(false);
        addAudioButton.addActionListener(e ->  {
            // open file explorer
            JnaFileChooser jFileChooser = new JnaFileChooser();
            jFileChooser.setTitle("Create playlist");
            jFileChooser.addFilter("MP3 Files", "mp3");
            jFileChooser.setCurrentDirectory("src\\assests\\audios");
            jFileChooser.setOpenButtonText("Select");
            jFileChooser.setMultiSelectionEnabled(true);
            boolean result = jFileChooser.showOpenDialog(AudioPlaylistDialog.this);

            File[] selectedFiles = jFileChooser.getSelectedFiles();
            if (result && selectedFiles != null) {
                for (File file : selectedFiles) {
                    JLabel filePathLabel = new JLabel(file.getPath());
                    filePathLabel.setFont(new Font("Dialog", Font.BOLD, 12));
                    filePathLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));

                    // add to the list
                    audioPaths.add(filePathLabel.getText());

                    // add to container
                    audioContainer.add(filePathLabel);
                }

                //refreshes dialog to show newly added JLabels
                audioContainer.revalidate();

            }
        });
        add(addAudioButton);

        // save playlist button
        JCustomButton savePlaylistButton = new JCustomButton("Save");
        savePlaylistButton.setBounds(215, (int) (getHeight() * 0.80), 100, 25);
        savePlaylistButton.setFont(new Font("Dialog", Font.BOLD, 14));
        savePlaylistButton.setRadius(15);
        savePlaylistButton.setBorder(null);
        savePlaylistButton.setColors(new Color(0xDEF9C4), null, Color.GRAY, Color.LIGHT_GRAY);
        savePlaylistButton.setForeground(new Color(0x1A5319));
        savePlaylistButton.setFocusable(false);
        savePlaylistButton.addActionListener(e -> {
            try {
                JnaFileChooser jFileChooser = new JnaFileChooser();
                jFileChooser.setTitle("Playlist Name");
                jFileChooser.setCurrentDirectory("src\\assests\\playlists");
                boolean result = jFileChooser.showSaveDialog(AudioPlaylistDialog.this);

                if(result) {
                    // we use getSelectedFile() to get reference to the file that we are about to save
                    File selectedFile = jFileChooser.getSelectedFile();

                    // convert to txt file if not done so already
                    // this will check to see if the file does not have the ".txt" file extension
                    if(!selectedFile.getName().substring(selectedFile.getName().length() - 4).equalsIgnoreCase(".txt")) {
                        selectedFile = new File(selectedFile.getAbsoluteFile() + ".txt");
                    }

                    // create the new file at the destination directory
                    selectedFile.createNewFile();

                    // now we will write all of the audio paths to this
                    FileWriter fileWriter = new FileWriter(selectedFile);
                    BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

                    // iterate through our audio paths list and write each String into the file
                    // each audio will be written in their own row
                    for (String audioPath : audioPaths)
                        bufferedWriter.write(audioPath + "\n");

                    selectedFile.setWritable(false);

                    bufferedWriter.close();

                    // display success dialog
                    JOptionPane.showMessageDialog(AudioPlaylistDialog.this, "Created Playlist Successfully", "save playlist", JOptionPane.PLAIN_MESSAGE);
                    this.dispose();

                }
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        });
        add(savePlaylistButton);
    }
}
