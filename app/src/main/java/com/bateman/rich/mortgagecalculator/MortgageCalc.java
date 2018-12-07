package com.bateman.rich.mortgagecalculator;

import java.util.Calendar;
import java.util.Date;

/**
 * A helper class for calculating mortgage information.
 */
public class MortgageCalc {
    public static final int MONTHS_IN_YEAR=12;

    // Private Members
    private double loanPrincipal;
    private int mortgageTermInYears;
    private double interestRate;
    private double baseMonthlyPayment;
    private double addlMonthlyPayment;
    private int numPaymentsRemaining;
    private Date currentDate;
    private Date completionDate; // The date we expect to be done with this loan.

    /**
     * Initializes the mortgage calc with some base values.
     * This way, all values are correct and sensible.
     * Then caller can change one at a time, and all information should be correct.
     * @param loanPrincipal The current amount remaining on the loan
     * @param mortgageTermInYears What is the original term of the loan?  15, 30 years?
     */
    public MortgageCalc(double loanPrincipal, int mortgageTermInYears, double interestRate) {
        this.loanPrincipal = loanPrincipal;
        this.mortgageTermInYears = mortgageTermInYears;
        this.interestRate = interestRate;
        this.currentDate = new Date();
        this.addlMonthlyPayment = 0;

        deriveBaseMonthlyPayment();
        deriveNumPaymentsRemaining();
        deriveCompletionDate();
    }

    public double getLoanPrincipal() {
        return loanPrincipal;
    }

    public void setLoanPrincipal(double loanPrincipal) {
        this.loanPrincipal = loanPrincipal;
        deriveCompletionInformation();
    }

    public double getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(double interestRate) {
        this.interestRate = interestRate;
        deriveCompletionInformation();
    }

    public Date getCompletionDate() {
        return completionDate;
    }

    public int getMortgageTermInYears() {

        return mortgageTermInYears;
    }

    public void setMortgageTermInYears(int mortgageTermInYears) {
        this.mortgageTermInYears = mortgageTermInYears;
        deriveCompletionInformation();
    }

    public double getBaseMonthlyPayment() {
        return baseMonthlyPayment;
    }

    public void setBaseMonthlyPayment(double baseMonthlyPayment) {
        this.baseMonthlyPayment = baseMonthlyPayment;
        deriveCompletionInformation();
    }

    public double getAddlMonthlyPayment() {
        return addlMonthlyPayment;
    }

    public void setAddlMonthlyPayment(double addlMonthlyPayment) {
        this.addlMonthlyPayment = addlMonthlyPayment;
        deriveCompletionInformation();
    }

    public int getNumPaymentsRemaining() {
        return numPaymentsRemaining;
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
        double iPlus1 = monthlyInterestRate + 1; // (i+1)
        int totalPayments = mortgageTermInYears * MONTHS_IN_YEAR; // n, the number of payments in the loan
        double operandA = monthlyInterestRate * java.lang.Math.pow(iPlus1, totalPayments);
        double operandB = java.lang.Math.pow(iPlus1, totalPayments) - 1;
        baseMonthlyPayment = (operandA/operandB) * loanPrincipal;
    }

    /**
     * Given the current loan, figure out how many payments are left.
     */
    private void deriveNumPaymentsRemaining() {
        double monthlyInterestRate = getMonthlyInterestRate();
        double actualMonthlyPayment = baseMonthlyPayment + addlMonthlyPayment;
        double operand1 = 1 - (monthlyInterestRate*loanPrincipal) / actualMonthlyPayment;
        double operand2 = 1 + monthlyInterestRate;
        numPaymentsRemaining = (int) java.lang.Math.ceil(-1 * java.lang.Math.log(operand1) / java.lang.Math.log(operand2));
    }

    private double getMonthlyInterestRate() {
        double monthlyInterestRate = interestRate/MONTHS_IN_YEAR; // i, the monthly interest rate.
        return monthlyInterestRate;
    }

    private void deriveCompletionDate() {
        completionDate = new Date(currentDate.getTime());
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, numPaymentsRemaining);
        completionDate = cal.getTime();
    }
}
