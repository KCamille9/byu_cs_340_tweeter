package edu.byu.cs.tweeter.client.view.main;

import androidx.lifecycle.ViewModel;

public class PageViewModel extends ViewModel {
    private int index = 0;
    private String text = "";

    public void setIndex(int value) {
        index = value;
        text = "Hello world from section: " + index;
    }

    public String getText() {
        return text;
    }
}
