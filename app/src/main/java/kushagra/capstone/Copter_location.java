package kushagra.capstone;

/**
 * Created by kushagra on 3/12/17.
 */

public class Copter_location {
    private double latitude;
    private double longitude;
    public Copter_location() {
    }
    public Copter_location(double latitude,double longitude,boolean valid) {
        this.latitude=latitude;
        this.longitude=longitude;
    }
    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}
