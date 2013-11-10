import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.advanced.*;
import org.tritonus.share.sampled.file.TAudioFileFormat;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class AudioTest extends JFrame {

    private static final String SAVE_FILE_PATH = "/home/alkisti/listOfSongs.txt";
    JList<String> list;
    JList<String> searchList;
    JFileChooser fc;
    AdvancedPlayer mp3Player;
    Thread t;
    File file;
    boolean playNextSong = false;
    JSlider slider;
    Timer timer2;
    Timer timer3;
    TextField search;
    char letter1;
    int index1;
    int match = 0;

    public AudioTest() {
        setLayout(new FlowLayout());
        init();
    }

    public void init() {

        search = new TextField("              ");
        add(search);
        search.setText("");
        JButton butSearch = new JButton("Search");
        add(butSearch);
        JPanel panel = new JPanel();
        JPanel panel2 = new JPanel();
        panel.setSize(400, 400);
        panel.setSize(400, 400);
        add(panel);
        add(panel2);
        final DefaultListModel<String> model = new DefaultListModel<String>();
        final DefaultListModel<String> model2 = new DefaultListModel<String>();
        list = new JList<String>(model);
        searchList = new JList<String>(model2);


        list.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                JList list = (JList) evt.getSource();
                file = new File(list.getSelectedValue().toString());
                if (evt.getClickCount() == 2) {
                    try {
                        playMusic();
                    } catch (Exception e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                }
            }
        });

        try {
            File txtFile = new File(SAVE_FILE_PATH);
            BufferedReader br = new BufferedReader(new FileReader(txtFile));
            String line;
            while ((line = br.readLine()) != null) {
                model.addElement(line);
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            fc = new JFileChooser("/home/alkisti/");
            JButton openFC = new JButton("Select file");
            openFC.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
                    int returnVal = fc.showOpenDialog(AudioTest.this);

                    if (returnVal == JFileChooser.APPROVE_OPTION) {
                        file = fc.getSelectedFile();
                        if (file.isDirectory()) {
                            File[] listOfFile = file.listFiles();
                            for (int j = 0; j < listOfFile.length; j++) {
                                String name = listOfFile[j].getName();
                                System.out.println(name);
                                if (name.endsWith(".mp3")) {
                                    model.addElement(listOfFile[j].getAbsolutePath());
                                }
                            }
                        } else {
                            String song = file.getAbsolutePath();
                            model.addElement(song);
                        }


                    }
                }
            });
            add(openFC);

        } catch (Exception a) {
            a.printStackTrace();
        }

        butSearch.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int songs = searchList.getModel().getSize();
                for (int i = 0; i < songs; i++) {
                    model2.removeElementAt(i);

                }

                String insertedWord = search.getText().toLowerCase();
                int numOfSongs = list.getModel().getSize();
                for (int i = 0; i < numOfSongs; i++) {
                    String[] currentItems = list.getModel().getElementAt(i).split("/");
                    String currentSong = currentItems[currentItems.length - 1].toLowerCase();
                    System.out.println("Current song : " + currentSong + " contains? " + currentSong.equalsIgnoreCase(insertedWord));
                    //if (currentSong.contains(insertedWord)) {
                    //   model2.addElement(list.getModel().getElementAt(i));
                    //}
                    if(doesKeyMatchValue(insertedWord, currentSong)) {
                       model2.addElement(list.getModel().getElementAt(i));
                    }
                }
            }

//                    if(Fuzzy.equals(currentSong, insertedWord)) {
//                        model2.addElement(list.getModel().getElementAt(i));
//                    }

