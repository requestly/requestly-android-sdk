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

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.CheckResult;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;

import io.requestly.android.core.R;
import io.requestly.android.core.Requestly;
import io.requestly.android.core.modules.logs.lib.lynx.main.model.Trace;
import io.requestly.android.core.modules.logs.lib.lynx.main.model.TraceLevel;
import io.requestly.android.core.modules.logs.lib.lynx.main.presenter.LynxRequestlyPresenter;
import io.requestly.android.core.modules.logs.lib.lynx.main.renderer.TraceRendererBuilder;
import io.requestly.android.core.modules.logs.lib.lynx.main.ui.TracesListAdapter;

/**
 * Main library view. Custom view based on a RelativeLayout used to show all the information
 * printed by the Android Logcat. Add this view to your layouts if you want to show your Logcat
 * traces and configure it using styleable attributes.
 *
 * @author Pedro Vicente Gomez Sanchez.
 */
public class LynxRequestlyView extends RelativeLayout implements LynxRequestlyPresenter.View {

  private static final String LOGTAG = "LynxRequestlyView";
  private static final String SHARE_INTENT_TYPE = "text/plain";
  private static final CharSequence SHARE_INTENT_TITLE = "Application Logcat";
  private static final int DEFAULT_POSITION = 0;

  private LynxRequestlyPresenter presenter;
  private LynxConfig lynxConfig;

  private RecyclerView rv_traces;
  private EditText et_filter;
  private ImageButton ib_share;
  private Spinner sp_filter;

  private TracesListAdapter rv_adapter;
  private boolean autoScrollEnabled = true;

  public LynxRequestlyView(Context context) {
    this(context, null);
  }

  public LynxRequestlyView(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public LynxRequestlyView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    initializeConfiguration(attrs);
    initializePresenter();
    initializeView();
  }

  /**
   * Initializes LynxPresenter if LynxView is visible when is attached to the window.
   */
  @Override protected void onAttachedToWindow() {
    super.onAttachedToWindow();
    if (isVisible()) {
      attachPresenterView();
    }
  }

