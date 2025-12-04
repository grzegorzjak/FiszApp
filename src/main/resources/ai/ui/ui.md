# Architektura UI dla FiszApp – sekcja „Cards”

## 1. Przegląd struktury UI

UI FiszApp jest zorganizowane wokół głównego dziennego przepływu: **dodaj słowa → wygeneruj paczkę kart → zaakceptuj/edytuj/odrzuć → powtarzaj w SM‑2 → obserwuj statystyki**. Sekcja **„Cards”** jest jednym z głównych filarów nawigacji i odpowiada za całość pracy z fiszkami (generacja, akceptacja, archiwizacja).

Globalna nawigacja (top‑navbar) obejmuje pozycje:

- **Today** – dzienny dashboard (wejście do Cards i Reviews).
- **Words** – zarządzanie słowami wejściowymi do generacji.
- **Cards** – praca z fiszkami (Drafts, Accepted, Archived).
- **Reviews** – dzienne powtórki SM‑2 (tylko karty ACCEPTED).
- **Stats** – podstawowe statystyki (akceptacja, wykorzystanie promptów itp.).

Sekcja **„Cards”** wewnętrznie dzieli się na trzy podwidoki (tabs / zakładki), oparte na `status` karty:

- **Drafts & Generation** – karty `draft` + formularz generacji.
- **Accepted** – karty `accepted` (w SRS, dostępne do archiwizacji).
- **Archived** – karty `archived` (kosz / soft delete, tylko do podglądu).

Wszystkie trzy widoki wykorzystują ten sam komponent listy kart oraz wspólny komponent szczegółu karty (frontEn, backPl, usedWords). UI jest **server‑driven** (Thymeleaf + HTMX) z użyciem Alpine.js tylko do lokalnego stanu (formularze, modale, toasty).

## 2. Lista widoków

### 2.1 Layout główny aplikacji

- **Nazwa widoku:** Layout główny (shell)
- **Ścieżka widoku:** layout wspólny dla `/*` (np. szablon `base.html`)
- **Główny cel:** Zapewnienie spójnego szkieletu (top‑navbar, kontener, komunikaty globalne) dla wszystkich sekcji.
- **Kluczowe informacje do wyświetlenia:**
  - Logo/nazwa aplikacji.
  - Linki nawigacyjne: Today, Words, Cards, Reviews, Stats.
  - Informacja o zalogowanym użytkowniku / link do logowania/wylogowania.
  - Globalne komunikaty (np. utrata sesji, błąd 500).
- **Kluczowe komponenty widoku:**
  - `TopNavbar`
  - `MainContainer` (max‑width, padding)
  - `GlobalAlertPanel` (opcjonalny)
- **UX, dostępność i bezpieczeństwo:**
  - Widoczne wyróżnienie aktywnej sekcji (np. Cards podświetlone).
  - Linki nawigacyjne dostępne z klawiatury (tab index, focus styles).
  - Sekcje Cards i Reviews ukryte lub nieklikalne dla użytkowników niezalogowanych.
  - Przy odpowiedzi 401 – globalny komunikat i przekierowanie do logowania.

---

### 2.2 Today – dzienny dashboard

- **Nazwa widoku:** Today
- **Ścieżka widoku:** `/today` (domyślny widok po zalogowaniu)
- **Główny cel:** Być „planem dnia” – pokazać, co użytkownik ma dziś do zrobienia oraz dać szybkie skróty do Cards i Reviews.
- **Kluczowe informacje do wyświetlenia:**
  - Liczba draftów kart oczekujących na akceptację.
  - Liczba kart „due today” w Review.
  - Informacja o wykorzystanych promptach generacji i limitach.
  - Proste komunikaty „empty state” (np. brak draftów / brak powtórek).
- **Kluczowe komponenty widoku:**
  - `TodaySummaryCards` (kafelki z liczbami)
  - CTA:
    - „Go to drafts / Generate cards” → Cards → Drafts & Generation.
    - „Start today’s reviews” → Reviews.
- **UX, dostępność i bezpieczeństwo:**
  - Jeden wyraźny „primary CTA” (zwykle do Drafts & Generation) aby pchnąć użytkownika do głównej pracy.
  - Komunikaty tekstowe czytelne i zrozumiałe (np. „You have 7 drafts to review”).
  - Widok dostępny tylko dla zalogowanych; przy 401 – redirect do logowania.

---

### 2.3 Words – lista i formularz słów

