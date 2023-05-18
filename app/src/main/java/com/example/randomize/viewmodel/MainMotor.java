package com.example.randomize.viewmodel;

import android.app.Application;
import android.net.Uri;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.randomize.models.MainViewState;
import com.example.randomize.utils.PassphraseRepository;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.List;

public class MainMotor extends AndroidViewModel {
    private static final int DEFAULT_WORD_COUNT = 6;
    private final PassphraseRepository repo;
    private Uri wordsDoc = PassphraseRepository.ASSET_URI;
    public final MutableLiveData<MainViewState> viewStates =
            new MutableLiveData<>();

    public MainMotor(@NonNull Application application) {
        super(application);

        repo = PassphraseRepository.get(application);
        generatePassphrase(DEFAULT_WORD_COUNT);
    }

    public void generatePassphrase() {
        final MainViewState current = viewStates.getValue();

        if (current == null) {
            generatePassphrase(DEFAULT_WORD_COUNT);
        }
        else {
            generatePassphrase(current.wordCount);
        }
    }

    public void generatePassphrase(int wordCount) {
        viewStates.setValue(new MainViewState(true, null, wordCount, null));

        ListenableFuture<List<String>> future = repo.generate(wordsDoc, wordCount);

        future.addListener((Runnable)() -> {
            try {
                viewStates.postValue(new MainViewState(false,
                        TextUtils.join(" ", future.get()), wordCount, null));
            }
            catch (Exception e) {
                viewStates.postValue(new MainViewState(false, null, wordCount, e));
            }
        }, Runnable::run);
    }

    public void generatePassphrase(Uri wordsDoc) {
        this.wordsDoc = wordsDoc;

        generatePassphrase();
    }
}
