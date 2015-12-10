package cn.edu.uestc.wilson.ISPRSODC.gps;

public class GPSReport extends Location{
    private int time;
    public MovePattern movePattern;
    public TerminalIndex terminalIndex;
    public Direction direction;
    
    /**
     * 
     * @param route_id
     * @param bus_id
     * @param day
     * @param time
     * @param lng
     * @param lat
     */
    public GPSReport(int time,double lng,double lat){
	super(lng,lat);
	this.time = time;
	this.movePattern = MovePattern.STATIONAREA;
	this.terminalIndex = TerminalIndex.ZERO;
	this.direction = Direction.TERMINAL;
    }
    
    public GPSReport(int time){
	super(0,0);
	this.time = time;
	this.direction = Direction.ERROR;
    }
    
    public int getTime(){
	return this.time;
    }
}
