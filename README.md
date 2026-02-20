# Kadali – Electrical Usage Analysis System

Kadali is a professional energy management tool designed to monitor, analyze, and optimize electrical consumption in office and residential environments. It provides a "Data-First" approach to energy monitoring by calculating estimated operational costs based on real-world appliance wattage and usage duration.

## 🛠 Technology Stack
- **Language**: Kotlin
- **Architecture**: MVVM + Repository Pattern
- **Database**: Firebase Firestore (Cloud-First)
- **Dependency Injection**: Koin
- **UI Components**: Material 3, MPAndroidChart
- **PDF Engine**: Android PdfDocument with Custom Canvas rendering

## 🏗 Architecture: Cloud-First Firestore
Kadali is built with a **Cloud-First** philosophy. It utilizes Firebase Firestore as its primary and only data source, ensuring:
- **Real-time Synchronization**: Data changes propagate instantly across all connected devices.
- **Offline Resilience**: Automatically caches data for offline use and syncs upon reconnection.
- **Reactive Data Flow**: Uses Kotlin Coroutines and Flows to stream data from the database directly to the UI.

## ✨ Features
- **Project-Based Room Management**: Create and organize office spaces as distinct rooms.
- **Detailed Device Inventory**: Track power rating (Watt), quantity, and daily usage hours for every electrical appliance.
- **Real-time Analytics Dashboard**:
    - Instant calculation of Daily, Monthly, and Yearly costs.
    - Energy distribution breakdown via interactive Bar Charts.
- **Electrical Tariff Configuration**: Adjustable Rp/kWh settings to match local utility rates.
- **Analytical PDF Reports**: Generate 12-section professional reports including:
    - Detailed room directories.
    - Technical device-by-device load analysis.
    - Projected cost trends and load classification.

## 📊 Calculation Methodology
Energy calculations are based on standard electrical engineering formulas:
- **Energy (kWh)**: `(Power Rating (W) × Quantity × Usage Hours) / 1000`
- **Cost Estimation**: `Energy (kWh) × Tariff (Currency/kWh)`
- **Connected Load**: `Power Rating (W) × Quantity`

## 📂 Firestore Data Structure
The database is structured for efficiency and scalability:
- `rooms/`: Document ID based room definitions.
- `devices/`: Appliances linked via `roomId`.
- `config/global`: Global application settings and active tariff profiles.

## 🚀 How It Works
1. **Set Tariff**: Configure your current electricity price in the Settings tab.
2. **Add Rooms**: Define the spaces you want to monitor (e.g., "Server Room", "Finance Hub").
3. **Inventory Devices**: List the appliances in each room with their wattage and average usage hours.
4. **Analyze & Optimize**: Monitor the Dashboard for high-consumption areas and generate analytical reports for energy efficiency planning.

---
*Created and maintained as part of the Kadali Smart Energy Initiative.*
