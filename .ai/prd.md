# Dokument wymagań produktu (PRD) - FiszApp
## 1. Przegląd produktu
FiszApp to webowa aplikacja do nauki języków, która automatyzuje tworzenie wysokiej jakości fiszek w formie zdań (EN→PL) z wykorzystaniem słów dostarczanych przez użytkownika. Aplikacja łączy prosty system kont, generowanie treści przez AI, dzienny przegląd i akceptację fiszek oraz integrację z algorytmem powtórek SM‑2. MVP koncentruje się na prostocie, niskich kosztach operacyjnych i jasnych ograniczeniach (dzienny harmonogram, brak zaawansowanych integracji).

Cele produktu:
- Zredukować czas i barierę tworzenia fiszek o wysokiej jakości (zdania zamiast pojedynczych słów).
- Zwiększyć motywację do regularnej nauki SRS poprzez wygodę AI i prosty rytuał dzienny.
- Osiągnąć ≥75% akceptacji AI‑fiszki i ≥75% udziału AI w powstawaniu nowych fiszek.

Założenia kluczowe:
- Fiszki zawsze w formacie EN (front) = PL (back), treść zdania 4 – 8 słów, poziom ~B1/B2, neutralne, bez rzadkich idiomów.
- Każda fiszka musi wykorzystywać co najmniej 2 „słowa” (słowo/fraza/fragment) dodane przez użytkownika.
- Jedno „słowo” może trafić tylko do jednej fiszki (per użytkownik).
- Prosty system użytkowników i prosta integracja z gotowym SM‑2 (Again/Hard/Good/Easy).

## 2. Problem użytkownika
Tworzenie dobrych fiszek manualnie jest czasochłonne. Powszechne narzędzia skupiają się na pojedynczych słowach, co osłabia kontekst i zapamiętywanie. Użytkownicy:
- tracą czas na wymyślanie i redagowanie zdań,
- zniechęcają się brakiem szybkiej gratyfikacji (długi czas przygotowania materiału),
- mają trudność w utrzymaniu rytmu powtórek bez wygodnego „daily flow”,
- potrzebują, by to samo „słowo” nie pojawiało się w wielu fiszkach (spójność, brak dublowania),
- chcą mieć kontrolę: akceptować/odrzucać/edytować propozycje oraz wiedzieć, które „słowa” zostały wykorzystane.

Jak FiszApp rozwiązuje problem:
- Automatyzuje generowanie zdań EN z tłumaczeniem PL na bazie „słów” użytkownika.
- Zapewnia dzienny przegląd do akceptacji (paczki) i prosty interfejs powtórek SM‑2.
- Utrzymuje reguły jakości: długość, poziom, brak rzadkich idiomów, najpopularniejsze znaczenia.
- Gwarantuje unikalne „zużycie” słowa w dokładnie jednej fiszce per użytkownik.
- Obsługuje zmiany/usunięcia „słów”: fiszki są automatycznie wycofywane z SRS i ponownie trafiają do akceptacji po regeneracji.

## 3. Wymagania funkcjonalne
3.1 Konta i dostęp
- Rejestracja e‑mail + hasło, logowanie, wylogowanie.
- Weryfikacja adresu e‑mail linkiem, reset hasła przez e‑mail.
- Sesje 24 h oparte o JWT; proste odświeżanie przez ponowne logowanie.
- Rate‑limit logowania (np. 5 prób/15 min/IP).

3.2 Zarządzanie „słowami” (Input)
- Dodanie „słowa” (pojedyncze słowo, fraza, fragment zdania) w EN lub PL.
- Podgląd listy „słów”, edycja, usuwanie.
- Normalizacja do formy kanonicznej (np. lowercase, trimming, prosty lemat/odmiana), z zachowaniem oryginału do wyświetlania.
- Walidacje: minimalna i maksymalna długość, znaki dozwolone, brak pustych wpisów.
- Oznaczenie statusu „wolne”/„zużyte” (czy zostało użyte w fiszce).

