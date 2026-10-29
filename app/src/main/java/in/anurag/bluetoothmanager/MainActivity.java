package in.anurag.bluetoothmanager;

import android.Manifest;
import android.app.*;
import android.os.*;
import android.bluetooth.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.provider.Settings;
import android.graphics.Color;
import android.text.*;
import android.view.*;
import android.widget.*;
import java.util.*;

public class MainActivity extends Activity {
    private BluetoothAdapter adapter;
    private TextView status;
    private EditText search;
    private LinearLayout rows;
    private boolean registered;
    private final Map<String,BluetoothDevice> devices = new LinkedHashMap<>();
    private boolean allowed() {
        return checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN)==PackageManager.PERMISSION_GRANTED
            && checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT)==PackageManager.PERMISSION_GRANTED;
    }
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {try {
            if (!allowed()) return;
            String action=intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action) || BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                BluetoothDevice device=intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if(device!=null) { devices.put(device.getAddress(),device); render(); }
                if(BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) status.setText("Pairing status updated. Use Bluetooth settings to connect.");
            } else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) status.setText("Scan finished · "+devices.size()+" devices listed");
            else if(BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) refresh();
        } catch (SecurityException e) { if(status!=null) status.setText("Nearby devices permission changed. Grant access and retry."); }}
    };
    public void onCreate(Bundle state) {
        super.onCreate(state);
        BluetoothManager manager=getSystemService(BluetoothManager.class);
        adapter=manager==null ? null : manager.getAdapter();
        ScrollView scroll=new ScrollView(this);
        LinearLayout body=new LinearLayout(this); body.setOrientation(LinearLayout.VERTICAL); body.setPadding(28,48,28,28); body.setBackgroundColor(Color.rgb(13,23,39));
        scroll.addView(body);setContentView(scroll);
        TextView title=text("Bluetooth Manager",28);body.addView(title);
        body.addView(text("Your devices, within reach",16));
        status=text("Allow Nearby devices to get started.",16);body.addView(status);
        button(body,"Allow access / Enable Bluetooth",()->{
            if(!allowed()) requestPermissions(new String[]{Manifest.permission.BLUETOOTH_SCAN,Manifest.permission.BLUETOOTH_CONNECT},1);
            else enable();
        });
        button(body,"Refresh paired devices",this::refresh);
        button(body,"Scan nearby",this::scan);
        button(body,"Stop scan",()->{stop();status.setText("Scan stopped.");});
        button(body,"Open Bluetooth settings",this::settings);
        search=new EditText(this);search.setSingleLine(true);search.setTextColor(Color.WHITE);search.setHintTextColor(Color.LTGRAY);search.setHint("Search name or address · IPL08");body.addView(search);
        search.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence s,int st,int c,int a){}public void onTextChanged(CharSequence s,int st,int b,int c){render();}public void afterTextChanged(Editable e){}});
        rows=new LinearLayout(this); rows.setOrientation(LinearLayout.VERTICAL); body.addView(rows);
        body.addView(text("Paired does not mean connected. Audio connection and disconnection are managed in Android Bluetooth settings. Nearby devices may be hidden unless in pairing mode.",14));
        IntentFilter filter=new IntentFilter();filter.addAction(BluetoothDevice.ACTION_FOUND);filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        if(Build.VERSION.SDK_INT>=33) registerReceiver(receiver,filter,Context.RECEIVER_EXPORTED);else registerReceiver(receiver,filter);
        registered=true;
        if(adapter==null)status.setText("This phone has no Bluetooth adapter.");
        else if(allowed())refresh();
    }
    private TextView text(String s,int size){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(Color.WHITE);t.setPadding(0,12,0,12);return t;}
    private void button(LinearLayout box,String title,Runnable action){Button b=new Button(this);b.setText(title);b.setAllCaps(false);box.addView(b);b.setOnClickListener(v->{try{action.run();}catch(SecurityException e){status.setText("Nearby devices permission is required.");}catch(Exception e){status.setText("Action unavailable. Try Android Bluetooth settings.");}});}
    private boolean ready(){try {if(adapter==null){status.setText("Bluetooth unavailable.");return false;}if(!allowed()){status.setText("Tap Allow access first.");return false;}if(!adapter.isEnabled()){status.setText("Bluetooth is off. Tap Enable Bluetooth.");return false;}return true;} catch (SecurityException e) { if(status!=null) status.setText("Nearby devices permission changed. Grant access and retry."); return false; }}
    private void enable(){try {if(adapter==null||!allowed())return;if(!adapter.isEnabled())startActivity(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE));else refresh();} catch (SecurityException e) { if(status!=null) status.setText("Nearby devices permission changed. Grant access and retry."); }}
    private void refresh(){try {if(!ready())return;stop();devices.clear();for(BluetoothDevice d:adapter.getBondedDevices())devices.put(d.getAddress(),d);render();status.setText(devices.size()+" paired devices · connection status not inferred");} catch (SecurityException e) { if(status!=null) status.setText("Nearby devices permission changed. Grant access and retry."); }}
    private void scan(){try {if(!ready())return;stop();for(BluetoothDevice d:adapter.getBondedDevices())devices.put(d.getAddress(),d);render();status.setText(adapter.startDiscovery()?"Scanning discoverable devices…":"Could not start scan. Wait and try again.");} catch (SecurityException e) { if(status!=null) status.setText("Nearby devices permission changed. Grant access and retry."); }}
    private void stop(){try {if(adapter!=null&&allowed()&&adapter.isDiscovering())adapter.cancelDiscovery();} catch (SecurityException e) { if(status!=null) status.setText("Nearby devices permission changed. Grant access and retry."); }}
    private void settings(){stop();startActivity(new Intent(Settings.ACTION_BLUETOOTH_SETTINGS));}
    private void render(){try {
        if(rows==null||!allowed())return;
        rows.removeAllViews();String query=search.getText().toString().trim().toLowerCase(Locale.ROOT);int count=0;
        for(BluetoothDevice d:devices.values()){
            String name=d.getName();if(name==null||name.isEmpty())name="Unnamed device";
            String address=d.getAddress();if(!(name+" "+address).toLowerCase(Locale.ROOT).contains(query))continue;
            count++;final String label=name;
            LinearLayout card=new LinearLayout(this);card.setOrientation(LinearLayout.VERTICAL);card.setPadding(16,10,16,18);rows.addView(card);
            card.addView(text(name,20));
            String bond=d.getBondState()==BluetoothDevice.BOND_BONDED?"Paired with this phone":d.getBondState()==BluetoothDevice.BOND_BONDING?"Pairing…":"Discovered nearby";
            card.addView(text(address+"\n"+bond,14));
            button(card,"Select device",()->new AlertDialog.Builder(this).setTitle(label).setMessage("Pair this accessory or open Android settings to manage its connection.")
                .setPositiveButton("Bluetooth settings",(dialog,which)->settings())
                .setNeutralButton("Pair",(dialog,which)->{
                    try {if(!ready())return;stop();if(d.getBondState()==BluetoothDevice.BOND_BONDED)status.setText("Already paired. Open Bluetooth settings to connect.");else status.setText(d.createBond()?"Pairing requested. Confirm the Android prompt.":"Pairing could not start. Put the accessory in pairing mode.");}
                    catch(SecurityException e){status.setText("Nearby devices permission is required.");}
                }).setNegativeButton("Cancel",null).show());
        }
        if(count==0)rows.addView(text(query.isEmpty()?"No devices listed. Refresh paired devices or scan nearby.":"No matching device. Try scanning with the accessory in pairing mode.",16));
    } catch (SecurityException e) { if(status!=null) status.setText("Nearby devices permission changed. Grant access and retry."); }}
    public void onRequestPermissionsResult(int request,String[] permissions,int[] results){super.onRequestPermissionsResult(request,permissions,results);if(allowed())enable();else status.setText("Permission denied. You can retry or grant Nearby devices in app settings.");}
    protected void onResume(){try {super.onResume();if(adapter!=null&&allowed()){if(adapter.isEnabled())refresh();else status.setText("Bluetooth is off. Tap Enable Bluetooth.");}} catch (SecurityException e) { if(status!=null) status.setText("Nearby devices permission changed. Grant access and retry."); }}
    protected void onPause(){stop();super.onPause();}
    protected void onDestroy(){if(registered)unregisterReceiver(receiver);super.onDestroy();}
}
