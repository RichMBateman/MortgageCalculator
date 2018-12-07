package com.bateman.rich.mortgagecalculator;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    // Logging
    private static final String TAG = "MainActivity";

    // Defaults
    public static final int DEFAULT_PRINCIPAL = 300000;
    public static final int DEFAULT_TERM = 30;
    public static final double DEFAULT_RATE = 0.04;

    // Bundle Keys
    public static final String BUNDLE_PRINCIPAL = "Principal";
    public static final String BUNDLE_TERM = "Term";
    public static final String BUNDLE_RATE = "Rate";
    public static final String BUNDLE_BASE_PYMT = "Base";
    public static final String BUNDLE_ADDL_PYMT = "Addl";

    // Mortgage Data
    private MortgageCalc mortgageCalc;
    
    // Widgets
    private TextView labelLoanPrincipal;
    private TextView labelMortgageTerm;
    private TextView labelInterestRate
    private TextView labelMonthlyPayment;
    private TextView labelAddlMonthlyPayment;
    private TextView labelYourResultDescription;
    private TextView labelActualTargetMonthResult;
    private EditText inputLoanPrincipal;
    private EditText inputMortgageTerm;
    private EditText inputInterestRate;
    private EditText inputMonthlyPayment;
    private EditText inputAddlMonthlyPayment;
    
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initializeUI();
        initializeMortgageCalc();
        refreshUI();
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.d(TAG, "onSaveInstanceState: in");
        outState.putDouble(BUNDLE_PRINCIPAL, mortgageCalc.getLoanPrincipal());
        outState.putInt(BUNDLE_TERM, mortgageCalc.getMortgageTermInYears());
        outState.putDouble(BUNDLE_RATE, mortgageCalc.getInterestRate());
        outState.putDouble(BUNDLE_BASE_PYMT, mortgageCalc.getBaseMonthlyPayment());
        outState.putDouble(BUNDLE_ADDL_PYMT, mortgageCalc.getAddlMonthlyPayment());
        super.onSaveInstanceState(outState);
        Log.d(TAG, "onSaveInstanceState: out");
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        Log.d(TAG, "onRestoreInstanceState: in");
        double principal = savedInstanceState.getDouble(BUNDLE_PRINCIPAL);
        int term = savedInstanceState.getInt(BUNDLE_TERM);
        double rate = savedInstanceState.getDouble(BUNDLE_RATE);
        double base = savedInstanceState.getDouble(BUNDLE_BASE_PYMT);
        double addl = savedInstanceState.getDouble(BUNDLE_ADDL_PYMT);

        mortgageCalc = new MortgageCalc(principal, term, rate);
        mortgageCalc.setBaseMonthlyPayment(base);
        mortgageCalc.setAddlMonthlyPayment(addl);

        refreshUI();
        super.onRestoreInstanceState(savedInstanceState);
        Log.d(TAG, "onRestoreInstanceState: out");
    }

    private void initializeUI() {
        findWidgets();
        hookupTextChangedEvents();
    }

    private void findWidgets() {
        labelLoanPrincipal = findViewById(R.id.labelEnterLoanPrincipal);
        labelMortgageTerm = findViewById(R.id.labelEnterMortgageTerm);
        labelMortgageTerm = findViewById(R.id.labelInterestRate);
        labelMonthlyPayment = findViewById(R.id.labelMonthlyPayment);
        labelAddlMonthlyPayment = findViewById(R.id.labelAdditionalMonthlyPayment);
        labelYourResultDescription = findViewById(R.id.labelYourResult);
        labelActualTargetMonthResult = findViewById(R.id.labelResult);
        inputLoanPrincipal = findViewById(R.id.inputLoanPrincipal);
        inputMortgageTerm = findViewById(R.id.inputMortgageTermInYears);
        inputInterestRate = findViewById(R.id.inputMortgageInterestRate);
        inputMonthlyPayment = findViewById(R.id.inputMonthlyPayment);
        inputAddlMonthlyPayment = findViewById(R.id.inputAdditionalPayment);
    }

    private void hookupTextChangedEvents() {
        inputLoanPrincipal.addTextChangedListener(new GenericTextWatcher(inputLoanPrincipal));
        inputAddlMonthlyPayment.addTextChangedListener(new GenericTextWatcher(inputAddlMonthlyPayment));
        inputMonthlyPayment.addTextChangedListener(new GenericTextWatcher(inputMonthlyPayment));
        inputMortgageTerm.addTextChangedListener(new GenericTextWatcher(inputMortgageTerm));
        inputInterestRate.addTextChangedListener(new GenericTextWatcher(inputInterestRate));
    }

    private void initializeMortgageCalc() {
        mortgageCalc = new MortgageCalc(DEFAULT_PRINCIPAL, DEFAULT_TERM, DEFAULT_RATE);
    }

    private void refreshUI() {
        double principal = mortgageCalc.getLoanPrincipal();
        inputLoanPrincipal.setText(String.format("$%f", principal));

        int term = mortgageCalc.getMortgageTermInYears();
        inputMortgageTerm.setText(String.format("%d", term));

        double interestRate = mortgageCalc.getInterestRate() * 100;
        inputInterestRate.setText(String.format("0.00f%%", interestRate));

        double baseMonthlyPayment = mortgageCalc.getBaseMonthlyPayment();
        inputMonthlyPayment.setText(String.format("$%f", baseMonthlyPayment));

        double addlMonthlyPayment = mortgageCalc.getBaseMonthlyPayment();
        inputAddlMonthlyPayment.setText(String.format("$%f", addlMonthlyPayment));

        Date targetDate = mortgageCalc.getCompletionDate();
        SimpleDateFormat dateFormat = new SimpleDateFormat("M, yyyy");
        labelActualTargetMonthResult.setText(dateFormat.format(targetDate));
    }

    /**
     * From: https://stackoverflow.com/questions/5702771/how-to-use-single-textwatcher-for-multiple-edittexts/6172024#6172024
     * A pattern for responding to text changed to different editors.
     */
    private class GenericTextWatcher implements TextWatcher {
        private View view;
        private GenericTextWatcher(View view) {
            this.view = view;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}

        @Override
        public void afterTextChanged(Editable s) {
            String text = s.toString();
            switch(view.getId()) {
                case R.id.inputAdditionalPayment:
                    mortgageCalc.setAddlMonthlyPayment(Double.parseDouble(text));
                    break;
                case R.id.inputLoanPrincipal:
                    mortgageCalc.setLoanPrincipal(Double.parseDouble(text));
                    break;
                case R.id.inputMonthlyPayment:
                    mortgageCalc.setBaseMonthlyPayment(Double.parseDouble(text));
                    break;
                case R.id.inputMortgageInterestRate:
                    mortgageCalc.setInterestRate(Double.parseDouble(text) / 100);
                    break;
                case R.id.inputMortgageTermInYears:
                    mortgageCalc.setMortgageTermInYears(Integer.parseInt(text));
                    break;
            }
        }
    }
}
