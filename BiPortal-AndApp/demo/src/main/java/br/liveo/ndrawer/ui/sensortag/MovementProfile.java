/**************************************************************************************************
 Filename:       SensorTagMovementProfile.java
 Revised:        $Date: Wed Apr 22 13:01:34 2015 +0200$
 Revision:       $Revision: 599e5650a33a4a142d060c959561f9e9b0d88146$

 Copyright (c) 2013 - 2015 Texas Instruments Incorporated

 All rights reserved not granted herein.
 Limited License.

 Texas Instruments Incorporated grants a world-wide, royalty-free,
 non-exclusive license under copyrights and patents it now or hereafter
 owns or controls to make, have made, use, import, offer to sell and sell ("Utilize")
 this software subject to the terms herein.  With respect to the foregoing patent
 license, such license is granted  solely to the extent that any such patent is necessary
 to Utilize the software alone.  The patent license shall not apply to any combinations which
 include this software, other than combinations with devices manufactured by or for TI ('TI Devices').
 No hardware patent is licensed hereunder.

 Redistributions must preserve existing copyright notices and reproduce this license (including the
 above copyright notice and the disclaimer and (if applicable) source code license limitations below)
 in the documentation and/or other materials provided with the distribution

 Redistribution and use in binary form, without modification, are permitted provided that the following
 conditions are met:

 * No reverse engineering, decompilation, or disassembly of this software is permitted with respect to any
 software provided in binary form.
 * any redistribution and use are licensed by TI for use only with TI Devices.
 * Nothing shall obligate TI to provide you with source code for the software licensed and provided to you in object code.

 If software source code is provided to you, modification and redistribution of the source code are permitted
 provided that the following conditions are met:

 * any redistribution and use of the source code, including any resulting derivative works, are licensed by
 TI for use only with TI Devices.
 * any redistribution and use of any object code compiled from the source code and any resulting derivative
 works, are licensed by TI for use only with TI Devices.

 Neither the name of Texas Instruments Incorporated nor the names of its suppliers may be used to endorse or
 promote products derived from this software without specific prior written permission.

 DISCLAIMER.

 THIS SOFTWARE IS PROVIDED BY TI AND TI'S LICENSORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
 BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 IN NO EVENT SHALL TI AND TI'S LICENSORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 POSSIBILITY OF SUCH DAMAGE.


 **************************************************************************************************/
package br.liveo.ndrawer.ui.sensortag;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.liveo.ndrawer.ui.activity.MainActivity;
import br.liveo.ndrawer.ui.adapter.RequestClass;
import br.liveo.ndrawer.ui.adapter.RequestEpcisCapture;
import br.liveo.ndrawer.ui.fragment.MainFragment;

public class MovementProfile extends GenericBluetoothProfile {

    public static double mLastKnownMotionX = 0;
    public static double mLastKnownMotionY= 0;
    public static double mLastKnownMotionZ = 0;
    private SharedPreferences prefs;

    public static int status = 0;

