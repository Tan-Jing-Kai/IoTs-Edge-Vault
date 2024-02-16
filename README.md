# Edge-Vault

**Edge-Vault** is an Android-based IoT application tailored for robust security and effective device management. It integrates a user-friendly mobile interface with cloud and hardware interactions to offer an all-encompassing IoT management solution.

## Features

- **Firebase Authentication**: Utilizes Firebase for streamlined and secure user registration and login, ensuring data privacy and integrity.
- **User Registration**: Empowers users to create new accounts with email verification, enforcing strong password policies for heightened security.
- **User Login**: Provides a smooth login experience, with error handling for failed attempts and navigational links for new user registration.
- **Main Interface**: Serves as the operational center for user interactions, allowing for the control and monitoring of IoT devices.
- **WiFi Management**: Includes WiFi connectivity management, enabling users to configure and connect IoT devices to local networks.
- **Real-Time Data Interaction**: Connects to cloud services for real-time data handling, ensuring that users have up-to-the-minute information.
- **Splash & Welcome Screens**: Delivers an engaging initial experience with splash and welcome screens that gracefully transition users into the main application.

## Technical Overview

- **LoginActivity**: Manages user login with Firebase, complete with input validation and user feedback mechanisms.
- **RegistrationActivity**: Handles new user sign-ups, incorporating form validation and secure password requirements.
- **MainActivity**: Acts as the hub for device control and user settings, with integrated WiFi setup and cloud interactions.
- **SSIDActivity**: Dedicated to managing WiFi connections, essential for IoT device networking.
- **SplashActivity**: Displays a branded launch screen, creating a positive first impression as the app loads.
- **WelcomeActivity**: Offers a welcoming onboarding experience for new users, including a smooth transition for those returning to the app.

## Getting Started

To get started with Edge-Vault, follow the instructions in each component's directory:

- `LoginActivity`: Review the authentication flow and Firebase setup.
- `RegistrationActivity`: Set up user registration with strong password enforcement.
- `MainActivity`: Configure the main user interface and device management capabilities.
- `SSIDActivity`: Implement WiFi management logic.
- `SplashActivity` and `WelcomeActivity`: Customize the initial user experience as per your branding needs.

## Acknowledgments

- Contributed by Jing Kai
- Our gratitude to the Firebase team for their robust authentication system.