- **Nazwa widoku:** Words
- **Ścieżka widoku:** `/words`
- **Główny cel:** Umożliwienie dodawania, edycji i usuwania słów/fraz, które będą używane przy generowaniu kart.
- **Kluczowe informacje do wyświetlenia:**
  - Lista słów z paginacją (tekst, język, status „Free/Used”).
  - Formularz dodawania/edycji słowa (EN/PL).
  - Informacja o liczbie słów „Free” (np. „You have X free words”). 
- **Kluczowe komponenty widoku:**
  - `WordForm` (Alpine.js)
  - `WordList` (server‑rendered tabela z HTMX ładowaniem częściowym)
  - `WordPaginationControls`
  - Panel z informacją „You have X free words” + przycisk „Generate cards” (link do Cards → Drafts & Generation).
- **UX, dostępność i bezpieczeństwo:**
  - Walidacje inline w formularzu (błędy pod polami).
  - Puste stany z jasnym CTA: „Add your first word”.
  - Komunikaty przy próbie usunięcia użytego słowa (ostrzeżenie o konsekwencjach dla kart/SRS).
  - Brak ekspozycji `userId` w UI – backend identyfikuje użytkownika po tokenie.

---

### 2.4 Cards – główny widok sekcji (shell)

- **Nazwa widoku:** Cards (shell)
- **Ścieżka widoku:** `/cards` (z parametrem/tabem `?tab=drafts|accepted|archived` lub segmentem URL)
- **Główny cel:** Zapewnienie wspólnego layoutu dla wszystkich podwidoków kart oraz przełączania między Drafts / Accepted / Archived.
- **Kluczowe informacje do wyświetlenia:**
  - Nagłówek sekcji („Cards”).
  - Zakładki/nawigacja wewnętrzna: Drafts, Accepted, Archived.
  - Globalny panel błędów/sukcesów dla operacji na kartach.
- **Kluczowe komponenty widoku:**
  - `CardsTabs` (przełącznik Drafts/Accepted/Archived)
  - `CardsAlertPanel`
  - Slot na listę kart + panel generacji (w zależności od podwidoku).
- **UX, dostępność i bezpieczeństwo:**
  - Wyraźny aktywny tab (ARIA `role="tablist"` / `role="tab"`).
  - Linki/taby dostępne z klawiatury.
  - Komunikaty statusu (`role="status"`) dla akcji Accept/Reject/Archive/Generate.

---

### 2.5 Cards – Drafts & Generation

- **Nazwa widoku:** Cards – Drafts & Generation
- **Ścieżka widoku:** `/cards?tab=drafts` (domyślny podwidok sekcji Cards)
- **Główny cel:** Zarządzanie draftami oraz uruchamianie generacji nowych kart.
- **Kluczowe informacje do wyświetlenia:**
  - Formularz generacji paczki (parametr `maxCards`, licznik pozostałych promptów, informacje o wymaganiach co do „free words”). 
  - Lista kart w statusie `draft` z paginacją (frontEn, skrócony backPl, daty).
  - Szczegóły draftu: pełne zdanie EN/PL, lista `usedWords` z linkami do Words.
- **Kluczowe komponenty widoku:**
  - `CardGenerationForm`
  - `CardList` (dla statusu `draft`)
  - `CardDetailDrawer` / `CardDetailAccordion`
  - Akcje na kartach: `AcceptCardButton`, `RejectCardButton`, `EditCardButton`
  - `CardsPaginationControls`
- **UX, dostępność i bezpieczeństwo:**
  - Formularz generacji nad listą, tak aby użytkownik widział zarówno nowe, jak i istniejące drafty.
  - Po wysłaniu formularza generacji – loader + komunikat o wyniku (ile kart wygenerowano, ile pozostało promptów).
  - Akcje Accept/Reject dostępne jako przyciski tekstowe + ikony (opcjonalnie), z jasnym opisem („Accept card”, „Reject card”).
  - Edycja draftu wymusza zachowanie oryginalnego zestawu `usedWords` (walidacja po stronie backendu, komunikat przy błędzie).
  - Błędy: brak wystarczającej liczby `free words`, przekroczenie limitu promptów, naruszenie reguł zdań – pokazane w panelu alertów i/lub przy polach formularza.

---

### 2.6 Cards – Accepted

- **Nazwa widoku:** Cards – Accepted
- **Ścieżka widoku:** `/cards?tab=accepted`
- **Główny cel:** Podgląd i zarządzanie kartami zaakceptowanymi, które są włączone do SM‑2; główna akcja to archiwizacja (soft delete).
- **Kluczowe informacje do wyświetlenia:**
  - Lista kart `accepted` (frontEn, skrócony backPl, daty utworzenia/akceptacji).
  - Wskazanie, czy karta jest aktywna w SRS (domyślnie wszystkie accepted).
  - Szczegóły karty (wspólny komponent z Drafts) + lista `usedWords`.
