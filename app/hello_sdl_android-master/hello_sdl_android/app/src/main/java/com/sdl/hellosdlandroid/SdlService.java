package com.sdl.hellosdlandroid;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbManager;
import android.os.IBinder;
import android.util.Log;

import com.smartdevicelink.exception.SdlException;
import com.smartdevicelink.proxy.LockScreenManager;
import com.smartdevicelink.proxy.RPCRequest;
import com.smartdevicelink.proxy.RPCResponse;
import com.smartdevicelink.proxy.SdlProxyALM;
import com.smartdevicelink.proxy.callbacks.OnServiceEnded;
import com.smartdevicelink.proxy.callbacks.OnServiceNACKed;
import com.smartdevicelink.proxy.interfaces.IProxyListenerALM;
import com.smartdevicelink.proxy.rpc.AddCommand;
import com.smartdevicelink.proxy.rpc.AddCommandResponse;
import com.smartdevicelink.proxy.rpc.AddSubMenuResponse;
import com.smartdevicelink.proxy.rpc.AlertManeuverResponse;
import com.smartdevicelink.proxy.rpc.AlertResponse;
import com.smartdevicelink.proxy.rpc.ChangeRegistrationResponse;
import com.smartdevicelink.proxy.rpc.Choice;
import com.smartdevicelink.proxy.rpc.CreateInteractionChoiceSetResponse;
import com.smartdevicelink.proxy.rpc.DeleteCommandResponse;
import com.smartdevicelink.proxy.rpc.DeleteFileResponse;
import com.smartdevicelink.proxy.rpc.DeleteInteractionChoiceSetResponse;
import com.smartdevicelink.proxy.rpc.DeleteSubMenuResponse;
import com.smartdevicelink.proxy.rpc.DiagnosticMessageResponse;
import com.smartdevicelink.proxy.rpc.DialNumberResponse;
import com.smartdevicelink.proxy.rpc.EndAudioPassThruResponse;
import com.smartdevicelink.proxy.rpc.GenericResponse;
import com.smartdevicelink.proxy.rpc.GetDTCsResponse;
import com.smartdevicelink.proxy.rpc.GetVehicleDataResponse;
import com.smartdevicelink.proxy.rpc.GetWayPointsResponse;
import com.smartdevicelink.proxy.rpc.Image;
import com.smartdevicelink.proxy.rpc.ListFiles;
import com.smartdevicelink.proxy.rpc.ListFilesResponse;
import com.smartdevicelink.proxy.rpc.MenuParams;
import com.smartdevicelink.proxy.rpc.OnAudioPassThru;
import com.smartdevicelink.proxy.rpc.OnButtonEvent;
import com.smartdevicelink.proxy.rpc.OnButtonPress;
import com.smartdevicelink.proxy.rpc.OnCommand;
import com.smartdevicelink.proxy.rpc.OnDriverDistraction;
import com.smartdevicelink.proxy.rpc.OnHMIStatus;
import com.smartdevicelink.proxy.rpc.OnHashChange;
import com.smartdevicelink.proxy.rpc.OnKeyboardInput;
import com.smartdevicelink.proxy.rpc.OnLanguageChange;
import com.smartdevicelink.proxy.rpc.OnLockScreenStatus;
import com.smartdevicelink.proxy.rpc.OnPermissionsChange;
import com.smartdevicelink.proxy.rpc.OnStreamRPC;
import com.smartdevicelink.proxy.rpc.OnSystemRequest;
import com.smartdevicelink.proxy.rpc.OnTBTClientState;
import com.smartdevicelink.proxy.rpc.OnTouchEvent;
import com.smartdevicelink.proxy.rpc.OnVehicleData;
import com.smartdevicelink.proxy.rpc.OnWayPointChange;
import com.smartdevicelink.proxy.rpc.PerformAudioPassThruResponse;
import com.smartdevicelink.proxy.rpc.PerformInteraction;
import com.smartdevicelink.proxy.rpc.PerformInteractionResponse;
import com.smartdevicelink.proxy.rpc.PutFile;
import com.smartdevicelink.proxy.rpc.PutFileResponse;
import com.smartdevicelink.proxy.rpc.ReadDIDResponse;
import com.smartdevicelink.proxy.rpc.ResetGlobalPropertiesResponse;
import com.smartdevicelink.proxy.rpc.ScrollableMessageResponse;
import com.smartdevicelink.proxy.rpc.SendLocationResponse;
import com.smartdevicelink.proxy.rpc.SetAppIconResponse;
import com.smartdevicelink.proxy.rpc.SetDisplayLayoutResponse;
import com.smartdevicelink.proxy.rpc.SetGlobalPropertiesResponse;
import com.smartdevicelink.proxy.rpc.SetMediaClockTimerResponse;
import com.smartdevicelink.proxy.rpc.ShowConstantTbtResponse;
import com.smartdevicelink.proxy.rpc.ShowResponse;
import com.smartdevicelink.proxy.rpc.Slider;
import com.smartdevicelink.proxy.rpc.SliderResponse;
import com.smartdevicelink.proxy.rpc.SoftButton;
import com.smartdevicelink.proxy.rpc.SpeakResponse;
import com.smartdevicelink.proxy.rpc.StreamRPCResponse;
import com.smartdevicelink.proxy.rpc.SubscribeButtonResponse;
import com.smartdevicelink.proxy.rpc.SubscribeVehicleDataResponse;
import com.smartdevicelink.proxy.rpc.SubscribeWayPointsResponse;
import com.smartdevicelink.proxy.rpc.SystemRequestResponse;
import com.smartdevicelink.proxy.rpc.UnsubscribeButtonResponse;
import com.smartdevicelink.proxy.rpc.UnsubscribeVehicleDataResponse;
import com.smartdevicelink.proxy.rpc.UnsubscribeWayPointsResponse;
import com.smartdevicelink.proxy.rpc.UpdateTurnListResponse;
import com.smartdevicelink.proxy.rpc.VrHelpItem;
import com.smartdevicelink.proxy.rpc.enums.FileType;
import com.smartdevicelink.proxy.rpc.enums.HMILevel;
import com.smartdevicelink.proxy.rpc.enums.ImageType;
import com.smartdevicelink.proxy.rpc.enums.InteractionMode;
import com.smartdevicelink.proxy.rpc.enums.KeyboardEvent;
import com.smartdevicelink.proxy.rpc.enums.LayoutMode;
import com.smartdevicelink.proxy.rpc.enums.LockScreenStatus;
import com.smartdevicelink.proxy.rpc.enums.RequestType;
import com.smartdevicelink.proxy.rpc.enums.SdlDisconnectedReason;
import com.smartdevicelink.proxy.rpc.enums.SoftButtonType;
import com.smartdevicelink.proxy.rpc.enums.SystemAction;
import com.smartdevicelink.proxy.rpc.enums.TextAlignment;
import com.smartdevicelink.proxy.rpc.listeners.OnRPCResponseListener;
import com.smartdevicelink.transport.BTTransportConfig;
import com.smartdevicelink.transport.BaseTransportConfig;
import com.smartdevicelink.transport.MultiplexTransportConfig;
import com.smartdevicelink.transport.TCPTransportConfig;
import com.smartdevicelink.transport.TransportConstants;
import com.smartdevicelink.transport.USBTransportConfig;
import com.smartdevicelink.util.CorrelationIdGenerator;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