3.3 Generowanie fiszek (Card)
- Źródło: zestaw „słów” użytkownika; selekcja dowolna przez AI z zachowaniem reguł.
- Reguły generacji:
  - zdanie EN 4–8 słów (~B1/B2), neutralne treści, brak rzadkich idiomów,
  - tłumaczenie PL (najbardziej popularne znaczenia),
  - co najmniej 2 „słowa” z listy użytkownika,
  - każde „słowo” może być użyte tylko w jednej fiszce (per użytkownik),
  - treść angielska na froncie, polska na rewersie,
  - przechowywanie listy „słów” wykorzystanych w fiszce (transparentność).
- Harmonogram:
  - na żądanie (on‑demand)
- Limity kosztowe/operacyjne:
  - 1 prompt generuje do 10 nowych fiszek,
- Stany fiszki: draft (do akceptacji) → accepted (w SRS) → archived/invalidated (po zmianie/usunięciu „słów”).
- Edycja draftu przez użytkownika: dozwolona pod warunkiem zachowania wykorzystanych „słów”.

3.4 Przegląd i akceptacja
- Ekran paczki dziennej: lista fiszek w stanie draft.
- Akcje per fiszka: Akceptuj / Odrzuć / Edytuj.
- Odrzucone fiszki nie zużywają „słów” (słowa wracają do puli „wolnych”).
- Podgląd użytych „słów” w fiszce (oznaczenie i link do szczegółów „słowa”).

3.5 Synchronizacja zmian „słów”
- Edycja/usunięcie „słowa” użytego w zaakceptowanej fiszce:
  - natychmiastowe wycofanie fiszki z SRS,
  - zwrot wszystkich „słów” z tej fiszki do puli „wolnych”,
  - oznaczenie fiszki jako archived/invalidated,
  - ponowne wykorzystanie „słów” przy kolejnej generacji (kolejna paczka).

3.6 Powtórki SM‑2
- Integracja z gotowym SM‑2, interfejs ocen: Again/Hard/Good/Easy.
- Liczniki i harmonogram SM‑2 utrzymywane w bazie.
- Przejrzyste stany: „due today”, „upcoming”, „snooze na jutro” (1×/karta).

3.7 Statystyki i limity
- acceptance_rate = zaakceptowane / wygenerowane, liczony raz dziennie (np. nocą).
- Widok dziennych metryk: wygenerowane, zaakceptowane, odrzucone

3.8 Moderacja i jakość
- Weryfikacje jakości: długość zdania, brak rzadkich idiomów.

3.9 RODO i bezpieczeństwo
- Usunięcie konta i wszystkich danych na żądanie.

3.10 UX i dostępność
- Płynny przepływ: Dodaj „słowa” → Paczka → Akceptacja → SM‑2 → Powtórki.
- Czytelne oznaczanie „słów” użytych w fiszce.
- Stany pustej listy (empty states) i jasne komunikaty błędów.
- Przystosowanie do desktop (MVP web), responsywność podstawowa.

## 4. Granice produktu
- W zakresie: web‑aplikacja (desktop‑first), rejestracja/logowanie, CRUD „słów” i fiszek, generacja AI w paczkach, akceptacja/edycja/odrzucenie, integracja SM‑2, harmonogram, proste statystyki, RODO delete, podstawowa moderacja.
- Poza zakresem (MVP): własny zaawansowany algorytm SRS, import/eksport wielu formatów (PDF, DOCX), współdzielenie zestawów, integracje z zewnętrznymi platformami, aplikacje mobilne.
- Ograniczenia operacyjne: 1 prompt ≤10 fiszek, powtórki ≤30/dzień, generacja on‑demand.
- Techniczne uproszczenia: brak real‑time współpracy, brak tagowania/kategoryzacji, brak wersjonowania fiszek poza statusem.

## 5. Historyjki użytkowników
US‑001
Tytuł: Rejestracja konta
Opis: Jako nowy użytkownik chcę założyć konto e‑mail/hasło, aby móc zapisywać „słowa” i fiszki.
Kryteria akceptacji:
- Formularz z e‑mail, hasło, potwierdzenie hasła; walidacje podstawowe.
- Wysłany link weryfikacyjny e‑mail; konto aktywne po weryfikacji.
- Po rejestracji i weryfikacji mogę się zalogować.

