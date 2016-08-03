build() {
    ./gradlew clean shadowJar
}
publish() {
    ./gradlew clean shadowJar publish
}
release() {
     ./gradlew clean shadowJar githubRelease
}
