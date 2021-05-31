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
    private static EasyBase config;
    public static void main(String[] args) throws Exception {
        config = new EasyBase("config");
        config.createData("portName", new String("COM3"));
        config.createData("S1", new String("Bus[0].Gain"));
        config.createData("S2", new String("Bus[1].Gain"));
        config.createData("S3", new String("Strip[1].Gain"));
        config.createData("S4", new String("Strip[2].Gain"));
        config.createData("S5", new String("Strip[3].Gain"));
        config.createData("S6", new String("Strip[5].Gain"));
        config.createData("S7", new String("Strip[6].Gain"));
        config.createData("S8", new String("Strip[0].Gain"));

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

                        if(map(sliderValuesIntRaw.get(0), 0, 1023, -30f, 12f) <= -30){
                            Voicemeeter.setParameterStringA("Bus[0].Mute", "true");
                        }
                        else{
                            Voicemeeter.setParameterStringA("Bus[0].Mute", "");
                            Voicemeeter.setParameterFloat((String) config.getData("S1"), map(sliderValuesIntRaw.get(0), 0, 1023, -30f, 12f));//Speaker
                        }

                        if(map(sliderValuesIntRaw.get(1), 0, 1023, -30f, 12f) <= -30){
                            Voicemeeter.setParameterStringA("Bus[1].Mute", "true");
                        }
                        else{
                            Voicemeeter.setParameterStringA("Bus[1].Mute", "");
                            Voicemeeter.setParameterFloat((String) config.getData("S2"), map(sliderValuesIntRaw.get(1), 0, 1023, -30f, 12f));//Headphones
                        }


                        if(map(sliderValuesIntRaw.get(2), 0, 1023, -25f, 12f) <= -25){
                            Voicemeeter.setParameterStringA("Strip[1].Mute", "true");
                        }
                        else{
                            Voicemeeter.setParameterStringA("Strip[1].Mute", "");
                            Voicemeeter.setParameterFloat((String) config.getData("S3"), map(sliderValuesIntRaw.get(2), 0, 1023, -25f, 12f)); //Spotify
                        }

                        if(sliderValuesIntVoicemeeter.get(3) <= -59){
                            Voicemeeter.setParameterStringA("Strip[2].Mute", "true");
                        }
                        else{
                            Voicemeeter.setParameterStringA("Strip[2].Mute", "");
                            Voicemeeter.setParameterFloat((String) config.getData("S4"), sliderValuesIntVoicemeeter.get(3)); //Browser
                        }


                        if(sliderValuesIntVoicemeeter.get(4) <= -59){
                            Voicemeeter.setParameterStringA("Strip[3].Mute", "true");
                        }
                        else{
                            Voicemeeter.setParameterStringA("Strip[3].Mute", "");
                            Voicemeeter.setParameterFloat((String) config.getData("S5"), sliderValuesIntVoicemeeter.get(4)); //Game
                        }

                        if(sliderValuesIntVoicemeeter.get(5) <= -59){
                            Voicemeeter.setParameterStringA("Strip[5].Mute", "true");
                        }
                        else{
                            Voicemeeter.setParameterStringA("Strip[5].Mute", "");
                            Voicemeeter.setParameterFloat((String) config.getData("S6"), sliderValuesIntVoicemeeter.get(5)); //VAIO
                        }

                        if(sliderValuesIntVoicemeeter.get(6) <= -59){
                            Voicemeeter.setParameterStringA("Strip[6].Mute", "true");
                        }
                        else{
                            Voicemeeter.setParameterStringA("Strip[6].Mute", "");
                            Voicemeeter.setParameterFloat((String) config.getData("S7"), sliderValuesIntVoicemeeter.get(6)); //AUX
                        }

                        if(sliderValuesIntVoicemeeter.get(7) <= -59){
                            Voicemeeter.setParameterStringA("Strip[0].Mute", "true");
                        }
                        else{
                            Voicemeeter.setParameterStringA("Strip[0].Mute", "");
                            Voicemeeter.setParameterFloat((String) config.getData("S8"), sliderValuesIntVoicemeeter.get(7)); //Mic
                        }




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