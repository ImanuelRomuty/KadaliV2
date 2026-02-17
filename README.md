# ⚡ Office Energy Calculator (Android)

A modern Android application to **estimate, analyze, and simulate office electricity usage** based on real equipment data per room.

Designed to help organizations understand:

* Where electricity is being used 🏢
* How much it costs 💰
* How to optimize consumption 📉

---

## ✨ Features

### 📊 Dashboard

* Bar chart visualization of electricity cost per room.
* Estimated cost:

  * Daily
  * Weekly
  * Monthly
  * Yearly
* Automatically highlights the **most energy-consuming room**.

### 🧮 Simulation Calculator

* Simulate device usage without saving data.
* Try "what-if" scenarios for energy savings.

### ⚙️ Settings

* Manual electricity tariff configuration (Rp/kWh).
* Displays currently active tariff.

### 🏷️ Room & Device Management

* Add / Edit / Delete rooms.
* Add / Edit / Delete electrical devices inside each room.
* Automatic recalculation after every change.

### 📄 Export PDF Report

* Generate professional electricity usage reports.
* Includes summaries, breakdowns, and projections.
* Saved locally for sharing or audit documentation.

### 🎨 Modern Dark UI

* Single-theme modern dark design (no light/dark switching).
* Color palette derived from app logo.
* Clean dashboard-style interface.

---

## 🏗️ Architecture

The app follows **Clean Architecture + MVVM** to ensure scalability and maintainability.

```
Presentation Layer  → UI, ViewModel
Domain Layer        → Business Logic (Energy Calculation)
Data Layer          → Local Database, Repository
```

### Pattern Used:

* MVVM (Model–View–ViewModel)
* Repository Pattern
* Reactive Data Flow

---

## 🛠️ Tech Stack

| Category             | Technology                  |
| -------------------- | --------------------------- |
| Language             | Kotlin 🟣                   |
| Architecture         | MVVM + Clean Architecture   |
| UI                   | XML + Material Design       |
| Dependency Injection | Koin                        |
| Local Database       | Room                        |
| Async Processing     | Kotlin Coroutines           |
| Reactive Updates     | StateFlow / Flow            |
| Navigation           | Jetpack Navigation          |
| Charting             | MPAndroidChart              |
| PDF Generation       | Android PdfDocument API     |
| Storage              | MediaStore (Scoped Storage) |

---

## 📚 Libraries Used

```gradle
// Dependency Injection
io.insert-koin:koin-android

// Room Database
androidx.room:room-runtime
androidx.room:room-ktx

// Lifecycle & ViewModel
androidx.lifecycle:lifecycle-viewmodel-ktx

// Coroutines
org.jetbrains.kotlinx:kotlinx-coroutines-android

// Navigation
androidx.navigation:navigation-fragment-ktx
androidx.navigation:navigation-ui-ktx

// Charts
com.github.PhilJay:MPAndroidChart
```

---

## ⚡ Energy Calculation Formula

The application uses the standard electrical formula:

```
Energy (kWh) = (Power × Usage Hours × Quantity) / 1000
Cost = Energy × Tariff
```

Example:

```
AC (900W) × 8h × 2 units = 14.4 kWh/day
14.4 × Rp 1,444 = Rp 20,793/day
```

---

## 📁 Project Structure

```
feature/
 ├── dashboard/
 ├── simulation/
 ├── settings/
 └── room/

domain/
 ├── model/
 ├── usecase/
 └── calculator/

data/
 ├── local/
 └── repository/

core/
 └── ui / theme / utils
```

---

## 🚀 Getting Started

### 1️⃣ Clone the Repository

```bash
git clone https://github.com/your-username/office-energy-calculator.git
```

### 2️⃣ Open in Android Studio

Use **Android Studio Hedgehog or newer**.

### 3️⃣ Sync Gradle

Allow dependencies to download.

### 4️⃣ Run the App

Connect a device or emulator and press ▶️.

---

## 📄 PDF Report Output

Reports are automatically saved to:

```
Documents / OfficeEnergyReports/
```

Filename example:

```
Energy_Report_2026-02-18.pdf
```

---

## 🎯 Future Roadmap

* 📷 AI-based device recognition
* 📈 Energy trend analytics
* 🏢 Multi-building support
* ☁️ Cloud synchronization
* 📬 Scheduled report export

---


## 📜 License

This project is intended for educational and internal office optimization use.

---

💡 *Built to make energy usage visible, measurable, and optimizable.*
