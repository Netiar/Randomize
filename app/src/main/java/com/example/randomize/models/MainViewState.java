package com.example.randomize.models;

public class MainViewState {
    public final boolean isLoading;
    public final String content;
    public final int wordCount;
    public final Throwable error;

    public MainViewState(boolean isLoading, String content, int wordCount, Throwable error) {
        this.isLoading = isLoading;
        this.content = content;
        this.wordCount = wordCount;
        this.error = error;
    }
}
