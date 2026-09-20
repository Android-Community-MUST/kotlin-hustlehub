# Data Structures & Algorithms — HustleHub Android

This document explains every non-trivial DSA decision in the Android codebase. It exists so future engineers understand *why* a structure was chosen, not just what it is.

---

## 1. Min-Heap (Priority Queue) — Top-K Featured Services

**Where**: `HomeViewModel.selectTopFeatured()` + `featuredScore()`

### The Problem
The home screen Featured Hustlers carousel needs the top-5 services by a composite score. The previous implementation ran 3 separate `sortedByDescending` passes + a `distinctBy` + `take(5)` — equivalent to 4 × O(n log n) work, repeated every time a new page loads via infinite scroll.

### The Solution
A **Min-Heap** (minimum priority queue) of size K selects the top-K elements in O(n log k).

**How it works**:
1. Maintain a `PriorityQueue<Service>` with the *smallest* `featuredScore` at the top.
2. For each service in the list:
   - If the heap has fewer than K items → insert unconditionally.
   - If the new item's score exceeds the heap minimum → evict the minimum, insert the new item.
3. The heap always holds the best K services seen so far.

**Complexity**: O(n log 5) ≈ O(n) vs O(n log n) for sort  
**Implementation**: `java.util.PriorityQueue` with `compareBy { featuredScore(it) }`

```kotlin
private fun selectTopFeatured(merged: List<Service>, k: Int = MAX_FEATURED_COUNT): List<Service> {
    if (merged.size <= k) return merged
    val heap = PriorityQueue<Service>(k, compareBy { featuredScore(it) })
    for (service in merged) {
        if (heap.size < k) {
            heap.add(service)
        } else if (featuredScore(service) > featuredScore(heap.peek()!!)) {
            heap.poll()
            heap.add(service)
        }
    }
    return heap.sortedByDescending { featuredScore(it) }
}
```

---

## 2. Trie (Prefix Tree) — Search Autocomplete

**Where**: `core/search/SearchTrie.kt`, integrated in `SearchViewModel`

### The Problem
Every character the user typed in the search box waited 300ms (debounce), then fired a network request. No suggestions appeared during the debounce window — the search bar felt dead.

### The Solution
A **Trie** stores words as character paths from a root node. Each prefix maps to a set of completions. Lookup is O(L) where L is the prefix length — constant time regardless of the total vocabulary size.

```
root
 └─ 'h'
      └─ 'a'
           └─ 'i'
                └─ 'r'  → suggestions: ["Hair Braiding", "Hair Cutting"]
```

**Two-layer architecture**:
1. **Instant layer** (Trie): On every keystroke, return suggestions already in the Trie — zero network, < 1ms.
2. **Network layer** (backend): Fire `GET /discovery/suggestions?q=...` to get full-catalog results, insert them into the Trie, update the UI. Next time the user types the same prefix, the Trie already has the answer.

**Result**: Suggestions feel instant. Network calls only fire when the user types a new prefix, not on every character.

---

## 3. LRU Cache (LinkedHashMap) — Bounded Service List

**Where**: `core/cache/LruServiceCache.kt`, integrated in `HomeViewModel`

### The Problem
Infinite scroll appends pages to `services: List<Service>` in `HomeUiState`. After 10+ pages, this list holds 200+ service objects in memory — and grows without bound. Deep scroll sessions on low-RAM devices could trigger GC pressure or OOM.

### The Solution
A **Least-Recently-Used Cache** backed by `LinkedHashMap` in access-order mode. When the cache exceeds `maxSize` (100), the least-recently-accessed entry is automatically evicted.

**Why `LinkedHashMap`?** Java's `LinkedHashMap(capacity, loadFactor, accessOrder=true)` maintains a doubly-linked list of entries in access order. `removeEldestEntry()` is called after every `put()` — if `size > maxSize`, the eldest (least-recently-accessed) entry is evicted. This gives O(1) amortized insert and O(1) eviction with no manual bookkeeping.

**Ceiling**: 100 services — the right balance between memory and scroll depth for a campus marketplace.

---

## 4. Exponential Backoff + Jitter — Network Retry

**Where**: `core/network/RetryPolicy.kt`

### The Problem
When the backend is temporarily overloaded, a naive retry immediately re-fires the request — potentially making the overload worse (thundering herd problem).

### The Solution
**Exponential Backoff**: Each retry waits twice as long as the previous: 300ms → 600ms → 1200ms → ... → capped at 8s.  
**Jitter**: A random ±20% is added to each delay. This desynchronizes retries from multiple clients so they don't all hammer the server at the exact same moment.

```kotlin
delay = min(delay * 2, maxDelayMs)                    // exponential growth
jitter = (delay * 0.2 * Random.nextDouble()).toLong()  // ±20% random jitter
```

**Complexity**: O(1) per retry decision. Max total wait before giving up: ~8s for 3 attempts.

---

## 5. Weighted Sum Model — Client-Side Featured Scoring

**Where**: `HomeViewModel.featuredScore()`

A lightweight scoring function classifies services into 3 priority tiers, each offset so they never overlap:

| Tier | Condition | Score range |
|------|-----------|-------------|
| Featured | `isFeatured == true` | `2.0 + averageRating` (2.0–7.0) |
| Rated | `averageRating > 0` | `1.0 + averageRating` (1.0–6.0) |
| Unrated | `averageRating == 0` | `0.0` |

The `+2.0` and `+1.0` offsets ensure that a paid-featured service with a 0-star rating still ranks above a non-featured service with a 5-star rating, which is the correct business priority.

---

## 6. Map Pin Distance Offload — Client CPU & Battery Preservation

**Where**: `MapViewModel.fetchPins()`, `MapRepositoryImpl.kt`, `DiscoveryApiService.kt`

### The Problem
When displaying campus and nearby providers on the map, `MapViewModel` previously fetched all pins and ran `haversineDistance()` on every single pin in Kotlin on the main device thread. For 100+ pins, this performed hundreds of trigonometric calculations (`sin`, `cos`, `atan2`, `sqrt`) unnecessarily on mobile hardware.

### The Solution
The backend (PostGIS) already computes geographic distance via `ST_Distance` during spatial indexing and returns `distanceMeters` in `MapPinResponseDto`.
- **Primary path**: `MapViewModel` checks `pin.distanceMeters`. If present, the calculation is skipped entirely — O(1) assignment.
- **Offline fallback**: If loading from Room cache without precomputed distance, `haversineDistance()` is executed as a fallback.
- **Result**: Significant CPU and battery savings during map interaction and continuous polling.

---

## 7. Spatial k-NN and Trending Endpoints

**Where**: `DiscoveryApiService.kt`

Exposes:
- `GET /discovery/nearest?lat=&lng=&limit=`: True nearest-first discovery powered by PostGIS `<->` operator on the GiST index.
- `GET /discovery/trending?category=&page=&size=`: Dynamic trending discovery powered by exponential time-decay scoring.

---

## Adding a New DSA Decision

When you introduce a non-trivial data structure or algorithm, document it here:

1. **Where** — file and function name
2. **The Problem** — what was slow or broken
3. **The Solution** — which data structure and why it fits
4. **Complexity** — Big-O before and after
5. **Trade-offs** — what you gave up (memory, code complexity, etc.)
