# SImpleAlarmManager-Android
Simple Alarm Manager is the library which can be used to create, cancel and to handle onBoot, OnTimeChange and OnTimeZone cases to avoid 100 lines of code.

# Instructions

**Example **
```java

 - Periodic Time Alarm
 
         SimpleAlarmManager.runPeriodicAlarm(this,
                2 * 60 * 1000,
                16, 55, 10,"PeriodicAlarm_1"
        );
        //PeriodicAlarm_1 is just example

  - another example 
        SimpleAlarmManager.runPeriodicAlarm(this,
                AlarmManager.INTERVAL_DAY,2019,8,25      // 8 is month +1 i.e : july month
                16, 55, 0,"PeriodicAlarm_2"
        );
        
  - another example 
        SimpleAlarmManager.runPeriodicAlarm(this,
                AlarmManager.INTERVAL_DAY,
                0, 0, 0,"PeriodicAlarm_midnight"
        );
       
        
 - One Time Alarm 
 
         SimpleAlarmManager.runOneAlarm(context,
            2019,
            Calendar.AUGUST, 22, 16, 51, 10,SimpleAlarmManager.OneAlarmKey+" any string you want");

```
Stored Tag should be unique for each alarm. 
 

**Handle on Boot/Restart, onTimeChange and onTimeZoneChange case**

Create BootAndTimeChangeReceiver  receiver that fire when boot completes or at time changes or time zone changes and add it on Manifest as given:

*1) BootAndTimeChangeReceiver*
```java
public class BootAndTimeChangeReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
         if ((action.equals(Intent.ACTION_BOOT_COMPLETED))
                ||
                (action.equals("android.intent.action.QUICKBOOT_POWERON"))
        )
        {
            SimpleAlarmManager.handleBootComplete(context);


        } else // TIME_SET
        {
            SimpleAlarmManager.handleTimeChange(context);
        }
      }

    }
}
```
Register BootAndTimeChangeReceiver on Manifest as follows.

``` xml
    <receiver
            android:name="modules.alarm.receivers.BootAndTimeChangeReceiver"
            android:enabled="true"
            android:exported="false"
             >
            <intent-filter>
                 <action android:name="android.intent.action.BOOT_COMPLETED" />
                <action android:name="android.intent.action.QUICKBOOT_POWERON" />
                <action android:name="android.intent.action.DATE_CHANGED" />
                <action android:name="android.intent.action.TIMEZONE_CHANGED" />
                <action android:name="android.intent.action.TIME_SET" />
                <action android:name="android.intent.action.TIME_TICK" />
            </intent-filter>
        </receiver>
```

Register AlarmReceiver on Manifest as follows.

``` xml your reciever here
    <receiver
            android:name="modules.alarm.receivers.AlarmReceiver"
            android:enabled="true"
            android:exported="false"
             >    
        </receiver>
```

Add onBoot Permission
```xml
<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED"/>
```



*2) AlarmReceiver*

```java
public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
     
     SimpleAlarmManager.refreshAndScheduleNext(context);

        String tagOfAlarm = intent.getStringExtra(SimpleAlarmManager.TagOfAlarmIntentKey);

        String newTagOfAlarm = SimpleAlarmManager.getTagOfAlarmIfFound(context,tagOfAlarm);
       
        if(!newTagOfAlarm.contains(OneAlarmShownKey) && !newTagOfAlarm.isEmpty())
        {
            SimpleAlarmManager.updateTagOfOneAlarm(context,tagOfAlarm);
           // Log.e("AlarmReceiverOnReceive","AlarmReceiverOnReceive");
        }

    }
}

```
To cancel an alarm:

```java
   SimpleAlarmManager.cancelAlarm(context,tagOfAlarm);
```