- **Kluczowe komponenty widoku:**
  - `CardList` (dla statusu `accepted`)
  - `CardDetailDrawer` / `CardDetailAccordion`
  - `ArchiveCardButton` (akcja PATCH → `status=archived`)
  - `ArchiveConfirmModal`
- **UX, dostępność i bezpieczeństwo:**
  - Brak przycisków Accept/Reject – tylko podgląd + Archive.
  - Archiwizacja wymaga potwierdzenia w modal’u (z opisem konsekwencji: karta znika z Reviews).
  - Po udanym archiwizowaniu:
    - Karta znika z listy Accepted.
    - Można wyświetlić toast „Card archived” oraz odświeżyć licznik na Today/Stats (pośrednio).
  - W razie błędów (np. próba archiwizacji karty, która nie jest `accepted`) – czytelny komunikat biznesowy.

---

### 2.7 Cards – Archived

- **Nazwa widoku:** Cards – Archived
- **Ścieżka widoku:** `/cards?tab=archived`
- **Główny cel:** Podgląd kart zarchiwizowanych (kosz) – zarówno odrzuconych draftów, jak i kart zarchiwizowanych manualnie; brak akcji przywracania w MVP.
- **Kluczowe informacje do wyświetlenia:**
  - Lista kart `archived` (frontEn, skrócony backPl, daty utworzenia/archiwizacji).
  - Szczegóły karty (wspólny komponent) w trybie tylko do odczytu.
- **Kluczowe komponenty widoku:**
  - `CardList` (dla statusu `archived`)
  - `CardDetailDrawer` / `CardDetailAccordion` (bez przycisków akcji)
- **UX, dostępność i bezpieczeństwo:**
  - W nagłówku widoczne wyraźne oznaczenie (np. „Archived cards – read only”). 
  - Brak przycisków zmian statusu – użytkownik nie może przypadkowo przywrócić kart.
  - Puste stany (np. „You don’t have any archived cards yet”).

---

### 2.8 Reviews – powtórki SM‑2

- **Nazwa widoku:** Reviews
- **Ścieżka widoku:** `/reviews`
- **Główny cel:** Przeprowadzenie dziennych powtórek kart w stanie `accepted` zgodnie z algorytmem SM‑2.
- **Kluczowe informacje do wyświetlenia:**
  - Aktualna karta do powtórki (frontEn → backPl po akcji „show answer”). 
  - Oceny SM‑2: Again/Hard/Good/Easy.
  - Licznik: ile kart zostało dziś do powtórzenia / ile wykonano.
- **Kluczowe komponenty widoku:**
  - `ReviewCardPane` (prezentacja pojedynczej karty)
  - `ShowAnswerButton`
  - `Sm2RatingButtons` (Again/Hard/Good/Easy)
  - `ReviewProgressBar`
  - `ReviewDoneState` („You’re done for today”).
- **UX, dostępność i bezpieczeństwo:**
  - Flow 1‑po‑1, klawiaturowe skróty (opcjonalnie) do szybkiego oceniania.
  - Po archiwizacji karty w Cards, karta nie powinna już trafić na ten ekran (logika po stronie backendu).
  - Czytelne zakończenie sesji („No more cards due today”).

---

### 2.9 Stats – statystyki

- **Nazwa widoku:** Stats
- **Ścieżka widoku:** `/stats`
- **Główny cel:** Prezentacja podstawowych metryk dotyczących generacji, akceptacji i powtórek, w tym acceptance_rate.
- **Kluczowe informacje do wyświetlenia:**
  - Dzienna liczba wygenerowanych, zaakceptowanych i odrzuconych kart.
  - acceptance_rate per dzień.
  - Liczba wykorzystanych promptów generacji.
- **Kluczowe komponenty widoku:**
  - `StatsSummaryCards`
  - `StatsTableDailyMetrics`
  - (opcjonalnie) proste wykresy (np. liniowy/kolumnowy).
- **UX, dostępność i bezpieczeństwo:**
  - Prosty wybór zakresu dat (np. select „Last 7 days” / „Last 30 days”). 
  - Czytelne opisy metryk, bez żargonu technicznego.
  - Widok dostępny tylko dla zalogowanych użytkowników.

---

### 2.10 Widoki uwierzytelniania (wysoki poziom)

