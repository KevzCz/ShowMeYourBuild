package net.pixeldreamstudios.showmeyourbuild.client.renderer;

public class PotionEffectRenderer {

    public static String formatDuration(int ticks, boolean isSnapshot) {
        if (ticks < 0 || ticks == Integer.MAX_VALUE) return "∞";

        int seconds = ticks / 20;
        int minutes = seconds / 60;
        seconds %= 60;

        return String.format("%d:%02d", minutes, seconds);
    }

}