  /**
   * Stops LynxPresenter when LynxView is detached from the window.
   */
  @Override protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    detachPresenterView();
  }

  /**
   * Initializes or stops LynxPresenter based on visibility changes. Doing this Lynx is not going
   * to
   * read your application Logcat if LynxView is not visible or attached.
   */
  @Override protected void onVisibilityChanged(View changedView, int visibility) {
    super.onVisibilityChanged(changedView, visibility);
    if (changedView != this) {
      return;
    }

    if (visibility == View.VISIBLE) {
      attachPresenterView();
    } else {
      detachPresenterView();
    }
  }

  /**
   * Given a valid LynxConfig object update all the dependencies to apply this new configuration.
   *
   * @param lynxConfig the lynx configuration
   */
  public void setLynxConfig(LynxConfig lynxConfig) {
    validateLynxConfig(lynxConfig);
    boolean hasChangedLynxConfig = !this.lynxConfig.equals(lynxConfig);
    if (hasChangedLynxConfig) {
      this.lynxConfig = (LynxConfig) lynxConfig.clone();
      updateFilterText();
      updateSpinner();
      presenter.setLynxConfig(lynxConfig);
    }
  }

  private void updateSpinner() {
    TraceLevel filterTraceLevel = lynxConfig.getFilterTraceLevel();
    sp_filter.setSelection(filterTraceLevel.ordinal());
  }

  /**
   * Returns the current LynxConfig object used.
   *
   * @return the lynx configuration
   */
  public LynxConfig getLynxConfig() {
    return lynxConfig;
  }

  /**
   * Given a {@code List<Trace>} updates the ListView adapter with this information and keeps the
   * scroll position if needed.
   */
  @Override public void showTraces(List<Trace> traces, int removedTraces) {
    rv_adapter.submitList(traces);
    rv_adapter.notifyDataSetChanged();

    if(autoScrollEnabled) {
        rv_traces.scrollToPosition(rv_adapter.getItemCount()-1);
    }
  }

  /**
   * Removes all the traces rendered in the ListView.
   */
  @Override public void clear() {
      rv_adapter.submitList(Collections.emptyList());
      rv_adapter.notifyDataSetChanged();
  }

  /**
   * Uses an intent to share content and given one String with all the information related to the
   * List of traces shares this information with other applications.
   */
  @CheckResult
  @Override public boolean shareTraces(String fullTraces) {
    try {
      shareTracesInternal(fullTraces);
      return true;
    } catch (RuntimeException exception1) { // Likely cause is a TransactionTooLargeException on API levels 15+.
      try {
        /*
         * Limit trace size to between 100kB and 400kB, since Unicode characters can be 1-4 bytes each.
         */
        int fullTracesLength = fullTraces.length();
        String truncatedTraces = fullTraces.substring(Math.max(0, fullTracesLength - 100000), fullTracesLength);
        shareTracesInternal(truncatedTraces);
        return true;
      } catch (RuntimeException exception2) { // Likely cause is a TransactionTooLargeException on API levels 15+.
        return false;
      }
    }
  }

  @Override public void notifyShareTracesFailed() {
    Toast.makeText(getContext(), "Share failed", Toast.LENGTH_SHORT).show();
  }

  @Override public void disableAutoScroll() {
      autoScrollEnabled = false;
  }

  @Override public void enableAutoScroll() {
      autoScrollEnabled = true;
  }

  private boolean isPresenterReady() {
    return presenter != null;
  }

  private void attachPresenterView() {
      if (isPresenterReady()) {
          presenter.setPresenterView(this);
      }
  }

  private void detachPresenterView() {
      if (isPresenterReady()) {
          presenter.setPresenterView(null);
      }
  }

  private boolean isVisible() {
    return getVisibility() == View.VISIBLE;
  }

  private void initializeConfiguration(AttributeSet attrs) {
      lynxConfig = Requestly.getInstance().getLogsConfig();
  }

  private void initializeView() {
    Context context = getContext();
    LayoutInflater layoutInflater = LayoutInflater.from(context);
    layoutInflater.inflate(R.layout.logs_lynx_requestly_view, this);
    mapGui();
    initializeRecyclerView();
    hookListeners();
  }

  private void mapGui() {
    et_filter = (EditText) findViewById(R.id.et_filter);
    ib_share = (ImageButton) findViewById(R.id.ib_share);
    sp_filter = (Spinner) findViewById(R.id.sp_filter);
    rv_traces = (RecyclerView) findViewById(R.id.traces_recycler_view);
    configureCursorColor();
    updateFilterText();
  }

  /**
   * Hack to change EditText cursor color even if the API level is lower than 12. Please, don't do
   * this at home.
   */
  private void configureCursorColor() {
    try {
      @SuppressLint("SoonBlockedPrivateApi") Field f = TextView.class.getDeclaredField("mCursorDrawableRes");
      f.setAccessible(true);
      f.set(et_filter, R.drawable.edit_text_cursor_color);
    } catch (Exception e) {
      Log.e(LOGTAG, "Error trying to change cursor color text cursor drawable to null.");
    }
  }

  private void initializeRecyclerView() {
      rv_adapter = new TracesListAdapter();
      rv_traces.setAdapter(rv_adapter);
  }

  private void hookListeners() {
      LinearLayoutManager layoutManager = (LinearLayoutManager)rv_traces.getLayoutManager();
      rv_traces.addOnScrollListener(new RecyclerView.OnScrollListener() {
          @Override
          public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
              super.onScrolled(recyclerView, dx, dy);
              int lastVisiblePos = layoutManager.findLastCompletelyVisibleItemPosition();
              presenter.onScrollToPosition(lastVisiblePos);
          }
      });
    et_filter.addTextChangedListener(new TextWatcher() {
      @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        //Empty
      }

      @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
        presenter.updateFilter(s.toString());
      }

      @Override public void afterTextChanged(Editable s) {
        //Empty
      }
    });

    ib_share.setOnClickListener(new OnClickListener() {
      @Override public void onClick(View v) {
        presenter.onShareButtonClicked();
      }
    });
    ArrayAdapter<TraceLevel> adapter =
        new ArrayAdapter<>(getContext(), R.layout.single_line_spinner_item, TraceLevel.values());
    sp_filter.setAdapter(adapter);
    sp_filter.setSelection(DEFAULT_POSITION);
    sp_filter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

      @Override
      public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        presenter.updateFilterTraceLevel((TraceLevel) parent.getItemAtPosition(position));
      }

      @Override public void onNothingSelected(AdapterView<?> parent) {

      }
    });
  }

  private void initializePresenter() {
      presenter = Requestly.getInstance().logsLynxPresenter;
      presenter.setPresenterView(this);
  }

  private void validateLynxConfig(LynxConfig lynxConfig) {
    if (lynxConfig == null) {
      throw new IllegalArgumentException(
          "You can't configure Lynx with a null LynxConfig instance.");
    }
  }

  private void updateFilterText() {
    if (lynxConfig.hasFilter()) {
      et_filter.append(lynxConfig.getFilter());
    }
  }

  private void shareTracesInternal(final String plainTraces) {
    Intent sharingIntent = new Intent(Intent.ACTION_SEND);
    sharingIntent.setType(SHARE_INTENT_TYPE);
    sharingIntent.putExtra(Intent.EXTRA_TEXT, plainTraces);
    getContext().startActivity(Intent.createChooser(sharingIntent, SHARE_INTENT_TITLE));
  }

  /**
   * Backdoor used to replace the presenter used in this view. This method should be used just for
   * testing purposes.
   */
  void setPresenter(LynxRequestlyPresenter presenter) {
    this.presenter = presenter;
  }
}
