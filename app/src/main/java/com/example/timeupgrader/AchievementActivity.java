package com.example.timeupgrader;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class AchievementActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_achievement);

        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.container_achievement);
        if (fragment == null) {
            fragment = new AchievementFragment();
            fm.beginTransaction().add(R.id.container_achievement, fragment).commit();
        }
    }
}
