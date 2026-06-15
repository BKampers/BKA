SELECT albums.artist AS Artist, albums.title AS Title, MAX(tracks.release_year) AS Release_year, MAX(tracks.play_count) AS Play_count, MAX(tracks.play_date) AS Latest_play_date, SUM(tracks.duration_millis) AS Total_duration_millis
FROM tracks
JOIN albums ON tracks.album_id = albums.id
$where
GROUP BY tracks.album_id
$order