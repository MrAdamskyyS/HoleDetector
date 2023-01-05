package com.adam_sobkowiak.holedetectorsobkowiak;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Date;

public class MainActivity extends Activity {

    private LocationManager locationManager;
    private LocationListener locationListener;
    private static final long MIN_TIME = 400;
    private static final float MIN_DISTANCE = 1000;
    private Location currentLocation;

    private TextView text_DD;
    private TextView text_OD;
    private TextView text_OD1;
    private TextView text_OD2;
    private TextView text_OD3;
    private TextView text_OD4;
    private TextView text_OD5;
    private TextView text_OD6;
    private TextView text_OD7;

    private Button button_DD;

    private Switch switch1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        text_DD = findViewById(R.id.text_DD);
        text_OD = findViewById(R.id.text_OD);
        text_OD1 = findViewById(R.id.text_OD1);
        text_OD2 = findViewById(R.id.text_OD2);
        text_OD3 = findViewById(R.id.text_OD3);
        text_OD4 = findViewById(R.id.text_OD4);
        text_OD5 = findViewById(R.id.text_OD5);
        text_OD6 = findViewById(R.id.text_OD6);
        text_OD7 = findViewById(R.id.text_OD7);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                // zapisz aktualną lokalizację do zmiennej currentLocation
                currentLocation = location;
                // pokaż button pozwalający na zapisanie lokalizacji do bazy danych
                button_DD.setVisibility(View.VISIBLE);
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
                } else {
                    // zatrzymaj pobieranie aktualnej lokalizacji
                    locationManager.removeUpdates(locationListener);
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
                saveLocationToDB(currentLocation);
                // ukryj button
                button_DD.setVisibility(View.GONE);
                // wyświetl komunikat o sukcesie
                Toast.makeText(MainActivity.this, "Lokalizacja zapisana do bazy danych", Toast.LENGTH_SHORT).show();
            }
        }
    });
}


    private void saveLocationToDB(Location location) {
        // otwórz połączenie z bazą danych
        SQLiteDatabase db = openOrCreateDatabase("locations", MODE_PRIVATE, null);
        // utwórz tabelę, jeśli nie istnieje
        db.execSQL("CREATE TABLE IF NOT EXISTS locations (timestamp DATETIME DEFAULT CURRENT_TIMESTAMP, latitude REAL, longitude REAL);");
        // utwórz zapytanie INSERT
        String sql = "INSERT INTO locations (latitude, longitude) VALUES (?, ?)";
        text_DD.setText("Dodano dziure");
        // utwórz obiekt zapytania
        SQLiteStatement statement = db.compileStatement(sql);
        // ustaw wartości dla zapytania
        statement.bindDouble(1, location.getLatitude());
        statement.bindDouble(2, location.getLongitude());
        // wykonaj zapytanie
        statement.executeInsert();
        // zamknij obiekt zapytania i połączenie z bazą danych
        statement.close();
        db.close();
    }
};
