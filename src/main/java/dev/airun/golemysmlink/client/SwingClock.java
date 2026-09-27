package dev.airun.golemysmlink.client;

public final class SwingClock {
    private boolean swinging;
    private boolean offhand;
    private int previousTime;
    private int startTick = Integer.MIN_VALUE;

    public int sample(boolean active, int time, int tick, boolean left) {
        if (active && (!swinging || left != offhand || time < previousTime)) {
            startTick = tick;
        }
        swinging = active;
        offhand = left;
        previousTime = time;
        return active && startTick != tick ? Math.max(0, time) : 0;
    }
}
