package com.swifteralex.asciicamera;

import android.Manifest;
import android.content.pm.PackageManager;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Size;
import android.widget.EditText;
import android.widget.Toast;
import androidx.camera.core.*;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;
import java.nio.ByteBuffer;

public class MainActivity extends AppCompatActivity implements LifecycleOwner {

    private String[] REQUIRED_PERMISSIONS = new String[] {Manifest.permission.CAMERA};
    private long lastAnalyzedTimestamp = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Request camera permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, 10);
        }
    }

    public void startCamera() {
        ImageAnalysisConfig config =
                new ImageAnalysisConfig.Builder()
                        .setTargetResolution(new Size(1065, 1988))
                        .setImageReaderMode(ImageAnalysis.ImageReaderMode.ACQUIRE_LATEST_IMAGE)
                        .build();

        ImageAnalysis imageAnalysis = new ImageAnalysis(config);

        imageAnalysis.setAnalyzer(
                new ImageAnalysis.Analyzer() {

                    @Override
                    public void analyze(ImageProxy image, int rotationDegrees) {

                        // Possible ASCII characters: !\u0022#$%\u0026'()*+,-/0123456789:;\u003c=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~
                        // Each character is 15 pixels across and 28 pixels high
                        // Width is 71 characters, height is 71 characters

                        long currentTimestamp = System.currentTimeMillis();

                        if (currentTimestamp - lastAnalyzedTimestamp >= 50) {
                            EditText editText = (EditText)findViewById(R.id.editText2);
                            editText.setKeyListener(null);

                            final ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                            buffer.rewind();
                            byte[] data = new byte[buffer.remaining()];
                            buffer.get(data);

                            Integer[] pixelGrid = new Integer[1080*1440];
                            for(int c = 0; c < 1440; c++){
                                for(int r = 0; r < 1080; r++){
                                    Integer toInt = data[1440*(1079-r) + c] & 255;
                                    pixelGrid[r + c*1080] = toInt;
                                }
                            }

                            StringBuilder sb = new StringBuilder();
                            int k = 0;

                            for(int j=0; j<5041; j++){

                                double pixelAverageLum = pixelGrid[(j%71)*15 + k*20*1080];

                                if(j%71 == 0 && j > 0){
                                    sb.append("\n");
                                    k++;
                                }

                                if(pixelAverageLum >= 230){
                                    sb.append(" ");
                                }else if(pixelAverageLum >= 209){
                                    sb.append("`");
                                }else if(pixelAverageLum >= 203){
                                    sb.append("-");
                                }else if(pixelAverageLum >= 197){
                                    sb.append("_");
                                }else if(pixelAverageLum >= 186){
                                    sb.append(":");
                                }else if(pixelAverageLum >= 180){
                                    sb.append("=");
                                }else if(pixelAverageLum >= 174){
                                    sb.append("+");
                                }else if(pixelAverageLum >= 168){
                                    sb.append("^");
                                }else if(pixelAverageLum >= 157){
                                    sb.append("/");
                                }else if(pixelAverageLum >= 151){
                                    sb.append("?");
                                }else if(pixelAverageLum >= 145){
                                    sb.append("[");
                                }else if(pixelAverageLum >= 139){
                                    sb.append("]");
                                }else if(pixelAverageLum >= 133){
                                    sb.append("c");
                                }else if(pixelAverageLum >= 127){
                                    sb.append("3");
                                }else if(pixelAverageLum >= 121){
                                    sb.append("I");
                                }else if(pixelAverageLum >= 115){
                                    sb.append("o");
                                }else if(pixelAverageLum >= 109){
                                    sb.append("f");
                                }else if(pixelAverageLum >= 103){
                                    sb.append("L");
                                }else if(pixelAverageLum >= 97){
                                    sb.append("0");
                                }else if(pixelAverageLum >= 91){
                                    sb.append("T");
                                }else if(pixelAverageLum >= 85){
                                    sb.append("O");
                                }else if(pixelAverageLum >= 79){
                                    sb.append("S");
                                }else if(pixelAverageLum >= 73){
                                    sb.append("D");
                                }else if(pixelAverageLum >= 67){
                                    sb.append("#");
                                }else if(pixelAverageLum >= 61){
                                    sb.append("X");
                                }else if(pixelAverageLum >= 55){
                                    sb.append("H");
                                }else if(pixelAverageLum >= 49){
                                    sb.append("E");
                                }else if(pixelAverageLum >= 38){
                                    sb.append("W");
                                }else if(pixelAverageLum >= 32){
                                    sb.append("$");
                                }else if(pixelAverageLum >= 16){
                                    sb.append("@");
                                }else if(pixelAverageLum >= 0){
                                    sb.append("M");
                                }
                            }

                            editText.setText(sb.toString());

                            lastAnalyzedTimestamp = currentTimestamp;
                        }
                    }
                }
        );

        CameraX.bindToLifecycle(this, imageAnalysis);
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 10) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            } else {
                Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
}
