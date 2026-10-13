# Notification Coverage Catalog

## Why this doc exists

The plan is to move toward a real app-style notification system — the Facebook-style experience where you get notified of a tag, a like, or a new message even when you're not currently in the app (mobile push, not just "something updated while you happened to be looking at the screen"). This doc catalogs **every event across the platform that a user would reasonably expect to be notified about**, tracks what coverage exists for each, and records what's actually been implemented so far.

**Status: the in-app notification layer (persisted + live push) has been wired up across every feature area below except real mobile push itself.** What's still missing is the last mile — forwarding these persisted rows to a phone that isn't running the app (APNs/FCM). See "What's still missing" at the bottom.

## Current infrastructure

Two mechanisms work together now:

1. **`NotificationWebSocketHandler`** (`Config/`) — an in-memory `Map<email, WebSocketSession>`. `sendToUser()` pushes live only if that user currently has a WebSocket connection open.
2. **`Notification` entity + `NotificationService.notify(...)`** (`Entity/`, `Service/`) — every domain event below now calls this single method, which **persists a `Notification` row** (fetchable later via `GET /api/notifications`, survives the user being offline) **and** attempts the live WebSocket push in the same call. This is the pattern every new event should follow.

So today: if you're offline when something happens, you'll see it in your notification list next time you open the app (`GET /api/notifications`, `GET /api/notifications/unread/count`). If you're online, you also get it live. What's *not* yet built is pushing it to your phone's lock screen while the app itself is fully closed — that requires a device-token registration table and an APNs/FCM (or OneSignal/Firebase) integration, neither of which exist yet.

**Migration note:** `Notification.NotificationType` gained ~30 new enum values during this work. Postgres's `notifications_type_check` CHECK constraint doesn't auto-update when Hibernate's `ddl-auto=update` sees new enum values (same class of issue hit earlier with `pet_sittings_status_check`) — it was manually widened via `ALTER TABLE ... DROP/ADD CONSTRAINT`. If the enum gains more values later, that constraint needs the same manual update or every insert with a new type will fail.

## Full event catalog by feature

Legend: 🔵 implemented (persisted + live push) · 🟡 email only (no in-app/push) · ⚪ not applicable / deliberately skipped

### Community Posts

| Event | Recipient | Coverage | Notes |
|---|---|---|---|
| Someone likes your post | Post author | 🔵 | `PostService.likePost` → `POST_LIKED`. Skipped if you like your own post. |
| Someone comments on your post | Post author | 🔵 | `PostService.addComment` → `POST_COMMENTED`. Skipped for self-comments. |
| Someone replies to your comment | Original commenter | 🔵 | `COMMENT_REPLIED` — notifies the parent comment's author, not the post author, when the comment has a `parentCommentId`. Skipped for self-replies. |
| Someone @mentions you | Mentioned user | ⚪ | **Still doesn't exist as a feature.** `Post.tags` is a content-category label, not a user tag — this needs a data-model addition (mention parsing, a `taggedUser` relation) before it can be notified on. Not part of this pass. |

### Chat / Messaging

| Event | Recipient | Coverage | Notes |
|---|---|---|---|
| New message | Other participant | 🔵 | `ChatService.sendMessage` → `CHAT_MESSAGE`, message text is `"{sender name}: {first 100 chars}"`. |
| Message edited | Other participant | ⚪ | Deliberately left WebSocket-only — a persisted "X edited a message" notification is not useful, it would just clutter the notification list. |
| Message deleted | Other participant | ⚪ | Same reasoning as edits. |

### Pet Sitting Marketplace

All 11 events already had WebSocket pushes via a single shared helper (`notifyParticipant`); that helper now also persists, so every one of these got upgraded in one change.

| Event | Recipient | Coverage |
|---|---|---|
| New booking request | Sitter | 🔵 |
| Sitter accepts | Owner | 🔵 |
| Sitter rejects | Owner | 🔵 |
| Payment succeeds → booking confirmed | Owner & sitter | 🔵 |
| Payment fails → booking cancelled | Owner & sitter | 🔵 |
| Sitter starts the booking (ONGOING) | Owner | 🔵 |
| Booking cancelled | Other party | 🔵 |
| Dispute raised | Other party | 🔵 |
| Dispute resolved | Owner & sitter | 🔵 |
| Booking completed | Owner & sitter | 🔵 |
| Payout released to sitter | Sitter | 🔵 |

### Pet Sitter Onboarding

| Event | Recipient | Coverage | Notes |
|---|---|---|---|
| Sitter application status changes | Applicant | 🔵 | `PetSitterService.updateStatus` → `SITTER_STATUS_UPDATE`. One generic type since admins can set any valid status, not just approve/reject — message text carries the actual new status. |

### Adoptions

| Event | Recipient | Coverage | Notes |
|---|---|---|---|
| Application submitted | Adopter & admin | 🟡🔵 | Email unchanged; added `ADOPTION_SUBMITTED` in-app to both the adopter (`adoption.getUser()`) and `app.admin.email`. |
| Application approved | Adopter & admin | 🟡🔵 | Added `ADOPTION_APPROVED`. |
| Application rejected | Adopter & admin | 🟡🔵 | Added `ADOPTION_REJECTED`. |
| Adoption completed | Adopter & admin | 🟡🔵 | Added `ADOPTION_COMPLETED`. |
| Application cancelled | — | ⚪ | Still sends nothing, by existing design (unchanged). |

