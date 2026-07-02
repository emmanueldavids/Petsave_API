# Community Posts Frontend Handoff

## Overview

This feature lets users share pet stories, tips, photos, and event announcements in a social feed. Posts support images, tags, likes, and threaded comments. Admins can pin important posts to the top of the feed or hide posts that violate guidelines.

Base URL:
- `http://localhost:8080/api/posts`
- `http://localhost:8080/api/comments`

Note: this is implemented under `/api/posts` (not `/api/community/posts`) because the existing frontend (`postAPI` in `lib/api.ts`, `PostSection.tsx`, `Post.tsx`) was already built against `/api/posts` with matching `PostRequest`/`PostResponse`/`CommentRequest`/`CommentResponse` shapes. If your frontend already uses `postAPI`, it should work against this backend with no client changes beyond what's noted below.

## Authentication

- Public reads:
  - `GET /api/posts`
  - `GET /api/posts/{id}`
- Authenticated actions:
  - Create, edit, delete own post
  - Like / unlike
  - Add comment or reply
  - Delete own comment
- Admin-only actions:
  - `PATCH /api/posts/{id}/pin`
  - `PATCH /api/posts/{id}/hide`

Ownership and role checks happen server-side. Attempting to edit/delete someone else's post, or pin/hide as a non-admin, returns `400` with a descriptive `message` (see Error Handling below) — not a silent no-op.

## Main Data Shapes

### PostResponse

```json
{
  "id": 1,
  "author": {
    "id": 4,
    "name": "Ada",
    "email": "ada@example.com"
  },
  "content": "My rescue story! #adoption-success",
  "imageUrls": ["https://res.cloudinary.com/.../post-images/abc.png"],
  "location": "Accra",
  "tags": ["adoption-success"],
  "likesCount": 3,
  "commentsCount": 2,
  "sharesCount": 0,
  "isLiked": false,
  "createdAt": "2026-07-02 20:06:15",
  "timeAgo": "1 hour ago",
  "comments": []
}
```

Notes:
- `author` only reliably includes `id`, `name`, `email`. Other `UserResponse` fields the DTO supports (`bio`, `location`, `avatarUrl`, `postsCount`, `petsAdopted`, `isOnline`, etc.) are **not populated yet** — the `User` entity doesn't carry that data today. Treat them as optional/absent in the UI.
- `isLiked` reflects the *requesting* user's like state. It's computed from the JWT if present, even on the public `GET` endpoints — send the `Authorization` header on feed/detail requests if the user is logged in, so like buttons render correctly without an extra round trip.
- `comments` is only populated by `GET /api/posts/{id}` (single post). The feed/list endpoint always returns `comments: []` — fetch the detail endpoint to show a post's comment thread.
- `sharesCount` exists in the shape for frontend compatibility but there's no share endpoint or increment logic yet — it will always be `0`.

### CommentResponse

```json
{
  "id": 1,
  "author": { "id": 4, "name": "Ada", "email": "ada@example.com" },
  "content": "Congrats!",
  "likesCount": 0,
  "isLiked": false,
  "parentCommentId": null,
  "createdAt": "2026-07-02 20:06:15",
  "timeAgo": "1 hour ago",
  "replies": [
    {
      "id": 2,
      "author": { "id": 5, "name": "James", "email": "james@example.com" },
      "content": "Thank you!",
      "likesCount": 0,
      "isLiked": false,
      "parentCommentId": 1,
      "createdAt": "2026-07-02 20:07:00",
      "timeAgo": "1 hour ago",
      "replies": []
    }
  ]
}
```

