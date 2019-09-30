package com.alarm;
 
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.alarm.receivers.AlarmReceiver;

public class SimpleAlarmManager {

    public static String AlarmManagerKey = "AlarmManager";
    public static String TagsOfAlarmsKey = "TagsOfAlarms";
    public static String TagOfAlarmIntentKey = "TagOfAlarmIntent";
    public static String TagOfAlarmStoreKey = "TagOfAlarmStore";
    public static String InterValKey = "InterVal";
    public static String YearKey = "Year";
    public static String MonthKey = "Month";
    public static String DayOfMonthkey = "DayOfMonth";
    public static String HourOfDayKey = "HourOfDay";
    public static String MinuteKey = "Minute";
    public static String SecondKey = "Second";

    public static String OneAlarmKey = "OneAlarm";

    public static String OneAlarmShownKey = "_OneAlarmShown";

    public static final String PeriodicAlarm_midnight = "PeriodicAlarm_midnight";

    public static void runOneAlarm(Context context, int year, int month, int dayOfMonth
            , int hourOfDay, int minute, int second, String tagOfAlarm) {

        if(!checkIfTagOfAlarmFound(context,tagOfAlarm))
        {
            store(context, tagOfAlarm, -1, year, month, dayOfMonth, hourOfDay
                    , minute, second);
            refreshTimes(context);
            scheduleNextAlarm(context);
        }

    }

    //periodic alarm from certain day