### Adoption Check-ins

| Event | Recipient | Coverage | Notes |
|---|---|---|---|
| Check-in submitted | Adopter | 🟡 | Deliberately left email-only — it's a self-triggered confirmation of the adopter's own action, an in-app notification for it adds no value (you don't need to be told about something you just did). |
| Health concern flagged (NEEDS_VET/CONCERNS) | Admin | 🟡🔵 | Added `CHECKIN_HEALTH_CONCERN` to `app.admin.email`. |
| Scheduled check-in reminder (daily, 9am) | Adopter | 🟡🔵 | Added `CHECKIN_REMINDER` — arguably the single most valuable addition in this pass, since it's a genuinely proactive nudge that now reaches the adopter even if they weren't looking. |
| Overdue check-in alert (72h+, daily 10am) | Admin | 🟡🔵 | Added `CHECKIN_OVERDUE` to `app.admin.email`. |

### Donations

| Event | Recipient | Coverage | Notes |
|---|---|---|---|
| Donation succeeds | Donor & admin | 🟡🔵 | Added `DONATION_SUCCESS` to the donor (`donation.getUser()`, skipped for anonymous/guest donations with no account) in `PaystackWebhookController.handleSuccess` — the confirmed live webhook path. Admin email unchanged. |
| Donation fails | Donor | 🟡🔵 | Added `DONATION_FAILED`, same guard for guest donations. Wired into the webhook's `handleFailure`, not the legacy `DonationService` path (which may or may not still run — out of scope here). |

### Rehoming

| Event | Recipient | Coverage | Notes |
|---|---|---|---|
| Someone applies for your rehomed pet | Pet owner | 🔵 | `PetRehomingService.applyForRehoming` → `REHOMING_APPLICATION_RECEIVED`. This was a real gap — owners previously had to manually check the applicants list. |
| Your application is approved | Applicant | 🔵 | `approveApplicant` → `REHOMING_APPLICATION_APPROVED`. |
| Your application is auto-rejected (another applicant chosen) | Other applicants | 🔵 | `REHOMING_APPLICATION_REJECTED`, sent to every other pending applicant in the same loop that auto-rejects them. |

### Foster Care

| Event | Recipient | Coverage | Notes |
|---|---|---|---|
| New foster application submitted | Admin | 🔵 | `FosterApplicationService.applyToFoster` → `FOSTER_APPLICATION_RECEIVED` to `app.admin.email`. |
| Application status changes | Applicant | 🔵 | `updateStatus` → `FOSTER_STATUS_UPDATE` (generic, same reasoning as sitter onboarding — admins can set APPROVED/ACTIVE/REJECTED). |
| Foster period marked complete | Applicant | 🔵 | `completeFoster` → `FOSTER_COMPLETED`. |

### Volunteer Program

| Event | Recipient | Coverage | Notes |
|---|---|---|---|
| Volunteer application status changes | Volunteer | 🔵 | `VolunteerService.updateStatus` → `VOLUNTEER_STATUS_UPDATE`. |
| New task assigned | Volunteer | 🔵 | `createTask` → `VOLUNTEER_TASK_ASSIGNED`. |
| Task marked complete | — | ⚪ | Still skipped — the volunteer is the one completing it, nothing to tell them. |

### Lost & Found

| Event | Recipient | Coverage | Notes |
|---|---|---|---|
| Report auto-expires (60 days) | Reporter | 🔵 | `LostFoundPetService.autoExpireReports` → `LOST_FOUND_EXPIRED`, added to the existing hourly sweep. |
| A matching report might be the same pet | Reporter | ⚪ | Still doesn't exist — this is a real feature to build (matching logic), not a notification-plumbing gap. Not part of this pass. |

### Auth

| Event | Recipient | Coverage | Notes |
|---|---|---|---|
| Signup OTP, welcome, resend OTP, password reset OTP | User | 🟡 | Unchanged, email-only — doesn't need an in-app equivalent. |

### Contact Form

| Event | Recipient | Coverage | Notes |
|---|---|---|---|
| Contact form submitted | Admin | 🟡 | Unchanged. Still goes to a hardcoded `khoswift@gmail.com` rather than the `app.admin.email` config every other admin alert uses — untouched in this pass since it's a normalization issue, not a coverage gap. |

## What's still missing

1. **Real mobile push (APNs/FCM).** Every event now reaches `GET /api/notifications` and, if the user is actively connected, a live WebSocket push — but nothing reaches a phone whose app is fully closed. That needs: a device-token registration table (new), a client-side SDK integration (mobile app work), and a delivery worker that reads new `Notification` rows and forwards them to APNs/FCM/OneSignal.
2. **@mentions/user-tagging.** Doesn't exist as a concept in the data model. If the literal "you got tagged" scenario from the original ask matters, this needs its own design pass (mention parsing, autocomplete, a `taggedUser` relation) before anything can notify on it.
3. **Lost & Found matching.** No mechanic exists to proactively suggest "this FOUND report might be your LOST pet" — a genuinely valuable feature, not just a missing notification hookup.
4. **Contact form admin address** still hardcoded rather than using `app.admin.email` — a small normalization, not a notification gap.
