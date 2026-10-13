# Pet Sitting Marketplace (with Earnings) Frontend Handoff

## Overview

Pet owners book trusted sitters from the platform. Sitters set their own day-rate and availability. Payment is collected upfront via Paystack, held until the sitting period ends, then released to the sitter (real Paystack Transfer, using whatever Paystack key is configured — currently a **test key**, so transfers run against Paystack's sandbox, not real money). Both parties can review each other afterward.

There is no existing frontend code for this feature — this is a from-scratch integration, same situation as Volunteer Management.

Base URLs:
- `http://localhost:8080/api/sitters`
- `http://localhost:8080/api/my-pets`
- `http://localhost:8080/api/pet-sittings`

## Read this first: two deliberate deviations from the original spec

1. **Pets are a new `OwnedPet` concept, not the existing shelter `Pet` entity.** The existing `Pet` model in this codebase is shelter/rescue inventory (adoption fee, rescue date, no owner) — it has no concept of "a user's own dog." A minimal `OwnedPet` entity was added instead (name, type, breed, notes) so a booking has something real to reference. You need `POST /api/my-pets` to register a pet before you can book a sitting for it — this endpoint isn't in the original spec table but is required to make `petId` in the booking request satisfiable.
2. **Sitter payout uses a real Paystack Transfer (test-mode), not just an internal ledger.** On booking completion, the platform attempts an actual Paystack Transfer to the sitter's bank account (via a cached Transfer Recipient), and only credits the internal wallet balance if that transfer actually succeeds. If the sitter has no bank details on file, or the transfer fails, the wallet is **not** credited and `paymentStatus` stays `PAID` (not `RELEASED`) — this is intentional; the code never claims a payout happened unless Paystack confirms it. See "Payout mechanics" below.

Also: **there's no endpoint in the original spec to approve a sitter from `PENDING` to `ACTIVE`.** Without one, no sitter could ever appear in the public list. Two endpoints were added to fix this (same gap, same fix pattern as the Rehoming and Volunteer modules earlier): `GET /api/sitters/admin` and `PATCH /api/sitters/{id}/status`.

## Authentication

- Public reads:
  - `GET /api/sitters` (active sitters only, filterable by `?city=`)
  - `GET /api/sitters/{id}` (sitter profile + reviews)
  - `GET /api/sitters/banks?currency=NGN` (Paystack bank list passthrough, for a bank-picker dropdown)
- Authenticated actions:
  - `POST /api/sitters` (register as sitter), `PUT /api/sitters/{id}` (update own profile/rates/bank details)
  - `GET /api/sitters/my`, `GET /api/sitters/my/wallet`
  - `POST /api/my-pets`, `GET /api/my-pets`
  - `POST /api/pet-sittings/book`, `GET /api/pet-sittings`, `GET /api/pet-sittings/{id}`
  - `PATCH /api/pet-sittings/{id}/confirm` (sitter only), `PATCH /api/pet-sittings/{id}/cancel` (owner or sitter)
  - `POST /api/pet-sittings/{id}/review`
- Admin-only:
  - `GET /api/sitters/admin` (review queue), `PATCH /api/sitters/{id}/status` (approve/suspend)
  - `PATCH /api/pet-sittings/{id}/complete` (force-complete)

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

- `paymentAuthorizationUrl` is **only present in the response from `POST /api/pet-sittings/book`** — redirect the owner there to pay. It's not stored/returned again on subsequent fetches of the same booking.
- `sitterNotes` exists on the entity but there's no endpoint that sets it yet (see Known Gaps).

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

## Status Flow (read carefully — this reconciles an ambiguity in the original spec)

The spec listed a `PATCH /confirm` endpoint ("Sitter confirms booking") separately from the payment webhook flow, without saying exactly what each transition does. Here's the reconciliation this implementation uses:

1. **`REQUESTED`** — booking created via `POST /api/pet-sittings/book`, Paystack payment initialized, not yet paid.
2. **`CONFIRMED`** — the Paystack webhook fired `charge.success`; `paymentStatus` flips to `PAID` automatically. No user action needed for this transition.
3. **`ONGOING`** — the **sitter** calls `PATCH /api/pet-sittings/{id}/confirm`. This is what "sitter confirms" means here: the sitter is acknowledging they've taken the pet in / the sitting has actually started. Only callable while `status = CONFIRMED`; only the sitter (not the owner) can call it.
4. **`COMPLETED`** — either an hourly background job auto-completes any `CONFIRMED` or `ONGOING` booking whose `endDate` was 24+ hours ago (whichever state it's stuck in, in case the sitter forgot to confirm), or an admin force-completes via `PATCH /api/pet-sittings/{id}/complete` at any time. Completion is also when payout is attempted (see below).
5. **`CANCELLED`** — via `PATCH /api/pet-sittings/{id}/cancel`, by either the owner or the sitter, only while `status` is `REQUESTED` or `CONFIRMED` (not after the sitter has started it or it's completed).
6. **`DISPUTED`** — defined in the status enum but **no endpoint reaches this state yet**. Reserved for a future dispute-resolution flow.

### Payment status (`paymentStatus`) values

`PENDING` → `PAID` (webhook) → `RELEASED` (payout succeeded) or stays `PAID` (payout couldn't be completed) — or `FAILED`/booking auto-cancelled if the Paystack charge itself failed.

**Cancellation does not auto-refund.** If a booking is cancelled while `paymentStatus = PAID`, it stays `PAID` after cancellation — there's no Paystack refund API call wired up. A `CANCELLED` + `PAID` combination is your signal that a manual refund is owed; build an admin view for this if you need one.

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

### Bookings

**`POST /api/pet-sittings/book`**
```json
{
  "sitterId": 1,
  "petId": 1,
  "startDate": "2026-07-15T09:00:00",
  "endDate": "2026-07-17T09:00:00",
  "ownerNotes": "Rex needs his meds twice daily",
  "callbackUrl": null
}
```
- **`sitterId` is the `PetSitter` profile id** (`PetSitterResponse.id`), not the sitter's user id — same convention established for `volunteerId` in the Volunteer module.
- `petId` must be a pet you registered via `POST /api/my-pets`.
- The sitter must be `ACTIVE`, and you can't book yourself.
- `callbackUrl` is optional — Paystack redirects there after checkout; defaults to `{frontendUrl}/pet-sittings/callback?ref={reference}` if omitted.
- Response includes `paymentAuthorizationUrl` — redirect the owner there immediately to complete payment.

**`GET /api/pet-sittings`** — all bookings where you're the owner OR the sitter, newest first.

**`GET /api/pet-sittings/{id}`** — only a participant (owner/sitter) or admin can view. `400` "You are not a participant in this booking" otherwise.

**`PATCH /api/pet-sittings/{id}/confirm`** — sitter only, only from `CONFIRMED` → `ONGOING` (see Status Flow above).

**`PATCH /api/pet-sittings/{id}/cancel`** — owner or sitter (or admin), only from `REQUESTED` or `CONFIRMED`.

**`PATCH /api/pet-sittings/{id}/complete`** — admin-only, force-completes regardless of timing (triggers the same payout attempt as the automatic 24h sweep).

**`POST /api/pet-sittings/{id}/review`**
```json
{ "rating": 5, "comment": "Great with Rex!" }
```
- Only while `status = COMPLETED`. Only a participant. One review per (booking, reviewer) — `400` "You have already reviewed this booking" on a second attempt.
- The reviewee is inferred automatically (the *other* party in the booking) — you don't specify who you're reviewing.
- If the owner reviews the sitter, the sitter's `averageRating`/`totalReviews` update immediately. If the sitter reviews the owner, the review is stored but there's no "owner profile" to roll it up into (owners don't have a public rating).

## Real-Time Notifications

Both participants get pushed WebSocket events (same `/ws?token=` connection used by Chat and Volunteers) at these points: `BOOKING_CONFIRMED` (payment succeeded), `BOOKING_PAYMENT_FAILED` (payment failed, booking auto-cancelled), `BOOKING_ONGOING` (sitter confirmed/started), `BOOKING_CANCELLED`, `BOOKING_COMPLETED`. Each payload: `{ "type": "...", "message": "...", "bookingId": 1, "timestamp": ... }`. These are delivery-only — always trust a fresh `GET` for the source of truth.

## Error Handling

Same convention as the rest of the API: `{"status":"error","message":"..."}`, `400` for ownership/role/state violations (not `403`) — check `message` for specifics. Truly unauthenticated requests get a plain `403`.

**Also worth knowing:** a pre-existing bug in this backend's global exception handling was found and fixed while building this feature — `@Valid` field-validation failures (e.g. a rating outside 1–5) used to return a generic `{"message":"An unexpected error occurred. Please try again.","status":"error"}` with HTTP `500`, across the **entire API**, not just this module. They now correctly return `400` with a structured body:
```json
{ "error": "Validation failed", "message": "Input validation failed", "errors": ["rating must be between 1 and 5"], "status": 400, "timestamp": "..." }
```
Note this shape (`errors` array, `status` as a number) is slightly different from the usual `{"status":"error","message":"..."}` convention used elsewhere — handle both shapes when parsing error responses.

Common messages: `"Sitter not found with id: {id}"`, `"This sitter is not currently accepting bookings"`, `"You cannot book yourself as a sitter"`, `"Pet not found with id: {id}"`, `"You can only book sitting for your own pet"`, `"endDate must be after startDate"`, `"Only the sitter can confirm this booking"`, `"Only the owner or sitter can cancel this booking"`, `"Only admins can force-complete a booking"`, `"You can only review a completed booking"`, `"You have already reviewed this booking"`.

## Known Gaps (not implemented)

- No refund automation — cancelling a paid booking leaves `paymentStatus: PAID`, requiring manual admin follow-up.
- No endpoint to set `sitterNotes` on a booking (field exists, unused).
- No `IN_PROGRESS`/dispute-resolution flow for `DISPUTED` status.
- No pagination on any list endpoint.
- Platform fee (15%) and badge-style thresholds are placeholders — confirm the real number before launch.
- `ratePerNight` is captured but not used in pricing (day-count only).
