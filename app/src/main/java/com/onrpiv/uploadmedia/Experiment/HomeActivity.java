package com.onrpiv.uploadmedia.Experiment;

import android.content.Context;
import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.onrpiv.uploadmedia.Learn.Laser1;
import com.onrpiv.uploadmedia.Learn.LearnFluids;
import com.onrpiv.uploadmedia.Learn.LearnPIV;
import com.onrpiv.uploadmedia.R;
import com.onrpiv.uploadmedia.pivFunctions.PIVPopupWindow;

public class HomeActivity extends AppCompatActivity {

    private Button startExperimentButton;
    private Button learnAboutFluidsButton;
    private Button learnPIVButton;

    // popup window stuff
    private Context context;
    private RelativeLayout relativeLayout;
    private Button popupWindowButton1;
    private Button popupWindowButton2;
    private PopupWindow popupWindow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        startExperimentButton = (Button)findViewById(R.id.startExperimentButton);
        startExperimentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent mIPIVOpen = new Intent(HomeActivity.this, MainActivity.class);
                startActivity(mIPIVOpen);
            }
        });

        learnAboutFluidsButton = (Button)findViewById(R.id.learnFluidsButton);
        learnAboutFluidsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent fluidsOpen = new Intent(HomeActivity.this, LearnFluids.class);
                startActivity(fluidsOpen);
            }
        });

        learnPIVButton = (Button)findViewById(R.id.learnPIVButton);
        learnPIVButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent learnPIV = new Intent(HomeActivity.this, LearnPIV.class);
                startActivity(learnPIV);
            }
        });

        //
        // POPUP WINDOW FUNCTIONALITY
        //

        // Notes: The navigation stuff is possible, but kind of messy when it comes to the coding. I
        // would like to create a class that can handle the popup window so that I don't have to add
        // this implementation inside of EVERY class that needs a popup window. So far this isn't
        // possible because we need to add the function executePopupWindow() in the PIVPopupWindow
        // class, but this can't be done because we need to set the intent to start in whatever
        // class it's in (i.e. HomeActivity.this). This is the only issue.

        // NOTE: For the back arrow to appear, getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // must be added the desired destination. In this case, it's added into the LearnPIV and the
        // Laser1 classes. Along with that line of code, the following function must be added to the
        // class as well:

//        @Override
//        public boolean onOptionsItemSelected(MenuItem item) {
//            switch (item.getItemId()) {
//                case android.R.id.home:
//                    onBackPressed();
//                    return true;
//            }
//
//            return super.onOptionsItemSelected(item);
//        }


        context = getApplicationContext();
        relativeLayout = (RelativeLayout) findViewById(R.id.homeActivityRelativeView);

        popupWindowButton1 = (Button) findViewById(R.id.textBox1);
        popupWindowButton2 = (Button) findViewById(R.id.textBox2);

        String windowMessageTitle1 = "Learn PIV Button";
        String windowMessageTitle2 = "Learn Fluids Button";

        String windowMessage1 = "Hello my name is kevin.";
        String windowMessage2 = "Hi my name is kevin and this is some more text, more than the other button that I created and blah blah blah more stuff here let's go!";

        LearnPIV learnPIV = new LearnPIV();
        Laser1 laser1 = new Laser1();
        HomeActivity homeActivity = new HomeActivity();

        executePopupWindow(popupWindowButton1, windowMessageTitle1, windowMessage1, learnPIV);
        executePopupWindow(popupWindowButton2, windowMessageTitle2, windowMessage2, laser1);

        // GOAL: to create the class that's commented out below and have it's function called instead
        // of having the executePopupWindow() function in EVERY class that needs a popup window.

//        PIVPopupWindow piv = new PIVPopupWindow(context, relativeLayout, popupWindowButton1, windowMessage1, windowMessageTitle1, homeActivity, learnPIV);
//        piv.createPopupWindow();
    }

    private void executePopupWindow(Button button, final String popUpWindowTitle, final String popupWindowMessage, final Object myClass) {

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);

                final View customView = inflater.inflate(R.layout.popup_window_info, null);

                TextView windowTitle = (TextView) customView.findViewById(R.id.popupWindowTitle);
                windowTitle.setText(popUpWindowTitle);

                TextView windowMessage = (TextView) customView.findViewById(R.id.popupWindowMessage);
                windowMessage.setText(popupWindowMessage);

                // New instance of popup window
                popupWindow = new PopupWindow(customView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

                // Setting an elevation value for popup window, it requires API level 21
                if (Build.VERSION.SDK_INT >= 21) {
                    popupWindow.setElevation(5.0f);
                }

                Button navigateButton = (Button) customView.findViewById(R.id.button_navigate);
                navigateButton.setText("Learn more?");
                navigateButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(new Intent(HomeActivity.this, myClass.getClass()));
                    }
                });

                Button closeButton = (Button) customView.findViewById(R.id.button_close);
                closeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        popupWindow.dismiss();
                    }
                });

                popupWindow.showAtLocation(relativeLayout, Gravity.CENTER, 0, 0);
            }
        });
    }
}
