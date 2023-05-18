package com.example.randomize.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.AssetManager;
import android.net.Uri;
import android.util.LruCache;

import androidx.concurrent.futures.CallbackToFutureAdapter;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;


public class PassphraseRepository {
    public static final Uri ASSET_URI =
            Uri.parse("file:///android_asset/eff_short_wordlist_2_0.txt");
    static final String ASSET_FILENAME = "eff_short_wordlist_2_0.txt";
    private static volatile PassphraseRepository INSTANCE;

    public synchronized static PassphraseRepository get(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new PassphraseRepository(context.getApplicationContext());
        }

        return INSTANCE;
    }

    private final ContentResolver resolver;
    private final AssetManager assets;
    private final LruCache<Uri, List<String>> wordsCache = new LruCache<>(4);
    private final SecureRandom random = new SecureRandom();
    private final Executor threadPool = Executors.newSingleThreadExecutor();

    private PassphraseRepository(Context context) {
        resolver = context.getContentResolver();
        assets = context.getAssets();
    }

    public ListenableFuture<List<String>> generate(Uri wordsDoc, int wordCount) {
        return CallbackToFutureAdapter.getFuture(completer -> {
            threadPool.execute(() -> {
                List<String> words;

                synchronized (wordsCache) {
                    words = wordsCache.get(wordsDoc);
                }

                try {
                    if (words == null) {
                        InputStream in;

                        if (wordsDoc.equals(ASSET_URI)) {
                            in = assets.open(PassphraseRepository.ASSET_FILENAME);
                        }
                        else {
                            in = resolver.openInputStream(wordsDoc);
                        }

                        words = readWords(in);
                        in.close();

                        synchronized (wordsCache) {
                            wordsCache.put(wordsDoc, words);
                        }
                    }

                    completer.set(rollDemBones(words, wordCount));
                }
                catch (Throwable t) {
                    completer.setException(t);
                }
            });

            return "generate words";
        });
    }

    private List<String> readWords(InputStream in) throws IOException {
        InputStreamReader isr = new InputStreamReader(in);
        BufferedReader reader = new BufferedReader(isr);
        String line;
        List<String> result = new ArrayList<>();

        while ((line = reader.readLine()) != null) {
            String[] pieces = line.split("\\s");

            if (pieces.length == 2) {
                result.add(pieces[1]);
            }
        }

        return result;
    }

    private List<String> rollDemBones(List<String> words, int wordCount) {
        List<String> result = new ArrayList<>();
        int size = words.size();

        for (int i = 0; i < wordCount; i++) {
            result.add(words.get(random.nextInt(size)));
        }

        return result;
    }
}
