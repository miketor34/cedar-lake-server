import java.util.*;
import java.io.*;
public class LocationAnalyzer {

    // This runs automatically every time a new GPS point comes in
    // lat and lon are sent straight from LocationServer
    public static boolean onLocationReceived(double latit, double lon, String fileName) throws Exception 
    {
        ArrayList <Double> lat = new ArrayList <Double>();
        ArrayList <Double> longitude = new ArrayList <Double>();
        File myFile = new File(fileName);
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
            if(Math.abs(lon - longitude.get(i)) < 0.0002 && Math.abs(latit - lat.get(i)) < 0.0002)
                isBoth=true;
        }
        return isBoth;

    }

    public static boolean getResult(double lat, double lon, String fileNombre) throws Exception 
    {
        boolean result = onLocationReceived(lat, lon,fileNombre);
        return result;
    }
}
