'''
Created on 2015. 12. 6.

@author: SUNgHOOn
'''

def capture_str():
    cap_str = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<!DOCTYPE project>
<epcis:EPCISDocument xmlns:epcis="urn:epcglobal:epcis:xsd:1" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
                     creationDate="2015-01-03T11:30:47.0Z" schemaVersion="1.1" xmlns:car="BiPortalGs1.xsd">
  <EPCISBody>
    <EventList>
      <ObjectEvent>
        <!-- Event 시간을 넣습니다!!! (When 정보!) -->
        <eventTime>2015-01-03T20:33:31.116-10:00</eventTime>
        <eventTimeZoneOffset>-10:00</eventTimeZoneOffset>
        <!-- When 정보 끝! -->

        <!--  What 정보.. Car ID가 들어가겠지요 Transformation 경우 input/output으로 나눠짐-->
        <epcList>
          <!--  차에 대한 epc 정보 -->
          <epc>urn:epc:id:sgtin:4012345.077889.27</epc>
        </epcList>
        <!-- What에 대한 정보 끝!-->

        <!-- Add, Observe, Delete 3가지가 있음 create나 add, 혹은 파괴되거나 없어지지 않으면 observe임-->
        <action>ADD</action>

        <!-- Why에 대한 정보 -->
        <bizStep>urn:epcglobal:cbv:bizstep:retail_selling</bizStep>
        <disposition>urn:epcglobal:cbv:disp:sellable_accessible</disposition>
        <!-- Why에 대한 정보 끝-->

        <!-- Where에 대한 정보, 어디서 차가 팔렸는지? 어디서 차가 고쳐졌는지 등-->
        <bizLocation>
          <!-- bizLocation indicates the location of the retail shop (차 수리소? 중고차 판매소?) -->
          <id>urn:epc:id:sgln:0614141.07346.1235</id>
          <extension>
            <geo>19.708886,-155.893430</geo>
          </extension>
        </bizLocation>
        <!-- Where에 대한 정보 끝-->

        <!-- Car 정보-->
        <car:PowerSensor>true</car:PowerSensor> <!-- Power가 켜지면 true, otherwise false-->
        <car:SpeedSensor>80.0</car:SpeedSensor><!--자동차 속도값, double-->
        
        <!--GPS Sensor -->
        <car:PositionSensorLat>1.111</car:PositionSensorLat>
        <car:PositionSensorLng>2.1111</car:PositionSensorLng>
        <car:PositionSensorAlt>3.11111</car:PositionSensorAlt>
        
        <car:RPMSensor>2000</car:RPMSensor>
        <car:BreakSensor>true</car:BreakSensor> <!--break 눌리면 true, otherwise false-->
      </ObjectEvent>
    </EventList>
  </EPCISBody>
</epcis:EPCISDocument>"""

    return cap_str
