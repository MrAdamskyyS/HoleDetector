package com.adam_sobkowiak.holedetectorsobkowiak;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
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

public class MainActivity extends AppCompatActivity implements SensorEventListener, LocationListener {

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor gyroscope;
    private LocationManager locationManager;
    private SQLiteDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicjalizuj menedżer sensory
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        // Inicjalizuj menedżer lokalizacji
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // Otwórz połączenie z bazą danych
        database = openOrCreateDatabase("Potholes", MODE_PRIVATE, null);
        // Wykonaj zapytanie do bazy danych, aby utworzyć tabelę "potholes"
        String sql = "CREATE TABLE IF NOT EXISTS potholes (latitude REAL, longitude REAL, depth REAL)";
        database.execSQL(sql);
        // Zamknij połączenie z bazą danych
        database.close();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Rejestruj nasłuchiwanie zdarzeń z akcelerometru i żyroskopu
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_NORMAL);
        // Rejestruj nasłuchiwanie zdarzeń z lokalizacji
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
    }


    @Override
    protected void onPause() {
        super.onPause();
        // Wstrzymaj nasłuchiwanie zdarzeń z akcelerometru i żyroskopu
        sensorManager.unregisterListener(this);
        // Wstrzymaj nasłuchiwanie zdarzeń z lokalizacji
        locationManager.removeUpdates(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // Pobierz dane z akcelerometru lub żyroskopu
        float[] data = event.values;
        // Przetwarzaj dane, aby wykryć dziury w drodze i pomierzyć ich głębokość
        // tutaj możesz dodać swój własny kod do analizy danych z akcelerometru lub żyroskopu
        // i wykrywania dziur w drodze
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    public void onLocationChanged(Location location) {
        // Pobierz aktualną lokalizację z GPS
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        // Przetwarzaj dane z GPS, aby wykryć dziury w drodze i pomierzyć ich głębokość
        // tutaj możesz dodać swój własny kod do analizy danych z GPS i wykrywania dziur w drodze
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // Obsługa zmiany statusu lokalizacji
    }

    @Override
    public void onProviderEnabled(String provider) {
        // Obsługa włączenia dostawcy lokalizacji
    }

    @Override
    public void onProviderDisabled(String provider) {
        // Obsługa wyłączenia dostawcy lokalizacji
    }

    // Funkcja do zapisywania informacji o dziurach w bazie danych
    private void savePothole(double latitude, double longitude, double depth) {
        // Otwórz połączenie z bazą danych
        database = openOrCreateDatabase("Potholes", MODE_PRIVATE, null);
        // Wykonaj zapytanie do bazy danych, aby zapisać informacje o dziurze
        String sql = "INSERT INTO potholes (latitude, longitude, depth) VALUES (?, ?, ?)";
        SQLiteStatement statement = database.compileStatement(sql);
        statement.bindDouble(1, latitude);
        statement.bindDouble(2, longitude);
        statement.bindDouble(3, depth);
        statement.execute();
        // Zamknij połączenie z bazą danych
        database.close();
    }
}