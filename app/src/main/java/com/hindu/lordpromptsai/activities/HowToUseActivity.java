package com.hindu.lordpromptsai.activities;

import android.annotation.SuppressLint;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.hindu.lordpromptsai.R;
import com.google.android.material.appbar.MaterialToolbar;
public class HowToUseActivity extends AppCompatActivity {

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_how_to_use);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // IMPORTANT: disable default title (we use TextView)
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        LinearLayout container = findViewById(R.id.stepsContainer);

        addStep(container,
                "1", "Choose a Hindu God Category",
                "Swipe through the tabs at the top to explore different Hindu Gods categories like Lord Krishna, Lord Shiva etc.");

        addStep(container,
                "2", "Devotional Music (on/off)",
                "Tap the audio speaker icon to enhance your spiritual experience with calming and uplifting devotional music dedicated to each deity.");

        addStep(container,
                "3", "Select any Hindu God Image",
                "Tap on any lord image card that matches your idea or inspiration.");

        addStep(container,
                "4", "Generate a Prompt",
                "Tap Generate Prompt to generate a rich, detailed AI prompt that accurately describes the image.");

        addStep(container,
                "5", "Copy the Prompt",
                "Once the prompt generation is complete, tap Copy Prompt to save the prompt text.");

        addStep(container,
                "6", "Use in AI Tools",
                "Paste the copied prompt into ChatGPT, Gemini, Co-Pilot, or other AI art tools. Pls ensure you are logged-in to the AI tools else image generation feature will not be available");

        addStep(container,
                "7", "Save to Favourites",
                "Tap the heart icon to save prompts for quick access later.");

        addStep(container,
                "8", "Share with Others",
                "Use the Share button to send prompts to friends and family.");
    }

    private void addStep(
            LinearLayout container,
            String number,
            String title,
            String desc
    ) {
        View view = LayoutInflater.from(this)
                .inflate(R.layout.item_how_to_use_step, container, false);

        ((TextView) view.findViewById(R.id.tvStepNumber)).setText(number);
        ((TextView) view.findViewById(R.id.tvStepTitle)).setText(title);
        ((TextView) view.findViewById(R.id.tvStepDescription)).setText(desc);

        container.addView(view);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

}