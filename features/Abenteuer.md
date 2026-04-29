# Feature: Abenteuer

- Ein Abenteuer besteht immer aus mindestens zwei Stationen.
- Ein Abenteuer hat immer einen Titel und eine Beschreibung.
- Für ein Abenteuer können optional noch eine Dauer und ein Schwierigkeitsgrad angegeben werden.
- Die Stationen müssen durch den Administrator in eine Reihenfolge gebracht werden können.

## Station / Ort

- Ein Ort besteht aus einem Namen sowie der Position (Latitude, Longitude)
- Die Orte eines Abenteuers müssen in eine Reihenfolge gebracht werden können.
- Es darf innerhalb eines Abenteuers keine identischen Orte geben.
- Es gibt zwei spezielle Orte:
    - Der erste Ort ist der Startpunkt des Abenteuers
    - Der letzte Ort ist das Ziel des Abenteuers

## Schwierigkeitsgrad

- Es gibt die Grade: Leicht, Medium, Schwer