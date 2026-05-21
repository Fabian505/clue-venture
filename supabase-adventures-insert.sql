-- Insert sample adventures with metadata and multiple locations.
WITH inserted_adventures AS (
    INSERT INTO public.adventures (
        title,
        summary,
        start_latitude,
        start_longitude,
        difficulty,
        estimated_duration_minutes
    ) VALUES
    ('Geheimnis der Altstadt', 'Spaziere durch die Altstadtgassen und folge den ersten Spuren.', 52.5209, 13.4095, 'mittel', 75),
    ('Hafenpfad', 'Ein Abenteuer zwischen Wasser, Kaimauern und versteckten Hinweisen.', 52.5141, 13.3567, 'leicht', 60),
    ('Code im Stadtpark', 'Knacke die Raetsel an den Wegen und finde den naechsten Treffpunkt.', 52.5018, 13.4471, 'schwer', 90)
    RETURNING id, title
)
INSERT INTO public.adventure_locations (
    adventure_id,
    name,
    latitude,
    longitude,
    order_index
)
SELECT
    ia.id,
    location_data.name,
    location_data.latitude,
    location_data.longitude,
    location_data.order_index
FROM inserted_adventures ia
JOIN (
    VALUES
        ('Geheimnis der Altstadt', 'Nikolaiviertel', 52.5186, 13.4067, 0),
        ('Geheimnis der Altstadt', 'Altes Stadthaus', 52.5168, 13.4094, 1),
        ('Hafenpfad', 'Spreeufer', 52.5137, 13.3546, 0),
        ('Hafenpfad', 'Anleger Ost', 52.5121, 13.3587, 1),
        ('Code im Stadtpark', 'Nordtor Park', 52.5035, 13.4445, 0),
        ('Code im Stadtpark', 'Seepavillon', 52.5009, 13.4489, 1),
        ('Code im Stadtpark', 'Suedeingang', 52.4987, 13.4461, 2)
) AS location_data(title, name, latitude, longitude, order_index)
ON location_data.title = ia.title;
