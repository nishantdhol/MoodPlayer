package com.example.hp.moodplayer;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Handler;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import com.google.gson.Gson;
import com.microsoft.projectoxford.emotion.EmotionServiceClient;
import com.microsoft.projectoxford.emotion.EmotionServiceRestClient;
import com.microsoft.projectoxford.emotion.contract.RecognizeResult;
import com.microsoft.projectoxford.emotion.rest.EmotionServiceException;
import com.example.hp.moodplayer.helper.ImageHelper;

/**
 * Created by hp on 03-11-2017.
 */

public class Detectmood extends Activity implements View.OnClickListener {
    public String mood;
    private static final int CAMERA_REQUEST = 1888;

    // Flag to indicate which task is to be performed.
    private static final int REQUEST_SELECT_IMAGE = 0;

    // The button to select an image
    private Button mButtonSelectImage;

    // The URI of the image selected to detect.
    private Uri mImageUri;

    // The image selected to detect.
    private Bitmap mBitmap;

    // The edit to show status and result.
    private TextView mTextView;


    private EmotionServiceClient client;

    private String textIntro = "Analyzing emotions from photos\n\nPlease select a photo To analyze emotions";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detectmood);

        if (client == null) {
            client = new EmotionServiceRestClient(getString(R.string.subscription_key));
        }

        mButtonSelectImage = (Button) findViewById(R.id.buttonSelectImage);
        mTextView = (TextView) findViewById(R.id.textViewResult);


        Toast toastIntro = Toast.makeText(Detectmood.this, textIntro, Toast.LENGTH_LONG);
        toastIntro.show();
    }


    // Called when image selection is done.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("Detectmood", "onActivityResult");
        switch (requestCode) {
            case CAMERA_REQUEST:

                if (resultCode == RESULT_OK) {
                    // If image is selected successfully, set the image URI and bitmap.
                    mImageUri = data.getData();

                    mBitmap = ImageHelper.loadSizeLimitedBitmapFromUri(mImageUri, getContentResolver());
                    if (mBitmap != null) {
                        // Show the image on screen.
                        ImageView imageView = (ImageView) findViewById(R.id.selectedImage);
                        imageView.setImageBitmap(mBitmap);

                        // Add detection log.
                        Log.d("RecognizeActivity", "Image: " + mImageUri + " resized to " + mBitmap.getWidth()
                                + "x" + mBitmap.getHeight());

                        doRecognize();
                    }
                }
                break;
            default:
                break;
        }
    }
    public void doRecognize() {
        mButtonSelectImage.setEnabled(false);

        // Do emotion detection using auto-detected faces.
        try {
            new doRequest().execute();
        } catch (Exception e) {
            mTextView.append("Error encountered. Exception is: " + e.toString());
        }
    }


    private List<RecognizeResult> processWithAutoFaceDetection() throws EmotionServiceException, IOException {
        Log.d("emotion", "Start emotion detection with auto-face detection");

        Gson gson = new Gson();

        // Put the image into an input stream for detection.
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, output);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(output.toByteArray());

        long startTime = System.currentTimeMillis();
        List<RecognizeResult> result = null;
        //
        // Detect emotion by auto-detecting faces in the image.
        //
        result = this.client.recognizeImage(inputStream);

        String json = gson.toJson(result);
        Log.d("result", json);
        Log.d("emotion", String.format("Detection done. Elapsed time: %d ms", (System.currentTimeMillis() - startTime)));

        return result;
    }

    @Override
    public void onClick(View target) {
      if(target == mButtonSelectImage){
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            }

    }

    private class doRequest extends AsyncTask<String, String, List<RecognizeResult>> {
        // Store error message
        private Exception e = null;

        @Override
        protected List<RecognizeResult> doInBackground(String... args) {
            try {
                return processWithAutoFaceDetection();
            } catch (Exception e) {
                this.e = e;    // Store error
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<RecognizeResult> result) {
            super.onPostExecute(result);
            // Display based on error existence
            if (e != null) {
                mTextView.setText("Error:\n" + "Please check the connection. Internet ");
                this.e = null;
            } else {
                if (result.size() == 0) {
                    mTextView.setText("");
                    mTextView.append("Face not found !!! :(");
                } else {
                    int count = 0;

                    Toast toastReselt = Toast.makeText(Detectmood.this, "\t\t\tAnalyze successfully\n\n \n" +
                            "View the results of the analysis", Toast.LENGTH_LONG);
                    toastReselt.show();

                    for (RecognizeResult r : result) {
                        mTextView.setText("");

                        mTextView.append(String.format("Faces %1$d -->", count + 1));


                        if (r.scores.anger > 0.60) {
                            mood="anger";
                            mTextView.append(String.format("\t anger : \t%.2f %%\n", r.scores.anger * 100));
                            int secs = 5; // Delay in seconds

                            Utils.delay(secs, new Utils.DelayCallback() {
                                @Override
                                public void afterDelay() {
                                    onCreateView();
                                }
                            });

                        } else if (r.scores.contempt > 0.60) {
                            mood="contempt";
                            mTextView.append(String.format("\t contempt : \t%.2f %%\n", r.scores.contempt * 100));
                            int secs = 5; // Delay in seconds

                            Utils.delay(secs, new Utils.DelayCallback() {
                                @Override
                                public void afterDelay() {
                                    onCreateView();
                                }
                            });

                        } else if (r.scores.disgust   > 0.60) {
                            mood="disgust";
                            mTextView.append(String.format("\t disgust : \t%.2f %%\n", r.scores.disgust * 100));
                            int secs = 5; // Delay in seconds

                            Utils.delay(secs, new Utils.DelayCallback() {
                                @Override
                                public void afterDelay() {
                                    onCreateView();
                                }
                            });

                        } else if (r.scores.fear      > 0.60) {
                            mood="fear";
                            mTextView.append(String.format("\t fear : \t%.2f %%\n", r.scores.fear * 100));
                            int secs = 5; // Delay in seconds

                            Utils.delay(secs, new Utils.DelayCallback() {
                                @Override
                                public void afterDelay() {
                                    onCreateView();
                                }
                            });

                        }else if (r.scores.happiness > 0.60) {
                            mood="happiness";
                            mTextView.append(String.format("\t happiness : \t%.2f %%\n", r.scores.happiness * 100));
                            int secs = 5; // Delay in seconds

                            Utils.delay(secs, new Utils.DelayCallback() {
                                @Override
                                public void afterDelay() {
                                    onCreateView();
                                }
                            });


                        }else if (r.scores.neutral   > 0.60) {
                            mood="neutral";
                            mTextView.append(String.format("\t neutral : \t%.2f %%\n", r.scores.neutral * 100));
                            int secs = 5; // Delay in seconds

                            Utils.delay(secs, new Utils.DelayCallback() {
                                @Override
                                public void afterDelay() {
                                    onCreateView();
                                }
                            });

                        }else if (r.scores.sadness   > 0.60) {
                            mood="sadness";
                            mTextView.append(String.format("\t sadness : \t%.2f %%\n", r.scores.sadness * 100));
                            int secs = 5; // Delay in seconds

                            Utils.delay(secs, new Utils.DelayCallback() {
                                @Override
                                public void afterDelay() {
                                    onCreateView();
                                }
                            });

                        }else if (r.scores.surprise  > 0.60) {
                            mood="surprise";
                            mTextView.append(String.format("\t suprise: \t%.2f %%\n", r.scores.surprise * 100));
                            int secs = 5; // Delay in seconds

                            Utils.delay(secs, new Utils.DelayCallback() {
                                @Override
                                public void afterDelay() {
                                    onCreateView();
                                }
                            });

                        }  else  mTextView.append(String.format("\t No Emotion \n"));

                        count++;
                    }
                    ImageView imageView = (ImageView) findViewById(R.id.selectedImage);
                    imageView.setImageDrawable(new BitmapDrawable(getResources(), mBitmap));
                }
            }
            mButtonSelectImage.setEnabled(true);
        }
    }
    public void onCreateView() {

    Intent intent = new Intent(getBaseContext(), RandomMusic.class);
        intent.putExtra("Mood",mood);
        startActivity(intent);
    }


}
 class Utils {

    // Delay mechanism

    public interface DelayCallback{
        void afterDelay();
    }

    public static void delay(int secs, final DelayCallback delayCallback){
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                delayCallback.afterDelay();
            }
        }, secs * 1000); // afterDelay will be executed after (secs*1000) milliseconds.
    }
}