//                for (int a = 0; a < numOfSongs; a++) {
//                    String[] currentItems = list.getModel().getElementAt(a).split("/");
//                    String currentSong = currentItems[currentItems.length - 1].toLowerCase();
//                }
            //}

            // }
        });

        JButton play = new JButton("Play");


        play.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                try {
                    playMusic();
                } catch (Exception e1) {
                    e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }


            }
        });
        JButton next = new JButton("next");
        next.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int elements = list.getModel().getSize();
                if (list.getSelectedIndex() < elements - 1) {
                    list.setSelectedIndex(list.getSelectedIndex() + 1);
                } else {
                    list.setSelectedIndex(0);
                }
                file = new File(list.getSelectedValue());
                System.out.println("Playing next song " + file.getName());

                playMusic();
            }
        });
        JButton previous = new JButton("previous");
        add(next);
        previous.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int elements = list.getModel().getSize();
                if (list.getSelectedIndex() > 0) {
                    list.setSelectedIndex(list.getSelectedIndex() - 1);
                } else {
                    list.setSelectedIndex(elements - 1);
                }
                file = new File(list.getSelectedValue());
                System.out.println("Playing next song " + file.getName());

                playMusic();
            }
        });
        add(previous);
        JButton stop = new JButton("Stop");
        stop.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                stopMusic();
            }
        });
        add(play);
        add(stop);
        JButton saveFile = new JButton("Save file");
        saveFile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                PrintWriter writer = null;
                try {
                    writer = new PrintWriter(SAVE_FILE_PATH, "UTF-8");
                } catch (FileNotFoundException e1) {
                    e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                } catch (UnsupportedEncodingException e1) {
                    e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
                int numOfSongs = list.getModel().getSize();
                for (int i = 0; i <= numOfSongs - 1; i++) {

                    writer.println(list.getModel().getElementAt(i));
                }

                writer.close();
            }
        });
        add(saveFile);
        add(list);
        add(searchList);
        panel.add(new JScrollPane(list));
        panel.add(new JScrollPane(searchList));

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                //check if song must change
                if (playNextSong) {
                    playNextSong = false;
                    int elements = list.getModel().getSize();
                    if (list.getSelectedIndex() < elements - 1) {
                        list.setSelectedIndex(list.getSelectedIndex() + 1);
                    } else {
                        list.setSelectedIndex(0);
                    }
                    file = new File(list.getSelectedValue());
                    System.out.println("Playing next song " + file.getName());

                    playMusic();
                }
            }
        }, 0, 1000);

        slider = new JSlider(JSlider.HORIZONTAL, 0, 100, 50); //min value 0, max value 100, initial value 50
        final JTextArea text = new JTextArea("50");
        add(slider);
        add(text);
        slider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                JSlider source = (JSlider) e.getSource();
                int value = (int) source.getValue();
                text.setText(Integer.toString(value));
            }
        });


    }


    private static int getDurationWithMp3Spi(File file) throws UnsupportedAudioFileException, IOException {

        AudioFileFormat fileFormat = AudioSystem.getAudioFileFormat(file);
        if (fileFormat instanceof TAudioFileFormat) {
            Map<?, ?> properties = ((TAudioFileFormat) fileFormat).properties();
            String key = "duration";
            Long microseconds = (Long) properties.get(key);
            int mili = (int) (microseconds / 1000);
            int seconds = mili / 1000;
            //int sec = (mili / 1000) % 60;
            //int min = (mili / 1000) / 60;
            //System.out.println("time = " + min + ":" + sec);
            return seconds;
        } else {
            throw new UnsupportedAudioFileException();
        }
    }

    public void playMusic() {
        if (timer2 != null) {
            timer2.cancel();
        }

        try {
            System.out.println("The duration of the song is : " + getDurationWithMp3Spi(file));
            slider.setMaximum(getDurationWithMp3Spi(file));
            slider.setValue(0);


//        AudioFileFormat baseFileFormat = new MpegAudioFileReader().getAudioFileFormat(file);
//        Map properties = baseFileFormat.properties();
//        Long duration = (Long) properties.get("duration");
//        System.out.println("playing music " + file.getName() + " " + duration);
            try {
                mp3Player = new AdvancedPlayer(new FileInputStream(file));
            } catch (JavaLayerException e1) {
                e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (FileNotFoundException e1) {
                e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }

            if (t != null && t.isAlive()) {
                t.stop();
                System.out.println("Stopping previous playback");
            }
            System.out.println("creating new playback");
            t = new Thread() {
                public void run() {
                    try {
                        mp3Player.play();
                        playNextSong = true;

                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            };
            t.start();
            System.out.println("Playing song " + file.getName());
            //File nextSong = new File(
        } catch (Exception e) {
            e.printStackTrace();
        }
        timer2 = new Timer();
        timer2.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                slider.setValue(slider.getValue() + 1);

            }
        }, 0, 1000);
    }

    public void stopMusic() {
        try {
            if (t != null && t.isAlive()) {
                t.stop();

            }
            System.out.println("Stoping song " + file.getName());

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        AudioTest window = new AudioTest();
        window.setVisible(true);
        window.setSize(500, 500);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    }

    public boolean doesKeyMatchValue(String insertedWord, String currentSong) {
        double match = 0.0;
        int lastPosition = -1;
        for (int index1 = 0; index1 < insertedWord.length(); index1++) {

            char letter1 = insertedWord.charAt(index1);

            for (int index2 = lastPosition + 1; index2 < currentSong.length(); index2++) {
                char letter2 = currentSong.charAt(index2);

                if (letter1 == letter2) {
                    if (lastPosition != -1) {
                        match -= (index2 - (lastPosition + 1)) * 0.2;
                    }

                    lastPosition = index2;
                    match++;
                    System.out.println("match letter" +  letter1 + " " + match);
                    break;
                }
            }
        }
        double success = match *1.0/ insertedWord.length();

        System.out.println(match + " " + insertedWord.length() + ", " + currentSong.length() + " " + success);

        return (success > 0.8);
    }

}