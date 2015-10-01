# WearCast

A library project which allows easy extension of Android Wear noification with Google Cast support

Note that this library is still in early development and there is lots of room for improvement.

## How it works

In Android Wear it is possible to set a Display Intent on a notification, which results in an Activity inside the notification see http://developer.android.com/training/wearables/apps/layouts.html. By hooking into the onResume and onPause events of this activity a search for Chromecast devices is triggered when the user looks at the notification on the watch, and the notifications actions are updated when a device is found.

All a developer needs to do is use the WearCastNotificationManager instead of the default NotificationManager and add a WearCastExtender when the NotificationBuilder is set up. In the WearcastExtender you can specify a MediaInfo object (From the Cast SDK) which determines what stream/video is linked to the notification.

## How to use
The library is currently hosted in a bintray maven repo so add this repo to your project

``` gradle

repositories {
    maven {
        url  "http://dl.bintray.com/rmokveld/maven"
    }
}

```

### Wear

As you can see in the sample-wear module implementation on the Wear side is very simple. You just need a wear module which has the following dependency


``` gradle

compile 'nl.rmokveld:wearcast-wear:0.1.15'

```

### Phone

On the phone side you can either use the wearcast-app library, where you still need to do some custom coding for starting the receiver app, or you can use the wearcast-app-ccl library which is the default implementation using ccl. Check out the source code of sample-app and library-app-ccl to know what custom code you need to implement.

``` gradle

compile 'nl.rmokveld:wearcast-app:0.1.15'

```
or

``` gradle

compile 'nl.rmokveld:wearcast-app-ccl:0.1.15'

```
