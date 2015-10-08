package com.example.aidltest;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Process;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

public class ShieldService extends Service{
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i("1312312", "323232");
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		Toast.makeText(ShieldService.this, "bind", 1000).show();
		return shieldService;
	}
	
	private final LinkUIProcess.Stub shieldService = new LinkUIProcess.Stub() {
		
		@Override
		public int getPid() throws RemoteException {
			
			return Process.myPid();
		}
		
		@Override
		public void basicTypes(int anInt, long aLong, boolean aBoolean,
				float aFloat, double aDouble, String aString)
				throws RemoteException {
			
		}
		
		public void showToast(){
			Toast.makeText(ShieldService.this, "success", 1000).show();
		}
	};

}
