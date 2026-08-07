SELECT 
  COALESCE(group_artist,'-') AS album_artist,
  group_title AS album_title,
  MAX(release_year) AS album_release_date,
  MAX(play_count) AS max_play_count,
  MAX(play_date) AS max_play_date 
FROM (
  SELECT
    CASE 
      WHEN album_id IS NULL THEN tracks.artist
      ELSE albums.artist
    END AS group_artist,
    CASE
      WHEN album_id IS NULL THEN tracks.title
      ELSE albums.title
    END AS group_title,
    release_year,
    play_count,
    play_date
  FROM tracks LEFT JOIN albums ON albums.id = tracks.album_id 
  ORDER BY play_count DESC, play_date DESC
  LIMIT 2000
)
GROUP BY album_title, album_artist
ORDER BY max_play_count DESC, max_play_date DESC
