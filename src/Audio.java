import java.io.File;


import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;

import com.mpatric.mp3agic.Mp3File;

// class used to describe a audio
public class Audio {
    private String audioTitle;
    private String audioArtist;
    private String audioLength;
    private String filePath;
    private Mp3File mp3File;
    private double frameRatePerMilliseconds;

    // Constructor
    public Audio(String filePath) {
        this.filePath = filePath;
        try {
            mp3File = new Mp3File(filePath);
            frameRatePerMilliseconds = (double) mp3File.getFrameCount() / mp3File.getLengthInMilliseconds();
            audioLength = convertToAudioLengthFormat();

            // use the jaudiotagger library to create an audiofile obj to read mp3 file's information
            AudioFile audioFile = AudioFileIO.read(new File(filePath));

            // read through the meta data of the audio file
            Tag tag = audioFile.getTag();
            if(tag != null) {
                audioTitle = tag.getFirst(FieldKey.TITLE);
                audioArtist = tag.getFirst(FieldKey.ARTIST);

            } else {
                // could not read through mp3 file's meta data
                audioTitle = "N/A";
                audioArtist = "N/A";
            }
        } catch(Exception e) {
            //e.printStackTrace();
        }
    }

    private String convertToAudioLengthFormat() {
        long minutes = mp3File.getLengthInSeconds() / 60;
        long seconds = mp3File.getLengthInSeconds() % 60;
        String formattedTime = String.format("%02d:%02d", minutes, seconds);

        return formattedTime;
    }

    // getters
    public String getAudioTitle() { return audioTitle; }

    public String getAudioArtist() { return audioArtist; }

    public String getAudioLength() { return audioLength; }

    public String getFilePath() { return filePath; }

    public Mp3File getMp3File() { return mp3File; }

    public double getFrameRatePerMilliseconds() { return frameRatePerMilliseconds; }

}
