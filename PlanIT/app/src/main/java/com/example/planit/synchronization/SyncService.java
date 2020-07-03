package com.example.planit.synchronization;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.example.planit.MainActivity;
import com.example.planit.broadcastReceivers.ReminderBroadcastReceiver;
import com.example.planit.database.Contract;
import com.example.planit.service.HabitService;
import com.example.planit.service.ServiceUtils;
import com.example.planit.service.TeamService;
import com.example.planit.utils.SharedPreference;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import model.DayOfWeek;
import model.Habit;
import model.HabitDayConnection;
import model.HabitFulfillment;
import model.HabitReminderConnection;
import model.HabitSyncDTO;
import model.Message;
import model.Reminder;
import model.Team;
import model.TeamSyncDTO;
import model.TeamUserConnection;
import model.User;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SyncService extends Service {

    public static String RESULT_CODE = "RESULT_CODE";
    public static int TYPE_WIFI = 1;
    public static int TYPE_MOBILE = 2;
    public static int TYPE_NOT_CONNECTED = 0;
    private boolean success = false;

    private DateFormat format = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Intent intent2 = new Intent(MainActivity.SYNC_DATA);
        int status = getConnectivityStatus(getApplicationContext());
        intent2.putExtra(RESULT_CODE, status);
        Log.e("SYNC_SERVICE", "CHECK SYNC");

        // the device has access to internet
        if (status == TYPE_WIFI || status == TYPE_MOBILE) {
            Log.e("SYNC_SERVICE", "SYNC STARTED");
            String email = SharedPreference.getLoggedEmail(this);
            HabitService apiHabitService = ServiceUtils.getClient().create(HabitService.class);
            TeamService apiTeamService = ServiceUtils.getClient().create(TeamService.class);
            Date date = null;
            Call<HabitSyncDTO> callHabits = null;
            Call<TeamSyncDTO> callTeam = null;
            if (SharedPreference.getLastSyncDate(this) != -1) {
                date = new Date(SharedPreference.getLastSyncDate(this));
                callHabits = apiHabitService.synchronizationHabits(email, date.getTime());
                callTeam = apiTeamService.synchronizationTeam(email, date.getTime());
            } else {
                callHabits = apiHabitService.synchronizationHabits(email, null);
                callTeam = apiTeamService.synchronizationTeam(email, null);
            }


            callHabits.enqueue(new Callback<HabitSyncDTO>() {
                @Override
                public void onResponse(Call<HabitSyncDTO> call, Response<HabitSyncDTO> response) {

                    if (response.code() == 200) {
                        Log.e("200", "200");
                        syncHabits(response.body());
                        SharedPreference.setLastSyncDate(SyncService.this, new Date());
                        success = true;
                    } else {
                        Log.e("400", "400");
                        success = false;
                    }
                }

                @Override
                public void onFailure(Call<HabitSyncDTO> call, Throwable t) {
                    success = false;
                    Log.e("tag", "Connection error");
                }
            });

            callTeam.enqueue(new Callback<TeamSyncDTO>() {
                @Override
                public void onResponse(Call<TeamSyncDTO> call, Response<TeamSyncDTO> response) {

                    if (response.code() == 200) {
                        Log.e("team", "200");
                        syncTeams(response.body());
                       /* Call<TaskSyncDTO> callTask;
                        TaskService taskService = ServiceUtils.getClient().create(TaskService.class);
                        if (SharedPreference.getLastSyncDate(SyncService.this) != -1) {
                            date = new Date(SharedPreference.getLastSyncDate(this));
                            callHabits = apiHabitService.synchronizationHabits(email, date.getTime());
                        } else {
                            callHabits = apiHabitService.synchronizationHabits(email, null);
                        }*/

                        success = true;
                    } else {
                        Log.e("team", "400");
                        success = false;
                    }
                }

                @Override
                public void onFailure(Call<TeamSyncDTO> call, Throwable t) {
                    success = false;
                    Log.e("team", "Connection error");
                }
            });


        }


        sendBroadcast(intent2);

        stopSelf();

        return START_NOT_STICKY;
    }

    public void syncTeams(TeamSyncDTO teamSyncDTO) {
        List<ContentProviderOperation> batchTeams = syncTeamContentProvider(teamSyncDTO);
        if (batchTeams.size() == 0) {
            Log.e("TEAM_SYNC", "EMPTY");
            return;
        }

        try {
            getContentResolver().applyBatch(Contract.Team.AUTHORITY, (ArrayList<ContentProviderOperation>) batchTeams);
        } catch (OperationApplicationException e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public List<ContentProviderOperation> syncTeamContentProvider(TeamSyncDTO syncDTO) {
        List<ContentProviderOperation> batch = new ArrayList<>();
        Map<Long, Integer> teamInsertedIndex = new HashMap<>();
        Map<Long, Integer> userInsertedIndex = new HashMap<>();

        for (User user : syncDTO.getUsers()) {
            String selection = Contract.User.COLUMN_GLOBAL_ID + " = ?";
            String[] selectionArgs = new String[]{Long.toString(user.getGlobalId())};
            Cursor userCursor = getContentResolver().query(Contract.User.CONTENT_URI_USER, null, selection, selectionArgs, null);
            // user needs to be updated in the local storage
            ContentProviderOperation operation = null;
            if (userCursor.getCount() > 0) {
                userCursor.moveToFirst();
                Uri uri = Uri.parse(Contract.User.CONTENT_URI_USER + "/" + userCursor.getInt(userCursor.getColumnIndex(Contract.User.COLUMN_ID)));
                ContentValues values = new ContentValues();
                values.put(Contract.User.COLUMN_EMAIL, user.getEmail());
                values.put(Contract.User.COLUMN_NAME, user.getName());
                values.put(Contract.User.COLUMN_LAST_NAME, user.getLastName());
                values.put(Contract.User.COLUMN_COLOUR, user.getColour());
                values.put(Contract.User.COLUMN_FIREBASE_ID, user.getFirebaseId());
                operation = ContentProviderOperation.newUpdate(uri).withValues(values).build();

            } else {
                ContentValues values = new ContentValues();
                values.put(Contract.User.COLUMN_EMAIL, user.getEmail());
                values.put(Contract.User.COLUMN_NAME, user.getName());
                values.put(Contract.User.COLUMN_LAST_NAME, user.getLastName());
                values.put(Contract.User.COLUMN_COLOUR, user.getColour());
                values.put(Contract.User.COLUMN_FIREBASE_ID, user.getFirebaseId());
                values.put(Contract.User.COLUMN_GLOBAL_ID, user.getGlobalId());
                userInsertedIndex.put(user.getGlobalId(), batch.size());
                operation = ContentProviderOperation.newInsert(Contract.User.CONTENT_URI_USER).withValues(values).build();

            }
            userCursor.close();
            batch.add(operation);
        }

        for (Team team : syncDTO.getTeams()) {
            Log.e("TEAM ID", team.getServerTeamId().toString());
            Log.e("TEAM CREATOR ID", team.getCreatorId().toString());
            String selection = Contract.Team.COLUMN_SERVER_TEAM_ID + " = ?";
            String[] selectionArgs = new String[]{Long.toString(team.getServerTeamId())};
            Cursor teamCursor = getContentResolver().query(Contract.Team.CONTENT_URI_TEAM, null, selection, selectionArgs, null);
            // team needs to be updated in the local storage
            ContentProviderOperation operation = null;
            if (teamCursor.getCount() > 0) {
                teamCursor.moveToFirst();
                Uri uri = Uri.parse(Contract.Team.CONTENT_URI_TEAM + "/" + teamCursor.getInt(teamCursor.getColumnIndex(Contract.Team.COLUMN_ID)));
                // if team is deleted
                // TODO: DELETED CONNECTED OBJECT
                if (team.isDeleted()) {
                    operation = ContentProviderOperation.newDelete(uri).build();
                    // update if needed
                } else {
                    ContentValues values = new ContentValues();
                    values.put(Contract.Team.COLUMN_TITLE, team.getName());
                    values.put(Contract.Team.COLUMN_DESCRIPTION, team.getDescription());
                    operation = ContentProviderOperation.newUpdate(uri).withValues(values).build();
                }
            } else {
                ContentValues values = new ContentValues();
                values.put(Contract.Team.COLUMN_TITLE, team.getName());
                values.put(Contract.Team.COLUMN_DESCRIPTION, team.getDescription());
                values.put(Contract.Team.COLUMN_SERVER_TEAM_ID, team.getServerTeamId());
                if (userInsertedIndex.containsKey(team.getCreatorId())) {
                    operation = ContentProviderOperation.newInsert(Contract.Team.CONTENT_URI_TEAM)
                            .withValues(values)
                            .withValueBackReference(Contract.Team.COLUMN_CREATOR, userInsertedIndex.get(team.getCreatorId()))
                            .build();
                } else {
                    selection = Contract.User.COLUMN_GLOBAL_ID + " = ?";
                    selectionArgs = new String[]{Long.toString(team.getCreatorId())};
                    Cursor userCursor = getContentResolver().query(Contract.User.CONTENT_URI_USER, null, selection, selectionArgs, null);
                    userCursor.moveToFirst();
                    values.put(Contract.Team.COLUMN_CREATOR, userCursor.getInt(userCursor.getColumnIndex(Contract.User.COLUMN_ID)));
                    userCursor.close();
                    operation = ContentProviderOperation.newInsert(Contract.Team.CONTENT_URI_TEAM).withValues(values).build();
                }

                teamInsertedIndex.put(team.getServerTeamId(), batch.size());


            }
            teamCursor.close();
            batch.add(operation);
        }

        for (TeamUserConnection conn : syncDTO.getTeamUserConnections()) {
            String selection = Contract.UserTeamConnection.COLUMN_GLOBAL_ID + " = ?";
            String[] selectionArgs = new String[]{Long.toString(conn.getId())};
            Cursor connCursor = getContentResolver().query(Contract.UserTeamConnection.CONTENT_URI_USER_TEAM, null, selection, selectionArgs, null);
            // team needs to be updated in the local storage
            ContentProviderOperation operation = null;
            if (connCursor.getCount() > 0) {
                connCursor.moveToFirst();
                Uri uri = Uri.parse(Contract.UserTeamConnection.CONTENT_URI_USER_TEAM + "/" + connCursor.getInt(connCursor.getColumnIndex(Contract.Team.COLUMN_ID)));
                // if team is deleted
                // TODO: DELETED BOOLEAN VALUE SEE
                if (conn.isDeleted()) {
                    operation = ContentProviderOperation.newDelete(uri).build();
                    // update if needed
                } else {
                    /*
                    ContentValues values = new ContentValues();
                    operation = ContentProviderOperation.newUpdate(uri).withValues(values).build();
                     */
                }
            } else {
                if (!conn.isDeleted()) {
                    ContentValues values = new ContentValues();
                    values.put(Contract.UserTeamConnection.COLUMN_GLOBAL_ID, conn.getId());

                    if (teamInsertedIndex.containsKey(conn.getTeamId()) && userInsertedIndex.containsKey(conn.getUser().getGlobalId())) {
                        operation = ContentProviderOperation.newInsert(Contract.UserTeamConnection.CONTENT_URI_USER_TEAM)
                                .withValues(values)
                                .withValueBackReference(Contract.UserTeamConnection.COLUMN_TEAM_ID, teamInsertedIndex.get(conn.getTeamId()))
                                .withValueBackReference(Contract.UserTeamConnection.COLUMN_USER_ID, userInsertedIndex.get(conn.getUser().getGlobalId()))
                                .build();
                    } else if (teamInsertedIndex.containsKey(conn.getTeamId()) && !userInsertedIndex.containsKey(conn.getUser().getGlobalId())) {
                        values.put(Contract.UserTeamConnection.COLUMN_USER_ID, conn.getUser().getEmail());
                        operation = ContentProviderOperation.newInsert(Contract.UserTeamConnection.CONTENT_URI_USER_TEAM)
                                .withValues(values)
                                .withValueBackReference(Contract.UserTeamConnection.COLUMN_TEAM_ID, teamInsertedIndex.get(conn.getTeamId()))
                                .build();
                    } else if (!teamInsertedIndex.containsKey(conn.getTeamId()) && userInsertedIndex.containsKey(conn.getUser().getGlobalId())) {
                        selection = Contract.Team.COLUMN_SERVER_TEAM_ID + " = ?";
                        selectionArgs = new String[]{Long.toString(conn.getTeamId())};
                        Cursor cursorTeam = getContentResolver().query(Contract.Team.CONTENT_URI_TEAM, null, selection, selectionArgs, null);
                        values.put(Contract.UserTeamConnection.COLUMN_TEAM_ID, cursorTeam.getInt(cursorTeam.getColumnIndex(Contract.Team.COLUMN_ID)));
                        cursorTeam.close();
                        operation = ContentProviderOperation.newInsert(Contract.UserTeamConnection.CONTENT_URI_USER_TEAM)
                                .withValues(values)
                                .withValueBackReference(Contract.UserTeamConnection.COLUMN_USER_ID, userInsertedIndex.get(conn.getUser().getEmail()))
                                .build();
                    } else {
                        selection = Contract.Team.COLUMN_SERVER_TEAM_ID + " = ?";
                        selectionArgs = new String[]{Long.toString(conn.getTeamId())};
                        Cursor cursorTeam = getContentResolver().query(Contract.Team.CONTENT_URI_TEAM, null, selection, selectionArgs, null);
                        values.put(Contract.UserTeamConnection.COLUMN_TEAM_ID, cursorTeam.getInt(cursorTeam.getColumnIndex(Contract.Team.COLUMN_ID)));
                        cursorTeam.close();
                        values.put(Contract.UserTeamConnection.COLUMN_USER_ID, conn.getUser().getEmail());
                        operation = ContentProviderOperation.newInsert(Contract.UserTeamConnection.CONTENT_URI_USER_TEAM)
                                .withValues(values)
                                .build();
                    }

                }

            }

            if (operation != null)
                batch.add(operation);

            connCursor.close();

        }

        for (Message message : syncDTO.getMessages()) {
            String selection = Contract.Message.COLUMN_GLOBAL_ID + " = ?";
            String[] selectionArgs = new String[]{Long.toString(message.getGlobalId())};
            Cursor messageCursor = getContentResolver().query(Contract.Message.CONTENT_URI_MESSAGE, null, selection, selectionArgs, null);
            // user needs to be updated in the local storage
            ContentProviderOperation operation = null;
            if (messageCursor.getCount() > 0) {
                messageCursor.moveToFirst();
                if (message.isDeleted()) {
                    operation = ContentProviderOperation.newDelete(Contract.Message.CONTENT_URI_MESSAGE)
                            .withSelection(selection, selectionArgs)
                            .build();
                } else {
                   /* ContentValues values = new ContentValues();
                    operation = ContentProviderOperation.newUpdate(uri).withValues(values).build();*/
                }


            } else {
                ContentValues values = new ContentValues();
                values.put(Contract.Message.COLUMN_CREATED_AT, message.getCreatedAt());
                values.put(Contract.Message.COLUMN_GLOBAL_ID, message.getGlobalId());
                values.put(Contract.Message.COLUMN_MESSAGE, message.getMessage());
                if (teamInsertedIndex.containsKey(message.getTeamId()) && userInsertedIndex.containsKey(message.getSender().getGlobalId())) {
                    operation = ContentProviderOperation.newInsert(Contract.Message.CONTENT_URI_MESSAGE)
                            .withValues(values)
                            .withValueBackReference(Contract.Message.COLUMN_TEAM_ID, teamInsertedIndex.get(message.getTeamId()))
                            .withValueBackReference(Contract.Message.COLUMN_SENDER_ID, userInsertedIndex.get(message.getSender().getGlobalId()))
                            .build();
                } else if (teamInsertedIndex.containsKey(message.getTeamId()) && !userInsertedIndex.containsKey(message.getSender().getGlobalId())) {
                    selection = Contract.User.COLUMN_GLOBAL_ID + " = ?";
                    selectionArgs = new String[]{Long.toString(message.getSender().getGlobalId())};
                    Cursor userCursor = getContentResolver().query(Contract.User.CONTENT_URI_USER, null, selection, selectionArgs, null);
                    userCursor.moveToFirst();
                    values.put(Contract.Message.COLUMN_SENDER_ID, userCursor.getInt(userCursor.getColumnIndex(Contract.User.COLUMN_ID)));
                    userCursor.close();
                    operation = ContentProviderOperation.newInsert(Contract.Message.CONTENT_URI_MESSAGE)
                            .withValues(values)
                            .withValueBackReference(Contract.UserTeamConnection.COLUMN_TEAM_ID, teamInsertedIndex.get(message.getTeamId()))
                            .build();
                } else if (!teamInsertedIndex.containsKey(message.getTeamId()) && userInsertedIndex.containsKey(message.getSender().getGlobalId())) {
                    selection = Contract.Team.COLUMN_SERVER_TEAM_ID + " = ?";
                    selectionArgs = new String[]{Long.toString(message.getTeamId())};
                    Cursor cursorTeam = getContentResolver().query(Contract.Team.CONTENT_URI_TEAM, null, selection, selectionArgs, null);
                    cursorTeam.moveToFirst();
                    values.put(Contract.Message.COLUMN_TEAM_ID, cursorTeam.getInt(cursorTeam.getColumnIndex(Contract.Team.COLUMN_ID)));
                    cursorTeam.close();

                    operation = ContentProviderOperation.newInsert(Contract.Message.CONTENT_URI_MESSAGE)
                            .withValues(values)
                            .withValueBackReference(Contract.UserTeamConnection.COLUMN_USER_ID, userInsertedIndex.get(message.getSender().getGlobalId()))
                            .build();
                } else {
                    selection = Contract.Team.COLUMN_SERVER_TEAM_ID + " = ?";
                    selectionArgs = new String[]{Long.toString(message.getTeamId())};
                    Cursor cursorTeam = getContentResolver().query(Contract.Team.CONTENT_URI_TEAM, null, selection, selectionArgs, null);
                    cursorTeam.moveToFirst();
                    values.put(Contract.Message.COLUMN_TEAM_ID, cursorTeam.getInt(cursorTeam.getColumnIndex(Contract.Team.COLUMN_ID)));
                    cursorTeam.close();
                    selection = Contract.User.COLUMN_GLOBAL_ID + " = ?";
                    selectionArgs = new String[]{Long.toString(message.getSender().getGlobalId())};
                    Cursor userCursor = getContentResolver().query(Contract.User.CONTENT_URI_USER, null, selection, selectionArgs, null);
                    userCursor.moveToFirst();
                    values.put(Contract.Message.COLUMN_SENDER_ID, userCursor.getInt(userCursor.getColumnIndex(Contract.User.COLUMN_ID)));
                    userCursor.close();
                    operation = ContentProviderOperation.newInsert(Contract.Message.CONTENT_URI_MESSAGE)
                            .withValues(values)
                            .build();
                }


            }

            messageCursor.close();
            if (operation != null)
                batch.add(operation);
        }


        return batch;
    }

    public void syncHabits(HabitSyncDTO syncDTO) {
        // TODO: ADD COLUMN DELETED
        List<ContentProviderOperation> batchReminders = syncHabitContentProvider(syncDTO);

        if (batchReminders.size() == 0) {
            Log.e("HABIT_SYNC", "EMPTY");
            return;
        }

        try {
            getContentResolver().applyBatch(Contract.Habit.AUTHORITY, (ArrayList<ContentProviderOperation>) batchReminders);
        } catch (OperationApplicationException e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        }


    }

    public List<ContentProviderOperation> syncHabitContentProvider(HabitSyncDTO syncDTO) {
        List<ContentProviderOperation> batch = new ArrayList<>();
        Map<Long, Integer> habitInsertedIndex = new HashMap<>();
        Map<Long, Integer> reminderInsertedIndex = new HashMap<>();

        // synchronization of reminders that are connected to habits
        for (Reminder reminder : syncDTO.getReminderConn()) {
            String selection = Contract.Reminder.COLUMN_GLOBAL_ID + " = ?";
            String[] selectionArgs = new String[]{Long.toString(reminder.getId())};
            Cursor cursorReminder = getContentResolver().query(Contract.Reminder.CONTENT_URI_REMINDER, null, selection, selectionArgs, null);
            // reminder needs to be updated in the local storage
            ContentProviderOperation operation = null;
            if (cursorReminder.getCount() > 0) {
                cursorReminder.moveToFirst();
                Uri uri = Uri.parse(Contract.Reminder.CONTENT_URI_REMINDER + "/" + cursorReminder.getInt(cursorReminder.getColumnIndex(Contract.Reminder.COLUMN_ID)));
                // if reminder is deleted
                // TODO: cancel alarms!
                if (reminder.isDeleted()) {
                    selection = Contract.HabitReminderConnection.COLUMN_REMINDER_ID + " = ?";
                    selectionArgs = new String[]{Long.toString(reminder.getId())};
                    batch.add(ContentProviderOperation.newDelete(Contract.HabitReminderConnection.CONTENT_URI_HABIT_REMINDER_CONN)
                            .withSelection(selection, selectionArgs).build());
                    operation = ContentProviderOperation.newDelete(uri).build();
                    // update if needed
                } else {
                    // TODO:  update alarms
                    ContentValues values = new ContentValues();
                    values.put(Contract.Reminder.COLUMN_DATE, format.format(reminder.getDate()));
                    operation = ContentProviderOperation.newUpdate(uri).withValues(values).build();
                }
            } else {
                // TODO: set alarms
                ContentValues values = new ContentValues();
                values.put(Contract.Reminder.COLUMN_GLOBAL_ID, reminder.getId().intValue());
                values.put(Contract.Reminder.COLUMN_DATE, format.format(reminder.getDate()));
                reminderInsertedIndex.put(reminder.getId(), batch.size());
                operation = ContentProviderOperation.newInsert(Contract.Reminder.CONTENT_URI_REMINDER).withValues(values).build();

            }
            cursorReminder.close();
            batch.add(operation);
        }


        for (Habit habit : syncDTO.getHabits()) {
            ContentProviderOperation operation = null;
            String selection = Contract.Habit.COLUMN_GLOBAL_ID + " = ?";
            String[] selectionArgs = new String[]{Long.toString(habit.getId())};
            Cursor cursorHabit = getContentResolver().query(Contract.Habit.CONTENT_URI_HABIT, null, selection, selectionArgs, null);
            if (cursorHabit.getCount() > 0) {
                cursorHabit.moveToFirst();
                Uri uri = Uri.parse(Contract.Habit.CONTENT_URI_HABIT + "/" + cursorHabit.getInt(cursorHabit.getColumnIndex(Contract.Habit.COLUMN_ID)));
                // habit and all connected entities need to be deleted
                if (habit.getDeleted()) {
                    Integer habitId = cursorHabit.getInt(cursorHabit.getColumnIndex(Contract.Habit.COLUMN_ID));
                    batch = deleteAndCancelReminders(habitId, batch);
                    batch = deleteHabitFulfillmentByHabit(habitId, batch);
                    if (cursorHabit.getInt(cursorHabit.getColumnIndex(Contract.Habit.COLUMN_NUMBER_OF_DAYS)) == -1) {
                        batch = this.deleteHabitDays(habitId, batch);
                    }
                    operation = ContentProviderOperation.newDelete(uri).build();
                    // habit needs to be updated
                } else {
                    ContentValues values = new ContentValues();
                    values.put(Contract.Habit.COLUMN_TITLE, habit.getTitle());
                    values.put(Contract.Habit.COLUMN_DESCRIPTION, habit.getDescription());
                    values.put(Contract.Habit.COLUMN_NUMBER_OF_DAYS, habit.getNumberOfDays());
                    values.put(Contract.Habit.COLUMN_GOAL, habit.getGoal());
                    operation = ContentProviderOperation.newUpdate(uri).withValues(values).build();
                }
            } else {
                ContentValues values = new ContentValues();
                values.put(Contract.Habit.COLUMN_TITLE, habit.getTitle());
                values.put(Contract.Habit.COLUMN_DESCRIPTION, habit.getDescription());
                values.put(Contract.Habit.COLUMN_NUMBER_OF_DAYS, habit.getNumberOfDays());
                values.put(Contract.Habit.COLUMN_GOAL, habit.getGoal());
                values.put(Contract.Habit.COLUMN_GLOBAL_ID, habit.getId());
                habitInsertedIndex.put(habit.getId(), batch.size());
                operation = ContentProviderOperation.newInsert(Contract.Habit.CONTENT_URI_HABIT).withValues(values).build();
            }
            cursorHabit.close();
            batch.add(operation);
        }

        for (HabitFulfillment fulfillment : syncDTO.getHabitFulfillment()) {
            ContentProviderOperation operation = null;
            String selection = Contract.HabitFulfillment.COLUMN_GLOBAL_ID + " = ?";
            String[] selectionArgs = new String[]{Long.toString(fulfillment.getId())};
            Cursor cursorHabitFulfillment = getContentResolver().query(Contract.HabitFulfillment.CONTENT_HABIT_FULFILLMENT, null, selection, selectionArgs, null);
            if (cursorHabitFulfillment.getCount() > 0) {
                cursorHabitFulfillment.moveToFirst();
                Uri uri = Uri.parse(Contract.HabitFulfillment.CONTENT_HABIT_FULFILLMENT + "/" + cursorHabitFulfillment.getInt(cursorHabitFulfillment.getColumnIndex(Contract.HabitFulfillment.COLUMN_ID)));
                // delete fulfillment of habit
                if (fulfillment.isDeleted()) {
                    operation = ContentProviderOperation.newDelete(uri).build();
                } else {
                    ContentValues values = new ContentValues();
                    values.put(Contract.HabitFulfillment.COLUMN_DATE, format.format(fulfillment.getDay()));
                    operation = ContentProviderOperation.newUpdate(uri).withValues(values).build();
                }
            } else {
                // delete operation for habit also delete fulfillment because of the foreign key constraint
                if (!fulfillment.isDeleted()) {
                    ContentValues values = new ContentValues();
                    values.put(Contract.HabitFulfillment.COLUMN_DATE, format.format(fulfillment.getDay()));
                    values.put(Contract.HabitFulfillment.COLUMN_GLOBAL_ID, fulfillment.getId());
                    if (!habitInsertedIndex.containsKey(fulfillment.getHabitId())) {
                        selection = Contract.Habit.COLUMN_GLOBAL_ID + " = ?";
                        selectionArgs = new String[]{Long.toString(fulfillment.getHabitId())};
                        Cursor cursorHabit = getContentResolver().query(Contract.Habit.CONTENT_URI_HABIT, null, selection, selectionArgs, null);
                        cursorHabit.moveToFirst();
                        values.put(Contract.HabitFulfillment.COLUMN_HABIT_ID, cursorHabit.getInt(cursorHabit.getColumnIndex(Contract.Habit.COLUMN_ID)));
                        operation = ContentProviderOperation.newInsert(Contract.HabitFulfillment.CONTENT_HABIT_FULFILLMENT)
                                .withValues(values)
                                .build();
                        cursorHabit.close();
                    } else {
                        operation = ContentProviderOperation.newInsert(Contract.HabitFulfillment.CONTENT_HABIT_FULFILLMENT)
                                .withValues(values)
                                .withValueBackReference(Contract.HabitFulfillment.COLUMN_HABIT_ID, habitInsertedIndex.get(fulfillment.getHabitId()))
                                .build();
                    }
                }

            }
            cursorHabitFulfillment.close();
            if (operation != null)
                batch.add(operation);
        }

        for (HabitDayConnection conn : syncDTO.getHabitDayConnection()) {
            ContentProviderOperation operation = null;
            String selection = Contract.HabitDayConnection.COLUMN_GLOBAL_ID + " = ?";
            String[] selectionArgs = new String[]{Long.toString(conn.getId())};
            Cursor cursorHabitDayConnection = getContentResolver().query(Contract.HabitDayConnection.CONTENT_URI_HABIT_DAY_CONN, null, selection, selectionArgs, null);
            if (cursorHabitDayConnection.getCount() > 0) {
                cursorHabitDayConnection.moveToFirst();
                Uri uri = Uri.parse(Contract.HabitDayConnection.CONTENT_URI_HABIT_DAY_CONN + "/" + cursorHabitDayConnection.getInt(cursorHabitDayConnection.getColumnIndex(Contract.HabitDayConnection.COLUMN_ID)));
                // delete fulfillment of habit
                if (conn.isDeleted()) {
                    operation = ContentProviderOperation.newDelete(uri).build();
                } else {
                    // connection between a day and a habit can only be created or deleted
                    // just in case update
                    /*ContentValues values = new ContentValues();
                    operation = ContentProviderOperation.newUpdate(uri).withValues(values).build();*/
                }
            } else {
                if (!conn.isDeleted()) {
                    ContentValues values = new ContentValues();
                    values.put(Contract.HabitDayConnection.COLUMN_GLOBAL_ID, conn.getId());
                    DayOfWeek day = DayOfWeek.valueOf(conn.getDay());
                    values.put(Contract.HabitDayConnection.COLUMN_HABIT_DAY_ID, day.ordinal() + 1);

                    if (!habitInsertedIndex.containsKey(conn.getHabitId())) {
                        selection = Contract.Habit.COLUMN_GLOBAL_ID + " = ?";
                        selectionArgs = new String[]{Long.toString(conn.getHabitId())};
                        Cursor cursorHabit = getContentResolver().query(Contract.Habit.CONTENT_URI_HABIT, null, selection, selectionArgs, null);
                        cursorHabit.moveToFirst();
                        values.put(Contract.HabitDayConnection.COLUMN_HABIT_ID, cursorHabit.getInt(cursorHabit.getColumnIndex(Contract.Habit.COLUMN_ID)));
                        operation = ContentProviderOperation.newInsert(Contract.HabitDayConnection.CONTENT_URI_HABIT_DAY_CONN)
                                .withValues(values)
                                .build();
                        cursorHabit.close();
                    } else {
                        operation = ContentProviderOperation.newInsert(Contract.HabitDayConnection.CONTENT_URI_HABIT_DAY_CONN)
                                .withValues(values)
                                .withValueBackReference(Contract.HabitFulfillment.COLUMN_HABIT_ID, habitInsertedIndex.get(conn.getHabitId()))
                                .build();
                    }
                }
            }
            cursorHabitDayConnection.close();
            if (operation != null)
                batch.add(operation);
        }

        for (HabitReminderConnection conn : syncDTO.getHabitReminderConnections()) {
            ContentProviderOperation operation = null;
            String selection = Contract.HabitReminderConnection.COLUMN_GLOBAL_ID + " = ?";
            String[] selectionArgs = new String[]{Long.toString(conn.getId())};
            Cursor cursorHabitReminderConnection = getContentResolver().query(Contract.HabitReminderConnection.CONTENT_URI_HABIT_REMINDER_CONN, null, selection, selectionArgs, null);
            if (cursorHabitReminderConnection.getCount() > 0) {
                cursorHabitReminderConnection.moveToFirst();
                Uri uri = Uri.parse(Contract.HabitReminderConnection.CONTENT_URI_HABIT_REMINDER_CONN + "/" + cursorHabitReminderConnection.getInt(cursorHabitReminderConnection.getColumnIndex(Contract.HabitReminderConnection.COLUMN_ID)));
                // delete reminder of habit
                if (conn.isDeleted()) {
                    operation = ContentProviderOperation.newDelete(uri).build();
                } else {
                    /*// connection between a reminder and a habit can only be created or deleted
                    // just in case update
                    ContentValues values = new ContentValues();
                    operation = ContentProviderOperation.newUpdate(uri).withValues(values).build();*/
                }
            } else {
                if (!conn.isDeleted()) {
                    ContentValues values = new ContentValues();
                    values.put(Contract.HabitReminderConnection.COLUMN_GLOBAL_ID, conn.getId());

                    // if habit is just created reminder wasn't present before
                    if (habitInsertedIndex.containsKey(conn.getHabitId())) {
                        operation = ContentProviderOperation.newInsert(Contract.HabitReminderConnection.CONTENT_URI_HABIT_REMINDER_CONN)
                                .withValues(values)
                                .withValueBackReference(Contract.HabitReminderConnection.COLUMN_HABIT_ID, habitInsertedIndex.get(conn.getHabitId()))
                                .withValueBackReference(Contract.HabitReminderConnection.COLUMN_REMINDER_ID, reminderInsertedIndex.get(conn.getReminderId()))
                                .build();
                    } else if (reminderInsertedIndex.containsKey(conn.getReminderId())) {
                        selection = Contract.Habit.COLUMN_GLOBAL_ID + " = ?";
                        selectionArgs = new String[]{Long.toString(conn.getHabitId())};
                        Cursor cursorHabit = getContentResolver().query(Contract.Habit.CONTENT_URI_HABIT, null, selection, selectionArgs, null);
                        cursorHabit.moveToFirst();
                        values.put(Contract.HabitReminderConnection.COLUMN_HABIT_ID, cursorHabit.getInt(cursorHabit.getColumnIndex(Contract.Habit.COLUMN_ID)));
                        operation = ContentProviderOperation.newInsert(Contract.HabitReminderConnection.CONTENT_URI_HABIT_REMINDER_CONN)
                                .withValues(values)
                                .withValueBackReference(Contract.HabitReminderConnection.COLUMN_REMINDER_ID, reminderInsertedIndex.get(conn.getReminderId()))
                                .build();
                    }
                    // it is unlikely that this will happen
                    else {
                        selection = Contract.Habit.COLUMN_GLOBAL_ID + " = ?";
                        selectionArgs = new String[]{Long.toString(conn.getHabitId())};
                        Cursor cursorHabit = getContentResolver().query(Contract.Habit.CONTENT_URI_HABIT, null, selection, selectionArgs, null);
                        cursorHabit.moveToFirst();
                        values.put(Contract.HabitReminderConnection.COLUMN_HABIT_ID, cursorHabit.getInt(cursorHabit.getColumnIndex(Contract.Habit.COLUMN_ID)));
                        cursorHabit.close();
                        selection = Contract.Reminder.COLUMN_GLOBAL_ID + " = ?";
                        selectionArgs = new String[]{Long.toString(conn.getReminderId())};
                        Cursor cursorReminder = getContentResolver().query(Contract.Reminder.CONTENT_URI_REMINDER, null, selection, selectionArgs, null);
                        values.put(Contract.HabitReminderConnection.COLUMN_REMINDER_ID, cursorReminder.getInt(cursorReminder.getColumnIndex(Contract.Reminder.COLUMN_ID)));
                        cursorReminder.close();
                        operation = ContentProviderOperation.newInsert(Contract.HabitDayConnection.CONTENT_URI_HABIT_DAY_CONN)
                                .withValues(values)
                                .build();

                    }
                }

            }
            cursorHabitReminderConnection.close();

            if (operation != null)
                batch.add(operation);

        }

        return batch;
    }

    private List<ContentProviderOperation> deleteAndCancelReminders(Integer habitId, List<ContentProviderOperation> batch) {
        Uri uri = Uri.parse(Contract.HabitReminderConnection.CONTENT_URI_HABIT_REMINDER_CONN + "/" + Contract.Habit.TABLE_NAME + "/" + habitId);
        Cursor cursor = this.getContentResolver().query(uri, null, null, null, null);
        if (cursor.getCount() > 0) {
            //delete for reminder column all values
            while (cursor.moveToNext()) {
                batch = deleteHabitReminderConn(cursor.getInt(cursor.getColumnIndex(Contract.HabitReminderConnection.COLUMN_ID)), batch);
                batch = deleteHabitReminders(cursor.getInt(cursor.getColumnIndex(Contract.HabitReminderConnection.COLUMN_REMINDER_ID)), batch);
                // cancel alarm for reminder
                if (batch != null) {
                    Intent alarmIntent = new Intent(this, ReminderBroadcastReceiver.class);
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(this, cursor.getInt(cursor.getColumnIndex(Contract.HabitReminderConnection.COLUMN_REMINDER_ID)), alarmIntent, PendingIntent.FLAG_NO_CREATE);
                    AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                    if (alarmManager != null)
                        alarmManager.cancel(pendingIntent);
                }
            }

            cursor.close();
        }

        return batch;
    }

    private List<ContentProviderOperation> deleteHabitFulfillmentByHabit(Integer id, List<ContentProviderOperation> batch) {
        Uri uri = Uri.parse(Contract.HabitFulfillment.CONTENT_HABIT_FULFILLMENT + "/" + Contract.Habit.TABLE_NAME + "/" + id);
        batch.add(ContentProviderOperation.newDelete(uri).build());
        return batch;
    }

    public List<ContentProviderOperation> deleteHabitReminders(Integer reminderId, List<ContentProviderOperation> batch) {
        Uri uri = Uri.parse(Contract.Reminder.CONTENT_URI_REMINDER + "/" + reminderId);
        batch.add(ContentProviderOperation.newDelete(uri).build());
        return batch;
    }

    public List<ContentProviderOperation> deleteHabitReminderConn(Integer id, List<ContentProviderOperation> batch) {
        Uri uri = Uri.parse(Contract.HabitReminderConnection.CONTENT_URI_HABIT_REMINDER_CONN + "/" + id);
        batch.add(ContentProviderOperation.newDelete(uri).build());
        return batch;
    }

    private List<ContentProviderOperation> deleteHabitDays(Integer id, List<ContentProviderOperation> batch) {
        Uri uri = Uri.parse(Contract.HabitDayConnection.CONTENT_URI_HABIT_DAY_CONN + "/" + Contract.Habit.TABLE_NAME + "/" + id);
        batch.add(ContentProviderOperation.newDelete(uri).build());
        return batch;
    }


    public static int getConnectivityStatus(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (null != activeNetwork) {
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI)
                return TYPE_WIFI;

            if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE)
                return TYPE_MOBILE;
        }
        return TYPE_NOT_CONNECTED;
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }
}
