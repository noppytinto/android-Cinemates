package mirror42.dev.cinemates.model.room;


import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.ArrayList;
import java.util.List;

import mirror42.dev.cinemates.model.notification.Notification;


// reserved for ROOM
@Dao
public interface NotificationDao {
    @Query("SELECT * FROM notification")
    List<Notification> getAll();

    @Query("SELECT * FROM notification WHERE id IN (:notificationIDs)")
    List<Notification> loadAllByIds(int[] notificationIDs);

    @Query("SELECT * FROM notification WHERE id LIKE :notificationID")
    Notification findByID(long notificationID);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertAll(ArrayList<Notification> notifications);

    @Delete
    void delete(Notification notification);

    @Update
    void update(Notification notification);

    @Update
    void update(ArrayList<Notification> notifications);

    @Query("SELECT EXISTS (SELECT 1 FROM notification WHERE isNew = 1)")
    boolean checkForNewNotifications();


    @Update
    void setNotificationAsOld(Notification x);
}
