package com.example.hp.moodplayer;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    Button mdetectmood;
    Button mrandommusic;

    String mood = "Random Music";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        mdetectmood = (Button) findViewById(R.id.detectmood);
        mrandommusic= (Button)  findViewById(R.id.randommusic);
    }
    public void onClick(View v){
        if(v.getId() == R.id.detectmood){
            Intent intent = new Intent(getBaseContext(), Detectmood.class);
                    startActivity(intent);
        }
        if(v.getId() == R.id.randommusic){
            Intent intent = new Intent(getBaseContext(), RandomMusic.class);
            intent.putExtra("Mood", mood);
            startActivity(intent);
        }
    }
}
