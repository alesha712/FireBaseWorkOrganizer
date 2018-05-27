package com.hqs.alx.mushalmapp2.data;

/**
 * Created by Alex on 08/02/2018.
 */

public class FireBaseConstants {

    private FireBaseConstants(){
    }

    /*
            Users constructor fields
     */
    public final static String USER_FULL_NAME = "fullName";
    public final static String USER_BITHDAY = "dateOfBirth";
    public final static String USER_EMAIL = "email";
    public final static String USER_PASSWORD = "password";
    public final static String USER_PHONE = "phone";
    public final static String USER_WORK_NAME = "workName";

    public final static String USER_DEVICE_TOKEN = "device_token";

    /*
            Other fields
     */

    public final static String ALL_APP_USERS = "AAallAppUsers";
    public final static String ALL_APP_WORKPLACES = "AAallWorkPlaces";
    public final static String CHILD_ADMIN_PASS = "adminPassword";
    public final static String CHILD_ADMIN = "admin";
    public final static String CHILD_USERS = "users";
    public final static String CHILD_MESSAGES = "messages";
    public final static String CHILD_CHAT_PHOTOS = "chat_photos";
    public final static String CHILD_WORKSCHEDULE = "WorkSchedule";
    public final static String CHILD_SCHEDULE_WORK_DAYS = "workDays";
    public final static String CHILD_SCHEDULE_SHIFTS_PER_DAY = "shiftsPerDay";
    public final static String CHILD_USER_WORK_PLACES = "workPlaces";
    public final static String CHILD_NOTIFICATION = "notifications";
    public final static String CHILD_PRIVATE_NOTIFICATION = "private_notifications";
    public final static String CHILD_USERS_SHIFTS = "all_users_shifts";
    public final static String CHILD_UPCOMING_SHIFTS = "NumOfUpcomingShifts";
    public final static String CHILD_UNREAD_MESSAGES = "UnreadMessages";

    //a refference for a single work schedule - if it is ready or not
    public final static String CHILD_READY = "ready";
    public final static String CHILD_PUBLISHED = "published";

    public final static String CHILD_USER_PHOTO = "userProfileImage";
    public final static String WORK_IMAGE = "workImage";

    public final static String CHILD_TODO_ITEMS = "ToDo";

}