US‑002
Tytuł: Logowanie/wylogowanie
Opis: Jako użytkownik chcę się zalogować i wylogować, by bezpiecznie korzystać z aplikacji.
Kryteria akceptacji:
- Logowanie e‑mail + hasło; sesja 24 h (JWT).
- Po wylogowaniu token nieważny; dostęp do zasobów zablokowany.
- Rate‑limit logowania przy wielokrotnych nieudanych próbach.

US‑003
Tytuł: Reset hasła
Opis: Jako użytkownik chcę zresetować hasło przez e‑mail.
Kryteria akceptacji:
- Formularz „zapomniałem hasła” wysyła link resetu.
- Ustawienie nowego hasła po kliknięciu w link.
- Po resecie mogę zalogować się nowym hasłem.

US‑004
Tytuł: Dodanie „słowa”
Opis: Jako użytkownik chcę dodać słowo/frazę/fragment (EN/PL), aby AI mogło tworzyć fiszki.
Kryteria akceptacji:
- Pole tekstowe z walidacjami (długość, znaki).
- Zapis oryginału i formy kanonicznej.
- Po zapisie status „wolne”.

US‑005
Tytuł: Przegląd i edycja „słów”
Opis: Jako użytkownik chcę zobaczyć listę „słów”, edytować lub usunąć je.
Kryteria akceptacji:
- Lista z paginacją/sortowaniem podstawowym.
- Edycja aktualizuje oryginał i przelicza kanoniczną formę.
- Usunięcie dostępne; ostrzeżenie, jeśli „słowo” jest użyte w zaakceptowanej fiszce.

US‑006
Tytuł: Widok statusu „zużycia” słowa
Opis: Jako użytkownik chcę wiedzieć, czy „słowo” zostało użyte w fiszce.
Kryteria akceptacji:
- Kolumna/znacznik „wolne”/„zużyte”.
- Link do fiszki, jeśli zużyte.

US‑008
Tytuł: Generacja on‑demand
Opis: Jako użytkownik chcę ręcznie wywołać generację paczki.
Kryteria akceptacji:
- Gdy nie wyczerpano dziennych limitów promptów, mogę wygenerować paczkę.
- Komunikat zliczający: ile fiszek powstało.
- Po limicie otrzymuję jasny komunikat o braku dostępnych promptów.

US‑009
Tytuł: Ekran akceptacji paczki
Opis: Jako użytkownik chcę akceptować/odrzucać/edytować fiszki draft.
Kryteria akceptacji:
- Lista draftów z akcjami: Akceptuj/Odrzuć/Edytuj.
- Edycja wymaga zachowania użytych „słów”.
- Odrzucenie zwraca „słowa” do statusu „wolne”.

US‑010
Tytuł: Podgląd użytych „słów”
Opis: Jako użytkownik chcę zobaczyć, które „słowa” zostały użyte w fiszce.
Kryteria akceptacji:
- Sekcja „Użyte słowa” w szczegółach fiszki.
- Kliknięcie przenosi do szczegółów „słowa”.

US‑011
Tytuł: Unikalne zużycie słów
Opis: Jako użytkownik oczekuję, że każde „słowo” trafi do maksymalnie jednej fiszki.
Kryteria akceptacji:
- Próba użycia „słowa” w drugiej fiszce (per użytkownik) blokowana.
- W bazie unikalne powiązanie słowo→fiszka (per user).

US‑012
Tytuł: Aktualizacja po zmianie „słowa”
Opis: Jako użytkownik chcę, aby zmiana/usunięcie „słowa” wycofywała powiązaną fiszkę z SRS.
Kryteria akceptacji:
- Fiszka opuszcza SRS natychmiast po zmianie/usunięciu „słowa”.
- „Słowa” z tej fiszki wracają do puli „wolnych”.
- Fiszka oznaczona archived/invalidated; gotowa do regeneracji w kolejnej paczce.

US‑013
Tytuł: Powtórki SM‑2
Opis: Jako użytkownik chcę codziennych powtórek do 30 fiszek.
Kryteria akceptacji:
- Lista „due” sortowana: najbardziej zaległe najpierw.
- Oceny Again/Hard/Good/Easy modyfikują stan SM‑2.

