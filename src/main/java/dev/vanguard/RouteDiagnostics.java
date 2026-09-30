package dev.vanguard;

import java.util.List;

public final class RouteDiagnostics {
    public record Observation(double time,BezierPath.Point position) {}
    public record Report(int samples,double meanCrossTrack,double maximumCrossTrack,double finalProgress) {}
    public Report analyze(BezierPath path,List<Observation> observations) {
        if(observations.isEmpty())throw new IllegalArgumentException("observations required");
        double progress=0,sum=0,maximum=0,last=-1;
        for(var observation:observations) {
            if(!Double.isFinite(observation.time())||observation.time()<0||observation.time()<last)throw new IllegalArgumentException("observations must be chronological");
            last=observation.time();progress=path.nearestProgress(observation.position(),progress);
            double error=path.at(progress).distance(observation.position());sum+=error;maximum=Math.max(maximum,error);
        }
        return new Report(observations.size(),sum/observations.size(),maximum,progress);
    }
}
