-- ============================================================================
-- SEED DATA: Quiz Questions for Adventures
-- ============================================================================
-- This file inserts sample quiz questions and answers for the 3 adventures
-- Questions are placed between checkpoints to enhance the adventure experience

-- ============================================================================
-- 1. GEHEIMNIS DER ALTSTADT - 2 Questions (between checkpoint 0→1 and at checkpoint 1)
-- ============================================================================

WITH adventure_data AS (
    SELECT id FROM public.adventures WHERE title = 'Geheimnis der Altstadt' LIMIT 1
),
question_1 AS (
    INSERT INTO public.quiz_questions (adventure_id, question_text, order_index, question_type)
    SELECT id, 'In welchem Jahr wurde die Nikolaikirche erbaut?', 0, 'multiple_choice'
    FROM adventure_data
    RETURNING id
),
question_2 AS (
    INSERT INTO public.quiz_questions (adventure_id, question_text, order_index, question_type)
    SELECT id, 'Welcher Fluss fließt durch Altstadt?', 1, 'multiple_choice'
    FROM adventure_data
    RETURNING id
)
INSERT INTO public.quiz_answers (question_id, answer_text, is_correct, answer_order)
SELECT q1.id, answer_text, is_correct, answer_order
FROM (
    SELECT * FROM (VALUES
        (1, '1270', true, 0),
        (1, '1380', false, 1),
        (1, '1450', false, 2),
        (1, '1550', false, 3),
        (2, 'Spree', true, 0),
        (2, 'Elbe', false, 1),
        (2, 'Havel', false, 2),
        (2, 'Isar', false, 3)
    ) AS answers(q_order, answer_text, is_correct, answer_order)
    WHERE q_order = 1
) AS a
CROSS JOIN (SELECT id FROM question_1) AS q1
UNION ALL
SELECT q2.id, answer_text, is_correct, answer_order
FROM (
    SELECT * FROM (VALUES
        (2, 'Spree', true, 0),
        (2, 'Elbe', false, 1),
        (2, 'Havel', false, 2),
        (2, 'Isar', false, 3)
    ) AS answers(q_order, answer_text, is_correct, answer_order)
    WHERE q_order = 2
) AS a
CROSS JOIN (SELECT id FROM question_2) AS q2;

-- ============================================================================
-- 2. HAFENPFAD - 2 Questions
-- ============================================================================

WITH adventure_data AS (
    SELECT id FROM public.adventures WHERE title = 'Hafenpfad' LIMIT 1
),
question_1 AS (
    INSERT INTO public.quiz_questions (adventure_id, question_text, order_index, question_type)
    SELECT id, 'Wann wurde der Hafen Berlin gegründet?', 0, 'multiple_choice'
    FROM adventure_data
    RETURNING id
),
question_2 AS (
    INSERT INTO public.quiz_questions (adventure_id, question_text, order_index, question_type)
    SELECT id, 'Was war das Hauptaugenmerk des Hafens ursprünglich?', 1, 'multiple_choice'
    FROM adventure_data
    RETURNING id
)
INSERT INTO public.quiz_answers (question_id, answer_text, is_correct, answer_order)
SELECT q.id, answer_text, is_correct, answer_order
FROM (
    SELECT * FROM (VALUES
        (1, '1', 1870, true, 0),
        (1, '1', 1890, false, 1),
        (1, '1', 1910, false, 2),
        (1, '1', 1930, false, 3),
        (2, '2', 'Kohletransport', true, 0),
        (2, '2', 'Personenverkehr', false, 1),
        (2, '2', 'Maschinenbau', false, 2),
        (2, '2', 'Luxusgütter', false, 3)
    ) AS answers(q_order, dummy, answer_text, is_correct, answer_order)
) AS a
CROSS JOIN (
    SELECT 1 AS q_order, id FROM question_1
    UNION ALL
    SELECT 2 AS q_order, id FROM question_2
) AS q
WHERE a.q_order = q.q_order;

-- ============================================================================
-- 3. CODE IM STADTPARK - 3 Questions (between each checkpoint)
-- ============================================================================

WITH adventure_data AS (
    SELECT id FROM public.adventures WHERE title = 'Code im Stadtpark' LIMIT 1
),
question_1 AS (
    INSERT INTO public.quiz_questions (adventure_id, question_text, order_index, question_type)
    SELECT id, 'In welchem Jahr wurde hier die erste Olympiade ausgetragen?', 0, 'multiple_choice'
    FROM adventure_data
    RETURNING id
),
question_2 AS (
    INSERT INTO public.quiz_questions (adventure_id, question_text, order_index, question_type)
    SELECT id, 'Wie viele Hektar umfasst der Tiergarten?', 1, 'multiple_choice'
    FROM adventure_data
    RETURNING id
),
question_3 AS (
    INSERT INTO public.quiz_questions (adventure_id, question_text, order_index, question_type)
    SELECT id, 'Welches Denkmal befindet sich im südlichen Teil des Parks?', 2, 'multiple_choice'
    FROM adventure_data
    RETURNING id
)
INSERT INTO public.quiz_answers (question_id, answer_text, is_correct, answer_order)
SELECT q.id, answer_text, is_correct, answer_order
FROM (
    SELECT * FROM (VALUES
        (1, '1', 1936, true, 0),
        (1, '1', 1928, false, 1),
        (1, '1', 1952, false, 2),
        (1, '1', 1945, false, 3),
        (2, '2', '210', true, 0),
        (2, '2', '180', false, 1),
        (2, '2', '250', false, 2),
        (2, '2', '150', false, 3),
        (3, '3', 'Sowjetisches Ehrenmal', true, 0),
        (3, '3', 'Bismarcksäule', false, 1),
        (3, '3', 'Kriegerdenkmal', false, 2),
        (3, '3', 'Friedensdenkmal', false, 3)
    ) AS answers(q_order, dummy, answer_text, is_correct, answer_order)
) AS a
CROSS JOIN (
    SELECT 1 AS q_order, id FROM question_1
    UNION ALL
    SELECT 2 AS q_order, id FROM question_2
    UNION ALL
    SELECT 3 AS q_order, id FROM question_3
) AS q
WHERE a.q_order = q.q_order;

-- ============================================================================
-- VERIFICATION: Check that all questions were inserted correctly
-- ============================================================================

SELECT 
    a.title,
    COUNT(qq.id) as question_count,
    STRING_AGG(qq.question_text, ' | ') as questions
FROM public.adventures a
LEFT JOIN public.quiz_questions qq ON a.id = qq.adventure_id
WHERE a.title IN ('Geheimnis der Altstadt', 'Hafenpfad', 'Code im Stadtpark')
GROUP BY a.id, a.title
ORDER BY a.title;

-- ============================================================================
-- DONE
-- ============================================================================
