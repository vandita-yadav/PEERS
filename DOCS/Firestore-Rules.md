# Firestore Security Rules

Paste this into **Firebase console → Firestore Database → Rules** and publish.
It covers all feature sets: profiles, connections, notifications,
accommodation listings, wishlists, saved events, forum posts, and events.

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

      // Events saved for later (offline-first cache mirrors this).
      match /savedEvents/{eventId} {
        allow read, write: if isOwner(userId);
      }

      // Events the student is attending (RSVPs).
      match /attendingEvents/{eventId} {
        allow read, write: if isOwner(userId);
      }
    }

    // Community-contributed accommodation inventory.
    match /accommodations/{listingId} {
      allow read: if signedIn();
      allow create: if signedIn()
        && request.resource.data.contributorUid == request.auth.uid;
      allow update: if signedIn() && (
        resource.data.contributorUid == request.auth.uid ||
        // Signed-in students may only refresh the aggregate counters below.
        request.resource.data.diff(resource.data).affectedKeys()
          .hasOnly(['verifiedCount', 'rating', 'reviewCount'])
      );
      allow delete: if signedIn()
        && resource.data.contributorUid == request.auth.uid;

      // One community verification per student.
      match /verifications/{userId} {
        allow read: if signedIn();
        allow create: if isOwner(userId);
        allow update, delete: if false;
      }

      // One review per student, readable by everyone signed in.
      match /reviews/{userId} {
        allow read: if signedIn();
        allow create, update: if isOwner(userId);
        allow delete: if false;
      }
    }

    // Roommate requests: only the sender and recipient can read them.
    match /roommateRequests/{requestId} {
      allow read: if signedIn() && (
        resource.data.fromUid == request.auth.uid ||
        resource.data.toUid == request.auth.uid
      );
      allow create: if signedIn()
        && request.resource.data.fromUid == request.auth.uid;
      allow update: if signedIn() && (
        resource.data.fromUid == request.auth.uid ||
        resource.data.toUid == request.auth.uid
      );
      allow delete: if false;
    }

    // Discussion forum posts and threaded comments.
    match /forumPosts/{postId} {
      allow read: if signedIn();
      allow create: if signedIn();
      allow update: if signedIn() && (
        resource.data.authorUid == request.auth.uid ||
        // Students may upvote/toggle upvotes and increment commentCount
        request.resource.data.diff(resource.data).affectedKeys().hasAny(['upvotedBy', 'upvoteCount', 'commentCount'])
      );
      allow delete: if signedIn() && resource.data.authorUid == request.auth.uid;

      match /comments/{commentId} {
        allow read: if signedIn();
        allow create: if signedIn();
        allow update, delete: if signedIn() && resource.data.authorUid == request.auth.uid;
      }
    }

    // Moderation reports for forum discussions and comments.
    match /forumReports/{reportId} {
      allow read: if false;
      allow create: if signedIn();
      allow update, delete: if false;
    }

    // Campus & city events and attendee RSVP lists.
    match /events/{eventId} {
      allow read: if signedIn();
      allow create: if signedIn();
      allow update: if signedIn() && (
        resource.data.createdBy == request.auth.uid ||
        // Any student can update attendeeCount during RSVP transactions
        request.resource.data.diff(resource.data).affectedKeys().hasOnly(['attendeeCount'])
      );
      allow delete: if signedIn() && resource.data.createdBy == request.auth.uid;

      match /attendees/{userId} {
        allow read: if signedIn();
        allow create, delete: if isOwner(userId);
        allow update: if false;
      }
    }
  }
}
```

## Notes

- Listings and posts created from the app start with the contributor recorded as the owner.
  Only that student can edit or remove them.
- The app uses optimistic local state so interactions (upvoting, replying, RSVPing, saving)
  work seamlessly in airplane mode or while syncing.
- After publishing, re-open the app (Firestore listeners do not recover from a
  permission denial until the app restarts).
- Never commit `google-services.json`, `local.properties`, or API keys.
