# Magazin Online

Aplicație web pentru un magazin online cu funcționalități de vânzare și cumpărare de produse, inclusiv negociere de prețuri.

## Funcționalități

### Administrator
- Verificarea și aprobarea vânzătorilor noi
- Dezactivarea conturilor de vânzător
- Vizualizarea tuturor produselor
- Vizualizarea istoricului vânzărilor

### Vânzător
- Adăugarea de produse (cu preț fix sau negociabil)
- Vizualizarea produselor proprii
- Ștergerea produselor
- Vizualizarea ofertelor pentru produsele proprii
- Aprobarea sau respingerea ofertelor
- Vizualizarea istoricului vânzărilor proprii

### Cumpărător
- Vizualizarea tuturor produselor disponibile
- Cumpărarea produselor cu preț fix
- Trimiterea de oferte pentru produsele cu preț negociabil
- Vizualizarea istoricului cumpărăturilor

## Tehnologii utilizate

- Spring Boot
- Spring Security
- Thymeleaf
- Bootstrap 5
- Firebase Firestore (bază de date)

## Configurare și rulare

### Cerințe
- Java 11 sau mai nou
- Gradle
- Cont Firebase și fișier de configurare serviceAccountKey.json

### Pași pentru rulare
1. Clonați repository-ul
2. Plasați fișierul serviceAccountKey.json în directorul rădăcină al proiectului
3. Rulați aplicația folosind Gradle:
   ```
   ./gradlew bootRun
   ```
   sau
   ```
   gradlew.bat bootRun
   ```
4. Accesați aplicația în browser la adresa http://localhost:8080

## Conturi predefinite

- Administrator: admin@email.com / admin
- Vânzător și cumpărător: creați-vă propriile conturi prin înregistrare

## Structura proiectului

- `src/main/java/App` - Clasa principală și configurarea Firebase
- `src/main/java/model` - Modelele de date (User, Product, Offer, SaleHistory)
- `src/main/java/service` - Servicii pentru interacțiunea cu baza de date
- `src/main/java/controller` - Controllere pentru gestionarea cererilor HTTP
- `src/main/java/config` - Configurări Spring (Security)
- `src/main/resources/templates` - Template-uri Thymeleaf pentru interfața utilizator
- `src/main/resources/static` - Resurse statice (CSS, JavaScript, imagini)

## Funcționalități detaliate

### Produse
- Fiecare produs are un ID, nume, preț, descriere, vânzător
- Produsele pot fi cu preț fix sau negociabil
- Produsele negociabile au și un preț minim (ascuns cumpărătorilor)

### Oferte
- Cumpărătorii pot face oferte pentru produsele negociabile
- Ofertele sub prețul minim sunt respinse automat
- Vânzătorii pot aproba sau respinge ofertele

### Vânzări
- Când un produs este cumpărat sau o ofertă este aprobată, se înregistrează în istoricul vânzărilor
- Produsul și ofertele asociate sunt șterse din sistem după vânzare