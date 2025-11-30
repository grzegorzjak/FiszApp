1. Cel widoku

Na jednym widoku chcesz móc:

Zobaczyć listę swoich słów.

Dodać nowe słowo.

Zedytować istniejące słowo.

Usunąć istniejące słowo.

2. Układ widoku (prosty schemat)

Proponuję taki układ od góry do dołu:

Nagłówek strony

Tytuł: Words

Krótki opis: np. „Tutaj zarządzasz słowami do generowania fiszek”.

Sekcja formularza (Dodaj / Edytuj słowo)
Jeden formularz, który działa w dwóch trybach:

Tryb Dodawania (domyślnie po wejściu na stronę)

Tryb Edycji (po kliknięciu „Edit” przy słowie)

Pola:

originalText – tekst słowa/frazy

language – select: EN / PL

Ukryte pole id – tylko w trybie edycji (lub w modelu, zależy od implementacji)

Przyciski:

w trybie dodania: [Add word]

w trybie edycji: [Save changes] + [Cancel]

Pod formularzem:

miejsce na komunikaty błędów walidacyjnych

miejsce na komunikat sukcesu (np. „Word saved”)

Sekcja listy słów (tabela)
Pod formularzem prosta tabela:

Kolumny:

Text (originalText)

Language

Status (optional: free/used)

Actions: [Edit] [Delete]

Jeśli lista jest pusta – zamiast tabeli:

komunikat: „You don’t have any words yet.”

przycisk [Add your first word] scrollujący / fokusujący formularz u góry.

3. Przepływy użytkownika (krok po kroku)
   3.1. Wejście na stronę

Użytkownik wchodzi na /words (lub /app/words).

Backend zwraca:

pusty formularz w trybie Dodawania

listę istniejących słów (jeśli są).

3.2. Dodawanie nowego słowa

Użytkownik wypełnia formularz (originalText, language).

Klika [Add word].

Backend:

waliduje dane,

tworzy nowe słowo,

zwraca stronę z:

listą zaktualizowanych słów (nowe na górze),

pustym formularzem (dalej w trybie Dodawania),

komunikatem sukcesu „Word added”.

UI: użytkownik widzi nowe słowo w tabeli.

3.3. Rozpoczęcie edycji słowa

W tabeli przy wybranym słowie użytkownik klika [Edit].

Strona:

przełącza formularz w tryb Edycji:

nagłówek formularza zmienia się na „Edit word”,

pola formularza są wypełnione danymi tego słowa,

ustawione jest ukryte id (albo endpoint z /words/{id}).

przyciski pod formularzem:

[Save changes]

[Cancel]

3.4. Zapis edytowanego słowa

Użytkownik modyfikuje tekst/język.

Klika [Save changes].

Backend:

wykonuje update słowa,

waliduje dane (np. duplikaty),

zwraca stronę z:

zaktualizowaną listą słów,

formularzem znowu w trybie Dodawania (pustym),

komunikatem „Changes saved”.

UI:

w tabeli widać zaktualizowane dane,

formularz znów służy do dodawania nowych słów.

3.5. Anulowanie edycji

Użytkownik w trakcie edycji klika [Cancel].

Strona:

czyści formularz,

przełącza tryb na Dodawanie (nagłówek „Add new word”, przycisk „Add word”).

3.6. Usuwanie słowa

Użytkownik klika [Delete] przy konkretnym słowie.

UI pokazuje prosty popup / modal potwierdzenia:

„Are you sure you want to delete this word?”

jeśli słowo jest „used”: dodatkowy tekst np.
„This word is used in accepted cards. They may be archived and removed from reviews.”

Po potwierdzeniu:

wysyłane jest żądanie usunięcia (np. DELETE lub POST z action=delete),

backend usuwa słowo,

strona wraca z:

listą bez usuniętego słowa,

formularzem w trybie Dodawania,

komunikatem „Word deleted”.

Jeśli to było ostatnie słowo – pojawia się empty state.

4. Stany brzegowe i błędy (prosto)

Pusta lista słów
Zamiast tabeli: komunikat + CTA „Add word”.

Błędy walidacji (np. puste pole, za długi tekst):

pod polem czerwony tekst,

nad/under formularzem lista błędów (opcjonalnie).

Błąd techniczny (np. problem z serwerem):

prosty komunikat nad formularzem:
„Something went wrong. Please try again.”

Duplikat słowa (biznes):

komunikat przy originalText:
„This word (or very similar) already exists in your list.”