- **Nazwa widoku:** Auth – Login/Register/Reset
- **Ścieżki widoków:** `/login`, `/register`, `/reset-password`
- **Główny cel:** Obsługa rejestracji, logowania i resetu hasła.
- **Kluczowe informacje do wyświetlenia:**
  - Pola formularzy (e‑mail, hasło, potwierdzenie hasła, nowe hasło).
  - Komunikaty o błędach logowania/hasła oraz linki do resetu.
- **Kluczowe komponenty widoku:**
  - `AuthForm`
  - `AuthAlertPanel`
- **UX, dostępność i bezpieczeństwo:**
  - Jasne komunikaty o błędach („Invalid email or password”).
  - Po poprawnym zalogowaniu – redirect do Today.
  - Ochrona rate‑limit po stronie backendu; UI pokazuje ogólny komunikat bez ujawniania szczegółów.

## 3. Mapa podróży użytkownika

### 3.1 Główny dzienny przepływ (happy path)

1. **Logowanie**
   - Użytkownik otwiera aplikację i loguje się przez widok Login.
   - Po sukcesie zostaje przekierowany do **Today**.

2. **Sprawdzenie planu dnia (Today)**
   - Na Today widzi:
     - liczbę draftów do przejrzenia,
     - liczbę kart do powtórki,
     - informację o dostępnych promptach.
   - Kliknięcie CTA **„Go to drafts / Generate cards”** przenosi go do **Cards → Drafts & Generation**.

3. **Generacja kart (Cards → Drafts & Generation)**
   - Użytkownik sprawdza, ile ma „free words” (informacja z Words / Today).
   - Wypełnia formularz generacji (np. `maxCards = 10`) i wysyła.
   - Po sukcesie:
     - lista draftów odświeża się,
     - użytkownik widzi komunikat „Generated 7 cards, you have 1 prompt left today”.

4. **Przegląd i akceptacja draftów**
   - Użytkownik rozwija szczegóły pierwszego draftu (drawer/accordion).
   - Ocenia jakość zdania EN/PL, opcjonalnie delikatnie edytuje treść.
   - Następnie wybiera:
     - **Accept** – karta przechodzi do `accepted`, zostaje dodana do SRS.
     - **Reject** – karta trafia do `archived`, a powiązane słowa wracają do puli „free words”.
   - Użytkownik powtarza krok dla całej paczki.

5. **Powtórki (Reviews)**
   - Po zakończeniu pracy z draftami (lub w dowolnym momencie z Today) użytkownik przechodzi do **Reviews**.
   - Przegląda kolejne karty, oceniając je (Again/Hard/Good/Easy).
   - Po ostatniej karcie widzi komunikat „You’re done for today”.

6. **Podgląd postępów (Stats)**
   - Użytkownik przechodzi do **Stats**, aby zobaczyć:
     - ile kart wygenerował i zaakceptował,
     - acceptance_rate dzisiaj i w poprzednich dniach,
     - wykorzystanie promptów.

### 3.2 Przepływ w obrębie „Words ↔ Cards”

- Z **Words**:
  - Panel „You have X free words” + przycisk „Generate cards” kierują użytkownika do **Cards → Drafts & Generation**.
  - W kolumnie „Status” słowa oznaczone jako „Used” mają link „View card”:
    - kliknięcie otwiera **Cards → Accepted** (lub `Get card` po ID) i pokazuje szczegóły powiązanej karty.
- Z **Cards**:
  - W szczegółach karty lista `usedWords` zawiera linki prowadzące do **Words**, z filtrem/przewinięciem do powiązanych słów.

### 3.3 Przypadki brzegowe w podróży użytkownika

- **Brak wystarczającej liczby słów („free words”)**
  - Użytkownik na Cards → Drafts & Generation próbuje wygenerować paczkę, ale ma <2 `free words`.
  - Widzi komunikat w panelu alertu: informacja o konieczności dodania słów + CTA/link „Go to Words”.
- **Brak draftów**
  - Liste draftów jest pusta:
    - użytkownik widzi komunikat „You don’t have any drafts yet. Generate your first batch.” + przycisk do generacji.
- **Brak kart do powtórki**
  - Reviews informuje „No cards due today. Come back tomorrow.”.

## 4. Układ i struktura nawigacji

### 4.1 Nawigacja główna (top‑navbar)

- Pozioma belka na górze ekranu, dostępna na wszystkich widokach.
- Elementy:
  - Logo/nazwa FiszApp po lewej.
  - Linki: Today, Words, Cards, Reviews, Stats (środkowa/prawa część).
  - Informacje o użytkowniku (np. e‑mail) + przycisk Logout po prawej.
