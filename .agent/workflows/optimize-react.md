---
description: Perform a Vercel-style React performance optimization audit and refactor.
---

# /optimize-react Workflow

This workflow systematically applies Vercel's React Best Practices to the codebase.

## Steps

1. **Scan and Identify Components**
   - Locate all React components (`.tsx`, `.jsx`) and API routes/handlers.
   - Summarize the current fetching patterns and state management strategies.

2. **Async Waterfall Audit**
   - Search for sequential `await` calls that don't depend on each other.
   - // turbo
   - Identify opportunities to use `Promise.all()` or defer awaits.

3. **Bundle Size & Import Efficiency**
   - Check for barrel file imports (e.g., `import { a, b } from '@/components'`).
   - Identify heavy libraries (e.g., `framer-motion`, `lucide-react`) that could be dynamic imports.

4. **Re-render & State Optimization**
   - Check for expensive `useState` initializations (e.g., `JSON.parse(localStorage.getItem(...))`).
   - Audit `useEffect` dependencies for over-triggering.

5. **Apply & Verify**
   - Propose specific code changes with a focus on "Correct vs. Incorrect" patterns from the Vercel guide.
   - Refactor the code after user approval.

## Reference Rules

- **CRITICAL**: Eliminate Waterfalls, Reduce Bundle Size.
- **HIGH**: Server-side performance (React Server Components caching).
- **MEDIUM**: State and Re-render optimization.
