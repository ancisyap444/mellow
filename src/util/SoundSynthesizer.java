package util;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;

public final class SoundSynthesizer {
    private static volatile boolean muted = false;

    private SoundSynthesizer() {
    }

    public static boolean isMuted() {
        return muted;
    }

    public static void setMuted(boolean isMuted) {
        muted = isMuted;
    }

    public static void toggleMute() {
        muted = !muted;
    }

    public static void playIncomingCallBeep() {
        playToneSequence(new double[]{880.0, 1174.6}, new int[]{65, 85}, 0.22);
    }

    public static void playDispatchConfirm() {
        playToneSequence(new double[]{659.25, 987.77}, new int[]{55, 75}, 0.24);
    }

    public static void playRejectBuzz() {
        playToneSequence(new double[]{195.0, 155.0}, new int[]{85, 110}, 0.26);
    }

    public static void playCalmChime() {
        playToneSequence(new double[]{523.25, 659.25, 783.99}, new int[]{55, 55, 75}, 0.20);
    }

    public static void playResolvedFanfare() {
        playToneSequence(new double[]{587.33, 783.99, 1046.50}, new int[]{60, 60, 110}, 0.25);
    }

    public static void playAftershockRumble() {
        playToneSequence(new double[]{92.5, 73.4, 82.4}, new int[]{110, 120, 130}, 0.30);
    }

    private static void playToneSequence(double[] freqs, int[] durationsMs, double volume) {
        if (muted || freqs == null || durationsMs == null || freqs.length != durationsMs.length) {
            return;
        }
        Thread audioThread = new Thread(() -> {
            try {
                float sampleRate = 22050f;
                AudioFormat format = new AudioFormat(sampleRate, 8, 1, true, false);
                try (SourceDataLine line = AudioSystem.getSourceDataLine(format)) {
                    line.open(format);
                    line.start();
                    for (int n = 0; n < freqs.length; n++) {
                        int samples = (int) ((durationsMs[n] / 1000.0) * sampleRate);
                        byte[] buf = new byte[samples];
                        for (int i = 0; i < samples; i++) {
                            double angle = 2.0 * Math.PI * i * freqs[n] / sampleRate;
                            double env = i < 80 ? (i / 80.0) : (i > samples - 160 ? ((samples - i) / 160.0) : 1.0);
                            buf[i] = (byte) (Math.sin(angle) * 127.0 * volume * Math.max(0.0, env));
                        }
                        line.write(buf, 0, buf.length);
                    }
                    line.drain();
                }
            } catch (Exception ignored) {
            }
        });
        audioThread.setDaemon(true);
        audioThread.start();
    }
}
