import java.util.*;
import java.io.*;
public class LocationAnalyzer {

    // This runs automatically every time a new GPS point comes in
    // lat and lon are sent straight from LocationServer
    public static boolean onLocationReceived(double latit, double lon) throws Exception 
    {
        ArrayList <Double> lat = new ArrayList <Double>();
        ArrayList <Double> longitude = new ArrayList <Double>();
        File myFile = new File("big_cedar_surf_zones.csv");
        Scanner myScan = new Scanner(myFile);

        while(myScan.hasNextLine())
        {
            String line = myScan.nextLine();
            String [] myLine = line.split(",");

            double longi = Double.parseDouble(myLine[1]);
            double lati = Double.parseDouble(myLine[0]);

            lat.add(lati);
            longitude.add(longi);

        }
        boolean isBoth=false; //this means unless this changed the GPS location is not in surf zone. 
        for(int i=0;i<lat.size();i++)
        {
            if((lon==(longitude.get(i)))&&(latit==lat.get(i)))
                isBoth=true;
        }
        return isBoth;

    }

    public static boolean getResult(double lat, double lon) throws Exception 
    {
        boolean result = onLocationReceived(lat, lon);
        return result;
    }
}