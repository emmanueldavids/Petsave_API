# Pet Sitting Marketplace (with Earnings) Frontend Handoff

## Overview

Pet owners book trusted sitters from the platform. **This is an accept-first marketplace**: the owner submits a booking request, the sitter must explicitly accept it, and only then does payment happen — not the other way around. Sitters set their own day-rate and availability. Once accepted, payment is collected upfront via Paystack, held until the sitting period ends, then released to the sitter (real Paystack Transfer, using whatever Paystack key is configured — currently a **test key**, so transfers run against Paystack's sandbox, not real money). Both parties can review each other afterward.

There is no existing frontend code for this feature — this is a from-scratch integration, same situation as Volunteer Management.

Base URLs:
- `http://localhost:8080/api/sitters`
- `http://localhost:8080/api/my-pets`
- `http://localhost:8080/api/pet-sittings`

## Read this first: deliberate deviations from the original spec

1. **Pets are a new `OwnedPet` concept, not the existing shelter `Pet` entity.** The existing `Pet` model in this codebase is shelter/rescue inventory (adoption fee, rescue date, no owner) — it has no concept of "a user's own dog." A minimal `OwnedPet` entity was added instead (name, type, breed, notes) so a booking has something real to reference. You need `POST /api/my-pets` to register a pet before you can book a sitting for it — this endpoint isn't in the original spec table but is required to make `petId` in the booking request satisfiable.
2. **Sitter payout uses a real Paystack Transfer (test-mode), not just an internal ledger.** On booking completion, the platform attempts an actual Paystack Transfer to the sitter's bank account (via a cached Transfer Recipient), and only credits the internal wallet balance if that transfer actually succeeds. If the sitter has no bank details on file, or the transfer fails, the wallet is **not** credited and `paymentStatus` stays `PAID` (not `RELEASED`) — this is intentional; the code never claims a payout happened unless Paystack confirms it. See "Payout mechanics" below.
3. **Booking and payment are two separate steps, not one.** `POST /api/pet-sittings/book` no longer touches Paystack at all — it just creates a `REQUESTED` booking and waits for the sitter to respond. Payment only happens after the sitter accepts, via a separate `POST /api/pet-sittings/{id}/pay` call. If your frontend was built expecting `paymentAuthorizationUrl` back from the `book` call, that's changed — see Status Flow below.

Also: **there's no endpoint in the original spec to approve a sitter from `PENDING` to `ACTIVE`.** Without one, no sitter could ever appear in the public list. Two endpoints were added to fix this (same gap, same fix pattern as the Rehoming and Volunteer modules earlier): `GET /api/sitters/admin` and `PATCH /api/sitters/{id}/status`.

### ⚠️ Local development: the webhook cannot reach your machine

`callback_url` (which you can pass to `/pay`) only controls where the *browser* redirects after checkout — it has no effect on payment confirmation. The actual `paymentStatus`/`status` update happens via Paystack's **webhook**, which is a separate URL configured once in the Paystack dashboard, called server-to-server. Paystack's servers can never reach `localhost`, so **during local testing, completing a real checkout will not update the booking on its own** — the sitter's wallet won't move and there's nothing to `confirm`.

Two ways to unblock this locally:
- Call the new `POST /api/pet-sittings/{id}/verify-payment` right after the owner lands back on your callback page — it reconciles the booking against Paystack directly, independent of the webhook. This is the quick option and needs no extra setup.
- Or tunnel `localhost` (e.g. ngrok) and point the Paystack dashboard's webhook URL at the tunnel — closer to production behavior, but more setup for local iteration.

This isn't specific to this feature — it's true of the Donation flow too, which has the same webhook-only limitation.

## Authentication

- Public reads:
  - `GET /api/sitters` (active sitters only, filterable by `?city=`)
  - `GET /api/sitters/{id}` (sitter profile + reviews)
  - `GET /api/sitters/banks?currency=NGN` (Paystack bank list passthrough, for a bank-picker dropdown)
- Authenticated actions:
  - `POST /api/sitters` (register as sitter), `PUT /api/sitters/{id}` (update own profile/rates/bank details)
  - `GET /api/sitters/my`, `GET /api/sitters/my/wallet`
  - `POST /api/my-pets`, `GET /api/my-pets`, `PUT /api/my-pets/{id}`, `DELETE /api/my-pets/{id}`
  - `POST /api/pet-sittings/book`, `GET /api/pet-sittings`, `GET /api/pet-sittings/{id}`
  - `PATCH /api/pet-sittings/{id}/accept` (sitter only), `PATCH /api/pet-sittings/{id}/reject` (sitter only)
  - `POST /api/pet-sittings/{id}/pay` (owner only, only after the sitter accepts)
  - `POST /api/pet-sittings/{id}/verify-payment` (owner, sitter, or admin — manual payment reconciliation, see below)
  - `PATCH /api/pet-sittings/{id}/confirm` (sitter only), `PATCH /api/pet-sittings/{id}/cancel` (owner or sitter)
  - `PATCH /api/pet-sittings/{id}/complete` (owner, only while `ONGOING` — see Status Flow; also admin, see below)
  - `PATCH /api/pet-sittings/{id}/notes` (owner or sitter), `PATCH /api/pet-sittings/{id}/dispute` (owner or sitter)
  - `POST /api/pet-sittings/{id}/review`
- Admin-only:
  - `GET /api/sitters/admin` (review queue), `PATCH /api/sitters/{id}/status` (approve/suspend)
  - `PATCH /api/pet-sittings/{id}/complete` also accepts an admin, force-completing regardless of status/timing (same endpoint as above — see Status Flow for the exact permission split)
  - `PATCH /api/pet-sittings/{id}/resolve-dispute`

## Main Data Shapes

### PetSitterResponse

```json
{
  "id": 1,
  "user": { "id": 5, "name": "Second User", "email": "seconduser1@example.com" },
  "bio": "Experienced dog sitter",
  "serviceRadius": 10,
  "location": "East Legon",
  "city": "Accra",
  "acceptedPetTypes": ["DOG", "CAT"],
  "ratePerDay": 50.00,
  "ratePerNight": null,
  "maxPetsAtOnce": 2,
  "status": "ACTIVE",
  "averageRating": 5.0,
  "totalReviews": 1,
  "createdAt": "2026-07-12 21:24:18",
  "updatedAt": "2026-07-12 21:24:18",
  "reviews": [ { "...": "PetSittingReviewResponse, see below" } ]
}
```

- `acceptedPetTypes`: array of `DOG | CAT | BIRD | RABBIT | OTHER`.
- **`bankCode`/`accountNumber`/`accountName`/`payoutDetailsOnFile` are only present when the requester is the sitter themselves or an admin** — omitted entirely (not `null`) on the public list/detail view. Same privacy pattern as the Volunteer module's `emergencyContact`.
- `reviews` is only populated by `GET /api/sitters/{id}` (single sitter detail) — the list endpoint (`GET /api/sitters`) always omits it, to keep that payload light.
- `ratePerNight` exists on the entity/DTO but **isn't used in the booking price calculation** — see "Pricing" below.

### OwnedPetResponse

```json
{ "id": 1, "name": "Rex", "petType": "DOG", "breed": "Labrador", "notes": null, "createdAt": "2026-07-12 21:28:17" }
```

### PetSittingResponse

```json
{
  "id": 1,
  "owner": { "id": 4, "name": "Existing Owner", "email": "existingowner1@example.com" },
  "sitter": { "id": 5, "name": "Second User", "email": "seconduser1@example.com" },
  "pet": { "id": 1, "name": "Rex", "petType": "DOG", "breed": "Labrador" },
  "startDate": "2026-07-15 09:00:00",
  "endDate": "2026-07-17 09:00:00",
  "totalAmount": 100.00,
  "status": "CONFIRMED",
  "paymentReference": "PETSIT_9c9283a445fb",
  "paymentStatus": "PAID",
  "ownerNotes": "Rex needs his meds twice daily",
  "sitterNotes": null,
  "paymentAuthorizationUrl": "https://checkout.paystack.com/uekjwwrle5ko8yx",
  "createdAt": "2026-07-12 21:30:49",
  "updatedAt": "2026-07-12 21:30:49"
}
```

- `paymentAuthorizationUrl` is **only present in the response from `POST /api/pet-sittings/{id}/pay`** (not from `book` anymore) — redirect the owner there to pay. It's not stored/returned again on subsequent fetches of the same booking.
- `paymentReference` is `null` until the owner actually calls `/pay` — it's generated at pay time now, not at booking time.
- `ownerNotes`/`sitterNotes` are each settable via `PATCH /api/pet-sittings/{id}/notes` — see API Contract below.
- `disputeReason` is only non-null once `status = DISPUTED` (or was, historically — it isn't cleared on resolution). Format: `"Raised by owner: ..."` or `"Raised by sitter: ..."`.

### PetSittingReviewResponse

```json
{
  "id": 1,
  "bookingId": 1,
  "reviewer": { "id": 4, "name": "Existing Owner", "email": "existingowner1@example.com" },
  "reviewee": { "id": 5, "name": "Second User", "email": "seconduser1@example.com" },
  "rating": 5,
  "comment": "Great with Rex!",
  "createdAt": "2026-07-12 21:36:21"
}
```

## Status Flow (read carefully — this is an accept-first flow, changed from earlier versions of this doc)

The original spec had a `PATCH /confirm` endpoint ("Sitter confirms booking") sitting oddly alongside a payment flow that paid the sitter *before* they'd agreed to anything. That's been restructured into a proper accept-first marketplace: the owner requests, the sitter accepts or rejects, and only after acceptance does money move.

1. **`REQUESTED`** — booking created via `POST /api/pet-sittings/book`. **No Paystack call happens at this point at all** — no `paymentReference`, no `paymentAuthorizationUrl`. The sitter gets a `BOOKING_REQUESTED` WebSocket notification.
2. **`ACCEPTED`** — the sitter calls `PATCH /api/pet-sittings/{id}/accept`. Only callable while `status = REQUESTED`; only the sitter. The owner gets notified (`BOOKING_ACCEPTED`) and can now proceed to pay.
   - Alternatively, the sitter can decline via `PATCH /api/pet-sittings/{id}/reject` → `status = REJECTED`, a terminal state. No money was ever involved, so there's nothing to refund. The owner gets notified (`BOOKING_REJECTED`).
3. **Payment** — the **owner** calls `POST /api/pet-sittings/{id}/pay` (optional body: `{"callbackUrl": "..."}`). Only callable while `status = ACCEPTED` — `400` "This booking must be accepted by the sitter before payment" otherwise. This is what generates `paymentReference` and calls Paystack's initialize endpoint, returning `paymentAuthorizationUrl` — redirect the owner there. `status` stays `ACCEPTED` at this point; nothing changes until the webhook fires.
4. **`CONFIRMED`** — the Paystack webhook fired `charge.success` for this booking's reference; `paymentStatus` flips to `PAID` and `status` flips to `CONFIRMED` automatically. No user action needed for this transition. (If the charge fails instead, `paymentStatus → FAILED` and `status → CANCELLED` automatically.)
5. **`ONGOING`** — the **sitter** calls `PATCH /api/pet-sittings/{id}/confirm`. This is what the original spec's "sitter confirms" meant, reinterpreted for this flow: the sitter is acknowledging they've taken the pet in / the sitting has actually started. Only callable while `status = CONFIRMED`; only the sitter (not the owner) can call it. (Yes, this means the sitter has two separate actions in this flow — `accept` early on to agree to the booking, and `confirm` later to mark it as actually underway once paid.)
6. **`COMPLETED`** — three ways to get here, via the same `PATCH /api/pet-sittings/{id}/complete` endpoint:
   - **The owner**, once `status = ONGOING` — they're the one who actually knows when they've picked their pet back up. `400` "You can only mark a booking complete once the sitter has confirmed and started it" if called too early.
   - **An admin**, at any time except a booking that's already `COMPLETED`/`CANCELLED`/`REJECTED` — a force-complete override, regardless of status/timing.
   - **Automatically**, via an hourly background sweep, for any `CONFIRMED` or `ONGOING` booking whose `endDate` was 24+ hours ago (covers the case where the owner never bothers to click "complete").
   - **The sitter cannot call this** — `400` "Only the owner can mark this booking complete" — letting them self-complete would mean they could trigger their own payout unilaterally.
   - Completion (however it's triggered) is also when payout is attempted (see below).
7. **`CANCELLED`** — via `PATCH /api/pet-sittings/{id}/cancel`, by either the owner or the sitter, only while `status` is `REQUESTED`, `ACCEPTED`, or `CONFIRMED` (not after the sitter has started it or it's completed). The cancellation window now extends through `ACCEPTED` — a booking can sit there unpaid for a while, and either side should be able to back out before money moves. If `paymentStatus = PAID` (i.e. cancelling a `CONFIRMED` booking), cancellation attempts a **real Paystack refund** automatically (see Payment status below); cancelling from `REQUESTED`/`ACCEPTED` never involves money since payment hasn't happened yet.
8. **`REJECTED`** — terminal, reached only via step 2 above. Deliberately distinct from `CANCELLED` for reporting purposes: a sitter declining a request is a different signal than either party backing out of an agreed, possibly-paid booking. `REJECTED` bookings don't count toward a sitter's capacity or a pet's overlap checks (see Bookings section below).
9. **`DISPUTED`** — either the owner or the sitter can raise one via `PATCH /api/pet-sittings/{id}/dispute` (body: `{"reason": "..."}`, optional), only while `status` is `ONGOING` or `COMPLETED` (nothing to dispute before the sitting has actually started). An admin resolves it via `PATCH /api/pet-sittings/{id}/resolve-dispute` (body: `{"resolution": "RELEASE_TO_SITTER"}` or `{"resolution": "REFUND_OWNER"}`) — see API Contract below for the full behavior of each resolution.

### Payment status (`paymentStatus`) values

`PENDING` → `PAID` (webhook) → `RELEASED` (payout succeeded) or stays `PAID` (payout couldn't be completed) — or `FAILED`/booking auto-cancelled if the Paystack charge itself failed. From `PAID`, a cancellation or a `REFUND_OWNER` dispute resolution can move it to `REFUNDED` — but only if Paystack actually confirms the refund.

**Refunds are real but honest about failure.** Cancelling a `PAID` booking (or resolving a dispute as `REFUND_OWNER`) triggers an actual Paystack refund call. If Paystack confirms it, `paymentStatus` → `REFUNDED`. If the refund call fails for any reason, `paymentStatus` **stays `PAID`** and the booking/dispute still transitions (`CANCELLED`) — the code never claims a refund happened unless Paystack confirms it, same honesty guarantee as the payout side. A `CANCELLED` + `PAID` combination is your signal that a manual refund is still owed.

**Once `paymentStatus = RELEASED`, no refund is possible at all** — the money has already left the platform for the sitter's bank account, and Paystack transfers can't be auto-reversed the way charges can. `resolve-dispute` with `REFUND_OWNER` on an already-`RELEASED` booking returns `400` "Cannot auto-refund — this booking's payment was already released to the sitter; resolve manually outside the system" rather than silently failing or lying about it. Build your dispute UI with this in mind: encourage disputes to be raised before the 24h auto-completion sweep releases funds, if refund-ability matters to your flow.

## Pricing

`totalAmount = ceil(days between startDate and endDate) × sitter.ratePerDay`. `ratePerNight` is stored but not currently used in this calculation — the spec's own flow diagram only referenced `ratePerDay`, so that's what's implemented. If you need night-based pricing, that's a follow-up.

## Payout mechanics (test-mode Paystack Transfer)

On completion, the platform:
1. Ensures the sitter has a Paystack Transfer Recipient (creates one via `bankCode`/`accountNumber`/`accountName` on file, caches the `recipient_code`).
2. Attempts a real Paystack Transfer for `totalAmount × (1 − platform fee%)` (fee defaults to **15%**, configurable server-side, not spec'd — confirm the real number).
3. **Only if that transfer succeeds**: `paymentStatus` → `RELEASED`, and `GET /api/sitters/my/wallet` balance increases by the net amount.
4. **If it fails** (no bank details on file, invalid account, Paystack API error): `paymentStatus` stays `PAID`, nothing is credited, and it's logged server-side as needing manual attention. The booking's own status still becomes `COMPLETED` either way — only the money-movement half can be "stuck."

This was tested against Paystack's real test API (`sk_test_...` key) end-to-end: recipient creation, transfer, and the whole webhook signature verification loop are live code paths, not mocked. In testing, a fabricated bank account number correctly failed Paystack's real-time account validation and the system correctly did **not** fake a release — that's the intended behavior, not a bug. If you test this yourselves, you'll need genuine Paystack test-mode bank account fixtures (check Paystack's docs/dashboard) for the transfer to actually succeed end-to-end; a made-up account number will safely fail exactly like it did in our test.

## API Contract

### Sitters

**`GET /api/sitters?city=Accra`** — public, active sitters only (optionally filtered by city, case-insensitive exact match).

**`GET /api/sitters/{id}`** — public, includes `reviews`. Not filtered by status (a `PENDING` sitter's profile still resolves directly).

**`GET /api/sitters/banks?currency=NGN`** — public passthrough to Paystack's bank list (also accepts `GHS`, etc.) — use this to populate a bank picker without exposing the Paystack secret key to the frontend.

**`POST /api/sitters`** — register:
```json
{
  "bio": "...", "serviceRadius": 10, "location": "East Legon", "city": "Accra",
  "acceptedPetTypes": ["DOG", "CAT"], "ratePerDay": 50.00, "ratePerNight": null,
  "maxPetsAtOnce": 2, "bankCode": "058", "accountNumber": "0000000000", "accountName": "..."
}
```
`acceptedPetTypes` and `ratePerDay` are required; everything else optional (bank fields can be added later via `PUT`). One sitter profile per user — `400` with `"You have already registered as a pet sitter"` on a second attempt. Starts at `status: "PENDING"`.

**`PUT /api/sitters/{id}`** — same body, replaces the profile. Only the sitter or an admin. **Changing bank details invalidates the cached Paystack recipient** (it's recreated on the next payout attempt automatically).

**`GET /api/sitters/my`** — `400` "You have not registered as a pet sitter yet" if none.

**`GET /api/sitters/my/wallet`** → `{ "balance": 0.00, "updatedAt": "..." }`.

**`GET /api/sitters/admin?status=PENDING`** — admin-only review queue, same convention as the rehoming/volunteer admin queues: omit `status` for `PENDING` (default), pass `ALL` for everything, or a specific status. `400` "Only admins can view the sitter review queue" otherwise.

**`PATCH /api/sitters/{id}/status`** — `{ "status": "ACTIVE" }`, admin-only. Valid values: `PENDING | ACTIVE | SUSPENDED`.

### My Pets

**`GET /api/my-pets`** — current user's registered pets.

**`POST /api/my-pets`** — `{ "name": "Rex", "petType": "DOG", "breed": "Labrador", "notes": null }`. `petType` must be one of the same 5 values as `acceptedPetTypes`.

**`PUT /api/my-pets/{id}`** — same body shape as create, replaces the pet's details. Only the pet's owner — `400` "You can only update your own pet" otherwise.

**`DELETE /api/my-pets/{id}`** — only the pet's owner (`400` "You can only delete your own pet" otherwise). **Blocked if the pet has any sitting booking on record, in any status** — `400` "This pet has sitting bookings on record and cannot be deleted". This isn't a soft restriction: `PetSitting.pet_id` is a real foreign key, so a pet with booking history genuinely can't be deleted without breaking that history. If you need a "remove this pet from my active list" UX for a pet with past bookings, that has to be a client-side hide, not a real delete — there's no archive/soft-delete flag on `OwnedPet` today.

### Bookings

**`POST /api/pet-sittings/book`**
```json
{
  "sitterId": 1,
  "petId": 1,
  "startDate": "2026-07-15T09:00:00",
  "endDate": "2026-07-17T09:00:00",
  "ownerNotes": "Rex needs his meds twice daily"
}
```
- **`sitterId` is the `PetSitter` profile id** (`PetSitterResponse.id`), not the sitter's user id — same convention established for `volunteerId` in the Volunteer module.
- `petId` must be a pet you registered via `POST /api/my-pets`.
- The sitter must be `ACTIVE`, and you can't book yourself.
- **`startDate` cannot be in the past** — `400` "startDate cannot be in the past".
- **The sitter's capacity is enforced.** A booking is rejected with `400` "This sitter is already fully booked for the requested dates" if the sitter already has `maxPetsAtOnce` (or more) `ACCEPTED`, `CONFIRMED`, or `ONGOING` bookings whose date ranges overlap the requested one. `REQUESTED` (not yet accepted) and `REJECTED` bookings don't count against capacity.
- **The same pet can't be double-booked either.** Same rule, same statuses (`ACCEPTED`/`CONFIRMED`/`ONGOING`) — if the pet already has one of those with a *different* sitter for an overlapping date range, this returns `400` "This pet already has a confirmed sitting booked for overlapping dates".
- **No `callbackUrl` here anymore** — that moved to `/pay` (see below), since payment no longer happens at booking time.
- **Response no longer includes `paymentAuthorizationUrl`** — this call only creates a `REQUESTED` booking. Nothing to redirect to yet; wait for the sitter to accept.

**`GET /api/pet-sittings`** — all bookings where you're the owner OR the sitter, newest first.

**`GET /api/pet-sittings/{id}`** — only a participant (owner/sitter) or admin can view. `400` "You are not a participant in this booking" otherwise.

**`PATCH /api/pet-sittings/{id}/accept`** — sitter only, only from `REQUESTED` → `ACCEPTED`. `400` "Only the sitter can accept this booking" / "This booking is not awaiting acceptance" otherwise. Notifies the owner (`BOOKING_ACCEPTED`) that they can now pay.

**`PATCH /api/pet-sittings/{id}/reject`** — sitter only, only from `REQUESTED` → `REJECTED`. Same error messages pattern as `accept`. Terminal — no further action possible on this booking. Notifies the owner (`BOOKING_REJECTED`).

**`POST /api/pet-sittings/{id}/pay`**
```json
{ "callbackUrl": null }
```
- Owner only, only from `status = ACCEPTED` — `400` "Only the owner can pay for this booking" / "This booking must be accepted by the sitter before payment" otherwise.
- Body is optional (send `{}` or omit entirely) — `callbackUrl` is optional within it; Paystack redirects there after checkout, defaulting to `{frontendUrl}/pet-sittings/callback?ref={reference}` if omitted.
- This is what generates `paymentReference` and calls Paystack — response includes `paymentAuthorizationUrl`, redirect the owner there immediately. `status` stays `ACCEPTED` until the webhook confirms payment (see Status Flow above).
- Can be called again if the owner abandons checkout the first time (still `ACCEPTED`, no state change) — it'll generate a fresh reference and authorization URL each time.

**`POST /api/pet-sittings/{id}/verify-payment`**
- Owner, sitter, or admin. `400` "You are not a participant in this booking" otherwise. `400` "This booking has no payment to verify yet" if `/pay` was never called (`paymentReference` is still `null`).
- **Manually reconciles payment status against Paystack directly** instead of waiting for the webhook — call this from your payment-callback page (the page the owner lands on after `callbackUrl` redirects them back from checkout) so the booking updates immediately rather than depending on the webhook's timing.
- **This is the important one for local development.** Paystack's webhook is a separate, dashboard-configured URL that Paystack's servers call directly — they can never reach `localhost`, no matter what `callbackUrl` you pass. Without hitting this endpoint (or tunneling `localhost` with something like ngrok and pointing the Paystack dashboard's webhook at it), a booking paid for during local testing will sit at `ACCEPTED`/`PENDING` forever: the sitter's wallet won't update and there's nothing to `confirm` — call `verify-payment` right after checkout to unblock local testing.
- Shares the exact same success/failure logic as the webhook (refactored into one place specifically so the two paths can't drift apart) — if Paystack reports the charge as `success`, this does exactly what the webhook does: `paymentStatus → PAID`, `status → CONFIRMED`, both parties notified.
- If Paystack reports `failed` or `reversed`, this cancels the booking the same way a failed webhook would.
- If Paystack reports `abandoned` (checkout page visited but not completed yet) or `pending`/`processing`, **nothing changes** — the booking is left exactly as-is, since the owner may still come back and complete the same checkout session. Don't assume "abandoned" means "give up."
- Idempotent and safe to call repeatedly — a no-op if the booking's already past `CONFIRMED`/`CANCELLED`.

**`PATCH /api/pet-sittings/{id}/confirm`** — sitter only, only from `CONFIRMED` → `ONGOING` (see Status Flow above).

**`PATCH /api/pet-sittings/{id}/cancel`** — owner or sitter (or admin), only from `REQUESTED`, `ACCEPTED`, or `CONFIRMED`.

**`PATCH /api/pet-sittings/{id}/complete`** — callable by the **owner** (only while `status = ONGOING`) or by an **admin** (any time except a booking that's already `COMPLETED`/`CANCELLED`/`REJECTED`). The sitter cannot call this. Triggers the same payout attempt as the automatic 24h sweep. See Status Flow above for the full permission breakdown and error messages.

**`POST /api/pet-sittings/{id}/review`**
```json
{ "rating": 5, "comment": "Great with Rex!" }
```
- Only while `status = COMPLETED`. Only a participant. One review per (booking, reviewer) — `400` "You have already reviewed this booking" on a second attempt.
- The reviewee is inferred automatically (the *other* party in the booking) — you don't specify who you're reviewing.
- If the owner reviews the sitter, the sitter's `averageRating`/`totalReviews` update immediately. If the sitter reviews the owner, the review is stored but there's no "owner profile" to roll it up into (owners don't have a public rating).

**`PATCH /api/pet-sittings/{id}/notes`**
```json
{ "notes": "Feed twice daily, allergic to chicken" }
```
- Owner or sitter only. Whichever one calls it updates *their own* notes field (`ownerNotes` for the owner, `sitterNotes` for the sitter) — there's a single `notes` field in the request; which column it lands in depends on who's calling. `400` "Only the owner or sitter can update notes on this booking" for anyone else.
- No status restriction — notes can be added/edited at any point in the booking's lifecycle.

**`PATCH /api/pet-sittings/{id}/dispute`**
```json
{ "reason": "Sitter never showed up" }
```
- Owner or sitter only (not admin — admin doesn't need to "raise" one, they resolve). Only while `status` is `ONGOING` or `COMPLETED`. `reason` is optional (stored as `"(no reason given)"` if omitted).
- Sets `status: "DISPUTED"` and `disputeReason: "Raised by owner: ..."` or `"Raised by sitter: ..."`.
- Notifies the *other* participant via WebSocket (`BOOKING_DISPUTED`).

**`PATCH /api/pet-sittings/{id}/resolve-dispute`**
```json
{ "resolution": "RELEASE_TO_SITTER" }
```
or
```json
{ "resolution": "REFUND_OWNER" }
```
- Admin-only — `400` "Only admins can resolve a dispute" otherwise. Only while `status = DISPUTED` — `400` "This booking is not currently disputed" otherwise (including if you try to resolve the same dispute twice).
- `RELEASE_TO_SITTER`: resolves in the sitter's favor — completes the booking exactly like normal completion (`status → COMPLETED`, real payout attempted, same success/failure honesty rules as the regular completion flow).
- `REFUND_OWNER`: resolves in the owner's favor — `status → CANCELLED`, real Paystack refund attempted if `paymentStatus = PAID` (see Payment status above for the honesty guarantee). **Blocked entirely with `400`** if `paymentStatus` is already `RELEASED` — that money is already gone from the platform, so this needs manual/support handling instead.
- Invalid `resolution` values get `400` "Invalid resolution: {value} (expected RELEASE_TO_SITTER or REFUND_OWNER)".

## Real-Time Notifications

Both participants get pushed WebSocket events (same `/ws?token=` connection used by Chat and Volunteers) at these points: `BOOKING_REQUESTED` (pushed to the **sitter** when an owner books them), `BOOKING_ACCEPTED` (pushed to the **owner** when the sitter accepts, prompting them to pay), `BOOKING_REJECTED` (pushed to the **owner** when the sitter declines), `BOOKING_CONFIRMED` (payment succeeded), `BOOKING_PAYMENT_FAILED` (payment failed, booking auto-cancelled), `BOOKING_ONGOING` (sitter confirmed/started), `BOOKING_CANCELLED`, `BOOKING_COMPLETED`, `BOOKING_DISPUTED` (pushed to the *other* participant when one raises a dispute). Each payload: `{ "type": "...", "message": "...", "bookingId": 1, "timestamp": ... }`. These are delivery-only — always trust a fresh `GET` for the source of truth.

**Dispute resolution notifications depend on which way it's resolved** — this is a subtlety worth getting right in your UI:
- `resolve-dispute` with `RELEASE_TO_SITTER` reuses the normal completion path, so both parties get `BOOKING_COMPLETED` (**not** a distinct "dispute resolved" event).
- `resolve-dispute` with `REFUND_OWNER` sends `DISPUTE_RESOLVED` to both parties instead.
- If you want a single "your dispute was resolved" banner regardless of outcome, listen for **both** `BOOKING_COMPLETED` and `DISPUTE_RESOLVED`, and check `status`/`paymentStatus` on the refetched booking to know which way it went.

## Error Handling

Same convention as the rest of the API: `{"status":"error","message":"..."}`, `400` for ownership/role/state violations (not `403`) — check `message` for specifics. Truly unauthenticated requests get a plain `403`.

**Also worth knowing:** a pre-existing bug in this backend's global exception handling was found and fixed while building this feature — `@Valid` field-validation failures (e.g. a rating outside 1–5) used to return a generic `{"message":"An unexpected error occurred. Please try again.","status":"error"}` with HTTP `500`, across the **entire API**, not just this module. They now correctly return `400` with a structured body:
```json
{ "error": "Validation failed", "message": "Input validation failed", "errors": ["rating must be between 1 and 5"], "status": 400, "timestamp": "..." }
```
Note this shape (`errors` array, `status` as a number) is slightly different from the usual `{"status":"error","message":"..."}` convention used elsewhere — handle both shapes when parsing error responses.

Common messages: `"Sitter not found with id: {id}"`, `"This sitter is not currently accepting bookings"`, `"You cannot book yourself as a sitter"`, `"Pet not found with id: {id}"`, `"You can only book sitting for your own pet"`, `"You can only update your own pet"`, `"You can only delete your own pet"`, `"This pet has sitting bookings on record and cannot be deleted"`, `"endDate must be after startDate"`, `"startDate cannot be in the past"`, `"This sitter is already fully booked for the requested dates"`, `"This pet already has a confirmed sitting booked for overlapping dates"`, `"Only the sitter can accept this booking"`, `"Only the sitter can reject this booking"`, `"This booking is not awaiting acceptance"` (accept/reject called outside `REQUESTED`), `"Only the owner can pay for this booking"`, `"This booking must be accepted by the sitter before payment"`, `"This booking has no payment to verify yet"`, `"Only the sitter can confirm this booking"`, `"Only the owner or sitter can cancel this booking"`, `"Only the owner can mark this booking complete"` (sitter/anyone else tries), `"You can only mark a booking complete once the sitter has confirmed and started it"` (owner tries too early), `"Only the owner or sitter can update notes on this booking"`, `"Only the owner or sitter of this booking can raise a dispute"`, `"A dispute can only be raised once the sitting has started or completed — current status: {status}"`, `"Only admins can resolve a dispute"`, `"This booking is not currently disputed"`, `"Cannot auto-refund — this booking's payment was already released to the sitter; resolve manually outside the system"`, `"You can only review a completed booking"`, `"You have already reviewed this booking"`.

## Known Gaps (not implemented)

- **No timeout on `ACCEPTED`-but-unpaid bookings.** Once a sitter accepts, the booking can sit there indefinitely if the owner never calls `/pay` — it occupies the sitter's capacity and blocks the pet from being double-booked the whole time, with no auto-expiry. Consider building a "pending payment, expires in X" UI affordance, or ask for a backend follow-up (e.g. auto-cancel `ACCEPTED` bookings unpaid after N hours) if this becomes a real problem.
- **Refund/payout failures require manual follow-up.** Both are real Paystack API calls now, but if either fails (invalid account, no bank details, Paystack error), the booking still transitions (`CANCELLED`/`COMPLETED`) while `paymentStatus` stays `PAID` — that combination is your signal to build an admin view for manual resolution. This was tested against Paystack's real test API; both the transfer and refund paths correctly declined to fake success on invalid test data.
- **Disputes can't be auto-refunded once payment is `RELEASED`** — by design, not a bug (see Payment status section above). No in-app path exists for that scenario beyond "resolve manually outside the system."
- No pagination on any list endpoint.
- Platform fee (15%) and badge-style thresholds are placeholders — confirm the real number before launch.
- `ratePerNight` is captured but not used in pricing (day-count only).
