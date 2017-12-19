package kushagra.capstone;

/**
 * Created by kushagra on 15/12/17.
 */

public class Image_info {
    private double latitude;
    private double longitude;
    public String name="";
    public Image_info() {

    }
    public Image_info(String name,double latitude, double longitude) {
        this.latitude=latitude;
        this.name = name;
        this.longitude=longitude;
    }
    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}
