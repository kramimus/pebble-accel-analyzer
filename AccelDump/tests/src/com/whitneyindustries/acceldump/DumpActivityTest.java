package com.whitneyindustries.acceldump;

import android.test.ActivityInstrumentationTestCase2;
import android.widget.Spinner;

/**
 * This is a simple framework for a test of an Application.  See
 * {@link android.test.ApplicationTestCase ApplicationTestCase} for more information on
 * how to write and extend Application tests.
 * <p/>
 * To run this test, you can type:
 * adb shell am instrument -w \
 * -e class com.whitneyindustries.acceldump.DumpActivityTest \
 * com.whitneyindustries.acceldump.tests/android.test.InstrumentationTestRunner
 */
public class DumpActivityTest extends ActivityInstrumentationTestCase2<DumpActivity> {

    public DumpActivityTest() {
        super("com.whitneyindustries.acceldump", DumpActivity.class);
    }

    public void testTzSelect() {
        final Spinner tzSelector = (Spinner)getActivity().findViewById(R.id.tz_selector);
        getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    tzSelector.requestFocus();
                }
            });
    }

}
