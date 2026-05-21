# Feature: Benutzer / Anwender

- Die Benutzer sollen in einer Tabelle `users` in Supabase gespeichert werden.
- Ein Benutzer soll über eine eindeutige Information des Smartphones (z. B. Seriennummer, IMEI) identifiziert werden. Diese Information soll jedoch durch Bildung eines Hashwerts anonymisiert werden.
- Ein Benutzer soll über Einstellungen seinen Benutzernamen ändern / angeben können.
- Jeder Benutzername muss eindeutig sein.

## Benutzerprofil

- Auf dem Benutzerprofil sollen die folgenden Informationen angezeigt werden:
    - Benutzername
    - Registrierungs- / Beitrittsdatum zur App
    - Rang auf der globalen Rangliste
    - Anzahl der abgeschlossenen Abenteuer

## Gruppen

- Es soll zwei Benutzergruppen geben: Administrator, Benutzer
- In der App soll es keine Gruppenverwaltung geben. Die Gruppenverwaltung findet direkt in Supabase statt.

### Administrator

- Ein Administrator kann Abenteuer erstellen, bearbeiten oder löschen.
- Ein Administrator kann alle Funktionen nutzen welche auch der Benutzer nutzen kann.

### Benutzer

- Ein Benutzer kann Abenteuer starten, unterbrechen und fortsetzen.
- Ein Benutzer darf seinen eigenen Benutzernamen ändern.
- Ein Benutzer kann sein eigenes oder die Benutzerprofile anderer Benutzer sehen.