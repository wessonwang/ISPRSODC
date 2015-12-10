package cn.edu.uestc.wilson.ISPRSODC.client;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.javaml.core.kdtree.KDTree;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.edu.uestc.wilson.ISPRSODC.afc.AFCLocater;
import cn.edu.uestc.wilson.ISPRSODC.cluster.DBSCANCluster;
import cn.edu.uestc.wilson.ISPRSODC.cluster.DirectionSpliter;
import cn.edu.uestc.wilson.ISPRSODC.cluster.LWDBSCANCluster;
import cn.edu.uestc.wilson.ISPRSODC.cluster.ODConstructor;
import cn.edu.uestc.wilson.ISPRSODC.cluster.PatternDetecter;
import cn.edu.uestc.wilson.ISPRSODC.cluster.StationSorter;
import cn.edu.uestc.wilson.ISPRSODC.gps.BRMapper;
import cn.edu.uestc.wilson.ISPRSODC.gps.BusRouter;
import cn.edu.uestc.wilson.ISPRSODC.gps.Direction;
import cn.edu.uestc.wilson.ISPRSODC.gps.GPSReport;
import cn.edu.uestc.wilson.ISPRSODC.gps.Location;
import cn.edu.uestc.wilson.ISPRSODC.gps.Station;
import cn.edu.uestc.wilson.ISPRSODC.gps.TransitCenter;
import cn.edu.uestc.wilson.ISPRSODC.util.Centroid;
import cn.edu.uestc.wilson.ISPRSODC.util.Saver;

public class NewModelV1 {
    private static final Logger log = LoggerFactory.getLogger(NewModelV1.class);
    private static int sampleInterval = 0;
    private static double walkerSpeed = 0;
    private static double busCruisingSpeed = 0;
    private static int minSTAT = 0;
    private static double maxRadius = 0;
    private static double eps4OD = 0;
    private static int minPts4OD = 0;
    private static double eps4OD2 = 0;
    private static double terminalAreaRadius = 0;
    private static double eps4ST = 0;
    private static int minPts4ST = 0;
    private static int AFCFrontTime = 0;
    private static int AFCBackTime = 0;
    private static String GPSCsvPath = null;
    private static String BusRouteCsvPath = null;
    private static String ResultStopListPath = null;
    private static String AFCCsvPath = null;
    private static String NewAFCCsvPath = null;
    private static AFCLocater afcLocater = null;
    
