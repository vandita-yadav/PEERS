# Firestore Security Rules

Paste this into **Firebase console → Firestore Database → Rules** and publish.
It covers the current feature set: profiles, connections, notifications,
accommodation listings, and wishlists.

```rules
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {

    function signedIn() {
      return request.auth != null;
    }

    function isOwner(uid) {
      return signedIn() && request.auth.uid == uid;
    }

    // Public profiles are readable by signed-in students, writable by the owner.
    match /users/{userId} {
      allow read: if signedIn();
      allow create, update: if isOwner(userId);
      allow delete: if false;

      // Connection state between the owner and one peer.
      match /connections/{peerId} {
        allow read, write: if isOwner(userId);
      }

      // In-app notifications for the owner.
      match /notifications/{notificationId} {
        allow read, write: if isOwner(userId);
      }

      // Saved accommodation listings.
      match /wishlist/{listingId} {
        allow read, write: if isOwner(userId);
      }
    }

    // Community-contributed accommodation inventory.
    match /accommodations/{listingId} {
      allow read: if signedIn();
      allow create: if signedIn()
        && request.resource.data.contributorUid == request.auth.uid;
      allow update, delete: if signedIn()
        && resource.data.contributorUid == request.auth.uid;
    }
  }
}
```

## Notes

- Listings created from the app start with the contributor recorded as the owner.
  Only that student can edit or remove them.
- The app writes no demo data to Firestore; demo listings are bundled locally
  until real listings are added.
- After publishing, re-open the app (Firestore listeners do not recover from a
  permission denial until the app restarts).
- Never commit `google-services.json`, `local.properties`, or API keys.
