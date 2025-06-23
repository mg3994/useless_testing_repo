name: Make APK and Push into release tab

on:
  push:
    branches:
      - '*'  # Trigger on pushes to all branches
  pull_request:
    branches:
      - '*'  # Trigger on pushes to all branches
  workflow_dispatch:  # Allows manual triggering of the workflow

jobs:
  build:
    name: Build APK
    runs-on: ubuntu-latest

    steps:
      - name: Checkout sources
        uses: actions/checkout@v4

      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          distribution: 'temurin'
          java-version: '17' # adjust if your project uses another version

      - name: Setup Gradle
        uses: gradle/actions/setup-gradle@v3

      - name: Build with Gradle
        run: ./gradlew assembleRelease

      - name: Push into Releases Tab
        uses: ncipollo/release-action@v1
        with:
          artifacts: app/build/outputs/apk/release/app-release.apk
          tag: v1.0.${{ github.run_number }}
          token: ${{ secrets.GITHUB_TOKEN }}
