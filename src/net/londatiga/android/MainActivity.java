package net.londatiga.android;

import java.io.File;
import java.io.IOException;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.WallpaperManager;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ResolveInfo;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.provider.MediaStore;
import android.view.Display;
import android.view.View;

import android.net.Uri;

import android.os.Bundle;
import android.os.Environment;

import android.widget.Button;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import android.widget.ImageView;

public class MainActivity extends Activity {
	private Uri mImageCaptureUri;
	private ImageView mImageView;
	
	private static final int PICK_FROM_CAMERA = 1;
	private static final int CROP_FROM_CAMERA = 2;
	private static final int PICK_FROM_FILE = 3;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.main);
        
		Button button 	= (Button) findViewById(R.id.btn_crop);
		mImageView		= (ImageView) findViewById(R.id.iv_photo);
		
		button.setOnClickListener(new View.OnClickListener() {	
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                
                startActivityForResult(intent, PICK_FROM_FILE);
			}
		});
    }
    
    @Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    if (resultCode != RESULT_OK) return;
	   
	    switch (requestCode) {
		    	
		    case PICK_FROM_FILE: 
		    	mImageCaptureUri = data.getData();
		    	
		    	doCrop();
	    
		    	break;	    	
	    
		    case CROP_FROM_CAMERA:	    	
		        Bundle extras = data.getExtras();
	
		        if (extras != null) {	        	
		            Bitmap photo = extras.getParcelable("data");
		            
		            mImageView.setImageBitmap(photo);
		            
		            WallpaperManager wmCrop =WallpaperManager.getInstance(getApplicationContext());
		            try {
						wmCrop.setBitmap(photo);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
		        }
	
		        File f = new File(mImageCaptureUri.getPath());            
		        
		        if (f.exists()) f.delete();
	
		        break;

	    }
	}
    
    private void doCrop() {
		final ArrayList<CropOption> cropOptions = new ArrayList<CropOption>();
    	
    	Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setType("image/*");
        
        List<ResolveInfo> list = getPackageManager().queryIntentActivities( intent, 0 );
        
        int size = list.size();
        
     
        	intent.setData(mImageCaptureUri);
        	//get screen dimention
        	Display display = getWindowManager().getDefaultDisplay(); 
        	int width = display.getWidth();  // deprecated
        	int height = display.getHeight();  // deprecated
        	
        	
        	
            intent.putExtra("aspectX", width*2);
            intent.putExtra("aspectY", height);
         //   intent.putExtra("outputX", 200);
          //  intent.putExtra("outputY", 200);
            intent.putExtra("scale", true);
            intent.putExtra("return-data", true);
           
                 
        	if (size == 1) {
        		Intent i 		= new Intent(intent);
	        	ResolveInfo res	= list.get(0);
	        	
	        	i.setComponent( new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
	        	
	        	startActivityForResult(i, CROP_FROM_CAMERA);
	        	Toast CropToast= Toast.makeText(getApplicationContext(), "CROP_FROM_CAMERA Başlatıldı !", Toast.LENGTH_SHORT);
	        	CropToast.show();
        	} else {
		        for (ResolveInfo res : list) {
		        	final CropOption co = new CropOption();
		        	
		        	co.title 	= getPackageManager().getApplicationLabel(res.activityInfo.applicationInfo);
		        	co.icon		= getPackageManager().getApplicationIcon(res.activityInfo.applicationInfo);
		        	co.appIntent= new Intent(intent);
		        	
		        	co.appIntent.setComponent( new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
		        	
		            cropOptions.add(co);
		        }
	        
		        CropOptionAdapter adapter = new CropOptionAdapter(getApplicationContext(), cropOptions);
		        
		        AlertDialog.Builder builder = new AlertDialog.Builder(this);
		        builder.setTitle("Choose Crop App");
		        builder.setAdapter( adapter, new DialogInterface.OnClickListener() {
		            public void onClick( DialogInterface dialog, int item ) {
		                startActivityForResult( cropOptions.get(item).appIntent, CROP_FROM_CAMERA);
		            	Toast ChoseToast= Toast.makeText(getApplicationContext(), "CROP_FROM_CAMERA App seçme başladı", Toast.LENGTH_SHORT);
			        	ChoseToast.show();
		            }
		        });
	        
		        builder.setOnCancelListener( new DialogInterface.OnCancelListener() {
		            @Override
		            public void onCancel( DialogInterface dialog ) {
		               
		                if (mImageCaptureUri != null ) {
		                    getContentResolver().delete(mImageCaptureUri, null, null );
		                    mImageCaptureUri = null;
		                }
		            }
		        } );
		        
		        AlertDialog alert = builder.create();
		        
		        alert.show();
        	}
      	
        
	}
}