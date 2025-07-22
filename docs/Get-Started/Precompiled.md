# Set up AMIRIS using precompiled Files

Instead of building AMIRIS yourself you can also use precompiled files.
This is exactly what AMIRIS-Py does when one executes `amiris install`.

1. Create a new folder called e.g. "amiris".
2. Download and extract the [amiris build](https://gitlab.com/dlr-ve/esy/amiris/amiris/-/jobs/artifacts/main/download?job=deploy:jdk11).
3. Move the `amiris-core_X.y-jar-with-dependencies.jar` from the `target` folder into your AMIRIS folder (i.e. the folder above) - ignore other files.
