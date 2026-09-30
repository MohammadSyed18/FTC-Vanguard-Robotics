package dev.vanguard;

/** Cubic path geometry for inspecting autonomous routes without robot hardware. */
public final class BezierPath {
    public record Point(double x,double y) {
        public Point {if(!Double.isFinite(x)||!Double.isFinite(y))throw new IllegalArgumentException("finite point required");}
        public double distance(Point other){return Math.hypot(x-other.x,y-other.y);}
    }
    private final Point a,b,c,d;
    public BezierPath(Point a,Point b,Point c,Point d) {
        if(a==null||b==null||c==null||d==null)throw new IllegalArgumentException("four points required");
        this.a=a;this.b=b;this.c=c;this.d=d;
    }
    public Point at(double progress) {
        check(progress);double inverse=1-progress;
        return new Point(inverse*inverse*inverse*a.x()+3*inverse*inverse*progress*b.x()+3*inverse*progress*progress*c.x()+progress*progress*progress*d.x(),
            inverse*inverse*inverse*a.y()+3*inverse*inverse*progress*b.y()+3*inverse*progress*progress*c.y()+progress*progress*progress*d.y());
    }
    public Point derivative(double progress) {
        check(progress);double inverse=1-progress;
        return new Point(3*inverse*inverse*(b.x()-a.x())+6*inverse*progress*(c.x()-b.x())+3*progress*progress*(d.x()-c.x()),
            3*inverse*inverse*(b.y()-a.y())+6*inverse*progress*(c.y()-b.y())+3*progress*progress*(d.y()-c.y()));
    }
    public double heading(double progress) {Point tangent=derivative(progress);return Math.atan2(tangent.y(),tangent.x());}
    public double approximateLength(int subdivisions) {
        if(subdivisions<1||subdivisions>100000)throw new IllegalArgumentException("subdivisions must be 1..100000");
        double length=0;Point previous=a;
        for(int index=1;index<=subdivisions;index++){Point current=at((double)index/subdivisions);length+=previous.distance(current);previous=current;}
        return length;
    }
    public double nearestProgress(Point position,double minimumProgress) {
        check(minimumProgress);double best=minimumProgress,bestDistance=at(best).distance(position);
        double step=(1-minimumProgress)/64;
        for(int index=1;index<=64;index++){double at=minimumProgress+step*index;double distance=at(at).distance(position);if(distance<bestDistance){best=at;bestDistance=distance;}}
        double low=Math.max(minimumProgress,best-step),high=Math.min(1,best+step);
        for(int index=0;index<24;index++) {
            double first=low+(high-low)/3,second=high-(high-low)/3;
            if(at(first).distance(position)<at(second).distance(position))high=second;else low=first;
        }
        double refined=(low+high)/2;
        return at(refined).distance(position)<bestDistance?refined:best;
    }
    private void check(double progress){if(!Double.isFinite(progress)||progress<0||progress>1)throw new IllegalArgumentException("progress must be 0..1");}
}
