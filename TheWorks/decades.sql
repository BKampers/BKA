SELECT 
  release_year / 10 * 10 AS decade,
  COUNT(DISTINCT album_id) AS "Album count"
FROM tracks
WHERE release_year IS NOT NULL
GROUP BY decade 