public class SdlService extends Service implements IProxyListenerALM{

	private static final String TAG 					= "SDL Service";

	private static final String APP_NAME 				= "eCalCharge";
	private static final String APP_ID 					= "8675309";
	
	private static final String ICON_FILENAME 			= "hello_sdl_icon.png";
	private static final String SDL_IMAGE_FILENAME  	= "sdl_full_image.png";
	private int iconCorrelationId;

	List<String> remoteFiles;
	
	private static final String WELCOME_SHOW 			= "Smart Charge your Ford Focus";
	private static final String WELCOME_SPEAK 			= "Smart Charge your Ford Focus";
	
	private static final String TEST_COMMAND_NAME 		= "Smart Charging";
	private static final int TEST_COMMAND_ID 			= 1;

	// TCP/IP transport config
	private static final int TCP_PORT = 12345;
	private static final String DEV_MACHINE_IP_ADDRESS = "10.0.2.2";

	public int autoCorrId = 0;
	private Integer mChoiceId1 = 101;
	private Integer mChoiceSetId1 = 11;
	private Integer mChoiceSetCorrId;

	// variable to create and call functions of the SyncProxy
	private SdlProxyALM proxy = null;

	private boolean firstNonHmiNone = true;
	private boolean isVehicleDataSubscribed = false;

	private String lockScreenUrlFromCore = null;
	private LockScreenManager lockScreenManager = new LockScreenManager();

	public static SdlService instance = null;

	public static SdlService getInstance() {
		return instance;
	}

	public void showWithSoftButtons(Integer corrID) {

		Vector<SoftButton> bVector = new Vector<>();
		SoftButton softBtn1 = new SoftButton();

		softBtn1.setType(SoftButtonType.SBT_TEXT);
		softBtn1.setIsHighlighted(true);
		softBtn1.setText("Yes");
		softBtn1.setSoftButtonID(55);
		softBtn1.setSystemAction(SystemAction.DEFAULT_ACTION);
		bVector.add(softBtn1);

		SoftButton sb2 = new SoftButton();
		sb2.setType(SoftButtonType.SBT_TEXT);
		sb2.setText("No");
		sb2.setIsHighlighted(false);
		sb2.setSoftButtonID(56);
		sb2.setSystemAction(SystemAction.DEFAULT_ACTION);
		bVector.add(sb2);

		try {
			proxy.show("Smart Charging", "Do you want to Smart Charge?", null, null, null, bVector, null, TextAlignment.RIGHT_ALIGNED, corrID);
		} catch (SdlException e) {
			Log.v("nntt", "Exception show softbutons, " + e);
		}
	}