    Context cont;


    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    public MovementProfile(Context con, BluetoothGattService service, MainActivity controller) {
        super(con,service,controller);

        cont = con;

        List<BluetoothGattCharacteristic> characteristics = this.mBTService.getCharacteristics();

        for (BluetoothGattCharacteristic c : characteristics) {
            if (c.getUuid().toString().equals(SensorTagGatt.UUID_MOV_DATA.toString())) {
                this.dataC = c;
            }
            if (c.getUuid().toString().equals(SensorTagGatt.UUID_MOV_CONF.toString())) {
                this.configC = c;
            }
            if (c.getUuid().toString().equals(SensorTagGatt.UUID_MOV_PERI.toString())) {
                this.periodC = c;
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    public static boolean isCorrectService(BluetoothGattService service) {
        if ((service.getUuid().toString().compareTo(SensorTagGatt.UUID_MOV_SERV.toString())) == 0) {
            return true;
        }
        else return false;
    }
    @SuppressLint("LongLogTag")
    @Override
    public void enableService() {
        int error = mBTLeService.writeCharacteristic(this.configC, new byte[] {0x7F,0x02});
        if (error != 0) {
            if (this.configC != null)
                Log.d("SensorTagMovementProfile","Sensor config failed: " + this.configC.getUuid().toString() + " Error: " + error);
        }
        error = this.mBTLeService.setCharacteristicNotification(this.dataC, true);
        if (error != 0) {
            if (this.dataC != null)
                Log.d("SensorTagMovementProfile","Sensor notification enable failed: " + this.configC.getUuid().toString() + " Error: " + error);
        }

        this.periodWasUpdated(500);
        this.isEnabled = true;
    }
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    @SuppressLint("LongLogTag")
    @Override
    public void disableService() {
        int error = mBTLeService.writeCharacteristic(this.configC, new byte[] {0x00,0x00});
        if (error != 0) {
            if (this.configC != null)
                Log.d("SensorTagMovementProfile","Sensor config failed: " + this.configC.getUuid().toString() + " Error: " + error);
        }
        error = this.mBTLeService.setCharacteristicNotification(this.dataC, false);
        if (error != 0) {
            if (this.dataC != null)
                Log.d("SensorTagMovementProfile","Sensor notification disable failed: " + this.configC.getUuid().toString() + " Error: " + error);
        }
        this.isEnabled = false;
    }
    public void didWriteValueForCharacteristic(BluetoothGattCharacteristic c) {

    }
    public void didReadValueForCharacteristic(BluetoothGattCharacteristic c) {

    }
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public void didUpdateValueForCharacteristic(BluetoothGattCharacteristic c) {
        byte[] value = c.getValue();
        if (c.equals(this.dataC)){
            Point3D v;
            v = Sensor.MOVEMENT_GYRO.convert(value);
            mLastKnownMotionX = v.x;
            mLastKnownMotionY = v.y;
            mLastKnownMotionZ = v.z;

            prefs = cont.getSharedPreferences("PrefName", cont.MODE_PRIVATE);
            String useremail = prefs.getString("useremail", "");

            if(mLastKnownMotionX > 40){
                if(status == 0){
                    updateEvent("VIBRATION_BEACON");
                    Vibration vb = new Vibration();
                    vb.execute(useremail);
                }
            }

        //    Log.i("무브", String.format("%.2f",mLastKnownMotionX) + ". " +String.format("%.2f",mLastKnownMotionY)+ "."+String.format("%.2f",mLastKnownMotionZ));
        }
    }

    private void updateEvent(String event) {
        RequestEpcisCapture erc = new RequestEpcisCapture();

        String eventDate = new SimpleDateFormat("yyyy-MM-dd").format((System.currentTimeMillis()));
        String eventTime = new SimpleDateFormat("HH:mm:ss").format((System.currentTimeMillis()));

        Location location = null;

        try {
            if (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
                location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            if(location == null && mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
                location = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }catch (SecurityException ex)
        {
            Log.i("Timer:", ex.getMessage());
        }

        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                "<!DOCTYPE project>\n" +
                "<epcis:EPCISDocument xmlns:epcis=\"urn:epcglobal:epcis:xsd:1\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" \n" +
                "                     creationDate=\"2015-01-03T11:30:47.0Z\" schemaVersion=\"1.1\" xmlns:car=\"BiPortalGs1.xsd\">\n" +
                "  <EPCISBody>\n" +
                "    <EventList>\n" +
                "      <ObjectEvent>\n" +
                "        <!-- When -->\n" +
                "        <eventTime>" + eventDate + "T" + eventTime + ".116-10:00</eventTime>\n" +
                "        <eventTimeZoneOffset>-10:00</eventTimeZoneOffset>\n" +
                "        <!-- When! -->\n" +
                "\n" +
                "        <!--  What -->\n" +
                "        <epcList>\n" +
                "          <epc>urn:epc:id:sgtin:1234567.123456.01</epc>\n" +
                "        </epcList>\n" +
                "        <!-- What!-->\n" +
                "\n" +
                "        <!-- Add, Observe, Delete -->\n" +
                "        <action>ADD</action>\n" +
                "\n" +
                "        <!-- Why -->\n" +
                "        <bizStep>urn:epcglobal:cbv:bizstep:"+ event +"</bizStep>\n" +
                "        <disposition>urn:epcglobal:cbv:disp:user_accessible</disposition>\n" +
                "        <!-- Why! -->\n" +
                "\n" +
                "        <!-- Where -->\n" +
                "        <bizLocation>\n" +
                "          <id>urn:epc:id:sgln:7654321.54321.1234</id>\n" +
                "          <extension>\n" +
                "            <geo>" + location.getLatitude() + "," + location.getLongitude() + "</geo>\n" +
                "          </extension>\n" +
                "        </bizLocation>\n" +
                "        <!-- Where! -->\n" +
                "      </ObjectEvent>\n" +
                "    </EventList>\n" +
                "  </EPCISBody>\n" +
                "</epcis:EPCISDocument>";

        erc.execute(xml);
    }

    private class Vibration extends AsyncTask<String, Void, String> {
        String url = null;
        String response = null;
        // Invoked by execute() method of this object
        @Override
        protected String doInBackground(String... params) {

            try {
                url = "http://125.131.73.198:3000/vibration";
                String useremail = params[0];

                RequestClass rc = new RequestClass(url);
                rc.AddParam("useremail", useremail);
                rc.Execute(1);
                response = rc.getResponse();
            }catch(Exception e){
                e.printStackTrace();
            }
            return response;
        }

        // Executed after the complete execution of doInBackground() method
        @Override
        protected void onPostExecute(String response) {
            if(response.length() != 0){
                return ;
            } else {
                return ;
            }
        }
    }




    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    public Map<String,String> getMQTTMap() {
        Point3D v = Sensor.MOVEMENT_ACC.convert(this.dataC.getValue());
        Map<String,String> map = new HashMap<String, String>();
        map.put("acc_x",String.format("%.2f",v.x));
        map.put("acc_y",String.format("%.2f",v.y));
        map.put("acc_z",String.format("%.2f",v.z));
        v = Sensor.MOVEMENT_GYRO.convert(this.dataC.getValue());
        map.put("gyro_x",String.format("%.2f",v.x));
        map.put("gyro_y",String.format("%.2f",v.y));
        map.put("gyro_z",String.format("%.2f",v.z));
        v = Sensor.MOVEMENT_MAG.convert(this.dataC.getValue());
        map.put("compass_x",String.format("%.2f",v.x));
        map.put("compass_y",String.format("%.2f",v.y));
        map.put("compass_z",String.format("%.2f",v.z));
        return map;
    }
}
