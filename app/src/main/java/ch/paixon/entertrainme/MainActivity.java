package ch.paixon.entertrainme;


import android.Manifest;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.common.util.ArrayUtils;

import java.text.DecimalFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.ListIterator;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import butterknife.OnTextChanged;
import ch.paixon.entertrainme.dtos.ConnectionContainerDto;
import ch.paixon.entertrainme.dtos.LocationContainerDto;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @BindView(R.id.inputFromAuto)
    EditText fromStationAuto;
    @BindView(R.id.inputToAuto)
    EditText toStationAuto;
    @BindView(R.id.labelFrom)
    TextView fromLabel;
    @BindView(R.id.labelTo)
    TextView toLabel;
    @BindView(R.id.departure1)
    TextView departure1;
    @BindView(R.id.arrival1)
    TextView arrival1;
    @BindView(R.id.departure2)
    TextView departure2;
    @BindView(R.id.arrival2)
    TextView arrival2;
    @BindView(R.id.departure3)
    TextView departure3;
    @BindView(R.id.arrival3)
    TextView arrival3;
    @BindView(R.id.departure4)
    TextView departure4;
    @BindView(R.id.arrival4)
    TextView arrival4;
    @BindView(R.id.departure5)
    TextView departure5;
    @BindView(R.id.arrival5)
    TextView arrival5;
    @BindView(R.id.departure6)
    TextView departure6;
    @BindView(R.id.arrival6)
    TextView arrival6;
    @BindView(R.id.radio_arrival)
    RadioButton arrivalRadio;
    @BindView(R.id.radio_departure)
    RadioButton departureRadio;
    @BindView(R.id.check_train)
    CheckBox trainCheck;
    @BindView(R.id.check_bus)
    CheckBox busCheck;
    @BindView(R.id.check_tram)
    CheckBox tramCheck;
    @BindView(R.id.check_ship)
    CheckBox shipCheck;
    @BindView(R.id.check_cableway)
    CheckBox cablewayCheck;
    @BindView(R.id.fromLocation)
    Button locationFrom;
    @BindView(R.id.toLocation)
    Button locationTo;
    @BindView(R.id.homeAddressAuto)
    EditText homeAddress;

    Button btnDatePicker, btnTimePicker;
    EditText txtDate, txtTime;
    private int mYear, mMonth, mDay, mHour, mMinute;
    private String isArrivalTime = "0";
    private static String NUMBER_OF_CONNECTIONS_LIMIT = "6";
    private LocationManager locationManager;
    private String actualLongitude;
    private String actualLatitude;

    private Instant timestamp = new Date().toInstant();
    private ZonedDateTime switzerlandTimeZone = timestamp.atZone(ZoneId.of("Europe/Zurich"));
    private DateTimeFormatter formatterDate = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private DateTimeFormatter formatterTime = DateTimeFormatter.ofPattern("HH:mm");
    private String actualDate = switzerlandTimeZone.format(formatterDate);
    private String actualTime = switzerlandTimeZone.format(formatterTime);

    private ArrayList<String> autocompleteList = new ArrayList<String>();
    private ArrayList<Pair<EditText, EditText>> favoritesList = new ArrayList<Pair<EditText, EditText>>();
    private static String SPINNER_PLACEHOLDER = "SELECT FAVORITE";
    private String[] favoritesArrayForSpinner = {SPINNER_PLACEHOLDER};
    private ArrayAdapter<String> adapterForSpinner;
    private Spinner dynamicSpinner;
    private boolean isUpdateInputs = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        initSupportActionBar();
        initDateAndTimePicker();
        initLocationManager();
        initAboutActivity();
        initSpinnerForFavorites();
        initTakeMeHome();

        arrivalRadio.setChecked(false);
        departureRadio.setChecked(true);
        trainCheck.setChecked(true);

        setLaterConnectionsVisibility(false);
        View showLessConnectionsButton = findViewById(R.id.showLessConnectionsButton);
        showLessConnectionsButton.setVisibility(View.GONE);
    }

    @Override
    public void onClick(View view) {
        DecimalFormat format = new DecimalFormat("###00.##");
        if (view == btnDatePicker) {
            final Calendar c = Calendar.getInstance();
            mYear = c.get(Calendar.YEAR);
            mMonth = c.get(Calendar.MONTH);
            mDay = c.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                    txtDate.setText(format.format(dayOfMonth) + "." + format.format(monthOfYear + 1) + "." + year);
                }
            }, mYear, mMonth, mDay);
            datePickerDialog.show();
        }
        if (view == btnTimePicker) {
            final Calendar c = Calendar.getInstance();
            mHour = c.get(Calendar.HOUR_OF_DAY);
            mMinute = c.get(Calendar.MINUTE);
            TimePickerDialog timePickerDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                    txtTime.setText(format.format(hourOfDay) + ":" + format.format(minute));
                }
            }, mHour, mMinute, false);
            timePickerDialog.show();
        }
    }

    private void initAboutActivity() {
        Button aboutButton = findViewById(R.id.about);
        aboutButton.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, AboutActivity.class)));
    }

    private void initSupportActionBar(){
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.drawable.logo_entertrainme_white_transparent_cut);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setTitle("  " + getSupportActionBar().getTitle());
    }

    /**
     * Set Arrival/Departure Station Logic
     */

    @OnTextChanged(R.id.inputFromAuto)
    public void onTextChangedFrom() {
        getLocationsForAutocomplete(fromStationAuto.getText().toString(), R.id.inputFromAuto);
        updateFavoriteButtonColor();
    }

    @OnTextChanged(R.id.inputToAuto)
    public void onTextChangedTo() {
        getLocationsForAutocomplete(toStationAuto.getText().toString(), R.id.inputToAuto);
        updateFavoriteButtonColor();
    }

    public void updateAutocomplete(int pId) {
        System.out.println(autocompleteList);
        AutoCompleteTextView editText = findViewById(pId);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, autocompleteList);
        editText.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    public void getLocationsForAutocomplete(String pLocation, int pId) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://transport.opendata.ch/v1/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        LocationService locationService = retrofit.create(LocationService.class);
        locationService.getLocations(pLocation, "", "", "station").enqueue(new Callback<LocationContainerDto>() {
            @Override
            public void onResponse(Call<LocationContainerDto> call, Response<LocationContainerDto> response) {
                if (response.isSuccessful()) {
                    LocationContainerDto locationContainer = response.body();
                    autocompleteList.clear();
                    for (int i = 0; i < locationContainer.stations.size(); i++) {
                        autocompleteList.add(locationContainer.stations.get(i).name);
                    }
                    updateAutocomplete(pId);
                }
            }

            @Override
            public void onFailure(Call<LocationContainerDto> call, Throwable t) {
                Log.d("Test", "fail");
            }

        });
    }

    /**
     * Search Connection Logic
     */

    @OnClick(R.id.search)
    public void onSearchClick() {

        departure1.setText("");
        arrival1.setText("");
        departure2.setText("");
        arrival2.setText("");
        departure3.setText("");
        arrival3.setText("");
        departure4.setText("");
        arrival4.setText("");
        departure5.setText("");
        arrival5.setText("");
        departure6.setText("");
        arrival6.setText("");

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://transport.opendata.ch/v1/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        ConnectionService connectionsService = retrofit.create(ConnectionService.class);
        connectionsService.searchConnections(fromStationAuto.getText().toString(), toStationAuto.getText().toString(), getTransportationOptions(), txtDate.getText().toString(), txtTime.getText().toString(), isArrivalTime, NUMBER_OF_CONNECTIONS_LIMIT).enqueue(new Callback<ConnectionContainerDto>() {
            @Override
            public void onResponse(Call<ConnectionContainerDto> call, Response<ConnectionContainerDto> response) {
                if (response.isSuccessful()) {
                    System.out.println(response);
                    fromLabel.setText(fromStationAuto.getText().toString());
                    toLabel.setText(toStationAuto.getText().toString());
                    ConnectionContainerDto connectionsContainer = response.body();
                    int numberOfConnections = connectionsContainer.connections.size();

                    if (numberOfConnections > 0) {
                        departure1.setText(DateTimeConverter(connectionsContainer.connections.get(0).from.departure.toInstant()));
                        arrival1.setText(DateTimeConverter(connectionsContainer.connections.get(0).to.arrival.toInstant()));
                    }
                    if (numberOfConnections > 1) {
                        departure2.setText(DateTimeConverter(connectionsContainer.connections.get(1).from.departure.toInstant()));
                        arrival2.setText(DateTimeConverter(connectionsContainer.connections.get(1).to.arrival.toInstant()));
                    }
                    if (numberOfConnections > 2) {
                        departure3.setText(DateTimeConverter(connectionsContainer.connections.get(2).from.departure.toInstant()));
                        arrival3.setText(DateTimeConverter(connectionsContainer.connections.get(2).to.arrival.toInstant()));
                    }
                    if (numberOfConnections > 3) {
                        departure4.setText(DateTimeConverter(connectionsContainer.connections.get(3).from.departure.toInstant()));
                        arrival4.setText(DateTimeConverter(connectionsContainer.connections.get(3).to.arrival.toInstant()));
                    }
                    if (numberOfConnections > 4) {
                        departure5.setText(DateTimeConverter(connectionsContainer.connections.get(4).from.departure.toInstant()));
                        arrival5.setText(DateTimeConverter(connectionsContainer.connections.get(4).to.arrival.toInstant()));
                    }
                    if (numberOfConnections > 5) {
                        departure6.setText(DateTimeConverter(connectionsContainer.connections.get(5).from.departure.toInstant()));
                        arrival6.setText(DateTimeConverter(connectionsContainer.connections.get(5).to.arrival.toInstant()));
                    }
                }
            }

            @Override
            public void onFailure(Call<ConnectionContainerDto> call, Throwable t) {
                Log.d("Test", "fail");
            }
        });
    }

    /**
     * Set Departure/Arrival Time and Transportation Logic
     */

    public String getTransportationOptions() {
        String tempString = new String();
        tempString = "";
        if (trainCheck.isChecked()) {
            tempString = tempString + "train";
        }
        if (busCheck.isChecked()) {
            if (tempString.equals("")) {
                tempString = tempString + "bus";
            } else {
                tempString = tempString + ",bus";
            }
        }
        if (tramCheck.isChecked()) {
            if (tempString.equals("")) {
                tempString = tempString + "tram";
            } else {
                tempString = tempString + ",tram";
            }
        }
        if (shipCheck.isChecked()) {
            if (tempString.equals("")) {
                tempString = tempString + "ship";
            } else {
                tempString = tempString + ",ship";
            }
        }
        if (cablewayCheck.isChecked()) {
            if (tempString.equals("")) {
                tempString = tempString + "cableway";
            } else {
                tempString = tempString + ",cableway";
            }
        }
        return tempString;
    }

    public void onRadioButtonClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();

        switch (view.getId()) {
            case R.id.radio_arrival:
                if (checked)
                    isArrivalTime = "1";
                break;
            case R.id.radio_departure:
                if (checked)
                    isArrivalTime = "0";
                break;
        }
    }

    /**
     * Set my Location Logic
     */

    private void initLocationManager() {
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
    }

    @OnClick(R.id.fromLocation)
    public void onFromLocationClick() {
        updateActualLocation();
        setNextStation(R.id.inputFromAuto);
    }

    @OnClick(R.id.toLocation)
    public void onToLocationClick() {
        updateActualLocation();
        setNextStation(R.id.inputToAuto);
    }

    public void updateActualLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Location actualLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (actualLocation != null) {
            actualLongitude = String.valueOf(actualLocation.getLongitude());
            actualLatitude = String.valueOf(actualLocation.getLatitude());
        }
    }

    public void setNextStation(int pId) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://transport.opendata.ch/v1/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        LocationService locationService = retrofit.create(LocationService.class);
        if (actualLongitude != null && actualLatitude != null) {
            locationService.getLocations("", actualLongitude, actualLatitude, "station").enqueue(new Callback<LocationContainerDto>() {
                @Override
                public void onResponse(Call<LocationContainerDto> call, Response<LocationContainerDto> response) {
                    System.out.println(actualLongitude);
                    System.out.println(actualLatitude);
                    System.out.println(response);
                    if (response.isSuccessful()) {
                        LocationContainerDto locationContainer = response.body();
                        AutoCompleteTextView editText = findViewById(pId);
                        editText.setText(locationContainer.stations.get(0).name);
                    }
                }

                @Override
                public void onFailure(Call<LocationContainerDto> call, Throwable t) {
                    Log.d("Test", "fail");
                }
            });
        }
    }

    /**
     * Date and Time Logic
     */

    private void initDateAndTimePicker() {
        btnDatePicker = (Button) findViewById(R.id.btn_date);
        btnTimePicker = (Button) findViewById(R.id.btn_time);
        txtDate = (EditText) findViewById(R.id.in_date);
        txtTime = (EditText) findViewById(R.id.in_time);
        txtDate.setEnabled(false);
        txtTime.setEnabled(false);
        btnDatePicker.setOnClickListener(this);
        btnTimePicker.setOnClickListener(this);
        txtDate.setText(actualDate);
        txtTime.setText(actualTime);
    }

    public String DateTimeConverter(Instant pUTC_DateTime) {
        Instant timestamp = pUTC_DateTime;
        ZonedDateTime SwitzerlandTimeZone = timestamp.atZone(ZoneId.of("Europe/Zurich"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
        String formattedString = SwitzerlandTimeZone.format(formatter);
        return formattedString;
    }

    /**
     * Swap  Logic
     */

    @OnClick(R.id.swap)
    public void onSwapClick() {
        EditText prevFromStation = new EditText(fromStationAuto.getContext());
        prevFromStation.setText(fromStationAuto.getText());
        fromStationAuto.setText(toStationAuto.getText());
        toStationAuto.setText(prevFromStation.getText());
        updateFavoriteButtonColor();
    }

    /**
     * Favorites Logic
     */

    public void initSpinnerForFavorites() {
        dynamicSpinner = findViewById(R.id.dynamic_spinner);
        adapterForSpinner = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, favoritesArrayForSpinner);
        dynamicSpinner.setAdapter(adapterForSpinner);

        dynamicSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String s = adapterView.getItemAtPosition(i).toString();
                if (isUpdateInputs) {
                    setInputsFromSelectedFavorite(s);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        dynamicSpinner.setOnTouchListener(new AdapterView.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                isUpdateInputs = true;
                return false;
            }
        });

        dynamicSpinner.setVisibility(View.GONE);
    }

    public void updateAdapterForSpinner() {
        adapterForSpinner = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, favoritesArrayForSpinner);
        dynamicSpinner.setAdapter(adapterForSpinner);
    }

    public void setInputsFromSelectedFavorite(String favorite) {
        if (favoritesArrayForSpinner.length != 0 && !favorite.equals(SPINNER_PLACEHOLDER)) {
            String parts[] = favorite.split(" - ");
            fromStationAuto.setText(parts[0]);
            toStationAuto.setText(parts[1]);
            updateFavoriteButtonColor();
        }
        dynamicSpinner.setSelection(0);
    }

    public void updateFavoritesArrayForSpinner() {
        ArrayList<String> stringList = new ArrayList<String>();
        for (Pair<EditText, EditText> pair : favoritesList) {
            stringList.add(pair.first.getText().toString() + " - " + pair.second.getText().toString());
        }
        String[] stringArray = stringList.toArray(new String[0]);
        favoritesArrayForSpinner = stringArray;
        favoritesArrayForSpinner = ArrayUtils.appendToArray(favoritesArrayForSpinner, SPINNER_PLACEHOLDER);
        String valueToSwap = favoritesArrayForSpinner[0];
        favoritesArrayForSpinner[0] = SPINNER_PLACEHOLDER;
        favoritesArrayForSpinner[favoritesArrayForSpinner.length - 1] = valueToSwap;
    }

    @OnClick(R.id.favorite)
    public void onFavoriteClick() {
        if (!isFavorite()) {
            EditText temp1 = new EditText(fromStationAuto.getContext());
            temp1.setText(fromStationAuto.getText());
            EditText temp2 = new EditText(toStationAuto.getContext());
            temp2.setText(toStationAuto.getText());
            favoritesList.add(new Pair<EditText, EditText>(temp1, temp2));
        } else {
            removeFavorite();
        }
        updateFavoriteButtonColor();
        isUpdateInputs = false;
        updateFavoritesArrayForSpinner();
        updateAdapterForSpinner();
    }

    private boolean isFavorite() {
        boolean isFavorite = false;
        if (!favoritesList.isEmpty()) {
            for (Pair<EditText, EditText> pair : favoritesList) {
                if (pair.first.getText().toString().equals(fromStationAuto.getText().toString()) && pair.second.getText().toString().equals(toStationAuto.getText().toString())) {
                    isFavorite = true;
                }
            }
        }
        return isFavorite;
    }

    private void removeFavorite() {
        if (!favoritesList.isEmpty()) {
            ListIterator<Pair<EditText, EditText>> listIterator = favoritesList.listIterator();
            for (Pair<EditText, EditText> it : favoritesList) {
                listIterator.next();
                if (it.first.getText().toString().equals(fromStationAuto.getText().toString()) && it.second.getText().toString().equals(toStationAuto.getText().toString())) {
                    break;
                }
            }
            listIterator.remove();
        }
    }

    private void updateFavoriteButtonColor() {
        Button favoriteButton = (Button) findViewById(R.id.favorite);
        if (isFavorite()) {
            favoriteButton.setBackgroundResource(R.drawable.mybuttonfavorite);
        } else {
            favoriteButton.setBackgroundResource(R.drawable.mybuttondark);
        }
    }

    @OnClick(R.id.showFavoritesButton)
    public void onClickShowFavoritesButton() {
        View view = findViewById(R.id.dynamic_spinner);
        if (view.getVisibility() == View.VISIBLE) {
            view.setVisibility(View.GONE);
        } else {
            view.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Take me Home Logic
     */

    public void initTakeMeHome() {
        View view = findViewById(R.id.takeMeHomeButton);
        view.setBackgroundResource(R.drawable.mybuttoninactive);
        homeAddress.setVisibility(View.GONE);
    }

    @OnTextChanged(R.id.homeAddressAuto)
    public void onTextChangedHomeAddress() {
        getLocationsForAutocomplete(homeAddress.getText().toString(), R.id.homeAddressAuto);
        View view = findViewById(R.id.takeMeHomeButton);
        if (homeAddress != null && !homeAddress.getText().toString().equals("")) {
            view.setBackgroundResource(R.drawable.mybuttondark);
        } else {
            view.setBackgroundResource(R.drawable.mybuttoninactive);
        }
    }

    @OnClick(R.id.showHomeAddressButton)
    public void onClickShowHomeAddressButton() {
        View view = findViewById(R.id.homeAddressAuto);
        if (view.getVisibility() == View.VISIBLE) {
            view.setVisibility(View.GONE);
        } else {
            view.setVisibility(View.VISIBLE);
        }
    }

    @OnClick(R.id.takeMeHomeButton)
    public void onClickTakeMeHomeButton() {
        if (homeAddress != null && !homeAddress.getText().toString().equals("")) {
            toStationAuto.setText(homeAddress.getText());
            updateFavoriteButtonColor();
        }
    }

    /**
     * Later Connections Logic
     */

    @OnClick(R.id.showLaterConnectionsButton)
    public void onClickShowLaterConnectionsButton() {
        setLaterConnectionsVisibility(true);
        View showLaterConnectionsButton = findViewById(R.id.showLaterConnectionsButton);
        showLaterConnectionsButton.setVisibility(View.GONE);
        View showLessConnectionsButton = findViewById(R.id.showLessConnectionsButton);
        showLessConnectionsButton.setVisibility(View.VISIBLE);
    }

    @OnClick(R.id.showLessConnectionsButton)
    public void onClickShowLessConnectionsButton() {
        setLaterConnectionsVisibility(false);
        View showLessConnectionsButton = findViewById(R.id.showLessConnectionsButton);
        showLessConnectionsButton.setVisibility(View.GONE);
        View showLaterConnectionsButton = findViewById(R.id.showLaterConnectionsButton);
        showLaterConnectionsButton.setVisibility(View.VISIBLE);
    }

    public void setLaterConnectionsVisibility(boolean visible) {

        View connection4Connection = findViewById(R.id.connection4Connection);
        View connection4Title = findViewById(R.id.connection4Title);
        View connection4TimeTitle = findViewById(R.id.connection4TimeTitle);
        View connection5Connection = findViewById(R.id.connection5Connection);
        View connection5Title = findViewById(R.id.connection5Title);
        View connection5TimeTitle = findViewById(R.id.connection5TimeTitle);
        View connection6Connection = findViewById(R.id.connection6Connection);
        View connection6Title = findViewById(R.id.connection6Title);
        View connection6TimeTitle = findViewById(R.id.connection6TimeTitle);

        if (visible) {
            arrival4.setVisibility(View.VISIBLE);
            departure4.setVisibility(View.VISIBLE);
            connection4Connection.setVisibility(View.VISIBLE);
            connection4Title.setVisibility(View.VISIBLE);
            connection4TimeTitle.setVisibility(View.VISIBLE);
            arrival5.setVisibility(View.VISIBLE);
            departure5.setVisibility(View.VISIBLE);
            connection5Connection.setVisibility(View.VISIBLE);
            connection5Title.setVisibility(View.VISIBLE);
            connection5TimeTitle.setVisibility(View.VISIBLE);
            arrival6.setVisibility(View.VISIBLE);
            departure6.setVisibility(View.VISIBLE);
            connection6Connection.setVisibility(View.VISIBLE);
            connection6Title.setVisibility(View.VISIBLE);
            connection6TimeTitle.setVisibility(View.VISIBLE);
        } else {
            arrival4.setVisibility(View.GONE);
            departure4.setVisibility(View.GONE);
            connection4Connection.setVisibility(View.GONE);
            connection4Title.setVisibility(View.GONE);
            connection4TimeTitle.setVisibility(View.GONE);
            arrival5.setVisibility(View.GONE);
            departure5.setVisibility(View.GONE);
            connection5Connection.setVisibility(View.GONE);
            connection5Title.setVisibility(View.GONE);
            connection5TimeTitle.setVisibility(View.GONE);
            arrival6.setVisibility(View.GONE);
            departure6.setVisibility(View.GONE);
            connection6Connection.setVisibility(View.GONE);
            connection6Title.setVisibility(View.GONE);
            connection6TimeTitle.setVisibility(View.GONE);
        }
    }
}