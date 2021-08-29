package com.example.soundtest2;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.media.MediaParser;
import android.media.MediaPlayer;
import android.os.Bundle;

import com.example.adapter.AudioAdapter;
import com.example.adapter.ClickItemListener;
import com.example.model.Audio;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    MediaPlayer mediaPlayer;

    RecyclerView rcvNatural,rvcTraffic,rcvFurniture,rcvSound;
    AudioAdapter audioAdapterNatrual, audioAdapterTraffic,
            audioAdapterFurniture, audioAdapterSound;

    Audio selectedAudio;
    String check = "";
    List<Audio> generateList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        addControls();
    }

    private void addControls() {
        // setAdapter for rcvNatural
        rcvNatural = findViewById(R.id.rcvNatural);
        audioAdapterNatrual = new AudioAdapter(MainActivity.this, new ClickItemListener() {
            @Override
            public void onClickItem(int audioPosition) {
                generateList = getListDataNatural();
                Audio audio = generateList.get(audioPosition);
                solveOnClick(audio);
            }
        });
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this,4);
        rcvNatural.setLayoutManager(gridLayoutManager);
        audioAdapterNatrual.setData(getListDataNatural());
        rcvNatural.setAdapter(audioAdapterNatrual);


        // setAdapter for rcvTraffic
        rvcTraffic = findViewById(R.id.rcvTraffic);
        audioAdapterTraffic = new AudioAdapter(MainActivity.this, new ClickItemListener() {
            @Override
            public void onClickItem(int audioPosition) {
                generateList = getListDataTraffic();
                Audio audio = generateList.get(audioPosition);
                solveOnClick(audio);
            }
        });
        GridLayoutManager gridLayoutManager1 = new GridLayoutManager(this,4);
        rvcTraffic.setLayoutManager(gridLayoutManager1);
        audioAdapterTraffic.setData(getListDataTraffic());
        rvcTraffic.setAdapter(audioAdapterTraffic);


        // setAdapter for rcvFurniture
        rcvFurniture = findViewById(R.id.rcvFurniture);
        audioAdapterFurniture = new AudioAdapter(MainActivity.this, new ClickItemListener() {
            @Override
            public void onClickItem(int audioPosition) {
                generateList = getListDataFurniture();
                Audio audio = generateList.get(audioPosition);
                solveOnClick(audio);
            }
        });
        GridLayoutManager gridLayoutManager2 = new GridLayoutManager(this,4);
        rcvFurniture.setLayoutManager(gridLayoutManager2);
        audioAdapterFurniture.setData(getListDataFurniture());
        rcvFurniture.setAdapter(audioAdapterFurniture);


        // setAdapter for rcvSound
        rcvSound = findViewById(R.id.rcvSound);
        audioAdapterSound = new AudioAdapter(MainActivity.this, new ClickItemListener() {
            @Override
            public void onClickItem(int audioPosition) {
                generateList = getListDataSound();
                Audio audio = generateList.get(audioPosition);
                solveOnClick(audio);
            }
        });
        GridLayoutManager gridLayoutManager3 = new GridLayoutManager(this,4);
        rcvSound.setLayoutManager(gridLayoutManager3);
        audioAdapterSound.setData(getListDataSound());
        rcvSound.setAdapter(audioAdapterSound);

    }

    private List<Audio> getListDataSound() {
        List<Audio> list = new ArrayList<>();
        list.add(new Audio("sound", R.raw.white_noise, R.drawable.ic_soundwave));
        return list;
    }

    private List<Audio> getListDataFurniture() {
        List<Audio> list = new ArrayList<>();
        list.add(new Audio("washingmachine", R.raw.washing_machine, R.drawable.ic_washingmachine));
        list.add(new Audio("vacuum", R.raw.vacuum_cleaner, R.drawable.ic_vacuum));
        list.add(new Audio("clock", R.raw.clock, R.drawable.ic_clock));
        list.add(new Audio("radio", R.raw.radio, R.drawable.ic_radio));
        list.add(new Audio("hairdryer", R.raw.hair_dryer, R.drawable.ic_hairdryer));
        list.add(new Audio("fan", R.raw.fan, R.drawable.ic_fan));
        list.add(new Audio("shower", R.raw.shower, R.drawable.ic_shower));
        list.add(new Audio("catsnores", R.raw.cat_purring, R.drawable.ic_cat));

        return list;
    }

    private List<Audio> getListDataTraffic() {
        List<Audio> list = new ArrayList<>();
        list.add(new Audio("car", R.raw.car, R.drawable.ic_car));
        list.add(new Audio("truck", R.raw.bus, R.drawable.ic_truck));
        list.add(new Audio("train", R.raw.train, R.drawable.ic_train));
        list.add(new Audio("plane", R.raw.airplane, R.drawable.ic_airplane));

        return list;
    }

    private List<Audio> getListDataNatural() {
        List<Audio> list = new ArrayList<>();
        list.add(new Audio("wave", R.raw.ocean, R.drawable.ic_wave));
        list.add(new Audio("fire", R.raw.fire, R.drawable.ic_fire));
        list.add(new Audio("waterflowing", R.raw.creek, R.drawable.ic_river));
        list.add(new Audio("forest", R.raw.forest, R.drawable.ic_forest));
        list.add(new Audio("heartbeat", R.raw.heart_beat, R.drawable.ic_heart));
        list.add(new Audio("windy", R.raw.wind, R.drawable.ic_windy));
        list.add(new Audio("rain", R.raw.rain, R.drawable.ic_rainy));
        list.add(new Audio("crikets", R.raw.night, R.drawable.ic_insect));

        return list;
    }

    private void solveOnClick(Audio audio) {
        selectedAudio = audio;
        if(mediaPlayer == null ) {
            mediaPlayer = mediaPlayer.create(MainActivity.this,selectedAudio.getMusic_file());
        }
        else{
            if(selectedAudio.getKey().equals(check)) {

            }
            else  {
                if(mediaPlayer.isPlaying())
                    mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = mediaPlayer.create(MainActivity.this,selectedAudio.getMusic_file());
            }
        }
        check = audio.getKey();
        if(mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
        else {
            mediaPlayer.start();
        }
    }
}