Notes:
- Only one level of nesting is supported (a reply to a reply still attaches as a child of the *top-level* comment's `replies` list via `parentCommentId`, but the tree returned is only two levels deep: comment → replies). There's no comment-like tracking per user yet — `likesCount` exists on the entity but there's no like/unlike endpoint for comments, so it will always read `0` / `isLiked: false`.

## API Contract

### 1) Get the feed

#### Request
- `GET /api/posts?page=0&size=10&tag=adoption-success`

`tag` is optional — omit it to get the unfiltered feed.

#### Response
- Spring `Page<PostResponse>` — access items via `response.data.content`.
- Pinned posts always sort first, then by `createdAt` descending.
- Hidden posts are excluded automatically.

#### Frontend behavior
- This matches the existing `postAPI.getAll(page, size)` call — no change needed.

### 2) Get a single post (with comments)

#### Request
- `GET /api/posts/{id}`

#### Response
- Full `PostResponse` including the nested `comments` tree.
- Returns `404`-style "not found" error if the post is hidden (moderated posts are not visible via direct link either).

### 3) Create a post (text only)

#### Request
- `POST /api/posts`

```json
{
  "content": "My rescue story! #adoption-success",
  "location": "Accra",
  "tags": ["adoption-success"]
}
```

`content` is required (max 2000 chars). `location` and `tags` are optional.

### 4) Create a post with images

#### Request
- `POST /api/posts/with-images` — `multipart/form-data`

Fields:
- `content` (text, required)
- `location` (text, optional)
- `tags` (repeat the field once per tag, optional)
- `images` (repeat the field once per file, optional — uploaded to Cloudinary under `post-images`)

This matches the existing `postAPI.createWithImages(...)` FormData shape — no change needed.

### 5) Edit own post

#### Request
- `PUT /api/posts/{id}`

Same body shape as create. `content` is required by validation even on edit (send the full current content, not a partial patch). Images cannot be changed via this endpoint today — there's no image-replace flow yet.

### 6) Delete own post

#### Request
- `DELETE /api/posts/{id}`

Cascades: deletes the post's comments and likes.

### 7) Like / unlike

#### Request
- `POST /api/posts/{id}/like`
- `DELETE /api/posts/{id}/like`

These are two separate calls (not a single toggle endpoint) — matches the existing `postAPI.like(id)` / `postAPI.unlike(id)`. Both are idempotent: liking an already-liked post or unliking an already-unliked post is a no-op that just returns the current `PostResponse`, not an error.

### 8) Add a comment or reply

#### Request
- `POST /api/posts/{id}/comments`

```json
{
  "content": "Congrats!",
  "parentCommentId": null
}
```

Omit `parentCommentId` (or send `null`) for a top-level comment; set it to an existing comment's `id` on the same post to reply. Replying to a reply is allowed at the API level, but the response tree only nests one level deep under the original top-level comment.

#### Frontend behavior
- `Post.tsx` currently keeps comments in local mock state and never calls a real endpoint for `handleComment`/`handleReply`. Wiring this up is the main frontend gap — call this endpoint instead of the local `setComments(...)`, then refetch `GET /api/posts/{id}` (or `invalidateQueries`) to get the authoritative comment tree and updated `commentsCount`.

### 9) Delete own comment

#### Request
- `DELETE /api/comments/{id}`

Only the comment's author (or an admin) can delete it. Deleting a top-level comment does not currently cascade-delete its replies as orphans in the UI tree — check with backend before relying on delete-with-replies behavior if you need it, it's not implemented.

### 10) Admin: pin / unpin a post

#### Request
- `PATCH /api/posts/{id}/pin`

No request body. Toggles `isPinned` — call it again to unpin. Admin-only (`role: "ADMIN"`), returns `400` with `"Only admins can pin posts"` otherwise.

### 11) Admin: hide / unhide a post (moderation)

#### Request
- `PATCH /api/posts/{id}/hide`

No request body. Toggles `isHidden` — call it again to unhide. Admin-only, returns `400` with `"Only admins can hide posts"` otherwise. Hidden posts disappear from the feed and from direct `GET /api/posts/{id}` lookups for everyone, including the author.

## Suggested UI States

- Feed: pinned posts get a visual "Pinned" badge, sorted first.
- Empty feed: friendly "No community posts yet. Create the first one." (already implemented in `PostSection.tsx`).
- Comment thread: show reply nesting one level deep; a "reply to a reply" should visually attach under the same top-level comment.
- Admin-only pin/hide controls should only render for `role === 'ADMIN'` users — the backend enforces this regardless, but hiding the buttons avoids a confusing 400 for other users.

## Error Handling

Errors come back as:

```json
{
  "status": "error",
  "message": "Only the author can edit this post"
}
```

- Ownership/role violations → `400` with a specific message (not `403`) — same convention used by the rehoming module. Check `message`, not just status code, if you want to show a tailored error.
- Truly unauthenticated requests to protected endpoints → `403 Forbidden` (Spring Security's default, before your request even reaches app logic).
- Common messages to handle: `"Only the author can edit this post"`, `"Only the author can delete this post"`, `"Only the author can delete this comment"`, `"Only admins can pin posts"`, `"Only admins can hide posts"`, `"Post not found with id: {id}"`, `"Authentication required"`.

## Known Gaps (not implemented)

- No share/repost endpoint or counter increment (`sharesCount` stays `0`).
- No per-user like tracking on comments (`likesCount`/`isLiked` on comments are inert).
- No search endpoint beyond the single `?tag=` filter.
- No notifications on new likes/comments/replies.
- `UserResponse.author` only has `id`/`name`/`email` populated — no avatar, bio, or online status yet.
