package ca.judacribz.mosquitomanager;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.OnClick;
import ca.judacribz.mosquitomanager.models.CB;
import ca.judacribz.mosquitomanager.models.DataHelper;

import static ca.judacribz.mosquitomanager.R.layout.activity_catch_basin;
import static ca.judacribz.mosquitomanager.firebase.Database.addCBFirebase;
import static ca.judacribz.mosquitomanager.util.UI.*;

public class CatchBasin extends AppCompatActivity implements LocationListener {

    private static final int REQUEST_GEOLOCATION_PERMS = 1;
    private LocationManager locationManager;

    private Double lon, lat;

    private EditText[] forms;

    private DataHelper dataHelper;

    @BindView(R.id.et_samplingDate)
    EditText etSamplingDate;
    @BindView(R.id.et_city)
    EditText etCity;
    @BindView(R.id.et_address)
    EditText etAddress;
    @BindView(R.id.et_lat)
    EditText etLat;
    @BindView(R.id.et_lon)
    EditText etLon;
    @BindView(R.id.et_numLarvae)
    EditText etNumLarvae;

    @BindView(R.id.spr_devStage)
    Spinner sprDevStage;

    @BindView(R.id.btn_addData)
    Button btnAddData;
    @BindView(R.id.btn_showCoords)
    Button btnShowCoords;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setInitView(this, activity_catch_basin, R.string.catch_basin_surveillance,  true);

        btnAddData.setText(R.string.add_data);
        btnShowCoords.setText(R.string.update_location_info);

        forms = new EditText[]{etSamplingDate, etCity, etAddress, etNumLarvae};
        dataHelper = new DataHelper(this);

        setDate();
        setSpinnerWithArray(this, R.array.life_stages, sprDevStage);

        verifyGeolocationPermission();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }

    private void setDate() {
        Date c = Calendar.getInstance().getTime();
        System.out.println("Current time => " + c);

        SimpleDateFormat df = new SimpleDateFormat("yy-MM-dd", Locale.CANADA);
        String formattedDate = df.format(c);
        etSamplingDate.setText(formattedDate);
    }

    private void verifyGeolocationPermission() {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            String[] perms = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
            requestPermissions(perms, REQUEST_GEOLOCATION_PERMS);
        } else {
            // geolocation permission granted, so request location updates
            verifyGeolocationEnabled();
        }
    }

    private void verifyGeolocationEnabled() {
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        if (locationManager != null) {
            // check if geolocation is enabled in settings
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                requestLocationUpdates();
            } else {
                // show the settings app to let the user enable it
                String locationSettings =
                        Settings.ACTION_LOCATION_SOURCE_SETTINGS;
                Intent enableGeoloc = new Intent(locationSettings);
                startActivity(enableGeoloc);

                // Note:  startActivityForResult() may be better here
            }
        }
    }

    private void requestLocationUpdates() {
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setPowerRequirement(Criteria.POWER_MEDIUM);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setSpeedRequired(false);
        criteria.setCostAllowed(false);

        String recommendedProvider = locationManager.getBestProvider(criteria,
                true);

        if (   checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {

            locationManager.requestLocationUpdates(
                    recommendedProvider,
                    3000,
                    1,
                    this
            );
        }
    }

    private Address geocode(double latitude, double longitude) {
        Address address = null;
        if (Geocoder.isPresent()) {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());

            try {
                List<Address> results = geocoder.getFromLocation(latitude,
                        longitude,
                        1);

                if (results.size() > 0) {
                    address = results.get(0);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return address;
    }

    @Override
    public void onLocationChanged(Location location) {

        Address address;
        lat = location.getLatitude();
        lon = location.getLongitude();

        // geocode the result - get the location name
        address = geocode(lat, lon);
        updateUI(address);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }


    @Override
    public boolean onCreateOptionsMenu(Menu mainMenu) {
        getMenuInflater().inflate(R.menu.menu_catch_basin, mainMenu);

        return super.onCreateOptionsMenu(mainMenu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.act_view_data:
                startActivity(new Intent(this, CatchBasinData.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    @OnClick(R.id.btn_showCoords)
    public void showCoordinates() {
        if (   ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
            || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            if (locationManager != null) {
                Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (lastKnownLocation == null)
                     lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

                if (lastKnownLocation != null) {
                    onLocationChanged(lastKnownLocation);
                    lat = lastKnownLocation.getLatitude();
                    lon = lastKnownLocation.getLongitude();
                    updateUI(geocode(lat, lon));
                }
            } else {
                Toast.makeText(this, "No last known Location", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void updateUI(Address address) {
        if (address != null) {
            etAddress.setText(address.getAddressLine(0).split(getString(R.string.comma))[0]);
            etCity.setText(address.getLocality());
            etLat.setText(String.valueOf(lat));
            etLon.setText(String.valueOf(lon));
        }
    }


    @OnClick(R.id.btn_addData)
    public void addData() {
        if (validateForm(this, forms)) {

            CB cb = new CB(
                    getTextString(forms[0]),
                    getTextString(forms[1]),
                    getTextString(forms[2]),
                    getTextInt(forms[3]),
                    sprDevStage.getSelectedItem().toString()
            );
            long id = dataHelper.addCatchBasin(cb);
            if (id != -1) {
                addCBFirebase(cb, id);
            } else {
                Toast.makeText(this, "Not Added", Toast.LENGTH_SHORT).show();
            }
        }
    }
}