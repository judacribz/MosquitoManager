package ca.judacribz.mosquitomanager.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import butterknife.ButterKnife;

import ca.judacribz.mosquitomanager.R;

public class UI {

    // Constants
    // --------------------------------------------------------------------------------------------
    // --------------------------------------------------------------------------------------------

    // Global Vars
    // --------------------------------------------------------------------------------------------
    private static boolean backPressedTwice = false;
    // --------------------------------------------------------------------------------------------

    /* Sets the content view, title bar and ButterKnife for the given activity
     *
     * (REQUIRED) Include following as top element in activity layout file:
     *
    <include
       android:id="@id/toolbar"
       layout="@layout/part_titlebar"/>
     */
    public static void setInitView(Activity act, int layoutId, int titleId, boolean setBackArrow) {
        setInitView(act, layoutId, act.getResources().getString(titleId), setBackArrow);
    }
    public static void setInitView(Activity act, int layoutId, String title, boolean setBackArrow) {
        act.setContentView(layoutId);
        ButterKnife.bind(act);
        setToolbar((AppCompatActivity) act, title, setBackArrow);
    }

    /* Exits app and goes to home screen if back pressed twice from this screen */
    public static void handleBackButton(Context context) {
        if (backPressedTwice) {
            Intent exitApp = new Intent(Intent.ACTION_MAIN);
            exitApp.addCategory(Intent.CATEGORY_HOME);
            exitApp.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(exitApp);
        } else {
            backPressedTwice = true;
            Toast.makeText(context, "Press back again to exit", Toast.LENGTH_SHORT).show();

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    backPressedTwice = false;
                }
            }, 2000);
        }
    }

    /* Sets up the title bar */
    public static String setToolbar(AppCompatActivity act, int titleId, boolean setBackArrow) {
        return setToolbar(act, act.getResources().getString(titleId), setBackArrow);
    }
    public static String setToolbar(AppCompatActivity act, String title, boolean setBackArrow) {
        // Set the part_titlebar to the activity
        act.setSupportActionBar((Toolbar) act.findViewById(R.id.toolbar));


        ActionBar actionBar = act.getSupportActionBar();
        if (actionBar != null) {

            actionBar.setDisplayShowTitleEnabled(false);

            if (setBackArrow) {
                actionBar.setDisplayHomeAsUpEnabled(true);
                actionBar.setDisplayShowHomeEnabled(true);
            }
        }

        // Set the title for the part_titlebar
        ((TextView) act.findViewById(R.id.title)).setText(title);

        return title;
    }

    /* Sets the spinner with the given array resource in the Activity */
    public static void setSpinnerWithArray(Activity act, int arrResId, Spinner spr) {
        ArrayAdapter<CharSequence> adapter =
                ArrayAdapter.createFromResource(act,
                        arrResId,
                        android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spr.setAdapter(adapter);
    }

    /* Sets the spinner with the given array resource in the Activity */
    public static void setSpinnerWithArray(Activity act, ArrayList<String> strArr, Spinner spr) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(act, android.R.layout.simple_list_item_1, strArr);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spr.setAdapter(adapter);
    }



    /* Gets string value from EditText element */
    public static String getTextString(EditText et) {
        return et.getText().toString().trim();
    }

    /* Gets integer value of text from EditText element */
    public static int getTextInt(EditText et) {
        return Integer.valueOf(getTextString(et));
    }

    /* Gets float value of text from EditText element */
    public static float getTextFloat(EditText et) {
        return Float.valueOf(getTextString(et));
    }

    /* Validates a EditTexts to be non-empty or else an error is set */
    public static boolean validateForm(Activity act, EditText[] ets) {
        boolean isValid = true;
        String text;

        for (EditText et : ets) {
            text = et.getText().toString().trim();

            if (text.isEmpty()) {
                et.setError(act.getString(R.string.err_required));
                isValid = false;
            }
        }

        return isValid;
    }

    public static void clearForm(EditText[] ets) {
        for (EditText et : ets) {
            et.setText("");
        }
    }

//        /* Checks to see if the database exists */
//        public static boolean exists(Context context) {
//            File dbFile = new File(context.getDatabasePath(
//                    context.getResources().getString(
//                            R.string.workouts
//                    ).toLowerCase()
//            ).toString());
//
//            return dbFile.exists();
//        }
}