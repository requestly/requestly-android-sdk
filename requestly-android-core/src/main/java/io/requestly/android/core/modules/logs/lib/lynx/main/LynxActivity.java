/*
 * Copyright (C) 2015 Pedro Vicente Gomez Sanchez.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.requestly.android.core.modules.logs.lib.lynx.main;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import io.requestly.android.core.R;


/**
 * Activity created to show a LynxView with "match_parent" configuration for LynxView
 * "layout_height" and "layout_width" attributes. To configure LynxView and all the information to
 * show use Activity extras and a LynxConfig object. Use getIntent() method to obtain a valid
 * intent
 * to start this Activity.
 *
 * @author Pedro Vicente Gomez Sanchez.
 */
public class LynxActivity extends Activity {

  private static final String LYNX_CONFIG_EXTRA = "extra_lynx_config";

  /**
   * Generates an Intent to start LynxActivity with a default LynxConfig object as configuration.
   *
   * @param context the application context
   * @return a new {@code Intent} to start {@link LynxActivity}
   */
  public static Intent getIntent(Context context) {
    return getIntent(context, new LynxConfig());
  }

  /**
   * Generates an Intent to start LynxActivity with a LynxConfig configuration passed as parameter.
   *
   * @param context the application context
   * @param lynxConfig the lynx configuration
   * @return a new {@code Intent} to start {@link LynxActivity}
   */
  public static Intent getIntent(Context context, LynxConfig lynxConfig) {
    if (lynxConfig == null) {
      lynxConfig = new LynxConfig();
    }
    Intent intent = new Intent(context, LynxActivity.class);
    intent.putExtra(LYNX_CONFIG_EXTRA, lynxConfig);
    return intent;
  }

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.logs_lynx_activity);
    LynxConfig lynxConfig = getLynxConfig();
    configureLynxView(lynxConfig);
    disableLynxShakeDetector();
  }

  @Override protected void onDestroy() {
    super.onDestroy();
    enableLynxShakeDetector();
  }

  private LynxConfig getLynxConfig() {
    Bundle extras = getIntent().getExtras();
    LynxConfig lynxConfig = new LynxConfig();
    if (extras != null && extras.containsKey(LYNX_CONFIG_EXTRA)) {
      lynxConfig = (LynxConfig) extras.getSerializable(LYNX_CONFIG_EXTRA);
    }
    return lynxConfig;
  }

  private void configureLynxView(LynxConfig lynxConfig) {
    LynxView lynxView = (LynxView) findViewById(R.id.lynx_view);
    lynxView.setLynxConfig(lynxConfig);
  }

  private void enableLynxShakeDetector() {
    LynxShakeDetector.enable();
  }

  private void disableLynxShakeDetector() {
    LynxShakeDetector.disable();
  }
}