- Zasady:
  - Sekcje Cards/Reviews/Stats widoczne tylko dla zalogowanych; dla niezalogowanych – CTA „Log in”.
  - Aktywna sekcja wyróżniona (kolor, underline).

### 4.2 Nawigacja wewnętrzna sekcji „Cards”

- Bezpośrednio pod nagłówkiem „Cards” znajduje się pasek zakładek:
  - **Drafts & Generation**
  - **Accepted**
  - **Archived**
- Przełączanie zakładek:
  - Zmienia parametr `tab` w URL lub podścieżkę.
  - Ładuje częściowy widok listy kart odpowiedniego statusu przez HTMX.
- Responsywność:
  - Na desktopie – pełne zakładki w poziomie.
  - Na mobile – prosty przełącznik (np. segmented control) lub dropdown.

### 4.3 Skróty (CTA) między sekcjami

- Today:
  - CTA do Cards → Drafts & Generation.
  - CTA do Reviews.
- Words:
  - Panel „You have X free words” + CTA „Generate cards”.
- Cards:
  - W szczegółach kart – linki do Words (do konkretnych słów).

## 5. Kluczowe komponenty

Poniżej lista komponentów UI, które są używane w wielu widokach i stanowią fundament architektury:

1. **TopNavbar**
   - Zawiera logo, główne linki nawigacji i sekcję użytkownika.
   - Odpowiada za widoczność sekcji w zależności od stanu zalogowania.

2. **MainContainer**
   - Odpowiada za spójny layout (max‑width, marginesy, tło) w całej aplikacji.

3. **GlobalAlertPanel**
   - Prezentuje globalne komunikaty (błędy, sukcesy) niezwiązane tylko z jednym formularzem.
   - Używany m.in. do komunikatów o 401, 500, błędach generacji.

4. **CardsTabs**
   - Przełącznik zakładek w sekcji Cards (Drafts/Accepted/Archived).
   - Implementuje ARIA `role="tablist"` dla dostępności.

5. **CardGenerationForm**
   - Formularz dla `/api/generation-batches`.
   - Wyświetla dostępne parametry, liczniki promptów, błędy walidacji.

6. **CardList**
   - Wspólny komponent tabeli/listy kart dla wszystkich statusów.
   - Parametryzowany statusem (Drafts, Accepted, Archived) i akcjami, które są dozwolone w danym kontekście.
   - Obsługuje paginację i sortowanie (przekazywane parametrami zapytania).

7. **CardDetailDrawer / CardDetailAccordion**
   - Prezentuje szczegóły jednej karty: pełny frontEn, backPl, listę `usedWords`, daty.
   - W zależności od kontekstu pokazuje przyciski akcji (Accept/Reject/Edit/Archive) lub działa w trybie read‑only.

8. **CardsPaginationControls**
   - Wspólny komponent nawigacji po stronach listy kart.
   - Oparta o pattern z listy „Words” (Previous/Next + informacja o zakresie).

9. **ArchiveConfirmModal**
   - Modal potwierdzający archiwizację karty.
   - Opisuje konsekwencje („This card will be removed from reviews”).

10. **WordBadge / WordLink**
    - Prezentuje pojedyncze słowo (tekst + status) oraz linkuje do widoku Words.
    - Używany w szczegółach kart (lista `usedWords`).

11. **EmptyState**
    - Komponent dla pustych list (Words, Cards, Reviews), który wyświetla ikonę, krótki opis i CTA („Add words”, „Generate cards”, „Come back tomorrow”).

12. **AuthForm**
    - Wspólny komponent dla Login/Register/Reset, z polami, walidacjami i panelami błędów.

13. **ReviewCardPane**
    - Prezentuje pojedynczą kartę w review (front → reveal back) wraz z przyciskami ocen SM‑2.

14. **StatsSummaryCards & StatsTableDailyMetrics**
    - Komponenty używane w sekcji Stats do prezentacji metryk w formie kafelków oraz tabeli.

---

Ta architektura UI zapewnia spójny, server‑driven interfejs, w którym sekcja **„Cards”** stanowi centralny element dziennego przepływu użytkownika – od generacji paczek, przez akceptację i powtórki, aż po archiwizację. Dzięki wykorzystaniu wspólnych komponentów (lista kart, szczegóły karty, panele alertów, paginacja) minimalizujemy złożoność implementacji i utrzymania, a jednocześnie zachowujemy jasny i przewidywalny UX.
