package com.bateman.rich.mortgagecalculator;

import java.util.Calendar;
import java.util.Date;

/**
 * A helper class for calculating mortgage information.
 */
public class MortgageCalc {
    static final int MONTHS_IN_YEAR=12;

    // Private Members
    private double initLoan;
    private double loanPrincipal;
    private int mortgageTermInYears;
    private double interestRate;
    private double baseMonthlyPayment; // derived field
    private double addlMonthlyPayment;
    private int numPaymentsRemaining; // derived field
    private Date currentDate;
    private Date completionDate; // derived field.  The date we expect to be done with this loan.

    /**
     * Initializes the mortgage calc with some base values.
     * This way, all values are correct and sensible.
     * Then caller can change one at a time, and all information should be correct.
     * @param loanPrincipal The current amount remaining on the loan
     * @param mortgageTermInYears What is the original term of the loan?  15, 30 years?
     */
    MortgageCalc(double initLoan, double loanPrincipal, int mortgageTermInYears, double interestRate) {
        this.initLoan = initLoan;
        this.loanPrincipal = loanPrincipal;
        this.mortgageTermInYears = mortgageTermInYears;
        this.interestRate = interestRate;
        this.currentDate = new Date();
        this.addlMonthlyPayment = 0;

        deriveBaseMonthlyPayment();
        deriveNumPaymentsRemaining();
        deriveCompletionDate();
    }

    double getInitLoan() {return initLoan;}

    void setInitLoan(double initLoan) {
        this.initLoan = initLoan;
        deriveBaseMonthlyPayment();
        deriveCompletionInformation();
    }

    double getLoanPrincipal() {
        return loanPrincipal;
    }

    void setLoanPrincipal(double loanPrincipal) {
        this.loanPrincipal = loanPrincipal;
        deriveCompletionInformation();
    }

    double getInterestRate() {
        return interestRate;
    }

    void setInterestRate(double interestRate) {
        this.interestRate = interestRate;
        deriveBaseMonthlyPayment();
        deriveCompletionInformation();
    }

    Date getCompletionDate() {
        return completionDate;
    }

    int getMortgageTermInYears() {

        return mortgageTermInYears;
    }

    void setMortgageTermInYears(int mortgageTermInYears) {
        this.mortgageTermInYears = mortgageTermInYears;
        deriveBaseMonthlyPayment();
        deriveCompletionInformation();
    }

    double getBaseMonthlyPayment() {
        return baseMonthlyPayment;
    }

    double getAddlMonthlyPayment() {
        return addlMonthlyPayment;
    }

    void setAddlMonthlyPayment(double addlMonthlyPayment) {
        this.addlMonthlyPayment = addlMonthlyPayment;
        deriveCompletionInformation();
    }

    private void deriveCompletionInformation() {
        deriveNumPaymentsRemaining();
        deriveCompletionDate();
    }

    /**
     * Figure out the BASE monthly payment (i.e. the minimum monthly amount you MUST pay),
     * based on the loan principal, the interest rate, and the mortgage term.
     */
    private void deriveBaseMonthlyPayment() {
        double monthlyInterestRate = getMonthlyInterestRate();
        if(monthlyInterestRate == 0) {
         baseMonthlyPayment = initLoan / (mortgageTermInYears * 12);
        } else {
            double iPlus1 = monthlyInterestRate + 1; // (i+1)
            int totalPayments = mortgageTermInYears * MONTHS_IN_YEAR; // n, the number of payments in the loan
            double operandA = monthlyInterestRate * java.lang.Math.pow(iPlus1, totalPayments);
            double operandB = java.lang.Math.pow(iPlus1, totalPayments) - 1;
            baseMonthlyPayment = (operandA / operandB) * initLoan;
        }
    }

    /**
     * Given the current loan, figure out how many payments are left.
     */
    private void deriveNumPaymentsRemaining() {
        double monthlyInterestRate = getMonthlyInterestRate();
        if(monthlyInterestRate==0) {
            numPaymentsRemaining =  (int) Math.ceil(loanPrincipal/(baseMonthlyPayment + addlMonthlyPayment));
        } else {
            double actualMonthlyPayment = baseMonthlyPayment + addlMonthlyPayment;
            double operand1 = 1 - (monthlyInterestRate * loanPrincipal) / actualMonthlyPayment;
            double operand2 = 1 + monthlyInterestRate;
            numPaymentsRemaining = (int) java.lang.Math.ceil(-1 * java.lang.Math.log(operand1) / java.lang.Math.log(operand2));
        }
    }

    private double getMonthlyInterestRate() {
        return interestRate/MONTHS_IN_YEAR;
    }

    private void deriveCompletionDate() {
        completionDate = new Date(currentDate.getTime());
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, numPaymentsRemaining);
        completionDate = cal.getTime();
    }
}