US‑014
Tytuł: Statystyki akceptacji
Opis: Jako użytkownik chcę widzieć dzienną akceptację i podstawowe metryki.
Kryteria akceptacji:
- acceptance_rate = zaakceptowane/wygenerowane przeliczany raz dziennie.
- Widok: wygenerowane, zaakceptowane, odrzucone, wykorzystane prompty.
- Filtr per dzień/zakres dat (podstawowy).

US‑015
Tytuł: Walidacje jakości zdań
Opis: Jako użytkownik chcę, by generowane zdania spełniały reguły jakości.
Kryteria akceptacji:
- Długość 4–8 słów EN, poziom ~B1/b2, bez rzadkich idiomów.
- Tłumaczenie PL z najpopularniejszym znaczeniem.
- Niespełnienie reguł → odrzucenie i komunikat w logu/telemetrii.

US‑016
Tytuł: Puste stany i komunikaty
Opis: Jako użytkownik chcę czytelnych komunikatów, gdy nie ma fiszek/słów.
Kryteria akceptacji:
- Empty state dla list „słów”, paczek, powtórek.
- Czytelne CTA: „Dodaj słowa”, „Wygeneruj paczkę”, „Wróć jutro”.

US‑017
Tytuł: Usunięcie konta (RODO)
Opis: Jako użytkownik chcę usunąć konto i dane.
Kryteria akceptacji:
- Usunięcie użytkownika i powiązanych danych w rozsądnym czasie.

US‑018
Tytuł: Autoryzacja dostępu
Opis: Jako użytkownik oczekuję, że tylko ja mam dostęp do moich danych.
Kryteria akceptacji:
- Endpoints zabezpieczone JWT; brak dostępu bez ważnego tokena.
- Próba odczytu cudzych danych kończy się 403.

US‑019
Tytuł: Edycja draftu z zachowaniem „słów”
Opis: Jako użytkownik chcę móc poprawić treść draftu bez zmiany zestawu „słów”.
Kryteria akceptacji:
- Edytor wymusza obecność oryginalnego zestawu „słów”.
- Próba usunięcia „słowa” blokowana komunikatem.
- Zapis aktualizuje draft, nie zmienia statusu „zużycia”.

US‑020
Tytuł: Reguła ≥2 „słów”
Opis: Jako użytkownik chcę mieć pewność, że fiszka używa przynajmniej 2 „słów”.
Kryteria akceptacji:
- Walidacja po stronie generacji i edytora.
- Próba akceptacji fiszki naruszającej regułę jest blokowana.

US‑021
Tytuł: Wybór najpopularniejszego znaczenia
Opis: Jako użytkownik chcę naturalnych tłumaczeń.
Kryteria akceptacji:
- Słowa wieloznaczne tłumaczone według najpopularniejszego znaczenia.

US‑023
Tytuł: Obsługa niewystarczającej puli „słów”
Opis: Jako użytkownik chcę informacji, gdy nie mam dość „słów” do generacji.
Kryteria akceptacji:
- Jeśli mniej niż 2 „wolne” słowa – generacja blokowana z komunikatem.
- Sugestia: „Dodaj przynajmniej 2 nowe słowa, aby wygenerować fiszki”.

US‑024
Tytuł: Kolizje/duplikaty kanoniczne
Opis: Jako użytkownik chcę uniknąć dublowania „słów” przez odmiany/pisownię.
Kryteria akceptacji:
- Dodanie nowego „słowa” o tej samej formie kanonicznej wywołuje ostrzeżenie i blokadę lub merge.
- Lista pokazuje oryginał + formę kanoniczną.

US‑025
Tytuł: Bezpieczne API publiczne
Opis: Jako integrator chcę, by backend miał proste, zabezpieczone endpointy.
Kryteria akceptacji:
- API REST z autoryzacją JWT.
- Dokumentacja OpenAPI dla MVP.

## 6. Metryki sukcesu
- Główne KPI:
  - acceptance_rate ≥ 75% (zaakceptowane/wygenerowane dziennie).
  - udział AI‑generacji ≥ 75% wszystkich nowych fiszek.
- Operacyjne KPI:
  - dzienna liczba wygenerowanych/zaakceptowanych/odrzuconych fiszek,
