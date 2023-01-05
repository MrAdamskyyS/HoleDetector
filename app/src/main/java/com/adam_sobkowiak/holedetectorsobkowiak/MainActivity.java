package com.adam_sobkowiak.holedetectorsobkowiak;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

public class MainActivity extends Activity {

    private LocationManager locationManager;
    private LocationListener locationListener;
    private static final long MIN_TIME = 100;
    private static final float MIN_DISTANCE = 1;
    private Location currentLocation;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private static final float MIN_SHAKE_VALUE = 4;
    private double accelerationCurrValue;
    private double accelerationPrevValue;
    private double changeInAccelleration;

    private TextView text_OD;

    private Button button_DD;

    private Switch switch1;

    private SensorEventListener sensorListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            float x = sensorEvent.values[0];
            float y = sensorEvent.values[1];
            float z = sensorEvent.values[2];

            accelerationCurrValue = Math.sqrt((x * x + y * y + z * z));
            changeInAccelleration = Math.abs(accelerationCurrValue - accelerationPrevValue);
            accelerationPrevValue = accelerationCurrValue;

                if (changeInAccelleration >= MIN_SHAKE_VALUE) {
                    button_DD.setVisibility(View.VISIBLE);
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            button_DD.setVisibility(View.GONE);
                        }
                    }, 15000);
                }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        text_OD = findViewById(R.id.text_OD);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                // zapisz aktualną lokalizację do zmiennej currentLocation
                currentLocation = location;
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
        };

        // pobierz switch z layoutu
        switch1 = findViewById(R.id.switch1);
        // ustaw słuchacz dla switcha
        switch1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // sprawdź czy użytkownik wyraził zgodę na uzyskiwanie dostępu do lokalizacji
                    if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // brak zgody, wyświetl komunikat i zakończ działanie aplikacji
                        Toast.makeText(MainActivity.this, "Brak uprawnień do pobierania lokalizacji", Toast.LENGTH_SHORT).show();
                        switch1.setChecked(false);
                        return;
                    }
                    // pobieraj aktualną lokalizację co MIN_TIME milisekund, jeśli ruch użytkownika przekroczy MIN_DISTANCE metrów
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, MIN_DISTANCE, locationListener);
                    mSensorManager.registerListener(sensorListener, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
                } else {
                    // zatrzymaj pobieranie aktualnej lokalizacji
                    locationManager.removeUpdates(locationListener);
                    mSensorManager.unregisterListener(sensorListener);
                    button_DD.setVisibility(View.GONE);
                }
            }
        });

        // pobierz button z layoutu
        button_DD = findViewById(R.id.button_DD);
        // ustaw słuchacz dla buttona
        button_DD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // sprawdź czy currentLocation zawiera dane o aktualnej lokalizacji
                if (currentLocation != null) {
                    // zapisz aktualną lokalizację do bazy danych
                    saveLocationToDB(currentLocation, changeInAccelleration);
                    // ukryj button
                    button_DD.setVisibility(View.GONE);
                    // wyświetl komunikat o sukcesie
                    Toast.makeText(MainActivity.this, "Lokalizacja zapisana do bazy danych", Toast.LENGTH_SHORT).show();
                }
            }
        });

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            // sprawdź czy zostało udzielone uprawnienie do uzyskiwania dostępu do lokalizacji
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // uprawnienie zostało udzielone, można rozpocząć pobieranie lokalizacji
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, MIN_DISTANCE, locationListener);
            } else {
                // uprawnienie nie zostało udzielone, wyświetl komunikat i zakończ działanie aplikacji
                Toast.makeText(MainActivity.this, "Brak uprawnień do pobierania lokalizacji", Toast.LENGTH_SHORT).show();
                switch1.setChecked(false);
            }
        }
    }
    private void saveLocationToDB(Location location, double changeInAccelleration) {
        // otwórz połączenie z bazą danych
        SQLiteDatabase db = openOrCreateDatabase("locations", MODE_PRIVATE, null);
        // utwórz tabelę, jeśli nie istnieje
        db.execSQL("CREATE TABLE IF NOT EXISTS locations (timestamp DATETIME DEFAULT CURRENT_TIMESTAMP, latitude REAL, longitude REAL, change REAL);");
        // utwórz zapytanie INSERT
        String sql = "INSERT INTO locations (latitude, longitude, change) VALUES (?, ?, ?)";
        // utwórz obiekt zapytania
        SQLiteStatement statement = db.compileStatement(sql);
        // ustaw wartości dla zapytania
        statement.bindDouble(1, location.getLatitude());
        statement.bindDouble(2, location.getLongitude());
        statement.bindDouble(3, changeInAccelleration);
        text_OD.setText("Lat: "+location.getLatitude() + ", Lon: "+location.getLongitude() +", Moc: "+ changeInAccelleration);
        // wykonaj zapytanie
        statement.executeInsert();
        // zamknij obiekt zapytania i połączenie z bazą danych
        statement.close();
        db.close();
    }
};


