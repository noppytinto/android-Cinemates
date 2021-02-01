package mirror42.dev.cinemates.model.room;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import mirror42.dev.cinemates.model.notification.Notification;


@Database(entities = {Notification.class}, version = 1, exportSchema = false)
public abstract class CinematesLocalDatabase extends RoomDatabase {
    private static volatile CinematesLocalDatabase instance;
    private static final String DB_NAME = "cinemates-room-db";


    //--------------------------------- CONSTRUCTORS

    public CinematesLocalDatabase() {}



    //--------------------------------- GETTERS/SETTERS

    public abstract NotificationDao getNotificationDao();

    public static synchronized CinematesLocalDatabase getInstance(Context context) {
        if (instance == null) {
            instance = create(context);
        }
        return instance;
    }



    //--------------------------------- METHODS

    private static CinematesLocalDatabase create(final Context context) {
        return Room.databaseBuilder(context, CinematesLocalDatabase.class, DB_NAME)
                .build();
    }

}// end CinematesLocalDatabase class
