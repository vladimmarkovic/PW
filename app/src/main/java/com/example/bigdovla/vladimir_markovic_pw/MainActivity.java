package com.example.bigdovla.vladimir_markovic_pw;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.math.BigDecimal;
import java.util.EnumSet;

import io.mpos.accessories.AccessoryFamily;
import io.mpos.accessories.parameters.AccessoryParameters;
import io.mpos.provider.ProviderMode;
import io.mpos.transactions.Transaction;
import io.mpos.transactions.parameters.TransactionParameters;
import io.mpos.ui.shared.MposUi;
import io.mpos.ui.shared.model.MposUiConfiguration;

public class MainActivity extends AppCompatActivity {

    static Transaction lastTransaction = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final MposUi ui = MposUi.initialize(this, ProviderMode.MOCK,
                "merchantIdentifier", "merchantSecretKey");

        ui.getConfiguration().setSummaryFeatures(EnumSet.of(
                MposUiConfiguration.SummaryFeature.CAPTURE_TRANSACTION
        ));

        AccessoryParameters accessoryParameters = new AccessoryParameters.Builder(AccessoryFamily.MOCK)
                .mocked()
                .build();

        ui.getConfiguration().setTerminalParameters(accessoryParameters);

        final EditText transactionAmount = (EditText) findViewById(R.id.transaction_edit);
        Button paymentButton = (Button) findViewById(R.id.payment_button);
        Button refundButton = (Button) findViewById(R.id.refund_button);

        paymentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (transactionAmount.getText() == null || transactionAmount.getText().toString().equals("")) {
                    Toast.makeText(MainActivity.this, "Enter an amount first", Toast.LENGTH_SHORT).show();
                    return;
                }

                TransactionParameters transactionParameters = new TransactionParameters.Builder()
                        .charge(BigDecimal.valueOf(Double.parseDouble(transactionAmount.getText().toString())), io.mpos.transactions.Currency.USD)
                        .subject("Random transaction")
                        .customIdentifier("myRandomIdentifier")
                        .build();

                Intent intent = ui.createTransactionIntent(transactionParameters);
                startActivityForResult(intent, MposUi.REQUEST_CODE_PAYMENT);
            }
        });

        refundButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (lastTransaction != null){
                    TransactionParameters transactionParameters = new TransactionParameters.Builder()
                            .refund(lastTransaction.getIdentifier())
                            .amountAndCurrency(lastTransaction.getAmount(), lastTransaction.getCurrency())
                            .build();

                    Intent intent = ui.createTransactionIntent(transactionParameters);
                    startActivity(intent);
                    lastTransaction = null;
                }
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == MposUi.REQUEST_CODE_PAYMENT) {
            if (resultCode == MposUi.RESULT_CODE_APPROVED) {
                // Transaction was approved

                // Capture the transaction for next refund
                lastTransaction = MposUi.getInitializedInstance().getTransaction();

                Toast.makeText(this, "Transaction approved, amount: " + lastTransaction.getAmount(), Toast.LENGTH_LONG).show();
            } else {
                // Card was declined, or transaction was aborted, or failed
                // (e.g. no internet or accessory not found)
                Toast.makeText(this, "Transaction was declined, aborted, or failed",
                        Toast.LENGTH_LONG).show();
            }
        }
    }
}