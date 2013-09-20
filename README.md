android-gcm-poc
===============

Proof-of-concept on Android GCM.

Directories:
* andgcmpoc-android/ : includes the android project. A simple application that registers to google and to our backend. Then receives notifications.
* andgcmpoc-srvr/ : includes a simple backend. Python 2.7 + bottle. Openshift ready.
 * Receives register petitions from android phone: recvr_id, domain, google_token.
 * Sends push notification to a mobile phone, given its recvr_id and domain.
