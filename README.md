# Zanieczyszczenie Powietrza

Aplikacja mobilna do monitorowania jakości powietrza, stworzona przy użyciu Jetpack Compose i Firebase.

## Opis

Aplikacja "Zanieczyszczenie Powietrza" pozwala użytkownikom na śledzenie aktualnych danych o jakości powietrza w wybranych lokalizacjach. Wykorzystuje API do pobierania danych o zanieczyszczeniach i prezentuje je w przystępnej formie.

## Funkcje

- Monitorowanie jakości powietrza w czasie rzeczywistym
- Wybór i zapisywanie ulubionych lokalizacji
- Uwierzytelnianie użytkowników z użyciem Firebase Auth

## Technologie

- Kotlin
- Jetpack Compose
- Firebase (Authentication, Firestore)
- Retrofit do komunikacji z API
- MVVM (Model-View-ViewModel) architektura

## Wymagania

- Android Studio Hedgehog (2023.1.1) lub nowszy
- Minimum SDK: 24 (Android 7.0)
- Gradle 8.0+

## Instalacja

> [!TIP]
> 1. Sklonuj repozytorium:
> git clone https://github.com/ms-matthew/zanieczyszczeniepowietrza.git
> 2. Otwórz projekt w Android Studio i zsynchronizuj z Gradle.
> 3. Uruchom aplikację na emulatorze lub urządzeniu.


## Konfiguracja

Aby korzystać z Firebase, musisz:
1. Utworzyć projekt w [Firebase Console](https://console.firebase.google.com/)
2. Dodać swoją aplikację Android do projektu Firebase
3. Pobrać plik `google-services.json` i umieścić go w katalogu `app/`

## Struktura projektu

app/
├── src/
│ └── main/
│ ├── java/com/example/zanieczyszczeniepowietrza/
│ │ ├── data/ # Klasy danych i repozytoria
│ │ ├── ui/ # Komponenty UI i ekrany
│ │ ├── viewmodel/ # ViewModele dla ekranów
│ │ └── MainActivity.kt # Główna aktywność aplikacji
│ └── res/ # Zasoby aplikacji (layouty, stringi, drawable itp.)


## Kontakt

Mateusz Stachowicz - [mateusz.stachowicz1@wp.pl] - [https://github.com/ms-matthew]

![image](https://github.com/user-attachments/assets/4bf30cbd-9e88-413b-be3f-f97ede3798ef)

![image](https://github.com/user-attachments/assets/3de0ff76-a882-4f93-813a-19b94fabb8f3)

![image](https://github.com/user-attachments/assets/136c848a-6057-43d0-ae00-10ccc0a234a9)

