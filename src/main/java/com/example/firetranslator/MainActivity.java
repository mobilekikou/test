package com.example.firetranslator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslator;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslatorOptions;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private Spinner fromSpinner, toSpinner;
    private TextInputEditText sourceText;
    private ImageView micIV;
    private MaterialButton translatebtn;
    private TextView translateTV;

    String[] fromLanguage ={"English","Afrikaans","Arabic","Bulgarian","Bengali","Catalan","Czech",
            "Welsh","Hindi","Urdu"};
    String[] toLanguage ={"English","Afrikaans","Arabic","Bulgarian","Bengali","Catalan","Czech",
            "Welsh","Hindi","Urdu"};

    private static  final  int REQUEST_PERMISSION_CODE=1;
    int languageCode,fromLanguageCode,toLanguageCode;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fromSpinner = findViewById(R.id.idFromSpinner);
        toSpinner =findViewById(R.id.idToSpinner);
        sourceText = findViewById(R.id.idEditSource);
        micIV = findViewById(R.id.idIVMic);
        translatebtn = findViewById(R.id.idBtnTranslation);
        translateTV = findViewById(R.id.idTranslatedTV);

        fromSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                toLanguageCode = getLanguageCode(toLanguage[i]);

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        ArrayAdapter toAdapter = new ArrayAdapter(this , R.layout.spinner_item, fromLanguage);
        toAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        toSpinner.setAdapter(toAdapter);
        //===============
        toSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                fromLanguageCode = getLanguageCode(fromLanguage[i]);

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        ArrayAdapter fromAdapter = new ArrayAdapter(this , R.layout.spinner_item, toLanguage);
        fromAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fromSpinner.setAdapter(fromAdapter);
        //================
        micIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "say something to the translate");
                try {
                    startActivityForResult(intent, REQUEST_PERMISSION_CODE);

                }catch (Exception e){
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, ""+e.getMessage() ,Toast.LENGTH_SHORT).show();
                }
            }
        }); {
            translatebtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    translateTV.setVisibility(View.VISIBLE);
                    translateTV.setText("");
                    if (sourceText.getText().toString().isEmpty()
                    ) {
                        Toast.makeText(MainActivity.this, "Please enter text to translate", Toast.LENGTH_SHORT).show();
                    }else if (fromLanguageCode!=0){
                        Toast.makeText(MainActivity.this,"Please select source Language", Toast.LENGTH_SHORT).show();
                    }else if (toLanguageCode!=0){
                        Toast.makeText(MainActivity.this,"Please select the language to make translation ",Toast.LENGTH_SHORT).show();
                    }else {
                        translateText(fromLanguageCode, toLanguageCode,sourceText.getText().toString());

                    }
                }
            });
        };



    }

    private void translateText(int fromLanguageCode, int toLanguageCode, String source) {
        translateTV.setText("Downloadding model, please wait...");
        FirebaseTranslatorOptions options = new FirebaseTranslatorOptions.Builder()
                .setSourceLanguage(fromLanguageCode)
                .setTargetLanguage(toLanguageCode)
                .build();
        FirebaseTranslator translator = FirebaseNaturalLanguage.getInstance().getTranslator(options);
        FirebaseModelDownloadConditions conditions= new FirebaseModelDownloadConditions.Builder().build();

        ((FirebaseTranslator) translator).downloadModelIfNeeded(conditions).addOnSuccessListener(new OnSuccessListener<Void>(){
            public void onSuccess(Void aVoid){
                translateTV.setText("Translation...");
                translator.translate(source).addOnSuccessListener(new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String s) {
                        translateTV.setText(s);

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this,"Falied to download model! check your internet connection.",Toast.LENGTH_SHORT).show();
                    }

                    protected void onActivityResult(int requestCode,int resultcode,@Nullable Intent data){
                        MainActivity.super.onActivityResult(requestCode,resultcode,data);
                        if(requestCode==REQUEST_PERMISSION_CODE){
                            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                            sourceText.setText(result.get(0));


                        };
                    }
                });
            }
        });
    }

    //String[] fromlanguage ={"To","English","Afrikaans","Arabic",
    // "Bulgarian","Bengali","Catalan","Czech","Welsh","Hindi",
    // "Urdu"};
    private int getLanguageCode(String language){
        int languageCode =0 ;
        switch (language){
            case "English":
                language = String.valueOf(FirebaseTranslateLanguage.EN);
                break;
            case "Afrikaans":
                language = String.valueOf(FirebaseTranslateLanguage.AF);
                break;
            case "Arabic":
                language = String.valueOf(FirebaseTranslateLanguage.AR);
                break;
            case "Bulgarian":
                language = String.valueOf(FirebaseTranslateLanguage.BG);
                break;
            case "Bengali":
                language = String.valueOf(FirebaseTranslateLanguage.BN);
                break;
            case "Catalan":
                language = String.valueOf(FirebaseTranslateLanguage.CA);
                break;
            case "Czech":
                language = String.valueOf(FirebaseTranslateLanguage.CS);
                break;
            case "Welsh":
                language = String.valueOf(FirebaseTranslateLanguage.CY);
                break;
            case "Hindi":
                language = String.valueOf(FirebaseTranslateLanguage.HI);
                break;
            case "Urdu":
                language = String.valueOf(FirebaseTranslateLanguage.UR);
                break;
            default:
                languageCode =0;
        }
        return languageCode;
    }
}