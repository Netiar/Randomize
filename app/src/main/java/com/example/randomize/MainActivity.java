package com.example.randomize;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;


import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.example.randomize.databinding.ActivityMainBinding;
import com.example.randomize.viewmodel.MainMotor;

public class MainActivity extends AppCompatActivity {
    private MainMotor motor;

    private final ActivityResultLauncher<String[]> openDoc =
            registerForActivityResult(new ActivityResultContracts.OpenDocument(),
                    new ActivityResultCallback<Uri>() {
                        @Override
                        public void onActivityResult(Uri uri) {
                            motor.generatePassphrase(uri);
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityMainBinding binding =
                ActivityMainBinding.inflate(getLayoutInflater());

        setContentView(binding.getRoot());

        motor = new ViewModelProvider(this).get(MainMotor.class);

        motor.viewStates.observe(this, viewState -> {
            binding.progress.setVisibility(
                    viewState.isLoading ? View.VISIBLE : View.GONE);

            if (viewState.content != null) {
                binding.passphrase.setText(viewState.content);
            }
            else if (viewState.error != null) {
                binding.passphrase.setText(viewState.error.getLocalizedMessage());
                Log.e("Diceware", "Exception generating passphrase",
                        viewState.error);
            }
            else {
                binding.passphrase.setText("");
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.actions, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.open) {
            openDoc.launch(new String[]{"text/plain"});

            return true;
        } else if (itemId == R.id.refresh) {
            motor.generatePassphrase();
            return true;
        } else if (itemId == R.id.word_count_4 ||
                itemId == R.id.word_count_5 || itemId == R.id.word_count_6 ||
                itemId == R.id.word_count_7 || itemId == R.id.word_count_8 ||
                itemId == R.id.word_count_9 || itemId == R.id.word_count_10) {
            item.setChecked(!item.isChecked());
            motor.generatePassphrase(Integer.parseInt(item.getTitle().toString()));

            return true;
        }



        return super.onOptionsItemSelected(item);
    }
}