	public void showWithSoftButtons_NO(Integer corrID) {

		Vector<SoftButton> bVector = new Vector<>();
		SoftButton softBtn1 = new SoftButton();

		softBtn1.setType(SoftButtonType.SBT_TEXT);
		softBtn1.setIsHighlighted(true);
		softBtn1.setText("OK");
		softBtn1.setSoftButtonID(57);
		softBtn1.setSystemAction(SystemAction.DEFAULT_ACTION);
		bVector.add(softBtn1);

		try {
			proxy.show("No Smart Charge", "Car will be charged in 2 Hrs", null, null, null, bVector, null, TextAlignment.CENTERED, corrID);
		} catch (SdlException e) {
			Log.v("nntt", "Exception show softbutons, " + e);
		}
	}

	public void showWithSoftButtons_YES_1(Integer corrID, boolean defaults, int hour, int minute) {

		if(defaults) {
			hour = 8;
			minute = 0;
		}

		if(hour > 23) {
			hour = 23;
		}
		if(minute > 59) {
			minute = 59;
		}

		String time = "";
		if(hour < 10) {
			time += "0" + hour;
		}
		else {
			time += hour;
		}
		time += ":";
		if(minute < 10) {
			time += "0" + minute;
		}
		else {
			time += minute;
		}

		Vector<SoftButton> bVector = new Vector<>();
		SoftButton softBtn1 = new SoftButton();

		softBtn1.setType(SoftButtonType.SBT_TEXT);
		softBtn1.setIsHighlighted(true);
		softBtn1.setText("Change");
		softBtn1.setSoftButtonID(441);
		softBtn1.setSystemAction(SystemAction.DEFAULT_ACTION);
		bVector.add(softBtn1);

		SoftButton softBtn2 = new SoftButton();

		softBtn2.setType(SoftButtonType.SBT_TEXT);
		softBtn2.setIsHighlighted(true);
		softBtn2.setText("Go!");
		softBtn2.setSoftButtonID(442);
		softBtn2.setSystemAction(SystemAction.DEFAULT_ACTION);
		bVector.add(softBtn2);

		try {
			String str = "Departure : " + time;
			proxy.show("Smart Charge", str, null, null, null, bVector, null, TextAlignment.CENTERED, corrID);
		} catch (SdlException e) {
			Log.v("nntt", "Exception show softbutons, " + e);
		}
	}

	public void showWithKeyBoard_YES(Integer corrID) {

		List<Integer> lst = new ArrayList<>();
		lst.add(1122);
		PerformInteraction performInteraction = new PerformInteraction();
		performInteraction.setInteractionLayout(LayoutMode.KEYBOARD);
		performInteraction.setInitialText("17:22");
		performInteraction.setCorrelationID(corrID);
		performInteraction.setInteractionChoiceSetIDList(lst);
		performInteraction.setInteractionMode(InteractionMode.MANUAL_ONLY);

		try {
			proxy.sendRPCRequest(performInteraction);
		} catch (SdlException e) {
			e.printStackTrace();
		}
	}

