package com.swifteralex.asciicamera;

import android.Manifest;
import android.content.pm.PackageManager;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Point;
import android.os.Bundle;
import android.util.Size;
import android.view.Display;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.camera.core.*;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;
import java.nio.ByteBuffer;

public class MainActivity extends AppCompatActivity implements LifecycleOwner {

    private String[] REQUIRED_PERMISSIONS = new String[] {Manifest.permission.CAMERA};
    private int screenWidth;
    private int screenHeight;
    private CameraX.LensFacing lensFacing = CameraX.LensFacing.BACK;
    private boolean reverseCameraButtonPressed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        screenWidth = size.x;
        screenHeight = size.y - 130; //Leave room for buttons at the bottom

        // Request camera permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, 10);
        }
    }

    public void reverseCameraButtonClicked(View view) {
        reverseCameraButtonPressed = true;
        if (lensFacing == CameraX.LensFacing.BACK){
            lensFacing = CameraX.LensFacing.FRONT;
        } else {
            lensFacing = CameraX.LensFacing.BACK;
        }
        startCamera();
    }

    public void startCamera() {
        CameraX.unbindAll();

        ImageAnalysisConfig config =
                new ImageAnalysisConfig.Builder()
                        .setTargetResolution(new Size(1440, 1080))
                        .setImageReaderMode(ImageAnalysis.ImageReaderMode.ACQUIRE_LATEST_IMAGE)
                        .setLensFacing(lensFacing)
                        .build();
        ImageAnalysis imageAnalysis = new ImageAnalysis(config);
        imageAnalysis.setAnalyzer(
                new ImageAnalysis.Analyzer() {
                    public void analyze(ImageProxy image, int rotationDegrees) {

                        // Possible ASCII characters: !\u0022#$%\u0026'()*+,-/0123456789:;\u003c=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~
                        // Each character is 15 pixels across and 28 pixels high

                        if (reverseCameraButtonPressed) { //This prevents any new images from being analyzed after the reverse camera button has been pressed
                            reverseCameraButtonPressed = false;
                            return;
                        }

                        int imageWidth = image.getWidth();
                        int imageHeight = image.getHeight();
                        int charactersByLength = screenWidth/15 - 1;
                        int charactersByHeight = screenHeight/28 - 1;

                        EditText editText = (EditText)findViewById(R.id.editText2);
                        editText.setKeyListener(null);

                        final ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                        buffer.rewind();
                        byte[] data = new byte[buffer.remaining()];
                        buffer.get(data);

                        StringBuilder sb = new StringBuilder();
                        int line = 0;

                        for(int j=0; j<charactersByLength*charactersByHeight; j++){

                            int pixelAverageLum;
                            if(lensFacing == CameraX.LensFacing.BACK) {
                                pixelAverageLum = data[(charactersByLength - j % charactersByLength) * imageWidth * (imageHeight / charactersByLength - 4)
                                        + line * (imageWidth / charactersByLength)] & 255;
                            }else{
                                pixelAverageLum = data[(charactersByLength - j % charactersByLength) * imageWidth * (imageHeight / charactersByLength - 4)
                                        + (charactersByHeight - line) * (imageWidth / charactersByLength)] & 255;
                            }

                            if(j%charactersByLength == 0 && j > 0){
                                sb.append("\n");
                                line++;
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
                            }else{
                                sb.append("M");
                            }
                        }

                        editText.setText(sb.toString());
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
