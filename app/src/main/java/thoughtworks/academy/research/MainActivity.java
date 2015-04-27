package thoughtworks.academy.research;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;


public class MainActivity extends ActionBarActivity {

    private Button uploadButton;
    private TextView textView;

    public static final int MEDIA_TYPE_IMAGE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        uploadButton = (Button) findViewById(R.id.upload_button);
        textView = (TextView) findViewById(R.id.textView);

        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadFile();
            }
        });
    }

    private void uploadFile() {
        Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, 1);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == MEDIA_TYPE_IMAGE && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            uploadFileToServer(selectedImage);
        }

    }

    Handler myHandler = new Handler() {
        public void handleMessage(Message msg) {
            textView.setText(msg.getData().getString("result"));
            super.handleMessage(msg);
        }
    };

    private void uploadFileToServer(final Uri selectedImage) {
        new Thread() {
            @Override
            public void run() {
                super.run();
                URI imageUri = null;
                try {
                    imageUri = new URI(selectedImage.toString());
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }

                HttpClient httpClient = new DefaultHttpClient();
                HttpPost post  = new HttpPost("http://192.168.1.114:3000/uploadFile");
                File file = new File(getPath(selectedImage));
                FileBody fb = new FileBody(file);
                HttpEntity entity = MultipartEntityBuilder.create().addPart("file", fb).build();
                post.setEntity(entity);
                try {
                    HttpResponse response = httpClient.execute(post);
                    String res = EntityUtils.toString(response.getEntity());

                    Message msg = new Message();
                    Bundle bundle = new Bundle();
                    bundle.putString("result", res);
                    msg.setData(bundle);

                    System.out.print(res);
                    myHandler.sendMessage(msg);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }


    public String getPath(Uri uri) {
        String[] projection = { MediaStore.MediaColumns.DATA };
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
            String filePath = cursor.getString(columnIndex);
            cursor.close();
            return filePath;
        } else {
            return uri.getPath();
        }
    }
}
