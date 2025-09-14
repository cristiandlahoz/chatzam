# chatzam

`chatzam` is a foundational Android application built using the standard Android Studio Navigation Drawer activity template. It serves as a starting point for developing Android apps that require a common side-navigation pattern.

The project is configured with a `MainActivity` that manages a `DrawerLayout`, a `NavigationView`, and a `NavHostFragment` for handling fragment-based navigation.

## Features

*   **Navigation Drawer:** A pre-configured side navigation menu for top-level destinations.
*   **Jetpack Navigation Component:** Utilizes a navigation graph (`mobile_navigation.xml`) to manage in-app navigation between fragments.
*   **MVVM Architecture:** The template is structured using the Model-View-ViewModel pattern, with `ViewModel` classes for each fragment to manage UI-related data.
*   **Material Design:** Implements Material Components for a modern and consistent user interface, including `Toolbar`, `FloatingActionButton`, and `NavigationView`.
*   **ViewBinding:** Uses ViewBinding to safely and easily interact with views in layout files.
*   **Gradle with Version Catalog:** Dependencies and versions are managed centrally using a TOML-based version catalog (`libs.versions.toml`).

## Getting Started

To get this project up and running on your local machine, follow these steps.

### Prerequisites

*   [Android Studio](https://developer.android.com/studio)
*   An Android Virtual Device (AVD) or a physical Android device.

### Installation

1.  **Clone the repository:**
    ```sh
    git clone https://github.com/cristiandlahoz/chatzam.git
    ```

2.  **Open in Android Studio:**
    *   Launch Android Studio.
    *   Select `File > Open...` and navigate to the cloned repository directory.

3.  **Build and Run:**
    *   Allow Android Studio to sync the project with the Gradle files.
    *   Select a run configuration (an emulator or a connected physical device).
    *   Click the "Run" button to build and install the application.

## Project Structure

The project follows a standard Android application structure:

*   `app/src/main/java/com/wornux/chatzam/`: Contains the main Java source code.
    *   `MainActivity.java`: The single activity that hosts the navigation drawer and the fragment container.
    *   `ui/`: This package is organized by feature, containing the fragments and their corresponding ViewModels.
        *   `home/`: Contains `HomeFragment` and `HomeViewModel`.
        *   `gallery/`: Contains `GalleryFragment` and `GalleryViewModel`.
        *   `slideshow/`: Contains `SlideshowFragment` and `SlideshowViewModel`.
*   `app/src/main/res/`: Contains all application resources.
    *   `layout/`: XML layout files for the activity and fragments.
    *   `menu/`: XML files defining the navigation drawer menu (`activity_main_drawer.xml`) and options menu.
    *   `navigation/`: The navigation graph (`mobile_navigation.xml`) that defines the app's navigation paths.
*   `gradle/`: Contains the Gradle wrapper and the version catalog (`libs.versions.toml`).
