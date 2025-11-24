# MVP Database Planning Summary
_Updated on 2025-11-11T17:45:03Z_

<conversation_summary>
<decisions>
1. Klucz główny: `id UUID` (np. UUIDv7) jako jednopólowy PK w każdej tabeli.
2. Separacja per użytkownik: każda tabela domenowa ma `user_id UUID`; unikalność i walidacje egzekwowane w kodzie.
3. Unikalność techniczna: w każdej tabeli obowiązuje unikalność pary `(user_id, id)`; relacje realizowane przez **kompozycyjne FK**.
4. Relacje: dodać FK tam, gdzie potrzebne; FK kompozycyjne `(user_id, *_id) → parent(user_id, id)` ustawione jako **DEFERRABLE INITIALLY IMMEDIATE**.
5. Usuwanie: `ON DELETE CASCADE` z `cards` do `srs_state` i `card_words`; domyślnie restrykcja w innych miejscach.
6. **Statusy `cards.status`: dozwolone wartości to wyłącznie `draft`, `accepted`, `archived`;** przejścia i walidacje wyłącznie w kodzie (bez ENUM/CHECK).
7. SRS (SM-2): osobna tabela `srs_state` powiązana z `cards`; spójność biznesowa kontrolowana w kodzie.
8. Usuwanie danych: tylko twarde delete (bez soft-delete, audytu, RODO).
9. Idempotencja: bez `client_side_id` i dodatkowych mechanizmów — rozwiązywane w kodzie według potrzeb.
10. Czas: używać `timestamp` (bez stref); przyjmujemy UTC i konwersje poza bazą.
11. RLS: prosta polityka per tabela domenowa — `USING (user_id = current_setting('app.user_id')::uuid)` oraz `WITH CHECK` z tym samym warunkiem; aplikacja ustawia `SET LOCAL app.user_id = :jwt_sub` po autoryzacji.
12. Indeksy: na tym etapie tylko te wynikające z PK/FK; reszta po zebraniu metryk.
</decisions>

<matched_recommendations>
1. Ujednolicenie PK na UUID — przyjęte.
2. Obowiązkowy `user_id` we wszystkich tabelach — przyjęte.
3. Zapobieganie „przekrosowym” FK poprzez `UNIQUE (user_id, id)` i FK kompozycyjne — przyjęte.
4. Zakres kluczy obcych i `CASCADE` z `cards` do `srs_state` i `card_words` — przyjęte.
5. Ustawienie wszystkich FK jako `DEFERRABLE INITIALLY IMMEDIATE` — przyjęte.
6. Minimalny zestaw tabel/kolumn (`users`, `words`, `cards`, `card_words`, `srs_state`) — przyjęte.
7. Czas w `timestamp` (UTC, bez stref w DB) — przyjęte.
8. Prosty RLS z `USING` i `WITH CHECK` po `user_id` — przyjęte.
9. Brak mechanizmów idempotencji (`client_side_id`) na MVP — potwierdzone.
10. Brak dodatkowych indeksów poza PK/FK — przyjęte.
11. **Ścisły katalog statusów kart (`draft`, `accepted`, `archived`) — przyjęte.**
</matched_recommendations>

<database_planning_summary>
a. **Główne wymagania schematu**
- Jednolity PK `id UUID` i kolumna `user_id` w każdej tabeli.
- Unikalność `(user_id, id)` w tabelach domenowych.
- Brak walidacji/constraintów biznesowych na poziomie DB; wszystko w kodzie.
- Prosty i konsekwentny RLS po `user_id`.
- Minimalny zestaw tabel dla MVP bez audytu/soft-delete.
- **`cards.status` ograniczony do: `draft`, `accepted`, `archived` (egzekwowane w kodzie).**

b. **Kluczowe encje i relacje**
- **users**(id, email)
- **words**(id, user_id, original_text, canonical_text, language) — FK `(user_id) → users(id)`
- **cards**(id, user_id, status, front_en, back_pl) — FK `(user_id) → users(id)`
- **card_words**(id, user_id, card_id, word_id) — FK `(user_id, card_id) → cards(user_id, id)`; FK `(user_id, word_id) → words(user_id, id)`; `ON DELETE CASCADE` z rodziców
- **srs_state**(id, user_id, card_id, easiness, interval_days, repetitions, due_at, last_grade) — FK `(user_id, card_id) → cards(user_id, id)`; `ON DELETE CASCADE`

c. **Bezpieczeństwo i skalowalność**
- RLS: jedna polityka `USING` + `WITH CHECK` na `user_id`; aplikacja ustawia `app.user_id` po autoryzacji.
- PK jako UUID ułatwia poziome skalowanie i import/eksport bez zależności od sekwencji.
- Brak dodatkowych indeksów na MVP; decyzje indeksowe odłożone do momentu zebrania realnych metryk zapytań.
- FK są `DEFERRABLE`, co ułatwia wsady/transakcje batchowe i upraszcza kolejność operacji.

d. **Nierozwiązane kwestie / doprecyzowania na kolejną iterację**
- Reguły biznesowe SRS (np. czy `srs_state` tylko dla `accepted`) — obecnie egzekwowane w kodzie.
- Ewentualne przyszłe tabele (np. tagi, metryki, użycie promptów) oraz polityka indeksowania po zebraniu metryk.
- Strategia obsługi duplikatów i współbieżności (advisory locks/mutexy) — na razie pozostaje w kodzie, bez `client_side_id`.
</database_planning_summary>

<unresolved_issues>
1. Formalizacja reguł SRS (wejście/wyjście kart do SRS) i ich kontrola w kodzie.
2. Kryteria dodawania indeksów w kolejnej iteracji (na podstawie metryk zapytań).
3. Ewentualna potrzeba dodatkowych encji (tagi, metryki), gdy zajdzie potrzeba produktowa.
</unresolved_issues>
</conversation_summary>
