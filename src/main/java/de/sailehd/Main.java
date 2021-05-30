package de.sailehd;

import com.fazecast.jSerialComm.*;
import de.sailehd.Voicemeeter.Voicemeeter;
import de.sailehd.support.Debug;
import de.sailehd.support.EasyBase;
import de.sailehd.support.TextColor;

import java.io.*;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {

    private Thread loop;

    public static void main(String[] args) throws Exception {
        EasyBase config = new EasyBase("config");
        config.createData("portName", new String("COM3"));

        String portName = (String) config.getData("portName");
        SerialPort comPort = null;

        Voicemeeter.init(true);
        Voicemeeter.login();


        for (SerialPort port: SerialPort.getCommPorts()) {
            if(port.getDescriptivePortName().contains(portName)){
                comPort = port;

            }
        }

        try {
            comPort.openPort();
            Debug.log(TextColor.GREEN + "Connected to port " + comPort.getDescriptivePortName() + ".");
            comPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 0, 0);
            Main main = new Main();
            main.runLoop(comPort);

        }
        catch (Exception e){
            Debug.log(TextColor.RED + "could not connect to " + comPort.getDescriptivePortName());
        }

    }

    public void runLoop(SerialPort port){
        this.loop = new Thread(() -> {
            long time = System.currentTimeMillis();

            InputStream in = port.getInputStream();
            try
            {
                Scanner scan = new Scanner(in);;

                int startSkip = 20;
                while (scan.hasNextLine()){
                    String line = scan.nextLine();
                    if(startSkip <= 0){
                        ArrayList<Integer> sliderValuesIntRaw = new ArrayList<Integer>();
                        ArrayList<Float> sliderValuesIntVoicemeeter = new ArrayList<Float>();

                        for (Integer value: parseInput(line)) {
                            sliderValuesIntRaw.add(map(value, 0, 1023, 1023, 0));
                            sliderValuesIntVoicemeeter.add(map(value, 0, 1023, 12f, -60f));
                        }

                        Voicemeeter.setParameterFloat("Bus[0].Gain", sliderValuesIntVoicemeeter.get(0)); //Speaker
                        Voicemeeter.setParameterFloat("Bus[1].Gain", sliderValuesIntVoicemeeter.get(1)); //Headphones
                        Voicemeeter.setParameterFloat("Strip[1].Gain", sliderValuesIntVoicemeeter.get(2)); //Spotify
                        Voicemeeter.setParameterFloat("Strip[2].Gain", sliderValuesIntVoicemeeter.get(3)); //Browser
                        Voicemeeter.setParameterFloat("Strip[3].Gain", sliderValuesIntVoicemeeter.get(4)); //Game
                        Voicemeeter.setParameterFloat("Strip[5].Gain", sliderValuesIntVoicemeeter.get(5)); //VAIO
                        Voicemeeter.setParameterFloat("Strip[6].Gain", sliderValuesIntVoicemeeter.get(6)); //AUX
                        Voicemeeter.setParameterFloat("Strip[0].Gain", sliderValuesIntVoicemeeter.get(7)); //Mic

                        Debug.log("Speaker: " + sliderValuesIntVoicemeeter.get(0) +
                                " # " + "Headphones: " + sliderValuesIntVoicemeeter.get(1) +
                                " # " + "Spotify: " + sliderValuesIntVoicemeeter.get(2) +
                                " # " + "Browser: " + sliderValuesIntVoicemeeter.get(3) +
                                " # " + "Game: " + sliderValuesIntVoicemeeter.get(4) +
                                " # " + "VAIO: " + sliderValuesIntVoicemeeter.get(5) +
                                " # " + "Aux: " + sliderValuesIntVoicemeeter.get(6) +
                                " # " + "Mic: " + sliderValuesIntVoicemeeter.get(7));
                    }
                    else{
                        startSkip--;
                    }
                }
            } catch (Exception e) { e.printStackTrace(); }


        });
        this.loop.setName("Loop");
        this.loop.start();
    }

    public static ArrayList<Integer> parseInput(String line){
        ArrayList<Integer> sliderValues = new ArrayList<Integer>();

        String[] valuesRaw = line.split("\\|");

        for (String valueRaw: valuesRaw) {
            sliderValues.add(Integer.parseInt(valueRaw));
        }

        return sliderValues;
    }

    public static long map(long x, long in_min, long in_max, long out_min, long out_max)
    {
        return (x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min;
    }

    public static float map(float x, float in_min, float in_max, float out_min, float out_max)
    {
        return (x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min;
    }

    public static int map(int x, int in_min, int in_max, int out_min, int out_max)
    {
        return (x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min;
    }
}