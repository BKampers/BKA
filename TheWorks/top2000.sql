SELECT 
  COALESCE(group_artist,'-'),
  group_title,
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
    play_count,
    play_date
  FROM tracks LEFT JOIN albums ON albums.id = tracks.album_id 
  ORDER BY play_count DESC, play_date DESC
  LIMIT 2000
)
GROUP BY group_title, group_artist
ORDER BY max_play_count DESC, max_play_date DESC
