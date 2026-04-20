SELECT albums.artist AS "Artist", albums.title AS "Title", MAX(tracks.release_year) AS "year", MAX(tracks.play_count) AS "played", MAX(tracks.play_date) AS latest_play_date, SUM(tracks.duration_millis) AS total_duration_millis
FROM tracks
JOIN albums ON tracks.album_id = albums.id
GROUP BY tracks.album_id
ORDER BY total_duration_millis