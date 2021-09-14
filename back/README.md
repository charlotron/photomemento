#Photo memento back

This back service is in charge of crawling multimedia and exposing them as a json api.
It uses the following dependencies:
 * **Metadata Extractor**: Extracts metadata from multimedia files ([https://github.com/drewnoakes/metadata-extractor][metadata-extractor]])
 * **Thumbnalaitor**: Resamples photos to a different size ([https://github.com/coobird/thumbnailator][thumbnailator]])
 * **FFMPEG**: To handle video files ([https://www.ffmpeg.org/][ffmpeg]])

#Requirements (For debugging)
Basic requirements are:
* JDK 11+ installed
* Docker installed
* Ffmpeg installed

#Requirements for running
Basic requirements are:
* Docker installed

### Debug commands (docker)
Build again the container:
```
docker-compose up --build
```
Go into the container once running (ensure it is uncommented in docker-compose.yml)
```
docker exec -it $(docker ps -qf "name=photo-memento-back-test") /bin/bash
```
CARE: Removes all the containers in docker:
```
docker rm $(docker ps -a -f status=exited -f status=created -q)

```

[metadata-extractor]: https://github.com/drewnoakes/metadata-extractor
[thumbnailator]: https://github.com/coobird/thumbnailator
[ffmpeg]: https://www.ffmpeg.org/
