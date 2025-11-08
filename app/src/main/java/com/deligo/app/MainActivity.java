package com.deligo.app;

import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.deligo.app.data.local.DeliGoDatabase;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView databaseStatusView = findViewById(R.id.database_status);

        DeliGoDatabase database = DeliGoDatabase.getInstance(getApplicationContext());
        String databaseName = database.getOpenHelper().getDatabaseName();
        if (databaseName == null) {
            databaseName = getString(R.string.unknown_database_name);
        }

        databaseStatusView.setText(getString(R.string.database_ready_message, databaseName));
    }
}
