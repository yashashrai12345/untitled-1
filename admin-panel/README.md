# Arrows Puzzle Escape — Admin Pattern Designer Website

This repository contains the official Admin Panel Website exclusively designed for creating, testing, publishing, and managing custom puzzle patterns for **Arrows Puzzle Escape**.

## Key Features

- **Interactive SVG Grid Designer**: Drag and click to draw multi-segment orthogonal arrow paths.
- **Advanced Pattern Construction**: Spirals, concentric rings, mazes, and symmetry tools (horizontal, vertical, rotation).
- **Live Game Validation**: Real-time bounds checking, segment clearance, overlap detection, and 1:1 Kotlin engine parity.
- **Topological & BFS Solver**: Automatic solvability verification and step-by-step move sequence determination.
- **Interactive Play-test Mode**: Embedded playable simulation with escape animations and collision feedback.
- **Real-Time Database Synchronization**: Syncs published patterns via Supabase PostgreSQL to all connected Android app clients.
- **Secure Secret Publishing**: Protects publishing operations via `ADMIN_PUBLISH_SECRET` without requiring username/password login.

---

## Quick Start (Local Development)

1. Navigate to the `admin-panel` directory:
   ```bash
   cd admin-panel
   ```

2. Install dependencies:
   ```bash
   npm install
   ```

3. Create a `.env` file based on `.env.example`:
   ```bash
   cp .env.example .env
   ```

4. Start the Vite development server:
   ```bash
   npm run dev
   ```

5. Open [http://localhost:3000](http://localhost:3000) in your browser.

---

## Supabase Database Setup (100% Free)

1. Create a free project at [Supabase.com](https://supabase.com).
2. Go to the **SQL Editor** in your Supabase Dashboard.
3. Open `supabase_schema.sql` (located at the root of the project) and execute the entire script.
4. Copy your **Project URL**, **Anon Key**, and **Service Role Key** from `Project Settings -> API`.

---

## Deployment on Vercel (100% Free Forever)

1. Log in to [Vercel.com](https://vercel.com) (sign up with GitHub).
2. Click **Add New -> Project** and import your GitHub repository.
3. Set **Root Directory** to `admin-panel`.
4. Vercel automatically detects Vite and Serverless API functions in `/api`.
5. Add the following **Environment Variables** in Vercel:
   - `VITE_SUPABASE_URL`: Your Supabase Project URL
   - `VITE_SUPABASE_ANON_KEY`: Your Supabase Anon Key
   - `SUPABASE_SERVICE_ROLE_KEY`: Your Supabase Service Role Key
   - `ADMIN_PUBLISH_SECRET`: A high-entropy secret string used when publishing patterns.
6. Click **Deploy**. Vercel will build and host your Admin Designer with **zero cold starts**!
