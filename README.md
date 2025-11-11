# Movito Team

Welcome to Movito team repository.  
We are a group of passionate developers working together on Android projects using Kotlin, Jetpack Compose, and modern development practices.

---

## Team Members
- Mohamed Ibrahim Abdel-Samee (Team Leader)  
- Youssef Sayed  
- Yehia Mohamed  
- Ahmed Essam  
- Basmala Wahid  
- Alyaa Osama

---

## Project Name
Movito (Movie Discovery App)

---

## Project Idea
We are building a Movie Discovery App that allows users to:
- Browse popular movies in a beautiful grid layout  
- View detailed information about a selected movie (title, synopsis, rating, release date)  
- Search for movies by title  
- Enjoy a modern UI/UX powered by Jetpack Compose and Material Design 3

---

## Functional Requirements  
The app should allow users to:  
- Login / Sign Up → Create a new account or sign in securely  
- Browse Popular Movies → View a grid/list of trending and top-rated movies  
- Search Movies → Find movies by title  
- View Movie Details → See title, synopsis, rating, release date, and poster  
- Watch Movie Trailer → Play official trailers inside the app  
- Add to Favorites → Save movies to a personal favorites list

---

## Technologies
- Language: Kotlin & XML  
- UI Toolkit: Jetpack Compose  
- Networking: Retrofit  
- Image Loading: Coil  
- Architecture: MVVM / Clean Architecture  
- Database: Room (for caching/offline mode)  
- Version Control: Git & GitHub  
- Testing: Unit Testing + Compose UI Testing  
- Security: Secure API key storage using gradle.properties (no hardcoded keys)  

---

## Project Plan  

### Week 1: Design & Setup  
- Project structure setup, Gradle dependencies, mock data  
- Screens & Navigation setup  

### Week 2: API & Data Layer  
- TMDB API integration, Repository & Data Models  
- Asynchronous calls with Coroutines + Flow  
- Loading & error handling  

### Week 3: Features & Improvements  
- Search, filters, pagination, UI enhancements  
- Optional offline caching with Room  

### Week 4: Final Touches & Release  
- Unit & UI testing, performance optimization  
- Splash Screen & App Icon  
- Prepare Play Store build  

---

## Team Responsibilities  

### Mohamed Ibrahim
UI:  
- Wireframe & High-fidelity UI Screen Design  
- Splash Screen  
- Home Screen  

Logic:  
- Splash Screen Logic  
- Home Logic (ViewModel)  
- Theme Mode Logic (Dark/Light Mode persistence)  
- Navigation Logic (Navigation Graph + Bottom Navigation Bar)  
- Project Setup (Git Repository, Dependencies)  
- Code Review  

### Basmala Wahid
UI:  
- Favorites Screen  
- Settings Screen  

Logic:  
- Favorites Logic (Add/Remove + Room DB)  
- Notifications Logic (Handling settings toggles)  

### Alyaa Osama
UI:  
- Sign Up Screen  
- Movie Details Screen   

Logic:  
- Sign Up Logic  
- Movie Details Logic (ViewModel)  

### Youssef Sayed
UI:  
- Sign In Screen  
- Profile Screen  

Logic:  
- Authentication Logic (Sign In, Continue with Google)  
- Session Management  
- Sign Out Logic  
- Profile Info Logic  

### Yehia Mohamed
UI:  
- Search Results UI  
- Home Screen Enhancements (Filter, Sort UI components)  

Logic:  
- API Service Setup  
- Search & Filter Logic (ViewModel)  
- Search Logic (ViewModel)  

### Ahmed Essam
UI:  
- Settings Enhancements (Notification Toggles UI)  
- Favorites List UI Integration  

Logic:  
- Database Integration (Favorites DB operations)  
- Theme Persistence Logic  
