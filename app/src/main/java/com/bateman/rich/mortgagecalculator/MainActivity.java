package com.bateman.rich.mortgagecalculator;

import android.annotation.SuppressLint;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.bateman.rich.rmblibrary.gui.AboutAppDialog;
import com.bateman.rich.rmblibrary.persistence.SharedAppData;

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

    private final SharedAppData m_sharedAppData = new SharedAppData();

    // Menu
    private static final int MENU_ID_ABOUT = 1001;
    
    // Widgets
    private TextView labelMonthlyPayment;
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
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE, MENU_ID_ABOUT, Menu.NONE, "About");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int menuId = item.getItemId();
        switch(menuId) {
            case MENU_ID_ABOUT:
                handleMenuShowAboutDialog();
                break;
            default:
                throw new IllegalStateException("Unknown menu item clicked.");
        }

        return super.onOptionsItemSelected(item);
    }

    private void handleMenuShowAboutDialog() {
        AboutAppDialog aboutDialog = new AboutAppDialog(this);
        aboutDialog.setIconId(R.mipmap.ic_launcher)
                .setTitleId(R.string.app_name)
                .setVersionName(BuildConfig.VERSION_NAME)
                .setCopyrightYear(2019)
                .show();
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
        m_sharedAppData.load(getApplicationContext());
        mortgageCalc.setInitLoan(m_sharedAppData.getDouble(SP_KEY_INITIAL_LOAN, DEFAULT_INIT_LOAN));
        mortgageCalc.setLoanPrincipal(m_sharedAppData.getDouble(SP_KEY_LOAN_PRINCIPAL, DEFAULT_PRINCIPAL));
        mortgageCalc.setAddlMonthlyPayment(m_sharedAppData.getDouble(SP_KEY_ADDL_PAYMENT, 0));
        mortgageCalc.setInterestRate(m_sharedAppData.getDouble(SP_KEY_INTEREST_RATE, DEFAULT_RATE));
        mortgageCalc.setMortgageTermInYears(m_sharedAppData.getInt(SP_KEY_MORTGAGE_TERM, DEFAULT_TERM));
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
        labelMonthlyPayment = findViewById(R.id.labelBaseMonthlyPayment);
        labelActualTargetMonthResult = findViewById(R.id.labelResult);
        inputInitLoan = findViewById((R.id.inputInitLoanAmount));
        inputLoanPrincipal = findViewById(R.id.inputLoanPrincipal);
        inputMortgageTerm = findViewById(R.id.inputMortgageTermInYears);
        inputInterestRate = findViewById(R.id.inputMortgageInterestRate);
        inputAddlMonthlyPayment = findViewById(R.id.inputAdditionalPayment);
    }

    private void hookupTextChangedEvents() {
        inputInitLoan.addTextChangedListener(new GenericTextWatcher(inputInitLoan));
        inputLoanPrincipal.addTextChangedListener(new GenericTextWatcher(inputLoanPrincipal));
        inputAddlMonthlyPayment.addTextChangedListener(new GenericTextWatcher(inputAddlMonthlyPayment));
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
        inputMortgageTerm.setOnFocusChangeListener(l);
        inputInterestRate.setOnFocusChangeListener(l);
    }

    private void initializeMortgageCalc() {
        mortgageCalc = new MortgageCalc(DEFAULT_INIT_LOAN, DEFAULT_PRINCIPAL, DEFAULT_TERM, DEFAULT_RATE);
    }

    @SuppressLint("DefaultLocale")
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
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM, yyyy", Locale.US);
        labelActualTargetMonthResult.setText(dateFormat.format(targetDate));
        ignoreEvents=false;
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
                m_sharedAppData.load(getApplicationContext());
                String text = sanitizeInputText(s.toString()); // This text should be correct due to input field settings.
                switch (view.getId()) {
                    case R.id.inputInitLoanAmount:
                        ignoreRefreshInitLoan=true;
                        Double initLoan = Double.parseDouble(text);
                        mortgageCalc.setInitLoan(initLoan);
                        m_sharedAppData.putDouble(SP_KEY_INITIAL_LOAN, initLoan);
                        break;
                    case R.id.inputAdditionalPayment:
                        ignoreRefreshAddlPayment = true;
                        Double addlPayment = Double.parseDouble(text);
                        mortgageCalc.setAddlMonthlyPayment(addlPayment);
                        m_sharedAppData.putDouble(SP_KEY_ADDL_PAYMENT, addlPayment);
                        break;
                    case R.id.inputLoanPrincipal:
                        ignoreRefreshPrincipal=true;
                        Double loanPrincipal = Double.parseDouble(text);
                        mortgageCalc.setLoanPrincipal(loanPrincipal);
                        m_sharedAppData.putDouble(SP_KEY_LOAN_PRINCIPAL, loanPrincipal);
                        break;
                        // 2018.12.11: No longer accepting input for this field.
//                    case R.id.inputMonthlyPayment:
//                        mortgageCalc.setBaseMonthlyPayment(Double.parseDouble(text));
//                        break;
                    case R.id.inputMortgageInterestRate:
                        ignoreRefreshRate=true;
                        double interestRate = Double.parseDouble(text) / 100.0;
                        mortgageCalc.setInterestRate(interestRate);
                        m_sharedAppData.putDouble(SP_KEY_INTEREST_RATE, interestRate);
                        break;
                    case R.id.inputMortgageTermInYears:
                        ignoreRefreshTerm=true;
                        int mortgageTerm = Integer.parseInt(text);
                        mortgageCalc.setMortgageTermInYears(mortgageTerm);
                        m_sharedAppData.putInt(SP_KEY_MORTGAGE_TERM, mortgageTerm);
                        break;
                }

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
