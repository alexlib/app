package com.onrpiv.uploadmedia.pivFunctions;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.onrpiv.uploadmedia.R;

public class PIVPopupWindow extends AppCompatActivity {

    // popup window variables
    private Context context;
    private RelativeLayout relativeLayout;
    private Button popupWindowButton;
    private PopupWindow popupWindow;
    private String popupWindowTitle;
    private String popupWindowMessage;
    private final Object myClass;
    private final Object toClass;

    // popup window class are variable declaration
    public PIVPopupWindow(Context myContext, RelativeLayout myRelativeLayout, Button myPopupWindowButton, String myWindowMessage, String myPopupWindowTitle, final Object thisMyClass, final Object thisToClass) {
        context = myContext;
        relativeLayout = myRelativeLayout;
        popupWindowButton = myPopupWindowButton;
        popupWindowTitle = myPopupWindowTitle;
        popupWindowMessage = myWindowMessage;
        myClass = thisMyClass; // class we want to start from
        toClass = thisToClass; // class we want to navigate to
    }

//    public void createPopupWindow() {
//
//        popupWindowButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                LayoutInflater inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
//
//                final View customView = inflater.inflate(R.layout.popup_window_info, null);
//
//                TextView windowTitle = (TextView) customView.findViewById(R.id.popupWindowTitle);
//                windowTitle.setText(popupWindowTitle);
//
//                TextView windowMessage = (TextView) customView.findViewById(R.id.popupWindowMessage);
//                windowMessage.setText(popupWindowMessage);
//
//                // New instance of popup window
//                popupWindow = new PopupWindow(customView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//
//                // Setting an elevation value for popup window, it requires API level 21
//                if (Build.VERSION.SDK_INT >= 21) {
//                    popupWindow.setElevation(5.0f);
//                }
//
//                Button navigateButton = (Button) customView.findViewById(R.id.button_navigate);
//                navigateButton.setText("Learn more?");
//                navigateButton.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        // Right here is the problem. We want to start in myClass.this (aka
//                        // HomeActivity.this), but how?
//                        startActivity(new Intent(context, toClass.getClass()));
//                    }
//                });
//
//                Button closeButton = (Button) customView.findViewById(R.id.button_close);
//                closeButton.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        popupWindow.dismiss();
//                    }
//                });
//
//                popupWindow.showAtLocation(relativeLayout, Gravity.CENTER, 0, 0);
//            }
//        });
//    }
}