	public void showWithSoftButtons_YES_2(Integer corrID) {

		Vector<SoftButton> bVector = new Vector<>();
		SoftButton softBtn1 = new SoftButton();

		softBtn1.setType(SoftButtonType.SBT_TEXT);
		softBtn1.setIsHighlighted(true);
		softBtn1.setText("OK");
		softBtn1.setSoftButtonID(77);
		softBtn1.setSystemAction(SystemAction.DEFAULT_ACTION);
		bVector.add(softBtn1);

		try {
			proxy.show("Smart Charge", "Smart Charging in Process...", null, null, null, bVector, null, TextAlignment.CENTERED, corrID);
		} catch (SdlException e) {
			Log.v("nntt", "Exception show softbutons, " + e);
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
        Log.d(TAG, "onCreate");
		super.onCreate();
		remoteFiles = new ArrayList<String>();
		instance = this;
	}

	//nxkundu
	public void create() {
		Log.v("create", "create choice set");
		mChoiceSetCorrId = 1212;
		Choice c1 = new Choice();
		c1.setChoiceID(2121);
		c1.setMenuName("c1 menu name");

		List<String> list1 = new ArrayList<>();
		list1.add("Choice one");
		c1.setVrCommands(list1);

		c1.setTertiaryText("tertiary text1");
		c1.setSecondaryText("secondary text1");
		//c1.setSecondaryImage(image1);

		Vector<Choice> vector = new Vector<>();
		vector.add(c1);

		Log.v("nntt", "creating choice set, ");
		try {
			proxy.createInteractionChoiceSet(vector, 1122, mChoiceSetCorrId);
		} catch (SdlException e) {
			Log.v("nntt", "Exception creating choice set, " + e);
		}

	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		//Check if this was started with a flag to force a transport connect
		boolean forced = intent !=null && intent.getBooleanExtra(TransportConstants.FORCE_TRANSPORT_CONNECTED, false);
        startProxy(forced, intent);

		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		disposeSyncProxy();
		super.onDestroy();
	}

	public SdlProxyALM getProxy() {
		return proxy;
	}

	public void startProxy(boolean forceConnect, Intent intent) {
        Log.i(TAG, "Trying to start proxy");
		if (proxy == null) {
			try {
                Log.i(TAG, "Starting SDL Proxy");
				BaseTransportConfig transport = null;
				//transport = new TCPTransportConfig(TCP_PORT, DEV_MACHINE_IP_ADDRESS, true);
				transport = new TCPTransportConfig(12345, "10.0.2.2", true);
				/*if(BuildConfig.TRANSPORT.equals("MBT")){
					int securityLevel;
					if(BuildConfig.SECURITY.equals("HIGH")){
						securityLevel = MultiplexTransportConfig.FLAG_MULTI_SECURITY_HIGH;
					}else if(BuildConfig.SECURITY.equals("MED")){
						securityLevel = MultiplexTransportConfig.FLAG_MULTI_SECURITY_MED;
					}else if(BuildConfig.SECURITY.equals("LOW")){
						securityLevel = MultiplexTransportConfig.FLAG_MULTI_SECURITY_LOW;
					}else{
						securityLevel = MultiplexTransportConfig.FLAG_MULTI_SECURITY_OFF;
					}
					transport = new MultiplexTransportConfig(this, APP_ID, securityLevel);
				}else if(BuildConfig.TRANSPORT.equals("LBT")){
					transport = new BTTransportConfig();
				}else if(BuildConfig.TRANSPORT.equals("TCP")){
					transport = new TCPTransportConfig(TCP_PORT, DEV_MACHINE_IP_ADDRESS, true);
				}else if(BuildConfig.TRANSPORT.equals("USB")) {
					if (intent != null && intent.hasExtra(UsbManager.EXTRA_ACCESSORY)) { //If we want to support USB transport
						if (android.os.Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.HONEYCOMB) {
							Log.e(TAG, "Unable to start proxy. Android OS version is too low");
							return;
						}
						//We have a usb transport
						transport = new USBTransportConfig(getBaseContext(), (UsbAccessory) intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY));
						Log.d(TAG, "USB created.");
					}
				}*/
				transport = new TCPTransportConfig(TCP_PORT, DEV_MACHINE_IP_ADDRESS, true);
				if(transport != null) {
					proxy = new SdlProxyALM(this, APP_NAME, false, APP_ID, transport);
				}
			} catch (SdlException e) {
				e.printStackTrace();
				// error creating proxy, returned proxy = null
				if (proxy == null) {
					stopSelf();
				}
			}
		}else if(forceConnect){
			proxy.forceOnConnected();
		}
	}

	public void disposeSyncProxy() {
		LockScreenActivity.updateLockScreenStatus(LockScreenStatus.OFF);

		if (proxy != null) {
			try {
				proxy.dispose();
			} catch (SdlException e) {
				e.printStackTrace();
			}
			proxy = null;

		}
		this.firstNonHmiNone = true;
		this.isVehicleDataSubscribed = false;
		
	}

	/**
	 * Will show a sample test message on screen as well as speak a sample test message
	 */
	public void showTest(){
		try {
			proxy.show(TEST_COMMAND_NAME, "Do you want Smart Charging?", TextAlignment.CENTERED, CorrelationIdGenerator.generateId());
			proxy.speak(TEST_COMMAND_NAME, CorrelationIdGenerator.generateId());
			showWithSoftButtons(CorrelationIdGenerator.generateId());
		} catch (SdlException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 *  Add commands for the app on SDL.
	 */
	public void sendCommands(){
		AddCommand command = new AddCommand();
		MenuParams params = new MenuParams();
		params.setMenuName(TEST_COMMAND_NAME);
		command = new AddCommand();
		command.setCmdID(TEST_COMMAND_ID);
		command.setMenuParams(params);
		command.setVrCommands(Arrays.asList(new String[]{TEST_COMMAND_NAME}));
		sendRpcRequest(command);
	}

	/**
	 * Sends an RPC Request to the connected head unit. Automatically adds a correlation id.
	 * @param request
	 */
	private void sendRpcRequest(RPCRequest request){
		request.setCorrelationID(CorrelationIdGenerator.generateId());
		try {
			proxy.sendRPCRequest(request);
		} catch (SdlException e) {
			e.printStackTrace();
		}
	}
	/**
	 * Sends the app icon through the uploadImage method with correct params
	 * @throws SdlException
	 */
	private void sendIcon() throws SdlException {
		iconCorrelationId = CorrelationIdGenerator.generateId();
		uploadImage(R.mipmap.ic_launcher, ICON_FILENAME, iconCorrelationId, true);
	}
	
	/**
	 * This method will help upload an image to the head unit
	 * @param resource the R.drawable.__ value of the image you wish to send
	 * @param imageName the filename that will be used to reference this image
	 * @param correlationId the correlation id to be used with this request. Helpful for monitoring putfileresponses
	 * @param isPersistent tell the system if the file should stay or be cleared out after connection.
	 */
	private void uploadImage(int resource, String imageName,int correlationId, boolean isPersistent){
		PutFile putFile = new PutFile();
		putFile.setFileType(FileType.GRAPHIC_PNG);
		putFile.setSdlFileName(imageName);
		putFile.setCorrelationID(correlationId);
		putFile.setPersistentFile(isPersistent);
		putFile.setSystemFile(false);
		putFile.setBulkData(contentsOfResource(resource));

		try {
			proxy.sendRPCRequest(putFile);
		} catch (SdlException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Helper method to take resource files and turn them into byte arrays
	 * @param resource Resource file id.
	 * @return Resulting byte array.
	 */
	private byte[] contentsOfResource(int resource) {
		InputStream is = null;
		try {
			is = getResources().openRawResource(resource);
			ByteArrayOutputStream os = new ByteArrayOutputStream(is.available());
			final int bufferSize = 4096;
			final byte[] buffer = new byte[bufferSize];
			int available;
			while ((available = is.read(buffer)) >= 0) {
				os.write(buffer, 0, available);
			}
			return os.toByteArray();
		} catch (IOException e) {
			Log.w(TAG, "Can't read icon file", e);
			return null;
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public void onProxyClosed(String info, Exception e, SdlDisconnectedReason reason) {
		stopSelf();
		if(reason.equals(SdlDisconnectedReason.LANGUAGE_CHANGE) && BuildConfig.TRANSPORT.equals("MBT")){
			Intent intent = new Intent(TransportConstants.START_ROUTER_SERVICE_ACTION);
			intent.putExtra(SdlReceiver.RECONNECT_LANG_CHANGE, true);
			sendBroadcast(intent);
		}
	}

	@Override
	public void onOnHMIStatus(OnHMIStatus notification) {
		if(notification.getHmiLevel().equals(HMILevel.HMI_FULL)){			
			if (notification.getFirstRun()) {
				// send welcome message if applicable
				performWelcomeMessage();
			}
			create();
			// Other HMI (Show, PerformInteraction, etc.) would go here
		}

		uploadImages();
		if(!notification.getHmiLevel().equals(HMILevel.HMI_NONE)
				&& firstNonHmiNone){
			sendCommands();
			//uploadImages();
			firstNonHmiNone = false;
			
			// Other app setup (SubMenu, CreateChoiceSet, etc.) would go here
		}else{
			//We have HMI_NONE
//			if(notification.getFirstRun()){
				uploadImages();
//			}
		}
		
	}

	public void performChoiceSet(Integer corrID) {

		String initPromt = "Select or say an option";
		String displayText = "Select or say an option";


		VrHelpItem item = new VrHelpItem();
		item.setText("Choice One");
		item.setPosition(1);
		Vector<VrHelpItem> vector = new Vector<>();
		vector.add(item);

		try {
			proxy.performInteraction(initPromt, displayText, mChoiceSetId1, vector, corrID);
		} catch (SdlException e) {
			Log.v("nntt", "Exception performing choiceSet, " + e);
		}
	}

	/**
	 * Listener for handling when a lockscreen image is downloaded.
	 */
	private class LockScreenDownloadedListener implements LockScreenManager.OnLockScreenIconDownloadedListener{

		@Override
		public void onLockScreenIconDownloaded(Bitmap icon) {
			Log.i(TAG, "Lock screen icon downloaded successfully");
			LockScreenActivity.updateLockScreenImage(icon);
		}

		@Override
		public void onLockScreenIconDownloadError(Exception e) {
			Log.e(TAG, "Couldn't download lock screen icon, resorting to default.");
			LockScreenActivity.updateLockScreenImage(BitmapFactory.decodeResource(getResources(),
					R.drawable.sdl));
		}
	}
	
	/**
	 * Will show a sample welcome message on screen as well as speak a sample welcome message
	 */
	private void performWelcomeMessage(){
		try {
			Image image = new Image();
			image.setValue(SDL_IMAGE_FILENAME);
			image.setImageType(ImageType.DYNAMIC);

			//Set the welcome message on screen
			proxy.show(APP_NAME, WELCOME_SHOW, null, null, null, null, null, image, null, null, TextAlignment.CENTERED, CorrelationIdGenerator.generateId());
			
			//Say the welcome message
			proxy.speak(WELCOME_SPEAK, CorrelationIdGenerator.generateId());
			
		} catch (SdlException e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 *  Requests list of images to SDL, and uploads images that are missing.
	 */
	private void uploadImages(){
		ListFiles listFiles = new ListFiles();
		listFiles.setOnRPCResponseListener(new OnRPCResponseListener() {
			@Override
			public void onResponse(int correlationId, RPCResponse response) {
				if(response.getSuccess()){
					remoteFiles = ((ListFilesResponse) response).getFilenames();
				}

				// Check the mutable set for the AppIcon
				// If not present, upload the image
				if(remoteFiles== null || !remoteFiles.contains(SdlService.ICON_FILENAME)){
					try {
						sendIcon();
					} catch (SdlException e) {
						e.printStackTrace();
					}
				}else{
					// If the file is already present, send the SetAppIcon request
					try {
						proxy.setappicon(ICON_FILENAME, CorrelationIdGenerator.generateId());
					} catch (SdlException e) {
						e.printStackTrace();
					}
				}

				// Check the mutable set for the SDL image
				// If not present, upload the image
				//if(remoteFiles== null || !remoteFiles.contains(SdlService.SDL_IMAGE_FILENAME)){
					uploadImage(R.drawable.sdl, SDL_IMAGE_FILENAME, CorrelationIdGenerator.generateId(), true);
				//}else{
					// If the file is already present, do nothing
				//}
			}
		});
		this.sendRpcRequest(listFiles);
	}

	@Override
	public void onListFilesResponse(ListFilesResponse response) {
		Log.i(TAG, "onListFilesResponse from SDL ");
	}

	@Override
	public void onPutFileResponse(PutFileResponse response) {
		Log.i(TAG, "onPutFileResponse from SDL");
		if(response.getCorrelationID() == iconCorrelationId){ //If we have successfully uploaded our icon, we want to set it
			try {
				proxy.setappicon(ICON_FILENAME, CorrelationIdGenerator.generateId());
			} catch (SdlException e) {
				e.printStackTrace();
			}
		}

	}
	
	@Override
	public void onOnLockScreenNotification(OnLockScreenStatus notification) {
		LockScreenActivity.updateLockScreenStatus(notification.getShowLockScreen());
	}

	@Override
	public void onOnCommand(OnCommand notification){
		Integer id = notification.getCmdID();
		if(id != null){
			switch(id){
				case TEST_COMMAND_ID:
					showTest();
					break;
			}
			//onAddCommandClicked(id);
		}
	}

	/**
	 *  Callback method that runs when the add command response is received from SDL.
	 */
	@Override
	public void onAddCommandResponse(AddCommandResponse response) {
		Log.i(TAG, "AddCommand response from SDL: " + response.getResultCode().name());

	}

	
	/*  Vehicle Data   */
	
	
	@Override
	public void onOnPermissionsChange(OnPermissionsChange notification) {
		Log.i(TAG, "Permision changed: " + notification);

		/* Uncomment to subscribe to vehicle data
		List<PermissionItem> permissions = notification.getPermissionItem();
		for(PermissionItem permission:permissions){
			if(permission.getRpcName().equalsIgnoreCase(FunctionID.SUBSCRIBE_VEHICLE_DATA.name())){
				if(permission.getHMIPermissions().getAllowed()!=null && permission.getHMIPermissions().getAllowed().size()>0){
					if(!isVehicleDataSubscribed){ //If we haven't already subscribed we will subscribe now
						//TODO: Add the vehicle data items you want to subscribe to
						//proxy.subscribevehicledata(gps, speed, rpm, fuelLevel, fuelLevel_State, instantFuelConsumption, externalTemperature, prndl, tirePressure, odometer, beltStatus, bodyInformation, deviceStatus, driverBraking, correlationID);
						proxy.subscribevehicledata(false, true, rpm, false, false, false, false, false, false, false, false, false, false, false, autoIncCorrId++);
					}
				}
			}
		}
		*/
	}
		
	@Override
	public void onSubscribeVehicleDataResponse(SubscribeVehicleDataResponse response) {
		if(response.getSuccess()){
			Log.i(TAG, "Subscribed to vehicle data");
			this.isVehicleDataSubscribed = true;
		}
	}
	
	@Override
	public void onOnVehicleData(OnVehicleData notification) {
		Log.i(TAG, "Vehicle data notification from SDL");
		//TODO Put your vehicle data code here
		//ie, notification.getSpeed().

	}
	
	/**
	 * Rest of the SDL callbacks from the head unit
	 */
	
	@Override
	public void onAddSubMenuResponse(AddSubMenuResponse response) {
        Log.i(TAG, "AddSubMenu response from SDL: " + response.getResultCode().name() + " Info: " + response.getInfo());
	}

	@Override
	public void onCreateInteractionChoiceSetResponse(CreateInteractionChoiceSetResponse response) {
        Log.i(TAG, "CreateInteractionChoiceSet response from SDL: " + response.getResultCode().name() + " Info: " + response.getInfo());
	}

	@Override
	public void onAlertResponse(AlertResponse response) {
        Log.i(TAG, "Alert response from SDL: " + response.getResultCode().name() + " Info: " + response.getInfo());
	}

	@Override
	public void onDeleteCommandResponse(DeleteCommandResponse response) {
        Log.i(TAG, "DeleteCommand response from SDL: " + response.getResultCode().name() + " Info: " + response.getInfo());
	}

	@Override
	public void onDeleteInteractionChoiceSetResponse(DeleteInteractionChoiceSetResponse response) {
        Log.i(TAG, "DeleteInteractionChoiceSet response from SDL: " + response.getResultCode().name() + " Info: " + response.getInfo());
	}

	@Override
	public void onDeleteSubMenuResponse(DeleteSubMenuResponse response) {
        Log.i(TAG, "DeleteSubMenu response from SDL: " + response.getResultCode().name() + " Info: " + response.getInfo());
	}

	@Override
	public void onPerformInteractionResponse(PerformInteractionResponse response) {
		//nxkundu
        Log.i(TAG, "PerformInteraction response from SDL: " + response.getResultCode().name() + " Info: " + response.getInfo());
	}

	@Override
	public void onResetGlobalPropertiesResponse(
			ResetGlobalPropertiesResponse response) {
        Log.i(TAG, "ResetGlobalProperties response from SDL: " + response.getResultCode().name() + " Info: " + response.getInfo());
	}

	@Override
	public void onSetGlobalPropertiesResponse(SetGlobalPropertiesResponse response) {
        Log.i(TAG, "SetGlobalProperties response from SDL: " + response.getResultCode().name() + " Info: " + response.getInfo());
	}

	@Override
	public void onSetMediaClockTimerResponse(SetMediaClockTimerResponse response) {
        Log.i(TAG, "SetMediaClockTimer response from SDL: " + response.getResultCode().name() + " Info: " + response.getInfo());
	}

	@Override
	public void onShowResponse(ShowResponse response) {
        Log.i(TAG, "Show response from SDL: " + response.getResultCode().name() + " Info: " + response.getInfo());
	}

	@Override
	public void onSpeakResponse(SpeakResponse response) {
        Log.i(TAG, "SpeakCommand response from SDL: " + response.getResultCode().name() + " Info: " + response.getInfo());
	}

	@Override
	public void onOnButtonEvent(OnButtonEvent notification) {
        Log.i(TAG, "OnButtonEvent notification from SDL: " + notification);
		System.out.println("Clicked 1");
	}

	@Override
	public void onOnButtonPress(OnButtonPress notification) {
        Log.i(TAG, "OnButtonPress notification from SDL: " + notification);
		System.out.println("Clicked 2");
		//System.out.println((notification.getFunctionName())); //CUSTOM_BUTTON
		int intButtonId = notification.getCustomButtonName();
		System.out.println(intButtonId);

		switch(intButtonId) {

			case 55:
				System.out.println("You Entered YES");
				showWithSoftButtons_YES_1(44, true, 0, 0);
				break;
			case 56:
				System.out.println("You Entered NO");
				showWithSoftButtons_NO(22);
				break;
			case 441:
				showWithKeyBoard_YES(999);
				break;
			case 442:
				showWithSoftButtons_YES_2(1265);
				break;

		}
	}

	@Override
	public void onSubscribeButtonResponse(SubscribeButtonResponse response) {
        Log.i(TAG, "SubscribeButton response from SDL: " + response.getResultCode().name() + " Info: " + response.getInfo());
	}

	@Override
	public void onUnsubscribeButtonResponse(UnsubscribeButtonResponse response) {
        Log.i(TAG, "UnsubscribeButton response from SDL: " + response.getResultCode().name() + " Info: " + response.getInfo());
	}


	@Override
	public void onOnTBTClientState(OnTBTClientState notification) {
        Log.i(TAG, "OnTBTClientState notification from SDL: " + notification);
	}

	@Override
	public void onUnsubscribeVehicleDataResponse(
			UnsubscribeVehicleDataResponse response) {
        Log.i(TAG, "UnsubscribeVehicleData response from SDL: " + response.getResultCode().name() + " Info: " + response.getInfo());

	}

	@Override
	public void onGetVehicleDataResponse(GetVehicleDataResponse response) {
        Log.i(TAG, "GetVehicleData response from SDL: " + response.getResultCode().name() + " Info: " + response.getInfo());

	}

	@Override
	public void onReadDIDResponse(ReadDIDResponse response) {
        Log.i(TAG, "ReadDID response from SDL: " + response.getResultCode().name() + " Info: " + response.getInfo());

	}

	@Override
	public void onGetDTCsResponse(GetDTCsResponse response) {
        Log.i(TAG, "GetDTCs response from SDL: " + response.getResultCode().name() + " Info: " + response.getInfo());

	}


	@Override
	public void onPerformAudioPassThruResponse(PerformAudioPassThruResponse response) {
        Log.i(TAG, "PerformAudioPassThru response from SDL: " + response.getResultCode().name() + " Info: " + response.getInfo());

	}

	@Override
	public void onEndAudioPassThruResponse(EndAudioPassThruResponse response) {
        Log.i(TAG, "EndAudioPassThru response from SDL: " + response.getResultCode().name() + " Info: " + response.getInfo());

	}

	@Override
	public void onOnAudioPassThru(OnAudioPassThru notification) {
		Log.i(TAG, "OnAudioPassThru notification from SDL: " + notification );
	}

	@Override
	public void onDeleteFileResponse(DeleteFileResponse response) {
        Log.i(TAG, "DeleteFile response from SDL: " + response.getResultCode().name() + " Info: " + response.getInfo());

	}

	@Override
	public void onSetAppIconResponse(SetAppIconResponse response) {
        Log.i(TAG, "SetAppIcon response from SDL: " + response.getResultCode().name() + " Info: " + response.getInfo());

	}

	@Override
	public void onScrollableMessageResponse(ScrollableMessageResponse response) {
        Log.i(TAG, "ScrollableMessage response from SDL: " + response.getResultCode().name() + " Info: " + response.getInfo());

	}

	@Override
	public void onChangeRegistrationResponse(ChangeRegistrationResponse response) {
        Log.i(TAG, "ChangeRegistration response from SDL: " + response.getResultCode().name() + " Info: " + response.getInfo());

	}

	@Override
	public void onSetDisplayLayoutResponse(SetDisplayLayoutResponse response) {
        Log.i(TAG, "SetDisplayLayout response from SDL: " + response.getResultCode().name() + " Info: " + response.getInfo());

	}

	@Override
	public void onOnLanguageChange(OnLanguageChange notification) {
        Log.i(TAG, "OnLanguageChange notification from SDL: " + notification);

	}

	@Override
	public void onSliderResponse(SliderResponse response) {
        Log.i(TAG, "Slider response from SDL: " + response.getResultCode().name() + " Info: " + response.getInfo());

	}


	@Override
	public void onOnHashChange(OnHashChange notification) {
        Log.i(TAG, "OnHashChange notification from SDL: " + notification);

	}

	@Override
	public void onOnSystemRequest(OnSystemRequest notification) {
        Log.i(TAG, "OnSystemRequest notification from SDL: " + notification);

		// Download the lockscreen icon Core desires
		if(notification.getRequestType().equals(RequestType.LOCK_SCREEN_ICON_URL) && lockScreenUrlFromCore == null){
			lockScreenUrlFromCore = notification.getUrl();
			if(lockScreenUrlFromCore != null && lockScreenManager.getLockScreenIcon() == null){
				lockScreenManager.downloadLockScreenIcon(lockScreenUrlFromCore, new LockScreenDownloadedListener());
			}
		}
	}

	@Override
	public void onSystemRequestResponse(SystemRequestResponse response) {
        Log.i(TAG, "SystemRequest response from SDL: " + response.getResultCode().name() + " Info: " + response.getInfo());

	}

	@Override
	public void onOnKeyboardInput(OnKeyboardInput notification) {
        Log.i(TAG, "OnKeyboardInput notification from SDL: " + notification);

		switch(notification.getEvent()) {
			case KEYPRESS:
				String data = notification.getData();
				if(data!= null && data.length() == 4 && data.matches("\\d+")) {

					Integer hour = Integer.parseInt(data.substring(0,2));
					Integer minute = Integer.parseInt(data.substring(2));

					System.out.println("Time of Departure = " + hour + " : " + minute);

					notification.setEvent(KeyboardEvent.ENTRY_ABORTED);
					showWithSoftButtons_YES_1(88, false, hour, minute);
				}
		}

	}

	@Override
	public void onOnTouchEvent(OnTouchEvent notification) {
        Log.i(TAG, "OnTouchEvent notification from SDL: " + notification);

	}

	@Override
	public void onDiagnosticMessageResponse(DiagnosticMessageResponse response) {
        Log.i(TAG, "DiagnosticMessage response from SDL: " + response.getResultCode().name() + " Info: " + response.getInfo());

	}

	@Override
	public void onOnStreamRPC(OnStreamRPC notification) {
        Log.i(TAG, "OnStreamRPC notification from SDL: " + notification);

	}

	@Override
	public void onStreamRPCResponse(StreamRPCResponse response) {
        Log.i(TAG, "StreamRPC response from SDL: " + response.getResultCode().name() + " Info: " + response.getInfo());

	}

	@Override
	public void onDialNumberResponse(DialNumberResponse response) {
        Log.i(TAG, "DialNumber response from SDL: " + response.getResultCode().name() + " Info: " + response.getInfo());

	}

	@Override
	public void onSendLocationResponse(SendLocationResponse response) {
        Log.i(TAG, "SendLocation response from SDL: " + response.getResultCode().name() + " Info: " + response.getInfo());

	}

	@Override
	public void onServiceEnded(OnServiceEnded serviceEnded) {

	}

	@Override
	public void onServiceNACKed(OnServiceNACKed serviceNACKed) {

	}

	@Override
	public void onShowConstantTbtResponse(ShowConstantTbtResponse response) {
        Log.i(TAG, "ShowConstantTbt response from SDL: " + response.getResultCode().name() + " Info: " + response.getInfo());

	}

	@Override
	public void onAlertManeuverResponse(AlertManeuverResponse response) {
        Log.i(TAG, "AlertManeuver response from SDL: " + response.getResultCode().name() + " Info: " + response.getInfo());

	}

	@Override
	public void onUpdateTurnListResponse(UpdateTurnListResponse response) {
        Log.i(TAG, "UpdateTurnList response from SDL: " + response.getResultCode().name() + " Info: " + response.getInfo());

	}

	@Override
	public void onServiceDataACK(int dataSize) {

	}

	@Override
	public void onGetWayPointsResponse(GetWayPointsResponse response) {
		Log.i(TAG, "GetWayPoints response from SDL: " + response.getResultCode().name() + " Info: " + response.getInfo());
	}

	@Override
	public void onSubscribeWayPointsResponse(SubscribeWayPointsResponse response) {
		Log.i(TAG, "SubscribeWayPoints response from SDL: " + response.getResultCode().name() + " Info: " + response.getInfo());
	}

	@Override
	public void onUnsubscribeWayPointsResponse(UnsubscribeWayPointsResponse response) {
		Log.i(TAG, "UnsubscribeWayPoints response from SDL: " + response.getResultCode().name() + " Info: " + response.getInfo());
	}

	@Override
	public void onOnWayPointChange(OnWayPointChange notification) {
		Log.i(TAG, "OnWayPointChange notification from SDL: " + notification);
	}

	@Override
	public void onOnDriverDistraction(OnDriverDistraction notification) {
		// Some RPCs (depending on region) cannot be sent when driver distraction is active.
	}

	@Override
	public void onError(String info, Exception e) {
	}

	@Override
	public void onGenericResponse(GenericResponse response) {
        Log.i(TAG, "Generic response from SDL: " + response.getResultCode().name() + " Info: " + response.getInfo());
	}

}
