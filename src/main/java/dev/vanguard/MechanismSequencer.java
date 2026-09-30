package dev.vanguard;

/** Sensor-gated index/feed state machine. Outputs require a hardware adapter. */
public final class MechanismSequencer {
    public enum State { IDLE, INDEXING, PUSHING, RETRACTING, FAULT }
    public record Input(boolean armed,boolean feed,boolean reverse,int preset,double turret,
                        boolean touch,int encoder,double now) {}
    public record Output(State state,double shooter,double intake,double turret,double pusher,
                         int indexTarget,String status) {}
    private static final double[] SPEEDS={.45,.6,.75,.9};
    private final DebouncedSwitch touch=new DebouncedSwitch(.03);
    private State state=State.IDLE;
    private double stateSince,armedSince,lastTime=Double.NaN;
    private boolean wasFeed,wasArmed;
    private int preset=-1,target;
    private String status="idle";

    public Output update(Input input) {
        if(input==null||!Double.isFinite(input.now())||input.now()<0||(!Double.isNaN(lastTime)&&input.now()<lastTime)
                ||!Double.isFinite(input.turret())||Math.abs(input.turret())>1||input.preset()<0||input.preset()>=SPEEDS.length)throw new IllegalArgumentException("invalid mechanism input");
        lastTime=input.now();touch.update(input.touch(),input.now());
        boolean rising=input.feed()&&!wasFeed;wasFeed=input.feed();
        if(!input.armed()) {
            state=State.IDLE;target=input.encoder();status="disarmed";wasArmed=false;
            return new Output(state,0,0,0,0,target,status);
        }
        if(!wasArmed||preset!=input.preset()) {
            armedSince=input.now();preset=input.preset();
            if(state!=State.FAULT){enter(State.IDLE,input.now());target=input.encoder();status="waiting for shooter spin-up";}
        }
        wasArmed=true;
        if(state==State.IDLE&&rising) {
            if(input.now()-armedSince<.5)status="shooter spinning up";
            else if(!touch.pressed())status="no indexed disk detected";
            else {
                if(input.encoder()>Integer.MAX_VALUE-50)throw new IllegalArgumentException("index target exceeds encoder range");
                target=input.encoder()+50;enter(State.INDEXING,input.now());status="indexing disk";}
        }
        switch(state) {
            case INDEXING -> {
                if(Math.abs((long)target-input.encoder())<=2){enter(State.PUSHING,input.now());status="feeding";}
                else if(input.now()-stateSince>1){enter(State.FAULT,input.now());status="index timeout; disarm to reset";}
            }
            case PUSHING -> {if(input.now()-stateSince>=.15){enter(State.RETRACTING,input.now());status="retracting";}}
            case RETRACTING -> {if(input.now()-stateSince>=.2){enter(State.IDLE,input.now());status="ready";}}
            default -> {}
        }
        boolean fault=state==State.FAULT;
        return new Output(state,fault?0:SPEEDS[preset],fault?0:input.reverse()?-.6:.6,
            fault?0:input.turret()*.35,state==State.PUSHING?1:0,target,status);
    }
    private void enter(State next,double now){state=next;stateSince=now;}
}
