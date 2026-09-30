package dev.vanguard;

/** Reports one rising edge only after an input stays stable for the debounce time. */
public final class DebouncedSwitch {
    private final double seconds;
    private boolean candidate,stable;
    private double candidateSince,lastTime=Double.NaN;
    public DebouncedSwitch(double seconds){if(!Double.isFinite(seconds)||seconds<0)throw new IllegalArgumentException("invalid debounce");this.seconds=seconds;}
    public boolean update(boolean raw,double now) {
        if(!Double.isFinite(now)||now<0||(!Double.isNaN(lastTime)&&now<lastTime))throw new IllegalArgumentException("monotonic time required");
        lastTime=now;
        if(raw!=candidate){candidate=raw;candidateSince=now;}
        boolean rising=false;
        if(candidate!=stable&&now-candidateSince>=seconds){stable=candidate;rising=stable;}
        return rising;
    }
    public boolean pressed(){return stable;}
}
