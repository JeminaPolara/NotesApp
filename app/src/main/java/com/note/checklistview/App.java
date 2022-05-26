package com.note.checklistview;

import android.app.Application;

public class App extends Application {

  private static Settings settings;


  public static Settings getSettings () {
    if (settings == null) {
      settings = new Settings();
    }
    return settings;
  }

}
