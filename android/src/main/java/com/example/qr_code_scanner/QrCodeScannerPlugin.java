package com.example.qr_code_scanner_plugin;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallback;
import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.embedding.engine.plugins.methodchannel.MethodCall;
import io.flutter.embedding.engine.plugins.methodchannel.MethodChannel;
import io.flutter.embedding.engine.plugins.methodchannel.MethodChannel.MethodCallHandler;
import io.flutter.embedding.engine.plugins.methodchannel.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.RequestPermissionsResultListener;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.util.Arrays;
import java.util.List;

public class QrCodeScannerPlugin implements FlutterPlugin, MethodCallHandler, ActivityAware {

    private static final int REQUEST_CODE_QR_SCAN = 101;
    private Activity activity;
    private MethodChannel methodChannel;

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding binding) {
        methodChannel = new MethodChannel(binding.getBinaryMessenger(), "scaner");
        methodChannel.setMethodCallHandler(this);
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        methodChannel.setMethodCallHandler(null);
    }

    @Override
    public void onAttachedToActivity(@NonNull ActivityPluginBinding binding) {
        activity = binding.getActivity();
        binding.addActivityResultListener(this);
    }

    @Override
    public void onDetachedFromActivityForConfigChanges() {
        activity = null;
    }

    @Override
    public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding binding) {
        activity = binding.getActivity();
    }

    @Override
    public void onDetachedFromActivity() {
        activity = null;
    }

    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
        if (call.method.equals("scan")) {
            startQrScanner(result);
        } else {
            result.notImplemented();
        }
    }

    
    private static final String TAG = "QrCodeScannerPlugin";

    private void startQrScanner(Result result) {
        if (activity == null) {
            result.error("ACTIVITY_NULL", "Activity is null", null);
            return;
        }

        Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        activity.startActivityForResult(intent, REQUEST_CODE_QR_SCAN);
    
    // You'll handle the result in onActivityResult method
    // For now, let's just send a success message to Flutter with a placeholder text
        result.success("Please handle onActivityResult for QR scanning result");
    }

// This method will handle the result after the camera activity is completed
    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_QR_SCAN && resultCode == Activity.RESULT_OK) {
           Bitmap bitmap = (Bitmap) data.getExtras().get("data");
           String result = decodeQRCode(bitmap);
            
            if (result != null) {
                methodChannel.invokeMethod("onQrScanned", result);
            } else {
                methodChannel.invokeMethod("onError", "QR code not found");
            }
        
            return true;
        }
        return false;
    }
    private String decodeQRCode(Bitmap bitmap) {
        try {
            com.google.zxing.Reader reader = new MultiFormatReader();
            int[] pixels = new int[bitmap.getWidth() * bitmap.getHeight()];
            bitmap.getPixels(pixels, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
    
            RGBLuminanceSource source = new RGBLuminanceSource(bitmap.getWidth(), bitmap.getHeight(), pixels);
            BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(source));
            
            Result result = reader.decode(binaryBitmap);
            return result.getText();
        } catch (Exception e) {
            Log.e(TAG, "Error decoding QR code", e);
            return null;
        }
    }

}
