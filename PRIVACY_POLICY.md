## We Jeep: Privacy policy

Welcome to the We Jeep app for Android!

This is an open source Android app developed by James Dy, Jordan Rivera, and Jercy Guevarra. The source code is available on GitHub under the GNU AGPL license (3.0 or later).

As an avid Android user myself, I take privacy very seriously.
I know how frustrating it is when apps collect your data without your knowledge.

### Data collected by the app

I hereby state, to the best of my knowledge and belief, that I have not programmed this app to collect any personally identifiable information. All data (app preferences (like theme) and alarms) created by the you (the user) is stored locally in your device only, and can be simply erased by clearing the app's data or uninstalling it. No analytics software is present in the app either.

### Explanation of permissions requested in the app

The list of permissions required by the app can be found in the `AndroidManifest.xml` file:

https://github.com/jamesdymagiba/wejeep/blob/main/app/src/main/AndroidManifest.xml
<br/>

| Permission | Why it is required |
| :---: | --- |
| `android.permission.INTERNET` | To share the user's location with others, the app must send data to a server. |
| `android.permission.ACCESS_FINE_LOCATION` | This permission gives your app access to the precise location of the device (via GPS, Wi-Fi, or mobile networks) |
| `android.permission.ACCESS_COARSE_LOCATION` | This permission provides access to the approximate location of the device (typically based on cell towers or Wi-Fi access points). |
 <hr style="border:1px solid gray">

If you find any security vulnerability that has been inadvertently caused by me, or have any question regarding how the app protectes your privacy, please send me an email or post a discussion on GitHub, and I will surely try to fix it/help you.

Yours sincerely,  
James Dy.  
Bulacan, Philippines.  
domingojamesdy@gmail.com
