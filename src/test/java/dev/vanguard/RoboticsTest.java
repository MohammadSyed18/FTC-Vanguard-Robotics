package dev.vanguard;
import java.util.List;

public class RoboticsTest {
    private static void near(double actual,double expected){if(Math.abs(actual-expected)>1e-4)throw new AssertionError(actual+" != "+expected);}
    private static MechanismSequencer.Input input(boolean armed,boolean feed,boolean touch,int encoder,double now){return new MechanismSequencer.Input(armed,feed,false,2,0,touch,encoder,now);}
    public static void main(String[] args) {
        var path=new BezierPath(new BezierPath.Point(0,0),new BezierPath.Point(3,0),new BezierPath.Point(7,0),new BezierPath.Point(10,0));
        near(path.approximateLength(100),10);near(path.at(0).x(),0);near(path.at(1).x(),10);
        near(path.nearestProgress(new BezierPath.Point(5,2),0),.5);
        var report=new RouteDiagnostics().analyze(path,List.of(new RouteDiagnostics.Observation(0,new BezierPath.Point(0,1)),new RouteDiagnostics.Observation(1,new BezierPath.Point(5,1))));
        near(report.meanCrossTrack(),1);
        var switchInput=new DebouncedSwitch(.03);if(switchInput.update(true,0))throw new AssertionError("early edge");
        if(!switchInput.update(true,.04))throw new AssertionError("missing stable edge");
        if(switchInput.update(true,.1))throw new AssertionError("repeated edge");
        var mechanisms=new MechanismSequencer();mechanisms.update(input(true,false,true,0,0));mechanisms.update(input(true,false,true,0,.6));
        var output=mechanisms.update(input(true,true,true,0,.61));if(output.indexTarget()!=50)throw new AssertionError("index step");
        output=mechanisms.update(input(true,true,true,50,.7));if(output.state()!=MechanismSequencer.State.PUSHING)throw new AssertionError("not feeding");
        output=mechanisms.update(input(true,true,true,50,.9));if(output.pusher()!=0)throw new AssertionError("pusher did not retract");
        output=mechanisms.update(input(false,false,true,50,1));near(output.shooter(),0);near(output.intake(),0);
        var changing=new MechanismSequencer();changing.update(input(true,false,true,0,0));changing.update(input(true,false,true,0,.6));
        changing.update(input(true,true,true,0,.61));changing.update(input(true,true,true,50,.7));
        var switched=changing.update(new MechanismSequencer.Input(true,false,false,3,0,true,50,.71));
        if(switched.pusher()!=0||switched.state()!=MechanismSequencer.State.IDLE)throw new AssertionError("preset change did not cancel feed");
        System.out.println("Vanguard geometry and mechanism checks passed");
    }
}
