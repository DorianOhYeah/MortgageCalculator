package com.example.mortgagecalculator;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class MainActivity extends AppCompatActivity implements View.OnClickListener,
        RadioGroup.OnCheckedChangeListener, CompoundButton.OnCheckedChangeListener {

    private EditText et_price;
    private EditText et_loan;
    private TextView tv_loan;
    private RadioGroup rg_payment;
    private CheckBox ck_business;
    private EditText et_business;
    private CheckBox ck_accumulation;
    private EditText et_accumulation;
    private TextView tv_payment;

    private boolean isInterest = true;
    private boolean hasBusiness = true;
    private boolean hasAccumulation = false;
    private int mYear;
    private double mBusinessRatio;
    private double mAccumulationRatio;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        et_price = findViewById(R.id.et_price);
        et_loan = findViewById(R.id.et_loan);
        tv_loan = findViewById(R.id.tv_loan);
        rg_payment = findViewById(R.id.rg_payment);
        rg_payment.setOnCheckedChangeListener(this);
        ck_business = findViewById(R.id.ck_business);
        ck_business.setOnCheckedChangeListener(this);
        et_business = findViewById(R.id.et_business);
        ck_accumulation = findViewById(R.id.ck_accumulation);
        ck_accumulation.setOnCheckedChangeListener(this);
        et_accumulation = findViewById(R.id.et_accumulation);
        tv_payment = findViewById(R.id.tv_payment);
        findViewById(R.id.btn_loan).setOnClickListener(this);
        findViewById(R.id.btn_calculate).setOnClickListener(this);
        initYearSpinner();
        initRatioSpinner();
    }

    //initialise the year spinner
    private void initYearSpinner() {
        ArrayAdapter<String> yearAdapter = new ArrayAdapter<String>(this,
                R.layout.item_select, yearDescArray);
        yearAdapter.setDropDownViewResource(R.layout.item_dropdown);
        Spinner sp_year = findViewById(R.id.sp_year);
        sp_year.setPrompt("Please choose the loan year");
        sp_year.setAdapter(yearAdapter);
        sp_year.setSelection(0);
        sp_year.setOnItemSelectedListener(new YearSelectedListener());
    }

    private String[] yearDescArray = {"5 years", "10 years", "15 years", "20 years", "30 years"};
    private int[] yearArray = {5, 10, 15, 20, 30};

    class YearSelectedListener implements AdapterView.OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            mYear = yearArray[arg2];
        }

        public void onNothingSelected(AdapterView<?> arg0) {}
    }

    //ratio spinner
    private void initRatioSpinner() {
        ArrayAdapter<String> ratioAdapter = new ArrayAdapter<String>(this,
                R.layout.item_select, ratioDescArray);
        ratioAdapter.setDropDownViewResource(R.layout.item_dropdown);
        Spinner sp_ratio = findViewById(R.id.sp_ratio);
        sp_ratio.setPrompt("Please enter the basic interest");
        sp_ratio.setAdapter(ratioAdapter);
        sp_ratio.setSelection(0);
        sp_ratio.setOnItemSelectedListener(new RatioSelectedListener());
    }

    private String[] ratioDescArray = {
            "business interest 4.90%　accumulation interest 3.25%",
            "business interest 5.15%　accumulation interest 3.25%",
    };
    private double[] businessArray = {4.90, 5.15};
    private double[] accumulationArray = {3.25, 3.25};

    class RatioSelectedListener implements AdapterView.OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            mBusinessRatio = businessArray[arg2];
            mAccumulationRatio = accumulationArray[arg2];
        }

        public void onNothingSelected(AdapterView<?> arg0) {}
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_loan) { // click calculating the total price
            if (TextUtils.isEmpty(et_price.getText().toString())) {
                Toast.makeText(this, "Price cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }
            if (TextUtils.isEmpty(et_loan.getText().toString())) {
                Toast.makeText(this, "loan cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }
            showLoan(); // total loan
        } else if (v.getId() == R.id.btn_calculate) { // click calculating the payment
            if (hasBusiness && TextUtils.isEmpty(et_business.getText().toString())) {
                Toast.makeText(this, "business cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }
            if (hasAccumulation && TextUtils.isEmpty(et_accumulation.getText().toString())) {
                Toast.makeText(this, "the total loan of accumulation fund cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!hasBusiness && !hasAccumulation) {
                Toast.makeText(this, "Please choose the loan of accumulation fund or the business loan", Toast.LENGTH_SHORT).show();
                return;
            }
            showRepayment(); // show the payment
        }
    }


    private void showLoan() {
        double total = Double.parseDouble(et_price.getText().toString());
        double rate = Double.parseDouble(et_loan.getText().toString()) / 100;
        String desc = String.format("The total loan is %s0000yuan", formatDecimal(total * rate, 2));
        tv_loan.setText(desc);
    }

    //make sure how many digits after the point
    private String formatDecimal(double value, int digit) {
        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(digit, RoundingMode.HALF_UP);
        return bd.toString();
    }

    // Calculate the total amount of repayment, interest and monthly payment according to the relevant conditions of the loan.
    private void showRepayment() {
        Repayment businessResult = new Repayment();
        Repayment accumulationResult = new Repayment();
        if (hasBusiness) { // apply for business loan
            double businessLoad = Double.parseDouble(et_business.getText().toString()) * 10000;
            double businessTime = mYear * 12;
            double businessRate = mBusinessRatio / 100;
            // payment for business loan
            businessResult = calMortgage(businessLoad, businessTime, businessRate, isInterest);
        }
        if (hasAccumulation) { // apply for accumulationload
            double accumulationLoad = Double.parseDouble(et_accumulation.getText().toString()) * 10000;
            double accumulationTime = mYear * 12;
            double accumulationRate = mAccumulationRatio / 100;
            // calculate the payment for the accumulation
            accumulationResult = calMortgage(accumulationLoad, accumulationTime, accumulationRate, isInterest);
        }
        String desc = String.format("The total payment is %s0000 yuan", formatDecimal(
                (businessResult.mTotal + accumulationResult.mTotal) / 10000, 2));
        desc = String.format("%s\n　　Total payment %s0000 yuan", desc, formatDecimal(
                (businessResult.mTotal + businessResult.mTotalInterest +
                        accumulationResult.mTotal + accumulationResult.mTotalInterest) / 10000, 2));
        desc = String.format("%s\ninterest %s0000 yuan", desc, formatDecimal(
                (businessResult.mTotalInterest + accumulationResult.mTotalInterest) / 10000, 2));
        desc = String.format("%s\n　　payment due为%d months", desc, mYear * 12);
        if (isInterest) { // Equivalent principal and interest
            desc = String.format("%s\npayment per month %syuan", desc, formatDecimal(
                    businessResult.mMonthRepayment + accumulationResult.mMonthRepayment, 2));
        } else { // Equivalent principal
            desc = String.format("%s\nfirst payment%syuan，next per month%syuan", desc, formatDecimal(
                    businessResult.mMonthRepayment + accumulationResult.mMonthRepayment, 2),
                    formatDecimal(businessResult.mMonthMinus + accumulationResult.mMonthMinus, 2));
        }
        tv_payment.setText(desc);
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        if (checkedId == R.id.rb_interest) { // Equivalent principal and interest
            isInterest = true;
        } else if (checkedId == R.id.rb_principal) { // Equivalent principal
            isInterest = false;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView.getId() == R.id.ck_business) { // tick the business loan
            hasBusiness = isChecked;
        } else if (buttonView.getId() == R.id.ck_accumulation) { // tich the accumulationload
            hasAccumulation = isChecked;
        }
    }

    // calculate the repayment based on the input information
    private Repayment calMortgage(double ze, double nx, double rate, boolean bInterest) {
        double zem = (ze * rate / 12 * Math.pow((1 + rate / 12), nx))
                / (Math.pow((1 + rate / 12), nx) - 1);
        double amount = zem * nx;
        double rateAmount = amount - ze;

        double benjinm = ze / nx;
        double lixim = ze * (rate / 12);
        double diff = benjinm * (rate / 12);
        double huankuanm = benjinm + lixim;
        double zuihoukuan = diff + benjinm;
        double av = (huankuanm + zuihoukuan) / 2;
        double zong = av * nx;
        double zongli = zong - ze;

        Repayment result = new Repayment();
        result.mTotal = ze;
        if (bInterest) {
            result.mMonthRepayment = zem;
            result.mTotalInterest = rateAmount;
        } else {
            result.mMonthRepayment = huankuanm;
            result.mMonthMinus = diff;
            result.mTotalInterest = zongli;
        }
        return result;
    }
}
