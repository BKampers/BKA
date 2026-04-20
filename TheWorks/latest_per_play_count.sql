SELECT 
  latest_play_count,
  latest_play_date, 
  COALESCE(albums.artist, track_artist),
  COALESCE(albums.title, track_title)
FROM albums RIGHT JOIN (
  SELECT tracks.id, tracks.album_id AS latest_album_id, latest.play_count AS latest_play_count, latest.max_play_date AS latest_play_date, tracks.artist AS track_artist, tracks.title AS track_title
  FROM tracks RIGHT JOIN (
    SELECT play_count, MAX(play_date) AS max_play_date
    FROM tracks
    GROUP BY play_count
  ) AS latest
  ON tracks.play_date = latest.max_play_date
)
ON latest_album_id = albums.id