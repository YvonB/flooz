package com.benahita.qrscan;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

import github.nisrulz.qreader.QRDataListener;
import github.nisrulz.qreader.QREader;

public class ScanActivity extends AppCompatActivity {

    // Val du QRcode
    private TextView qrCodeValue;
    //private TextView changeLang;
    private ImageView btnExit;
    private TextView homeBtn;
    private ImageView restartButton;

    // Les Btns



    // QREader
    private SurfaceView mySurfaceView;
    private QREader qrEader;
    private boolean hasCameraPermission = false;
    private boolean hasCallPermission = false;
    private static final String cameraPerm = Manifest.permission.CAMERA;
    private static final String callPerm = Manifest.permission.CALL_PHONE;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {

        fullScreen(); // Application full screen

        super.onCreate(savedInstanceState);

        loadLocale(); //  Langues

        setContentView(R.layout.activity_scan);

        // ============= La val du QR, la cam (surfaceView) & Les 03 btns ===============
        qrCodeValue = (TextView) findViewById(R.id.code_info); // Valeur du QRCode
        //changeLang = (TextView) findViewById(R.id.langues); // Choix de langues
        mySurfaceView = (SurfaceView) findViewById(R.id.camera_view); // La Surface view, la Cam
        btnExit =  (ImageView) findViewById(R.id.btn_exit); // Fermeture
        //btnExit.setTransformationMethod(null); // Texte du Btn en Miniscule
        restartButton = (ImageView) findViewById(R.id.btn_restart_activity); // Re scanner
        //restartButton.setTransformationMethod(null); // Texte du Btn en Miniscule
        homeBtn = (TextView) findViewById(R.id.home_btn); // home btn
        //homeBtn.setTransformationMethod(null); // home btn, texte minuscule

        // ============= Ouvrir la Cam ===============
        hasCameraPermission = RuntimePermissionUtil.checkPermissonGranted(this, cameraPerm);
        hasCallPermission = RuntimePermissionUtil.checkPermissonGranted(this, callPerm);

        if (hasCameraPermission) {

            if(hasCallPermission){

                setupQREader();

            }else{
                RuntimePermissionUtil.requestPermission(
                        ScanActivity.this,
                        callPerm,
                        200
                );
            }
        } else {
            RuntimePermissionUtil.requestPermission(
                    ScanActivity.this,
                    cameraPerm,
                    100
            );
        }

        // Changement de langues
       /* changeLang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showChangeLanguageDialog();
            }
        });*/

        // 3. Fermer l'application
        btnExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                exitApp();
            }
        });

        // Re scanner le Code  QR
        restartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                restartActivity();
            }
        });

    }

    // Methode pour l'affichage plein écran
    public void fullScreen(){
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_IMMERSIVE
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN;

        decorView.setSystemUiVisibility(uiOptions);
    }

    // Résultat de la demande d'autorisation faite au début de la demande
    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull final String[] permissions,
            @NonNull final int[] grantResults
    ) {
        if (requestCode == 100) {
            RuntimePermissionUtil.onRequestPermissionsResult(grantResults, new RPResultListener() {
                @Override
                public void onPermissionGranted() {
                    if ( RuntimePermissionUtil.checkPermissonGranted(ScanActivity.this, cameraPerm)) {
                        restartActivity();
                    }
                }

                @Override
                public void onPermissionDenied() {
                    // Do nothing
                    new AlertDialog.Builder(getApplicationContext())
                            .setMessage("L'application ne fonctionnera pas :(")
                            .show();
                }
            });
        }else if (requestCode == 200) {
            RuntimePermissionUtil.onRequestPermissionsResult(grantResults, new RPResultListener() {
                @Override
                public void onPermissionGranted() {
                    if ( RuntimePermissionUtil.checkPermissonGranted(ScanActivity.this, callPerm)) {
                        restartActivity();
                    }
                }

                @Override
                public void onPermissionDenied() {
                    // Do nothing
                    // Do nothing
                    new AlertDialog.Builder(getApplicationContext())
                            .setMessage("L'application ne vfonctionnera pas :(")
                            .show();
                }
            });
        }
    }

    // Methode permettant de stocker une langues choisie dans le préférence Système
    private void setLocale(String lang){
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
        // on stocke le donné dans le preférence du système
        SharedPreferences.Editor editor = getSharedPreferences("Settings", MODE_PRIVATE).edit();
        editor.putString("My_Lang", lang);
        editor.apply();
    }

    // Methode pour le chargement de la langue stockée dans le préférences tout a lheure
    public void loadLocale(){
        SharedPreferences prefs = getSharedPreferences("Settings", Activity.MODE_PRIVATE);
        String language = prefs.getString("My_Lang", "");
        setLocale(language);
    }

    // Methode permettant de voir le pop up permettant Changer les langues de lapp
    /*private void showChangeLanguageDialog() {
        // tableau des langues
        final String[] listItems = {"Malagasy", "Français", "English"};
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(ScanActivity.this);
        mBuilder.setTitle(R.string.lang_dialog_title);
        mBuilder.setSingleChoiceItems(listItems, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(which==0){
                    setLocale("mg");
                    recreate();
                }
                else if(which==1){
                    setLocale("fr");
                    recreate();
                }
                else if(which==2){
                    setLocale("en");
                    recreate();
                }

                // fermer le pop up lorsque une langue est séléctionnée
                dialog.dismiss();
            }
        });

        // Affiche le pop up contient la liste des langues
        AlertDialog mDialog = mBuilder.create();
        mDialog.show();
    }*/

    // Methode Quand l'utlisateur fait un retour sur son device = Fermer l'app
    @Override
    public void onBackPressed() {
        exitApp();
    }

    // Methode permettant de Fermer l'application avec le bouton Exit
    public void exitApp(){
                // TODO Auto-generated method stub
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ScanActivity.this);
                alertDialogBuilder.setMessage(R.string.exit_dialog_title);
                alertDialogBuilder.setPositiveButton(R.string.positive_btn_exit_dialog,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                Intent intent = new Intent(ScanActivity.this, MainActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                intent.putExtra("EXIT", true);
                                startActivity(intent);
                            }
                        });
                alertDialogBuilder.setNegativeButton(R.string.negative_btn_exit_dialog,new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        restartActivity();
                    }
                });

                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
    }

    // Methode permettant de redemarrer une activitée
    void restartActivity() {
        startActivity(new Intent(ScanActivity.this, ScanActivity.class));
        finish();
    }

    // Methode permettant de faire le SCAN du code QR
    void setupQREader() {
        // Init QREader
        // ------------
        qrEader = new QREader.Builder(this, mySurfaceView, new QRDataListener() {
            @Override
            public void onDetected(final String data) {
                qrCodeValue.post(new Runnable() {
                    @Override
                    public void run() {
                        try{
                            Intent callIntent = new Intent(Intent.ACTION_CALL).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            callIntent.setData(Uri.parse("tel:"+Uri.encode(data)));
                           //Toast.makeText(getApplicationContext(), "Data : "+Uri.encode(data), Toast.LENGTH_LONG).show();
                            // 5. Autorisatio d'appeler
                          /*  if (ActivityCompat.checkSelfPermission(ScanActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                                // Demande de permission d'appeler pour le téléphone
                                RuntimePermissionUtil.requestPermission(
                                        ScanActivity.this,
                                        callPerm,
                                        200
                                );

                            }*/

                            startActivity(callIntent);

                        }catch(Exception e){
                            e.printStackTrace();
                            new AlertDialog.Builder(getApplicationContext())
                                    .setMessage("Pas de carte Sim trouvé :(")
                                    .show();
                        }

                    }
                });
            }
        }).facing(QREader.BACK_CAM)
                .enableAutofocus(true)
                .height(400)
                .width(1800)
                .build();
    }

    // Methode permettant de faire une pause écran au moment de l'innactivitée de lApp
    @Override
    protected void onPause() {
        super.onPause();

        if (hasCameraPermission) {

            // Nettoyage en onPause()
            // --------------------
            qrEader.releaseAndCleanup();
        }
    }

    // Methode permettant de faire redemerer lapp apres une "inactivitée de lApp
    @Override
    protected void onResume() {
        super.onResume();

        if (hasCameraPermission) {

            // Init et Start avec SurfaceView
            // -------------------------------
            qrEader.initAndStart(mySurfaceView);
        }
    }

}
