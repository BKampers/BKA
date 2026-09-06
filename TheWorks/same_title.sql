SELECT 
  COALESCE(albums.artist, '-') AS Artist,
  albums.title AS Title
FROM albums
WHERE title IN (
  SELECT title
  FROM albums
  GROUP BY title
  HAVING COUNT(*) > 1
)
ORDER BY albums.title, albums.artist
