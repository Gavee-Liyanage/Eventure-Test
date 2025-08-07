package com.example.eventuretest.utils

object AdminConstants {
    // Firestore Collections
    const val EVENTS_COLLECTION = "events"
    const val ADMIN_USERS_COLLECTION = "admin_users"
    const val CATEGORIES_COLLECTION = "categories"

    // Event Status
    const val EVENT_STATUS_ACTIVE = "active"
    const val EVENT_STATUS_INACTIVE = "inactive"
    const val EVENT_STATUS_CANCELLED = "cancelled"
    const val EVENT_STATUS_COMPLETED = "completed"

    // Event Categories
    const val CATEGORY_MUSICAL = "Musical"
    const val CATEGORY_SPORTS = "Sports"
    const val CATEGORY_FOOD = "Food"
    const val CATEGORY_ART = "Art"

    // Intent Keys
    const val EXTRA_EVENT_ID = "extra_event_id"
    const val EXTRA_EVENT_OBJECT = "extra_event_object"
    const val EXTRA_IS_EDIT_MODE = "extra_is_edit_mode"

    // Request Codes
    const val REQUEST_CODE_PICK_IMAGE = 1001
    const val REQUEST_CODE_PICK_MULTIPLE_IMAGES = 1002
    const val REQUEST_CODE_DATE_PICKER = 1003
    const val REQUEST_CODE_TIME_PICKER = 1004
    const val REQUEST_CODE_LOCATION_PICKER = 1005

    // Permissions
    const val PERMISSION_CREATE_EVENT = "create_event"
    const val PERMISSION_EDIT_EVENT = "edit_event"
    const val PERMISSION_DELETE_EVENT = "delete_event"
    const val PERMISSION_VIEW_ANALYTICS = "view_analytics"

    // Validation Constants
    const val MIN_EVENT_NAME_LENGTH = 3
    const val MAX_EVENT_NAME_LENGTH = 100
    const val MIN_DESCRIPTION_LENGTH = 10
    const val MAX_DESCRIPTION_LENGTH = 1000
    const val MAX_IMAGES_PER_EVENT = 5

    // Date/Time Formats
    const val DATE_FORMAT_DISPLAY = "MMM dd, yyyy"
    const val TIME_FORMAT_DISPLAY = "HH:mm"
    const val DATETIME_FORMAT_FULL = "MMM dd, yyyy 'at' HH:mm"

    // Shared Preferences
    const val ADMIN_PREFS_NAME = "admin_preferences"
    const val PREF_ADMIN_ID = "admin_id"
    const val PREF_ADMIN_EMAIL = "admin_email"
    const val PREF_ADMIN_NAME = "admin_name"
    const val PREF_LAST_LOGIN = "last_login"
}