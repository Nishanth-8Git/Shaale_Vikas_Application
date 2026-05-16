# Shaale Vikas (School Development Bridge)

An industry-ready, hybrid-cloud Android application built during the **MindMatrix Industry Readiness Programme**. **Shaale Vikas** acts as a transparent digital bridge connecting rural school administrators directly with their alumni donor network to solve critical infrastructure needs.
(P.S You can view the demo in this YouTube video: https://youtu.be/suh0DdT8wY4 )
{OR over here}
<iframe width="932" height="537" src="https://www.youtube.com/embed/suh0DdT8wY4" title="Shaale Vikas Application Demo." frameborder="0" allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share" referrerpolicy="strict-origin-when-cross-origin" allowfullscreen></iframe>





---

##  Architecture Highlights 
Unlike standard single-database student projects, this application implements a production-grade **Dual-Cloud Strategy** to maximize storage efficiency and scale cost-effectively:
* **Firebase Firestore (NoSQL):** Serves as the real-time operational engine handling fast metadata sync, live pledge updates via atomic increment operations, and structural need profiling.
* **Supabase Storage (Object Vault):** Used to securely host high-resolution "Before & After" project verification media, ensuring free-tier database compliance while decoupling structured text from heavy binary storage.
* **Jetpack Compose & MVVM:** Built entirely with a modern reactive UI following strict separation of concerns—ensuring zero direct database or API hooks inside UI Composables.

---

##  In-Scope Features
* **Passcode-Authenticated Admin Portal:** Secure entry gates specifically mapped for Headmasters to log infrastructure requirements safely.
* **Standardized Infrastructure Profiling:** Dropdown/chip-based categorization prioritizing needs based on an auto-calculated urgency matrix.
* **Real-time Alumni Dashboard:** Interactive funding tracking displaying Material3-themed progress metrics.
* **Donor Hall of Fame:** Public acknowledgment registry updating in real-time as pledge commitments are completed.
* **Automated PDF Report Generator:** Built-in Android `PrintManager` utility transforming digital milestones into a physical or downloadable project completion ledger.

---

##  Setup & Installation Instructions

> [!IMPORTANT]  
> You **MUST** use **Android Studio** (Panda(currently as of May 2026) or later recommended) to properly compile, sync, and execute this project. Running via command-line gradle commands without the Android SDK environment _may cause dependency resolution errors._

### Prerequisites
1.  Download and install the latest stable version of [Android Studio](https://developer.android.com/studio).
2.  Ensure you have the **Android SDK (API Level 28 - Android 9.0 or higher)** installed via the SDK Manager.
3.  A physical Android device with Developer Options enabled OR an Android Emulator running API 28+.

### Steps to Run the Project
1.  **Clone the Repository:**
    ```bash
    git clone https://github.com/Nishanth-8Git/Shaale_Vikas_Application
    ```
2.  **Open in Android Studio:**
    * Launch Android Studio.
    * Import the project, by opening the folder in Android Studio.
3.  **Gradle Sync:**
    * Allow Android Studio to automatically download dependencies and run the Gradle sync task. 
    * Ensure the build finishes with a successful status check message in the `Build` tab.
4.  **Run the Application:**
    * Connect your test device via USB/Wi-Fi or launch your emulator.
    * Select your target device from the top toolbar dropdown menu.
    * Click the green **Run** button to compile and launch the application.
