package com.example.eyasu.mortgagecalculatortheme;

import java.text.NumberFormat;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.Button;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

public class MortgageCalculatorActivity extends Activity
        implements OnEditorActionListener, OnSeekBarChangeListener,
        OnCheckedChangeListener, OnItemSelectedListener,OnKeyListener, OnClickListener{

    // define variables for the widgets
    private EditText principal;
    private TextView montlyRate;
    private SeekBar mRateBar;
    private Spinner mNUMSpinner;
    private TextView weeklyRate;
    private SeekBar wRateBar;
    private EditText wNum;
    private RadioGroup RBtnGrp;
    private RadioButton monthlyRBtn;
    private RadioButton weeklyRBtn;
    private Button calculateBtn;
    private TextView singlePayment;
    private TextView totalPayment;


    // define the SharedPreferences object
    private SharedPreferences savedValues;

    // define radio button constants
    private final int PAY_MONTH = 0;
    private final int PAY_WEEK = 1;

    // define instance variables
    private String principalString = "";
    private String numberOfWeeksString = "";
    private float monthlyRatePercent = .15f;
    private float weeklyRatePercent = .15f;
    private int rounding = PAY_MONTH; // radio button selection
    private int numberOfMonths = 0;
    private float progress;// value of seekbar

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_main); // set layout with .xml file

        // get references from variables to the widgets
        principal = (EditText) findViewById(R.id.principal);
        montlyRate = (TextView) findViewById(R.id.montlyRate);
        mRateBar = (SeekBar) findViewById(R.id.mRateBar);
        mNUMSpinner = (Spinner) findViewById(R.id.mNUMSpinner);
        weeklyRate = (TextView) findViewById(R.id.weeklyRate);
        wRateBar = (SeekBar) findViewById(R.id.wRateBar);
        wNum = (EditText) findViewById(R.id.wNum);
        RBtnGrp = (RadioGroup)
                findViewById(R.id.RBtnGrp);
        monthlyRBtn = (RadioButton)
                findViewById(R.id.monthlyRBtn);
        weeklyRBtn = (RadioButton)
                findViewById(R.id.weeklyRBtn);
        calculateBtn = (Button) findViewById(R.id.CalculateBtn);
        singlePayment = (TextView) findViewById(R.id.singlePayment);
        totalPayment = (TextView) findViewById(R.id.totalPayment);

        // set array adapter for spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.month_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);
        mNUMSpinner.setAdapter(adapter);

        // set the listeners for widgets with this class
        principal.setOnEditorActionListener(this);
        principal.setOnKeyListener(this);
        wNum.setOnEditorActionListener(this);
        wNum.setOnKeyListener(this);
        mRateBar.setOnSeekBarChangeListener(this);
        mRateBar.setOnKeyListener(this);
        wRateBar.setOnSeekBarChangeListener(this);
        wRateBar.setOnKeyListener(this);
        RBtnGrp.setOnCheckedChangeListener(this);
        RBtnGrp.setOnKeyListener(this);
        mNUMSpinner.setOnItemSelectedListener(this);
        calculateBtn.setOnClickListener(this);

        // get SharedPreferences object
        savedValues = getSharedPreferences("SavedValues", MODE_PRIVATE);
    }

    @Override
    public void onPause() {
        // save the instance variables
        Editor editor = savedValues.edit();
        editor.putString("principalString", principalString);
        editor.putString("numberOfWeeksString", numberOfWeeksString);
        editor.putFloat("monthlyRatePercent", monthlyRatePercent);
        editor.putFloat("weeklyRatePercent", weeklyRatePercent);
        editor.putInt("rounding", rounding);
        editor.putInt("numberOfMonths", numberOfMonths);
        editor.commit();

        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();

        // get the instance variables
        principalString = savedValues.getString("principalString", "");
        numberOfWeeksString = savedValues.getString("numberOfWeeksString", "");
        monthlyRatePercent = savedValues.getFloat("monthlyRatePercent", 0.15f);
        weeklyRatePercent = savedValues.getFloat("weeklyRatePercent", 0.15f);
        rounding = savedValues.getInt("rounding", PAY_MONTH);
        numberOfMonths = savedValues.getInt("numberOfMonths", 1);

        // set the principal amount on its widget
        principal.setText(principalString);
        wNum.setText(numberOfWeeksString);



        // set selection on radio buttons
        // NOTE: this executes the onCheckedChanged method,
        if (rounding == PAY_MONTH) {
            monthlyRBtn.setChecked(true);
            mRateBar.setProgress((int)progress);


        } else if (rounding == PAY_WEEK) {
            weeklyRBtn.setChecked(true);
            wRateBar.setProgress((int) progress);

        }

        // set number of months on spinner

        int position = numberOfMonths/12 - 1;
        mNUMSpinner.setSelection(position);
    }

    //*****************************************************
    // Calculate and display results
    //*****************************************************

    public void calculateAndDisplay() {
        float pricipalNumber;
        float paymentTimes=.0f; //payment terms weekly or monthly
        float interestRate=0.0f; // weekly or monthly rate
        float numberOfWeeks=0.0f;
        double eachPayment=0; // value of singlePayment string


        //get principal (from string to value)
        principalString = principal.getText().toString();

        if (principalString.equals("")) {
            pricipalNumber = 0;
        }
        else {
            pricipalNumber = Float.parseFloat(principalString);
        }

        //get number of weeks (from string to value)
        numberOfWeeksString = wNum.getText().toString();
        if (numberOfWeeksString.equals("")) {
            numberOfWeeks = 0;
        }
        else {
            numberOfWeeks = Float.parseFloat(numberOfWeeksString);
            Toast.makeText(getApplicationContext(),"number of weeks is" + numberOfWeeks, Toast.LENGTH_LONG).show();

        }


        //decide source of payment terms (from monthly spinner or weekly editText) and interest rate (from monthly seekBar or weekly seekBar)
        if (rounding == PAY_MONTH) {
            interestRate=monthlyRatePercent;
            paymentTimes = (float)numberOfMonths;
            Toast.makeText(getApplicationContext(),"number of months is: " + numberOfMonths, Toast.LENGTH_LONG).show();
        }
        else if (rounding == PAY_WEEK) {
            interestRate=weeklyRatePercent;
            paymentTimes = numberOfWeeks;

        }

        Toast.makeText(getApplicationContext(),"interestRate: " + interestRate, Toast.LENGTH_LONG).show();

        // calculate single payment each term
        eachPayment = (interestRate*pricipalNumber*Math.pow((1+interestRate),paymentTimes)/(Math.pow((1+interestRate),paymentTimes)-1));


        // display the results with formatting
        NumberFormat currency = NumberFormat.getCurrencyInstance();
        singlePayment.setText(currency.format(eachPayment));
        totalPayment.setText(currency.format(eachPayment*paymentTimes));

    }

    //*****************************************************
    // Event handler for the EditText
    //*****************************************************
    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_DONE ||
                actionId == EditorInfo.IME_ACTION_UNSPECIFIED) {

        }
        return false;
    }

    //*****************************************************
    // Event handler for the SeekBar
    //*****************************************************
    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    //When seekBar changes, refresh interest rate/ display according to radio button selection
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress,
                                  boolean fromUser) {
        switch(seekBar.getId()) {
            case R.id.mRateBar:{
                montlyRate.setText(progress + "%");
                monthlyRatePercent = (float)progress/100.0f;
                //Toast.makeText(getApplicationContext(),"monthlyRate is" + monthlyRatePercent, Toast.LENGTH_LONG).show();
                break;}
            case R.id.wRateBar: {
                weeklyRate.setText(progress + "%") ;
                weeklyRatePercent = (float)progress/100.0f;
                //Toast.makeText(getApplicationContext(),"weeklyRate is" + weeklyRatePercent, Toast.LENGTH_LONG).show();
                break;
            }
            default: break;
        }
    }


    //after finishing moving seekBar, toast display the current interest rate
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        switch(seekBar.getId()) {
            case R.id.mRateBar:{
                Toast.makeText(getApplicationContext(),"monthlyRate is" + monthlyRatePercent, Toast.LENGTH_LONG).show();
                break;}
            case R.id.wRateBar: {

                Toast.makeText(getApplicationContext(),"weeklyRate is" + weeklyRatePercent, Toast.LENGTH_LONG).show();
                break;
            }
            default: break;
        }
    }


    //*****************************************************
    // Event handler for the RadioGroup
    //*****************************************************
    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            case R.id.monthlyRBtn:
                rounding = PAY_MONTH;
                break;
            case R.id.weeklyRBtn:
                rounding = PAY_WEEK;
                break;
        }

    }

    //*****************************************************
    // Event handler for the Spinner
    //*****************************************************
    @Override
    public void onItemSelected(AdapterView<?> parent, View v, int position,
                               long id) {
        numberOfMonths = (position + 1)*12;
        Toast.makeText(getApplicationContext(),"number of months is: " + numberOfMonths, Toast.LENGTH_LONG).show();

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {


    }

    //*****************************************************
    // Event handler for the calculate button
    // Start calculation and display
    //*****************************************************

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.CalculateBtn:
                calculateAndDisplay();//
                break;

        }
    }

    //*****************************************************
    // Event handler for the keyboard and DPad
    //*****************************************************
    @Override
    public boolean onKey(View view, int keyCode, KeyEvent event) {
        /*
        switch (keyCode) {
            case KeyEvent.KEYCODE_ENTER:
            case KeyEvent.KEYCODE_DPAD_CENTER:

                // calculateAndDisplay();

                // hide the soft keyboard
                InputMethodManager imm = (InputMethodManager)
                        getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(
                        principal.getWindowToken(), 0);
                // hide the soft keyboard
                InputMethodManager im = (InputMethodManager)
                        getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(
                        wNum.getWindowToken(), 0);
                // consume the event
                return true;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
            case KeyEvent.KEYCODE_DPAD_LEFT:
                if (view.getId() == R.id.mRateBar) {
                    //calculateAndDisplay();
                } else if (view.getId() == R.id.wRateBar) {

                }
                break;
        } */
        // don't consume the event
        return false;
    }

}