package kushagra.capstone;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;

/**
 * Created by kushagra on 15/12/17.
 */

public class DownloadImage extends AsyncTask<String,Void,Bitmap> {
    ImageView imageView;
    Context context;
    DownloadImage(ImageView imageView,Context context) {
        this.imageView = imageView;
        this.context = context;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Bitmap doInBackground(String... url) {
        String image_url=url[0];
        Bitmap image = null;
        try {
            InputStream in = new java.net.URL(image_url).openStream();
            image = BitmapFactory.decodeStream(in);
        }catch (MalformedURLException e) {
            e.printStackTrace();
            Toast.makeText(context,"Invalid Url",Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context,"IO Problem",Toast.LENGTH_LONG).show();
        }
        return image;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        imageView.setImageBitmap(bitmap);
        super.onPostExecute(bitmap);
    }
}