    public static void main(String[] args) throws IOException {
	// TODO Auto-generated method stub
	if(args.length != 18){
	    System.out.println("Usage: sampleInterval(Int) walkerSpeed(Double) busCruisingSpeed(Double) "
	    	+ "minSTAT(Int) maxRadius(Double) eps4OD(Double) minPts4OD(Int) eps4OD2(Double) terminalAreaRadius(Double) "
	    	+ "eps4ST(Double) minPts4ST(Int) AFCFrontTime AFCBackTime /path/to/GPS_DATA_ROUTE /path/to/BUS_ROUTE.csv /path/to/save/RESULT_STOP_LIST.csv "
	    	+ "/path/to/AFC.csv /path/to/save/NewAFC.csv");
	    System.exit(0);
	}
	
	//Parameters area.
	sampleInterval = Integer.parseInt(args[0]);
	walkerSpeed = Double.parseDouble(args[1]);
	busCruisingSpeed = Double.parseDouble(args[2]);
	minSTAT = Integer.parseInt(args[3]);
	maxRadius = Double.parseDouble(args[4]);
	eps4OD = Double.parseDouble(args[5]);
	minPts4OD = Integer.parseInt(args[6]);
	eps4OD2 = Double.parseDouble(args[7]);
	terminalAreaRadius = Double.parseDouble(args[8]);
	eps4ST = Double.parseDouble(args[9]);
	minPts4ST = Integer.parseInt(args[10]);
	AFCFrontTime = Integer.parseInt(args[11]);
	AFCBackTime = Integer.parseInt(args[12]);
	GPSCsvPath = args[13];
	BusRouteCsvPath = args[14];
	ResultStopListPath = args[15];
	AFCCsvPath = args[16];
	NewAFCCsvPath = args[17];
	
	long bt = System.currentTimeMillis();
	BRMapper brMapper = new BRMapper(BusRouteCsvPath);
	Map<Integer,Integer> BusRouteMap = brMapper.getBusRouteMap();
	
	afcLocater = new AFCLocater(AFCFrontTime,AFCBackTime);
	
	TransitCenter transitCenter = NewModelV1.constructCenter(GPSCsvPath, BusRouteMap);
	
	List<Integer> routeIdList = brMapper.getRouteIdList();
	int invalidCount = 0;
	int routeSum = routeIdList.size();
	int stop_id = 1;
	for(int route_id:routeIdList){
	    log.info("Route_id: "+ route_id + " is processing...");
	    long rbt = System.currentTimeMillis();
	    
	    //To extract terminals.
	    BusRouter busRouter = transitCenter.getBusRouter(route_id);
	    Object[] BTObjects = busRouter.getAllBusTrajectory();
	    KDTree GPSKDTree = busRouter.getGPSKDTree();
	    Station[] terminals = NewModelV1.getStations(BTObjects, GPSKDTree);
	    
	    if(terminals == null){
		log.info("No terminals can be detected ");
		invalidCount++;
		routeSum--;
		continue;
	    }
	    
	    Station S1 = terminals[0];
	    Station S2 = terminals[1];
	    
	    //To split direction: UP/DOWN.
	    DirectionSpliter directerSpliter = new DirectionSpliter(terminalAreaRadius);
	    Map<Direction,HashSet<Location>> directedSationCandidate = directerSpliter.split(S1, S2, BTObjects, GPSKDTree);
	    
	    //Station clustering to each direction.
	    Map<Direction,ArrayList<Station>> routeStopMap = new HashMap<Direction,ArrayList<Station>>();
	    Set<Direction> dicSet = directedSationCandidate.keySet();
	    for(Direction dic:dicSet){
		ArrayList<Station> rawStopList = new ArrayList<Station>();
		rawStopList.add(S1);
		rawStopList.add(S2);
		
		HashSet<Location> directedSet = directedSationCandidate.get(dic);
		DBSCANCluster stationCluster = new DBSCANCluster(eps4ST,minPts4ST);
		List<List<Location>> stationScatters = stationCluster.run(directedSet, GPSKDTree);

		for(List<Location> stationScatter:stationScatters){
		    double[] stationCoordination = Centroid.compute(stationScatter);
		    Station station = new Station(stationCoordination[0],stationCoordination[1]);
		    rawStopList.add(station);
		}
		
		ArrayList<Station> newStopList = new ArrayList<Station>();
		LWDBSCANCluster stationFinalCluster = new LWDBSCANCluster(eps4OD2,0);
		List<List<Location>> stationFinalScatters = stationFinalCluster.run(rawStopList);
		for(List<Location> stationFinalScatter:stationFinalScatters){
		    double[] stationFinalCoordination = Centroid.compute(stationFinalScatter);
		    Station station = new Station(stationFinalCoordination[0],stationFinalCoordination[1]);
		    newStopList.add(station);
		}
		
		StationSorter stationSorter = new StationSorter(newStopList,dic,busRouter);
		newStopList = stationSorter.sort();
		routeStopMap.put(dic, newStopList);
	    }
	    
	    stop_id = Saver.saveStopList(route_id, stop_id,routeStopMap, ResultStopListPath);
	    
	    routeSum--;
	    
	    long rft = System.currentTimeMillis();
	    log.info("Processing of route "+route_id+" finished. "+(rft-rbt)+" millis spent.");
	    log.info(routeSum+ " routes left. "+(rft-bt)/1000+"s spent total. "+invalidCount+" routes are skipped.");
	}
	
	log.info("Stop detection finished.");
	NewModelV1.AFCLocate(BusRouteMap, transitCenter);
    }
    
    
    private static void AFCLocate(Map<Integer,Integer> BusRouteMap,TransitCenter transitCenter) throws IOException{
	Reader AFCin = new FileReader(AFCCsvPath);
	CSVFormat AFCcsv = CSVFormat.DEFAULT.withHeader("guid","card_id","day","time","bus_id").withSkipHeaderRecord();
	Iterable<CSVRecord> AFCRecords = AFCcsv.parse(AFCin);
	
	String NEW_LINE_SEPARATOR = "\n";
	FileWriter NewAFCWriter = new FileWriter(NewAFCCsvPath);
	CSVFormat NewAFCFormat = CSVFormat.DEFAULT.withRecordSeparator(NEW_LINE_SEPARATOR);
	CSVPrinter NewAFCPrinter = new CSVPrinter(NewAFCWriter,NewAFCFormat);
	
	long bt = System.currentTimeMillis()/1000;
	long ft = 0;
	int i = 0;
	for(CSVRecord AFCRecord:AFCRecords){
	    @SuppressWarnings("rawtypes")
	    ArrayList newAFC = afcLocater.locate(AFCRecord, BusRouteMap, transitCenter);
	    
	    if(newAFC.size() != 0){
		NewAFCPrinter.printRecord(newAFC);
	    }
	    
	    if(i%100000 == 0){
		ft = System.currentTimeMillis()/1000;
		log.info(i+" AFCs finished. "+(ft-bt)+"s spent.");
	    }
	    i++;
	}
	
	log.info("AFC Location finished.");
	AFCin.close();
	NewAFCWriter.flush();
	NewAFCWriter.close();
	NewAFCPrinter.close();
    }
    
