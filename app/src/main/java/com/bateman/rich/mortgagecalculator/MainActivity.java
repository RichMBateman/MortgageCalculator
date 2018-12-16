package com.bateman.rich.mortgagecalculator;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
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

    // Shared Preferences Keys
    static final String SP_KEY_INITIAL_LOAN="InitialLoan";
    static final String SP_KEY_MORTGAGE_TERM="MortgageTerm";
    static final String SP_KEY_LOAN_PRINCIPAL="LoanPrincipal";
    static final String SP_KEY_INTEREST_RATE="InterestRate";
    static final String SP_KEY_ADDL_PAYMENT="AddlPayment";

    // Logging
    private static final String TAG = "MainActivity";

    // Defaults
    public static final int DEFAULT_INIT_LOAN=300000;
    public static final int DEFAULT_PRINCIPAL = 300000;
    public static final int DEFAULT_TERM = 30;
    public static final double DEFAULT_RATE = 0.04;

    // Bundle Keys
    public static final String BUNDLE_INIT_LOAN ="InitLoan";
    public static final String BUNDLE_PRINCIPAL = "Principal";
    public static final String BUNDLE_TERM = "Term";
    public static final String BUNDLE_RATE = "Rate";
//    public static final String BUNDLE_BASE_PYMT = "Base";
    public static final String BUNDLE_ADDL_PYMT = "Addl";

    // Mortgage Data
    private MortgageCalc mortgageCalc;
    
    // Widgets
    private TextView labelLoanPrincipal;
    private TextView labelMortgageTerm;
    private TextView labelInterestRate;
    private TextView labelMonthlyPayment;
    private TextView labelAddlMonthlyPayment;
    private TextView labelYourResultDescription;
    private TextView labelActualTargetMonthResult;
    private EditText inputInitLoan;
    private EditText inputLoanPrincipal;
    private EditText inputMortgageTerm;
    private EditText inputInterestRate;
    private EditText inputAddlMonthlyPayment;

    private boolean ignoreEvents;
    private boolean ignoreRefreshInitLoan;
    private boolean ignoreRefreshPrincipal;
    private boolean ignoreRefreshRate;
    private boolean ignoreRefreshTerm;
    private boolean ignoreRefreshAddlPayment;
    
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: start");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeUI();
        initializeMortgageCalc();
        refreshUI();
        Log.d(TAG, "onCreate: end");
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume: start");
        super.onResume();
        restoreSavedInputData();
        refreshUI();
        Log.d(TAG, "onResume: end");
    }

    private void restoreSavedInputData() {
        Log.d(TAG, "restoreSavedInputData: start");
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        mortgageCalc.setInitLoan(getDouble(sharedPreferences, SP_KEY_INITIAL_LOAN, DEFAULT_INIT_LOAN));
        mortgageCalc.setLoanPrincipal(getDouble(sharedPreferences, SP_KEY_LOAN_PRINCIPAL, DEFAULT_PRINCIPAL));
        mortgageCalc.setAddlMonthlyPayment(getDouble(sharedPreferences, SP_KEY_ADDL_PAYMENT, 0));
        mortgageCalc.setInterestRate(getDouble(sharedPreferences, SP_KEY_INTEREST_RATE, DEFAULT_RATE));
        mortgageCalc.setMortgageTermInYears(sharedPreferences.getInt(SP_KEY_MORTGAGE_TERM, DEFAULT_TERM));
        Log.d(TAG, "restoreSavedInputData: end");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.d(TAG, "onSaveInstanceState: start");
        outState.putDouble(BUNDLE_INIT_LOAN, mortgageCalc.getInitLoan());
        outState.putDouble(BUNDLE_PRINCIPAL, mortgageCalc.getLoanPrincipal());
        outState.putInt(BUNDLE_TERM, mortgageCalc.getMortgageTermInYears());
        outState.putDouble(BUNDLE_RATE, mortgageCalc.getInterestRate());
        // this is no longer an input field.
//        outState.putDouble(BUNDLE_BASE_PYMT, mortgageCalc.getBaseMonthlyPayment());
        outState.putDouble(BUNDLE_ADDL_PYMT, mortgageCalc.getAddlMonthlyPayment());
        super.onSaveInstanceState(outState);
        Log.d(TAG, "onSaveInstanceState: end");
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        Log.d(TAG, "onRestoreInstanceState: start");
        double initLoan = savedInstanceState.getDouble(BUNDLE_INIT_LOAN);
        double principal = savedInstanceState.getDouble(BUNDLE_PRINCIPAL);
        int term = savedInstanceState.getInt(BUNDLE_TERM);
        double rate = savedInstanceState.getDouble(BUNDLE_RATE);
        // user can no longer set base monthly payment.
//        double base = savedInstanceState.getDouble(BUNDLE_BASE_PYMT);
        double addl = savedInstanceState.getDouble(BUNDLE_ADDL_PYMT);

        mortgageCalc = new MortgageCalc(initLoan, principal, term, rate);
//        mortgageCalc.setBaseMonthlyPayment(base);
        mortgageCalc.setAddlMonthlyPayment(addl);

        refreshUI();
        super.onRestoreInstanceState(savedInstanceState);
        Log.d(TAG, "onRestoreInstanceState: end");
    }



    private void initializeUI() {
        findWidgets();
        hookupTextChangedEvents();
        hookupOnFocusChangedEvents();
    }

    private void findWidgets() {
        labelLoanPrincipal = findViewById(R.id.labelEnterLoanPrincipal);
        labelMortgageTerm = findViewById(R.id.labelEnterMortgageTerm);
        labelInterestRate = findViewById(R.id.labelInterestRate);
        labelMonthlyPayment = findViewById(R.id.labelBaseMonthlyPayment);
        labelAddlMonthlyPayment = findViewById(R.id.labelAdditionalMonthlyPayment);
        labelYourResultDescription = findViewById(R.id.labelYourResult);
        labelActualTargetMonthResult = findViewById(R.id.labelResult);
        inputInitLoan = findViewById((R.id.inputInitLoanAmount));
        inputLoanPrincipal = findViewById(R.id.inputLoanPrincipal);
        inputMortgageTerm = findViewById(R.id.inputMortgageTermInYears);
        inputInterestRate = findViewById(R.id.inputMortgageInterestRate);
        //inputMonthlyPayment = findViewById(R.id.inputMonthlyPayment);
        inputAddlMonthlyPayment = findViewById(R.id.inputAdditionalPayment);
    }

    private void hookupTextChangedEvents() {
        inputInitLoan.addTextChangedListener(new GenericTextWatcher(inputInitLoan));
        inputLoanPrincipal.addTextChangedListener(new GenericTextWatcher(inputLoanPrincipal));
        inputAddlMonthlyPayment.addTextChangedListener(new GenericTextWatcher(inputAddlMonthlyPayment));
       // inputMonthlyPayment.addTextChangedListener(new GenericTextWatcher(inputMonthlyPayment));
        inputMortgageTerm.addTextChangedListener(new GenericTextWatcher(inputMortgageTerm));
        inputInterestRate.addTextChangedListener(new GenericTextWatcher(inputInterestRate));
    }

    private void hookupOnFocusChangedEvents() {
        View.OnFocusChangeListener l = new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if(!hasFocus) {
                    refreshUI();
                }
            }
        };
        inputInitLoan.setOnFocusChangeListener(l);
        inputLoanPrincipal.setOnFocusChangeListener(l);
        inputAddlMonthlyPayment.setOnFocusChangeListener(l);
        //inputMonthlyPayment.setOnFocusChangeListener(l);
        inputMortgageTerm.setOnFocusChangeListener(l);
        inputInterestRate.setOnFocusChangeListener(l);
    }

    private void initializeMortgageCalc() {
        mortgageCalc = new MortgageCalc(DEFAULT_INIT_LOAN, DEFAULT_PRINCIPAL, DEFAULT_TERM, DEFAULT_RATE);
    }

    private void refreshUI() {
        ignoreEvents = true;

        if(!ignoreRefreshInitLoan) {
            double initLoan = mortgageCalc.getInitLoan();
            inputInitLoan.setText(String.format("%.2f", initLoan));
        }

        if(!ignoreRefreshPrincipal) {
            double principal = mortgageCalc.getLoanPrincipal();
            inputLoanPrincipal.setText(String.format("%.2f", principal));
        }

        if(!ignoreRefreshTerm) {
            int term = mortgageCalc.getMortgageTermInYears();
            inputMortgageTerm.setText(String.format("%d", term));
        }

        if(!ignoreRefreshRate) {
            double interestRate = mortgageCalc.getInterestRate() * 100;
            inputInterestRate.setText(String.format("%.4f", interestRate));
        }

        double baseMonthlyPayment = mortgageCalc.getBaseMonthlyPayment();
        labelMonthlyPayment.setText(String.format("Base monthly payment: $%.2f", baseMonthlyPayment));

        if(!ignoreRefreshAddlPayment) {
            double addlMonthlyPayment = mortgageCalc.getAddlMonthlyPayment();
            inputAddlMonthlyPayment.setText(String.format("%.2f", addlMonthlyPayment));
        }

        Date targetDate = mortgageCalc.getCompletionDate();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM, yyyy");
        labelActualTargetMonthResult.setText(dateFormat.format(targetDate));
        ignoreEvents=false;
    }

    // Good use for a library function.
    // From: https://stackoverflow.com/questions/16319237/cant-put-double-sharedpreferences
    // This converts to/from a double's "raw long bits" equivalent and stores it as a long.
    // The two data types have the same size.
    Editor putDouble(final Editor edit, final String key, final double value) {
        return edit.putLong(key, Double.doubleToRawLongBits(value));
    }

    double getDouble(final SharedPreferences prefs, final String key, final double defaultValue) {
        return Double.longBitsToDouble(prefs.getLong(key, Double.doubleToLongBits(defaultValue)));
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
            if(!ignoreEvents) {
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                Editor editor = sharedPreferences.edit();
                String text = sanitizeInputText(s.toString()); // This text should be correct due to input field settings.
                switch (view.getId()) {
                    case R.id.inputInitLoanAmount:
                        ignoreRefreshInitLoan=true;
                        Double initLoan = Double.parseDouble(text);
                        mortgageCalc.setInitLoan(initLoan);
                        putDouble(editor, SP_KEY_INITIAL_LOAN, initLoan);
                        break;
                    case R.id.inputAdditionalPayment:
                        ignoreRefreshAddlPayment = true;
                        Double addlPayment = Double.parseDouble(text);
                        mortgageCalc.setAddlMonthlyPayment(addlPayment);
                        putDouble(editor, SP_KEY_ADDL_PAYMENT, addlPayment);
                        break;
                    case R.id.inputLoanPrincipal:
                        ignoreRefreshPrincipal=true;
                        Double loanPrincipal = Double.parseDouble(text);
                        mortgageCalc.setLoanPrincipal(loanPrincipal);
                        putDouble(editor, SP_KEY_LOAN_PRINCIPAL, loanPrincipal);
                        break;
                        // 2018.12.11: No longer accepting input for this field.
//                    case R.id.inputMonthlyPayment:
//                        mortgageCalc.setBaseMonthlyPayment(Double.parseDouble(text));
//                        break;
                    case R.id.inputMortgageInterestRate:
                        ignoreRefreshRate=true;
                        double interestRate = Double.parseDouble(text) / 100.0;
                        mortgageCalc.setInterestRate(interestRate);
                        putDouble(editor, SP_KEY_INTEREST_RATE, interestRate);
                        break;
                    case R.id.inputMortgageTermInYears:
                        ignoreRefreshTerm=true;
                        int mortgageTerm = Integer.parseInt(text);
                        mortgageCalc.setMortgageTermInYears(mortgageTerm);
                        editor.putInt(SP_KEY_MORTGAGE_TERM, mortgageTerm);
                        break;
                }

                editor.commit();
                refreshUI();
                ignoreRefreshInitLoan=false;
                ignoreRefreshPrincipal=false;
                ignoreRefreshTerm=false;
                ignoreRefreshAddlPayment=false;
                ignoreRefreshRate=false;
            }
        }
    }

    // 2018.12.11: I had considered allowing the user to type the "$" or "%" sign (and displaying those symbols in the text)
    // but it creates a headache when then trying to parse that data.  Plus, without more sophisticated logic, the user might type something like
    // "35$24" or "4%4" which is DEFINITELY not what I want.  I could imagine the symbols disappearing WHILE the user is editing, but
    // for my first basic app, that is not what I want to deal with.
    private String sanitizeInputText(String s) {
//        String result = s.replace("$", "")
//                .replace("%", "");
//        result = result.trim();
        // If the string is empty, replace with 0.
        String result = s;
        if(result.equals("")) {
            result = "0";
        }
        return result;
    }
}
