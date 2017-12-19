package kushagra.capstone;

/**
 * Created by kushagra on 2/10/17.
 */

public class Location_object {
    private double latitude;
    private double longitude;
    public Location_object() {

    }
    public Location_object(double latitude,double longitude) {
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