    private static Station[] getStations(Object[] BTObjects,KDTree GPSKDTree){
	PatternDetecter patternDetecter = new PatternDetecter(sampleInterval,walkerSpeed,busCruisingSpeed,minSTAT,maxRadius);
	HashSet<Location> terminalCandidates = patternDetecter.getTerminalCandidates(BTObjects);
	DBSCANCluster dbCANCluster = new DBSCANCluster(eps4OD,minPts4OD);
	List<List<Location>> terminalCluster = dbCANCluster.run(terminalCandidates, GPSKDTree);
	Station[] terminals = ODConstructor.construct(terminalCluster);
	
	return terminals;
    }
    
    private static TransitCenter constructCenter(String GPSCsvPath,Map<Integer,Integer> BusRouteMap) throws IOException{
	Reader GPSin = new FileReader(GPSCsvPath);
	CSVFormat GPScsv = CSVFormat.DEFAULT.withHeader("bus_id","day","time","lng","lat").withSkipHeaderRecord();
	Iterable<CSVRecord> GPSrecords = GPScsv.parse(GPSin);
	
	TransitCenter transitCenter = new TransitCenter("SEHNZHEN");
	long bt = System.currentTimeMillis()/1000;
	long ft = 0;
	int i = 0;
	for(CSVRecord record:GPSrecords){
	    try{
		int bus_id = Integer.parseInt(record.get("bus_id"));
		int day = Integer.parseInt(record.get("day"));
		int time = Integer.parseInt(record.get("time"));
		double lng = Double.parseDouble(record.get("lng"));
		double lat = Double.parseDouble(record.get("lat"));
		int route_id = BusRouteMap.get(bus_id);
		
		if(lng<113.75 || lng>114.63 || lat<22.44 || lat>22.87){
		    continue;
		}
		
		GPSReport report = new GPSReport(time,lng,lat);
		transitCenter.receiveReport(route_id,bus_id,day,report);
	    }
	    catch(NumberFormatException e){
		continue;
	    }
	    
	    if(i%100000 == 0){
		ft = System.currentTimeMillis()/1000;
		log.info(i+" GPSs finished. "+(ft-bt)+"seconds spent.");
	    }
	    i++;
	}
	GPSin.close();
	return transitCenter;
    }
}