    public static void runPeriodicAlarm(Context context,
                                        long interval,
                                        int year, int month, int dayOfMonth
            ,int hourOfDay, int minute, int second
            , String tagOfAlarm) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, second);
        Calendar now = Calendar.getInstance();
        boolean isBefore = true;
        while (isBefore) {
            if (now.after(calendar)) {
                //calendar.add(Calendar.HOUR_OF_DAY, 24);
                calendar.setTimeInMillis(calendar.getTimeInMillis() + interval);
            } else {
                isBefore = false;
            }
        }

        store(context, tagOfAlarm, interval, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.HOUR_OF_DAY)
                , calendar.get(Calendar.MINUTE), calendar.get(Calendar.SECOND));

        refreshTimes(context);
        scheduleNextAlarm(context);
    }



    //periodic alarm from current day
    public static void runPeriodicAlarm(Context context,
                                        long interval, int hourOfDay, int minute, int second
            , String tagOfAlarm) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, second);
        Calendar now = Calendar.getInstance();
        boolean isBefore = true;
        while (isBefore) {
            if (now.after(calendar)) {
                //calendar.add(Calendar.HOUR_OF_DAY, 24);
                calendar.setTimeInMillis(calendar.getTimeInMillis() + interval);
            } else {
                isBefore = false;
            }
        }

        store(context, tagOfAlarm, interval, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.HOUR_OF_DAY)
                , calendar.get(Calendar.MINUTE), calendar.get(Calendar.SECOND));

        refreshTimes(context);
        scheduleNextAlarm(context);
    }



    public static boolean checkIfTagOfAlarmFound(Context context, String tagOfAlarm) {
        Set<String> set = SimpleAlarmManager.getAllStoredTags(context);
        for (Iterator<String> it = set.iterator(); it.hasNext(); ) {
            String tag = it.next();
            if (tag.contains(tagOfAlarm)) {
                return true;
            }
        }
        return false;
    }

    public static String getTagOfAlarmIfFound(Context context, String tagOfAlarm) {
        Set<String> set = SimpleAlarmManager.getAllStoredTags(context);
        for (Iterator<String> it = set.iterator(); it.hasNext(); ) {
            String tag = it.next();
            if (tag.contains(tagOfAlarm)) {
                return tag;
            }
        }
        return "";
    }

    private static void store(Context context, String tagOfAlarm,
                              long interval, int year, int month, int dayOfMonth
            , int hourOfDay, int minute, int second) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(AlarmManagerKey, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (sharedPreferences.contains(TagsOfAlarmsKey)) {
            Set<String> set = sharedPreferences.getStringSet(TagsOfAlarmsKey, new HashSet<String>());
            if (set != null && !set.isEmpty()) {
                set.add(tagOfAlarm);
                editor.putStringSet(TagsOfAlarmsKey, set);
                editor.apply();
            }
        } else {
            Set<String> set = new HashSet<>();
            set.add(tagOfAlarm);
            editor.putStringSet(TagsOfAlarmsKey, set);
            editor.apply();
        }

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(InterValKey, interval);
            jsonObject.put(YearKey, year);
            jsonObject.put(MonthKey, month);
            jsonObject.put(DayOfMonthkey, dayOfMonth);
            jsonObject.put(HourOfDayKey, hourOfDay);
            jsonObject.put(MinuteKey, minute);
            jsonObject.put(SecondKey, second);
            editor.putString(TagOfAlarmStoreKey + tagOfAlarm, jsonObject.toString()).apply();
        } catch (JSONException e) {

        }
    }


    public static void scheduleAlarm(Context context, String tagOfAlarm, Calendar calendar) {
        // Log.e("calendar alarm",calendar.getTimeInMillis()+"");
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra(TagOfAlarmIntentKey, tagOfAlarm);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                getRequestCodeOfPendingIntent(tagOfAlarm),
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        if (pendingIntent != null) {
            AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
             //alarmMgr.cancel(pendingIntent);

           if (Build.VERSION.SDK_INT >= 23) {
               alarmMgr.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,
                       calendar.getTimeInMillis(), pendingIntent);
           } else if (Build.VERSION.SDK_INT >= 19) {
               alarmMgr.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
           } else {
               alarmMgr.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
           }

//             if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                 alarmMgr.setAlarmClock(new AlarmManager.AlarmClockInfo(calendar.getTimeInMillis(),pendingIntent),pendingIntent);
//              }
//             else
//             {
//                 if (Build.VERSION.SDK_INT >= 19) {
//                     alarmMgr.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
//                 } else {
//                     alarmMgr.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
//                 }
//             }


        }

    }

    public static void removeTagOfAlarm(Context context, String tagOfAlarm) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(AlarmManagerKey, Context.MODE_PRIVATE);
        Set<String> set = SimpleAlarmManager.getAllStoredTags(context);

        SharedPreferences.Editor editor = sharedPreferences.edit();

        for (Iterator<String> it = set.iterator(); it.hasNext(); ) {
            String tag = it.next();
            if (tagOfAlarm.equals(tag)) {
                if (set != null && !set.isEmpty()) {
                    set.remove(tagOfAlarm);
                    editor.putStringSet(TagsOfAlarmsKey, set);
                    editor.apply();
                }
                break;
            }
        }

    }


    public static void updateTagOfOneAlarm(Context context, String tagOfAlarm) {
        if (tagOfAlarm.contains(OneAlarmKey)
        ) {
            SharedPreferences sharedPreferences = context.getSharedPreferences(AlarmManagerKey, Context.MODE_PRIVATE);
            Set<String> set = SimpleAlarmManager.getAllStoredTags(context);

            SharedPreferences.Editor editor = sharedPreferences.edit();

            for (Iterator<String> it = set.iterator(); it.hasNext(); ) {
                String tag = it.next();
                if (tagOfAlarm.equals(tag)) {
                    if (set != null && !set.isEmpty()) {
                        set.remove(tagOfAlarm);
                        set.add(tagOfAlarm+OneAlarmShownKey);
                        editor.putStringSet(TagsOfAlarmsKey, set);
                        editor.apply();
                    }
                    break;
                }
            }
        }
    }

    public void cancelAlarm(Context context, String tagOfAlarm) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra(TagOfAlarmIntentKey, tagOfAlarm);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, getRequestCodeOfPendingIntent(tagOfAlarm),
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        if (pendingIntent != null) {
            AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            alarmMgr.cancel(pendingIntent);
        }
        removeTagOfAlarm(context,tagOfAlarm);
    }

    public static void handleBootComplete(Context context) {
        refreshTimes(context);
        scheduleNextAlarm(context);
    }

    public static void handleTimeChange(Context context) {
        refreshTimes(context);
        scheduleNextAlarm(context);
    }

    public static void refreshAndScheduleNext(Context context) {
        SimpleAlarmManager.refreshTimes(context);
        SimpleAlarmManager.scheduleNextAlarm(context);
    }

    public static void refreshTimes(Context context) {
        Set<String> set = SimpleAlarmManager.getAllStoredTags(context);
        for (Iterator<String> it = set.iterator(); it.hasNext(); ) {
            String tag = it.next();
            SimpleAlarmManager.refreshWithTag(context, tag);
        }
    }

    public static void refreshWithTag(Context context, String tagOfAlarm) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(AlarmManagerKey, Context.MODE_PRIVATE);
        String registrationExtra = sharedPreferences.getString(TagOfAlarmStoreKey + tagOfAlarm, null);

        int interval,year,month,dayOfMonth, hourOfDay, minute, second;


        if (registrationExtra != null) {
            try {
                JSONObject jsonObject = new JSONObject(registrationExtra);
                interval = jsonObject.getInt(InterValKey);


                year = jsonObject.getInt(YearKey);
                month = jsonObject.getInt(MonthKey);
                dayOfMonth = jsonObject.getInt(DayOfMonthkey);

                hourOfDay = jsonObject.getInt(HourOfDayKey);
                minute = jsonObject.getInt(MinuteKey);
                second = jsonObject.getInt(SecondKey);

                if (interval != -1) {
                    Calendar calendar = Calendar.getInstance();
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    calendar.set(Calendar.MINUTE, minute);
                    calendar.set(Calendar.SECOND, second);
                    Calendar now = Calendar.getInstance();
                    boolean isBefore = true;
                    while (isBefore) {
                        if (now.after(calendar)) {
                            //calendar.add(Calendar.HOUR_OF_DAY, 24);
                            calendar.setTimeInMillis(calendar.getTimeInMillis() + interval);
                        } else {
                            isBefore = false;
                        }
                    }
                    store(context, tagOfAlarm, interval, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.HOUR_OF_DAY)
                            , calendar.get(Calendar.MINUTE), calendar.get(Calendar.SECOND));
                }

            } catch (JSONException e) {
            }
        }
    }

    public static void scheduleNextAlarm(Context context) {

        Set<String> set = SimpleAlarmManager.getAllStoredTags(context);
        ArrayList<Alarm> alarmPeriodicList = new ArrayList<>();
        for (Iterator<String> it = set.iterator(); it.hasNext(); ) {
            String tagOfAlarm = it.next();

            SharedPreferences sharedPreferences = context.getSharedPreferences(AlarmManagerKey, Context.MODE_PRIVATE);
            String registrationExtra = sharedPreferences.getString(TagOfAlarmStoreKey + tagOfAlarm, null);

            int interval, year, month, dayOfMonth, hourOfDay, minute, second;


            if (registrationExtra != null) {
                try {
                    JSONObject jsonObject = new JSONObject(registrationExtra);
                    interval = jsonObject.getInt(InterValKey);

                    year = jsonObject.getInt(YearKey);
                    month = jsonObject.getInt(MonthKey);
                    dayOfMonth = jsonObject.getInt(DayOfMonthkey);

                    hourOfDay = jsonObject.getInt(HourOfDayKey);
                    minute = jsonObject.getInt(MinuteKey);
                    second = jsonObject.getInt(SecondKey);

                    Calendar calendar = Calendar.getInstance();

                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    calendar.set(Calendar.MINUTE, minute);
                    calendar.set(Calendar.SECOND, second);


                    if (interval != -1) {

                        Alarm alarm = new Alarm();
                        alarm.tagOfAlarm = tagOfAlarm;
                        alarm.time = calendar.getTimeInMillis();
                        alarmPeriodicList.add(alarm);
                    } else {
                        scheduleAlarm(context, tagOfAlarm, calendar);
                    }

                } catch (JSONException e) {
                }
            }

        }

        if (alarmPeriodicList.size() > 0) {

            Collections.sort(alarmPeriodicList, new Comparator<Alarm>() {
                @Override
                public int compare(Alarm a1, Alarm a2) {
                    return Long.valueOf(a1.time).compareTo(Long.valueOf(a2.time));
                }
            });

//            for (int i = 0; i <alarmPeriodicList.size() ; i++) {
//                Log.e("alarmPeriodicListTimesPrint",alarmPeriodicList.get(i).time+"");
//            }

            //  Log.e("alarmPeriodicList.get(0).time",alarmPeriodicList.get(0).time+"");
            Calendar calendarNext = Calendar.getInstance();
            calendarNext.setTimeInMillis(alarmPeriodicList.get(0).time);
            scheduleAlarm(context, alarmPeriodicList.get(0).tagOfAlarm, calendarNext);
        }

    }

    public static Set<String> getAllStoredTags(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(AlarmManagerKey, Context.MODE_PRIVATE);
        return sharedPreferences.getStringSet(TagsOfAlarmsKey, new HashSet<String>());
    }


    public static int getRequestCodeOfPendingIntent(String tagOfAlarm) {
        return stringToInteger(tagOfAlarm).intValue();
    }

    public static BigInteger stringToInteger(String text) {
        BigInteger bigInt = new BigInteger(text.getBytes());
        return bigInt;
    }

    private static class Alarm {
        private String tagOfAlarm;
        private long time;

    }

}
