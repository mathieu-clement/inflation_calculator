package com.mathieuclement.android.sic.app;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.*;
import android.os.Build;
import android.widget.*;

import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Currency;


public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);

            SpinnerAdapter yearSpinnerAdapter = new ArrayAdapter<Integer>(
                    getActivity(),
                    android.R.layout.simple_list_item_1,
                    InflationTools.getYearsBoxedArray());


            final Spinner startSpinner = (Spinner) rootView.findViewById(R.id.spinner_start);
            startSpinner.setAdapter(yearSpinnerAdapter);
            startSpinner.setSelection(InflationTools.arrayIndex(1980));

            final Spinner endSpinner = (Spinner) rootView.findViewById(R.id.spinner_end);
            endSpinner.setAdapter(yearSpinnerAdapter);
            endSpinner.setSelection(InflationTools.arrayIndex(InflationTools.getLastKnownYear()));

            final EditText startPriceEditText = (EditText) rootView.findViewById(R.id.editText_start);

            final TextView resultTextView = (TextView) rootView.findViewById(R.id.textView_end_value);

            final NumberFormat format = NumberFormat.getCurrencyInstance(); // Locale currency format
            format.setCurrency(Currency.getInstance("CHF"));

            final TextView variationValueTextView = (TextView) rootView.findViewById(R.id.textView_variation_value);

            //final Button calculateButton = (Button) rootView.findViewById(R.id.button_calculate);
            final View.OnClickListener calculateClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        float oldPrice = Float.parseFloat(startPriceEditText.getText().toString());
                        float newPrice = InflationTools.newPrice(
                                getActivity(),
                                oldPrice,
                                (Integer) startSpinner.getSelectedItem(),
                                (Integer) endSpinner.getSelectedItem()
                        );
                        resultTextView.setText(format.format(newPrice));

                        float variationPercents = (newPrice / oldPrice - 1f) * 100f;
                        variationValueTextView.setText(String.format("%.2f %%", variationPercents));
                    } catch (IOException e) {
                        Log.e("MAIN_ACTIVITY", "InflationToolsError", e);
                    } catch (NumberFormatException nfe) {
                        resultTextView.setText("");
                        variationValueTextView.setText("000.00 %");
                    }
                }
            };
            //calculateButton.setOnClickListener(calculateClickListener);

            startPriceEditText.setOnKeyListener(new View.OnKeyListener() {
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    // If the event is a key-down event on the "enter" button
                    if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                            (keyCode == KeyEvent.KEYCODE_ENTER)) {
                        // Perform action on key press
                        calculateClickListener.onClick(null);
                        return true;
                    }
                    return false;
                }
            });
            startPriceEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    calculateClickListener.onClick(null);
                }
            });

            AdapterView.OnItemSelectedListener onItemSelectedListener = new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    calculateClickListener.onClick(null);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            };
            startSpinner.setOnItemSelectedListener(onItemSelectedListener);
            endSpinner.setOnItemSelectedListener(onItemSelectedListener);

            resultTextView.setText(format.format(0d));

            // Open soft keyboard automatically
            getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

            return rootView;
        }


    }
}
