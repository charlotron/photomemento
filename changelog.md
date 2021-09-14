VERSION 0.0.2 STABLE

#### Features
    * [BACK/FRONT] Adding english and spanish stopwords (but can be added more ones)
    * [BACK/FRONT] Support for searching in photos (wip broken preview)
    * [FRONT] Improved representation in map, now using markers instead of circles, multiple zoom levels, to be tested if lots of locations
    * [BACK] Support for text search over media and location resolve zoom dependant
    * [FRONT] Initial representation of locations in map
    * [BACK] Now locations collection is filled with location data and /media/location endpoint works properly
    * [FRONT] Some display improvements
    * [BACK/FRONT] Reprocess now can be done by images/videos only
    * [BACK] Now fully inverted photos will be rotated properly
    * [FRONT] Initial swipe left/right events on images
    * [FRONT] Fixed Go back when previewing an image
    * [FRONT] Fixed display errors for videos in gallery view
    * [FRONT] Improved visualization of sidebar
    * [BACK] Now detected rotation from metadata is used to automotically rotate images on resampling
    * [FRONT] Now, images and videos should show properly on preview "view" without exceeding limits, also video controls now are shown
    * [BACK/FRONT] Improving stats window, with more info related to threads
    * [BACK] Improving thread algorithm to prevent deadlocks, also added max execution time for processors
    * [FRONT] Now it is more probable to load images and videos in a proper way if it is a big size screen for first load
    * [BACK] Improved algorithm to detect rotated videos and invalid videos (invalid duration)
    * [BACK] Changed blocking queues by priority blocking queues, trying to process before latest photos based on path (desc order)
    * [GLOBAL] Improved video metadata detection and processing of old files
    * [GLOBAL] Adding new flag to retrieve only media processed
    * [GLOBAL] Adding initial support for videos, thumbnail generation and resampling video for animated thumb
    * [GLOBAL] Adding license
    * [FRONT] Fixed navbar issues on mobile view (now it collapses properly, and logo is a toggler)
    * [FRONT] Fixed navigation over directory now works again
    * [FRONT] Showing total number of processed by queue (stats)
    * [BACK] Adding more error control in threads, trying to prevent thread executor failures (jobs that never executes)
    * [BACK] "Visit" now also checks if a process is pending and retries
    * [BACK] Queues now cannot add same element twice at a time
    * [BACK] New states for processors (waiting, processed ok, failed, etc..)
    * [GLOBAL] Now an specific folder can be re-processed from directory view, use with caution
    * [GLOBAL] Added in front a "stats" section to monitor status for current process and overall system memory and cpu usage
    * [BACK] Improved thread algorythm: Included first save in database in a queue to prevent cpu spikes, now wait queue is locking a fixed delay thread executor instead of queuing events in executor (this should improve behaviour and reduces complexity)
    * [BACK] Now there is an api service to check system status and to be able to debug problems
    * [BACK] Fixed thread workers behaviour, now it works as expected (500 delay between executions of same thread, works properly)
    * [BACK] New helper classes to improve human dates and human byte/bit sizes
    * [BACK] Fixed a problem that made all of the workers to initialize to 1 thread 0 ms delay (causing high cpu load usage)

VERSION 0.0.1 UNSTABLE

#### Features
    * [BACK] Improved date detection algorythm again
    * [FRONT] Improving display of images/videos without thumbnail
    * [FRONT] Spinner, error when retrying data from server, retrying..
    * [BACK] Improved date detection algorythm
    * [BACK] Added manage -> check integrity (that checks if originals still exists then updates database)
    * [BACK] Nginx now does not show 404 on responses and redirects to index (still checking why not working properly)
    * [BACK] Now a cached image is removed when the original image is deleted
    * [BACK] Added "file.monitoring.files.exclusions" property to exclude files&dirs from watching/visiting (defaults to containing @ or starts with . for hidden files)
    * [BACK] Now on file modify it will be parsed again for media files
    * [FRONT] Adding slideshow mode to photo gallery
    * [FRONT] Initial version all media view
    * [FRONT] Initial version of infinite scroll and retrieve next group of results.
    * [FRONT] Image preview full screen, with go back and download option
    * [FRONT] Improving breadcrumb, adding initial gallery impl usin flex, initial dir navegation
    * [FRONT] Adding two sections: home/directory (very basic)
    * [FRONT] Adding navbar, sidebar, breadcrumb
    * [FRONT] Basic sketch for user interface
    * [FRONT] Implemented angular 11, mdb (bootstrap 4) on front
    * [BACK] Multithreading support with threadExecutors and workers
    * [BACK] Logic for clever date resolution based on media file name and metadata
    * [BACK] Reverse geocoding with open street map working if metadata provides gps lat/lon
    * [BACK] Api for retrieving media and directories ready
    * [BACK] Thumbnail generation support for images
    * [BACK] Storing in database when a media file is created, visited (initially) or deleted (recursively)
    * [BACK] Storing in database when a directory is created, visited (initially) or deleted (recursively)
    * [BACK] Initial version of file watcher ("provider")
    * Initial version
    
    
    
