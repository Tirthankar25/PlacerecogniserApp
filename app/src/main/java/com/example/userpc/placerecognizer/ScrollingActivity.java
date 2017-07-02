package com.example.userpc.placerecognizer;

import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.Locale;

public class ScrollingActivity extends AppCompatActivity implements TextToSpeech.OnInitListener{

    private TextToSpeech tts;
    private TextView text;
    private boolean isready=false;
    String speech;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scrolling);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        String title=getIntent().getStringExtra("output");
        toolbar.setTitle(title);
        setSupportActionBar(toolbar);

        String imageurl=getIntent().getStringExtra("img");
        ImageView img= (ImageView) findViewById(R.id.backdrop);
        Picasso.with(this)
                .load(imageurl)
                .into(img);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        tts=new TextToSpeech(this,this);
        text= (TextView) findViewById(R.id.text);
        speech=getIntent().getStringExtra("speech");

        text.setText(speech);
        Log.e("Speech",speech);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Playing the description", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                speakOut();
            }
        });
    }

    @Override
    public void onInit(int status) {

        if (status == TextToSpeech.SUCCESS) {
            int result = tts.setLanguage(Locale.US);
            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "This Language is not supported");
            } else {
                isready=true;
                speakOut();
            }

        } else {
            Log.e("TTS", "Initilization Failed!");
        }

    }

    @Override
    public void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }

    private void speakOut() {
        Log.e("Speech",""+isready);
        if(isready)
        {
            String txt=speech;
            tts.speak(txt, TextToSpeech.QUEUE_FLUSH, null);
        }
    }
}
